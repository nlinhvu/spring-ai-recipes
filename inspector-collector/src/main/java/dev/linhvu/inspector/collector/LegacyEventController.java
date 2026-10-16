package dev.linhvu.inspector.collector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dev.linhvu.inspector.Event;
import dev.linhvu.inspector.Timeline;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LegacyEventController {

	private final EventStore eventStore;

	private final Map<String, Timeline> runs = new TreeMap<>(); // producerId, timeline

	public LegacyEventController(EventStore eventStore) {
		this.eventStore = eventStore;
	}

	public synchronized void clearLegacy() {
		runs.clear();
	}

	@PostMapping("/api/legacy-events")
	public synchronized Map<String, String> accept(@RequestBody Event event) {
		Timeline timeline = runs.computeIfAbsent(event.producerId(), id -> new Timeline(id, 10000));
		Timeline.Result result = timeline.accept(event);
		if (result == Timeline.Result.ADDED) {
			Map<String, Object> wire = new LinkedHashMap<>();
			wire.put("type", "recipe-event");
			wire.put("runId", event.producerId());
			wire.put("producerId", event.producerId());
			wire.put("sourceSeq", event.sourceSeq());
			wire.put("occurredAt", event.occurredAt().toString());
			wire.put("operationId", event.operationId());
			wire.put("parentOperationId", event.parentOperationId());
			wire.put("kind", event.kind().name());
			wire.put("summary", event.summary());
			eventStore.add(wire);
		}
		return Map.of("result", result.name());
	}

	@GetMapping("/api/runs")
	public synchronized List<String> runs() { return List.copyOf(runs.keySet()); }

	@GetMapping("/api/events")
	public synchronized List<Timeline.Row> events(@RequestParam("producerId") String producerId) {
		Timeline timeline = runs.get(producerId);
		return timeline == null ? List.of() : timeline.snapshot();
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> invalid(IllegalArgumentException ex) {
		return ResponseEntity.badRequest().body(Map.of("error", "Invalid or conflicting event"));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, String>> full(IllegalStateException ex) {
		return ResponseEntity.status(503).body(Map.of("error", "Timeline capacity reached"));
	}
}
