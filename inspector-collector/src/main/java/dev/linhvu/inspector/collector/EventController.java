package dev.linhvu.inspector.collector;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
public class EventController {

	private final EventStore store;
	private final BlobStore blobs;
	private final LegacyEventController legacy;

	public EventController(EventStore store, BlobStore blobs, LegacyEventController legacy) {
		this.store = store;
		this.blobs = blobs;
		this.legacy = legacy;
	}

	@PostMapping("/events")
	public void event(@RequestBody Map<String, Object> event) {
		this.store.add(event);
	}

	@GetMapping("/stream")
	public SseEmitter stream() {
		return this.store.subscribe();
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> rejected(IllegalArgumentException ex) {
		return ResponseEntity.badRequest().body(Map.of("error", String.valueOf(ex.getMessage())));
	}

	@DeleteMapping("/events")
	public void clear() {
		synchronized (legacy) {
			this.store.clear();
			this.blobs.clear();
			this.legacy.clearLegacy();
		}
	}

}
