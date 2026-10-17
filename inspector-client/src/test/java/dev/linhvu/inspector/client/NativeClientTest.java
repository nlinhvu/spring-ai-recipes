package dev.linhvu.inspector.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import org.springframework.ai.util.JsonHelper;
import static org.junit.jupiter.api.Assertions.*;

class NativeClientTest {
	private static List<Map<String, Object>> parse(List<String> wire) {
		JsonHelper json = new JsonHelper();
		return wire.stream().map(body -> {
			@SuppressWarnings("unchecked") Map<String, Object> event = json.fromJson(body, Map.class);
			return event;
		}).toList();
	}
	@Test
	void writesNativeEnvelopeWithoutAllowingPayloadToReplaceIdentity() throws Exception {
		List<String> wire = new ArrayList<>();
		try (InspectorClient client = new InspectorClient("run-demo", wire::add)) {
			client.announce("run-start", () -> Map.of("app", "fixture", "upstreams", Map.of()));
			client.send("client-request", Map.of("callId", "call-1", "sourceSeq", 999, "runId", "wrong"));
			client.emit("op-1", dev.linhvu.inspector.Event.Kind.END, "done");
			client.awaitBackground();
			assertTrue(client.status().contains("pending=0"));
		}
		List<Map<String, Object>> events = parse(wire);
		assertEquals(List.of("run-start", "client-request", "recipe-event"), events.stream().map(e -> e.get("type")).toList());
		for (int i=0; i<events.size(); i++) {
			assertEquals("run-demo", events.get(i).get("runId"));
			assertEquals(i+1, ((Number) events.get(i).get("sourceSeq")).intValue());
			assertEquals(1, ((Number) events.get(i).get("v")).intValue());
			assertNotNull(events.get(i).get("producerId"));
			assertNotNull(events.get(i).get("ts"));
		}
	}
	@Test
	void isolatesPayloadFailureAndRejectsWorkAfterClose() {
		InspectorClient client = new InspectorClient(body -> fail("No valid payload should be sent"));
		client.send("bad", () -> { throw new IllegalArgumentException("private payload"); });
		client.close();
		client.send("later", Map.of());
		assertEquals("pending=0, skipped=2, failed=0", client.status());
	}
	@Test
	void reannouncesBeforeNextSuccessfulEventWithoutRetryingFailedBusinessEvent() throws Exception {
		List<String> wire = new ArrayList<>();
		AtomicBoolean offline = new AtomicBoolean();
		try (InspectorClient client = new InspectorClient(body -> {
			if (offline.get()) throw new IllegalStateException("offline");
			wire.add(body);
		})) {
			client.announce("run-start", () -> Map.of("app", "fixture")); client.awaitBackground();
			offline.set(true);
			client.send("lost-event", Map.of()); client.awaitBackground();
			client.send("during-backoff", Map.of());
			Thread.sleep(5100);
			offline.set(false);
			client.send("recovered-event", Map.of()); client.awaitBackground();
		}
		List<Map<String, Object>> events = parse(wire);
		assertEquals(List.of("run-start", "run-start", "recovered-event"), events.stream().map(e -> e.get("type")).toList());
		assertEquals(true, events.get(1).get("reannounce"));
		assertTrue(((Number) events.get(1).get("sourceSeq")).longValue()
				> ((Number) events.get(2).get("sourceSeq")).longValue());
	}
	@Test
	void requestUsesNativePathTokenAndTimeout() {
		var request = InspectorClient.eventRequest(URI.create("http://127.0.0.1:9002"), "{}", "demo-token");
		assertEquals("/api/events", request.uri().getPath());
		assertEquals("POST", request.method());
		assertEquals("demo-token", request.headers().firstValue("X-Inspector-Token").orElseThrow());
		assertEquals(java.time.Duration.ofSeconds(1), request.timeout().orElseThrow());
		assertFalse(InspectorClient.eventRequest(URI.create("http://127.0.0.1:9002"), "{}", " ")
				.headers().firstValue("X-Inspector-Token").isPresent());
	}
}
