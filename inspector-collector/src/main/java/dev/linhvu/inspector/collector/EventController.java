package dev.linhvu.inspector.collector;

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
public class EventController {

	private final Map<String, Timeline> runs = new TreeMap<>(); // producerId, TimeLine

	@PostMapping("/api/events")
	public synchronized Map<String, String> accept(@RequestBody Event event) {
		Timeline timeline = runs.computeIfAbsent(event.producerId(), id -> new Timeline(id, 10000));
		return Map.of("result", timeline.accept(event).name());
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
