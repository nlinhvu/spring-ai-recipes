package dev.linhvu.inspector.collector;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.linhvu.inspector.Event;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EventPipelineTest {
	private static Map<String, Object> event(String type, String run, String call) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("type", type);
		result.put("runId", run);
		if (call != null) result.put("callId", call);
		return result;
	}

	@Test
	void logEvictsOldestButKeepsOneOversizedEventAndSequenceAfterClear() {
		EventLog log = new EventLog(2_000);
		Map<String, Object> a = new LinkedHashMap<>(Map.of("body", "x".repeat(400)));
		Map<String, Object> b = new LinkedHashMap<>(Map.of("body", "y".repeat(400)));
		log.append(a);
		log.append(b);
		assertEquals(List.of(b), log.events());
		Map<String, Object> huge = new LinkedHashMap<>(Map.of("body", "z".repeat(5_000)));
		log.append(huge);
		assertEquals(List.of(huge), log.events());
		log.clear();
		Map<String, Object> next = event("run-start", "next", null);
		log.append(next);
		assertEquals(4L, next.get("seq"));
	}

	@Test
	void correlatesOpenCallsAndPreservesFirstUpstreamAfterClear() {
		EventStore store = new EventStore();
		try {
			Map<String, Object> start = event("run-start", "r", null);
			start.put("upstreams", Map.of("openai", "https://first.example"));
			store.add(start);

			Map<String, Object> again = event("run-start", "r", null);
			again.put("upstreams", Map.of("openai", "https://second.example"));
			store.add(again);

			store.add(event("client-request", "r", "c1"));

			Map<String, Object> tool = event("tool-start", "r", null);
			tool.put("toolId", "t1");
			store.add(tool);

			assertEquals("c1", tool.get("clientCallId"));

			Map<String, Object> nested = event("client-request", "r", "c2");
			store.add(nested);

			assertEquals("c1", nested.get("parentId"));
			assertEquals(true, nested.get("parentInferred"));
			assertEquals("c2", store.openCalls("r").get("clientCallId"));

			store.add(event("client-response", "r", "c2"));

			assertEquals("c1", store.openCalls("r").get("clientCallId"));
			store.clear();
			assertNull(store.openCalls("r").get("clientCallId"));
			assertEquals("https://first.example", store.upstream("r", "openai"));
		}
		finally {
			store.shutdown();
		}
	}

	@Test
	void legacyDedupAndClearPreserveCompatibilityWithoutMergingNativePosts() throws Exception {
		EventStore store = new EventStore();
		BlobStore blobs = new BlobStore(InspectorProperties.defaults());
		LegacyEventController legacy = new LegacyEventController(store);
		EventController controller = new EventController(store, blobs, legacy);
		var mvc = MockMvcBuilders.standaloneSetup(controller, legacy).build();
		String body = "{\"producerId\":\"p1\",\"sourceSeq\":2,\"occurredAt\":\"2026-10-16T00:00:00Z\","
				+ "\"operationId\":\"o1\",\"kind\":\"END\",\"summary\":\"done\"}";
		try {
			mvc.perform(post("/api/legacy-events").contentType("application/json").content(body))
					.andExpect(status().isOk()).andExpect(content().string("{\"result\":\"ADDED\"}"));

			mvc.perform(post("/api/legacy-events").contentType("application/json").content(body))
					.andExpect(content().string("{\"result\":\"DUPLICATE\"}"));

			assertEquals(1, store.events().size());

			mvc.perform(post("/api/legacy-events").contentType("application/json").content(body.replace("done", "changed")))
					.andExpect(status().isBadRequest());

			legacy.accept(new Event("p1", 1, Instant.parse("2026-10-16T00:00:00Z"), "o1", null, Event.Kind.START, "start"));

			assertEquals(List.of(2L, 1L), store.events().stream().map(e -> e.get("sourceSeq")).toList());

			for (int i = 0; i < 2; i++)
				mvc.perform(post("/api/events").contentType("application/json")
						.content("{\"type\":\"run-start\",\"runId\":\"native\"}")).andExpect(status().isOk());

			assertEquals(4, store.events().size());

			mvc.perform(delete("/api/events")).andExpect(status().isOk());

			assertTrue(store.events().isEmpty());
			assertTrue(legacy.runs().isEmpty());

			mvc.perform(get("/api/ping")).andExpect(status().isNotFound());
		}
		finally {
			store.shutdown();
		}
	}

	@RestController
	@RequestMapping("/api")
	static class RecordingController extends EventController {
		SseEmitter emitter;

		RecordingController(EventStore store, BlobStore blobs, LegacyEventController legacy) {
			super(store, blobs, legacy);
		}

		@Override
		@GetMapping("/stream")
		public SseEmitter stream() {
			emitter = super.stream(); return emitter;
		}
	}

	@Test
	void replaysHistoryThenLiveOnceAndReconnectReplaysRetainedHistory() throws Exception {
		EventStore store = new EventStore();
		BlobStore blobs = new BlobStore(InspectorProperties.defaults());
		LegacyEventController legacy = new LegacyEventController(store);
		RecordingController controller = new RecordingController(store, blobs, legacy);
		var mvc = MockMvcBuilders.standaloneSetup(controller).build();
		try {
			for (int i = 0; i < 300; i++) store.add(event("run-start", "history-" + i, null));
			var response = mvc.perform(get("/api/stream")).andExpect(request().asyncStarted()).andReturn();
			for (int i = 0; i < 20; i++) store.add(event("run-start", "live-" + i, null));
			List<Map<String, Object>> events = awaitEvents(response, 320);
			assertEquals(320, events.size());
			for (int i = 0; i < 320; i++) assertEquals(i + 1L, ((Number) events.get(i).get("seq")).longValue());
			controller.emitter.complete();
			var reconnect = mvc.perform(get("/api/stream")).andExpect(request().asyncStarted()).andReturn();
			assertEquals(320, awaitEvents(reconnect, 320).size());
			controller.emitter.complete();
		}
		finally {
			if (controller.emitter != null) controller.emitter.complete();
			store.shutdown();
		}
	}

	private static List<Map<String, Object>> awaitEvents(org.springframework.test.web.servlet.MvcResult result,
			int wanted) throws Exception {
		long deadline = System.nanoTime() + java.time.Duration.ofSeconds(5).toNanos();
		JsonMapper json = JsonMapper.builder().build();
		while (System.nanoTime() < deadline) {
			String text = result.getResponse().getContentAsString();
			if (text.endsWith("\n\n")) {
				List<Map<String, Object>> events = new ArrayList<>();
				for (String line : text.split("\n"))
					if (line.startsWith("data:")) {
						@SuppressWarnings("unchecked") Map<String, Object> parsed = json.readValue(line.substring(5), Map.class);
						events.add(parsed);
					}
				if (events.size() >= wanted) return events;
			}
			Thread.sleep(10);
		}
		throw new AssertionError("Timed out waiting for SSE data");
	}
}
