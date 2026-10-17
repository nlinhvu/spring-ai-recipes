package dev.linhvu.inspector.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

import dev.linhvu.inspector.Event;

import org.springframework.ai.util.JsonHelper;

public final class InspectorClient implements AutoCloseable {
	public static final int EVENTS_VERSION = 1;
	private static final long BACKOFF_MILLIS = 5000;
	private final String producerId = UUID.randomUUID().toString();
	private final String runId;
	private final JsonHelper json = new JsonHelper();
	private final Consumer<String> transport;
	private final ExecutorService sender = Executors.newSingleThreadExecutor(task -> {
		Thread thread = new Thread(task, "recipe-inspector-sender");
		thread.setDaemon(true);
		return thread;
	});
	private final AtomicLong pending = new AtomicLong();
	private final AtomicLong skipped = new AtomicLong();
	private final AtomicLong failed = new AtomicLong();
	private volatile long pausedUntil;
	private volatile boolean down;
	private volatile Announcement announcement;
	private long sequence;
	private record Announcement(String type, Supplier<Map<String, Object>> payload) { }

	public InspectorClient(URI collectorUri) { this(collectorUri, null, null); }
	public InspectorClient(URI collectorUri, String runId, String token) {
		this(runId, httpTransport(collectorUri, token));
	}
	InspectorClient(Consumer<String> transport) { this(null, transport); }
	InspectorClient(String runId, Consumer<String> transport) {
		this.runId = runId == null || runId.isBlank() ? producerId : runId;
		this.transport = transport;
	}

	static HttpRequest eventRequest(URI collectorUri, String body, String token) {
		if (!"http".equals(collectorUri.getScheme()) || !"127.0.0.1".equals(collectorUri.getHost())) {
			throw new IllegalArgumentException("Use an http://127.0.0.1 collector URL");
		}
		HttpRequest.Builder request = HttpRequest.newBuilder(collectorUri.resolve("/api/events"))
				.timeout(Duration.ofSeconds(1)).header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body));
		if (token != null && !token.isBlank()) request.header("X-Inspector-Token", token);
		return request.build();
	}

	private static Consumer<String> httpTransport(URI collectorUri, String token) {
		eventRequest(collectorUri, "{}", token);
		HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(500)).build();
		return body -> {
			try {
				int status = http.send(eventRequest(collectorUri, body, token),
						HttpResponse.BodyHandlers.discarding()).statusCode();
				if (status < 200 || status >= 300) throw new IllegalStateException("Collector HTTP " + status);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Sender interrupted", ex);
			}
			catch (IOException ex) { throw new IllegalStateException("Collector unavailable", ex); }
		};
	}

	public void emit(String operationId, Event.Kind kind, String summary) {
		emit(operationId, null, kind, summary);
	}
	public synchronized void emit(String operationId, String parentId, Event.Kind kind, String summary) {
		send("recipe-event", () -> {
			Event event = new Event(producerId, sequence, Instant.now(), operationId, parentId, kind, summary);
			Map<String, Object> payload = new LinkedHashMap<>();
			payload.put("occurredAt", event.occurredAt().toString());
			payload.put("operationId", event.operationId());
			payload.put("parentOperationId", event.parentOperationId());
			payload.put("kind", event.kind().name());
			payload.put("summary", event.summary());
			return payload;
		});
	}

	public synchronized void send(String type, Supplier<Map<String, Object>> payload) {
		String body = build(type, payload);
		if (body != null) background(body);
	}
	public void send(String type, Map<String, Object> payload) { send(type, () -> payload); }
	public synchronized void announce(String type, Supplier<Map<String, Object>> payload) {
		announcement = new Announcement(type, payload);
		send(type, payload);
	}

	private synchronized String build(String type, Supplier<Map<String, Object>> payload) {
		long seq = ++sequence;
		long ts = System.currentTimeMillis();
		if (ts < pausedUntil) { skipped.incrementAndGet(); return null; }
		try {
			Map<String, Object> event = new LinkedHashMap<>(payload.get());
			// Envelope keys are owned by the capture client.
			event.put("v", EVENTS_VERSION);
			event.put("type", type);
			event.put("runId", runId);
			event.put("ts", ts);
			event.put("producerId", producerId);
			event.put("sourceSeq", seq);
			return json.toJson(event);
		}
		catch (RuntimeException | LinkageError ex) {
			skipped.incrementAndGet();
			System.err.println("Inspector capture skipped: " + ex.getClass().getSimpleName());
			return null;
		}
	}

	private void background(String body) {
		pending.incrementAndGet();
		try {
			sender.execute(() -> {
				try { deliver(body); }
				finally { pending.decrementAndGet(); }
			});
		}
		catch (RejectedExecutionException ex) { pending.decrementAndGet(); skipped.incrementAndGet(); }
	}
	private void deliver(String body) {
		if (System.currentTimeMillis() < pausedUntil) { skipped.incrementAndGet(); return; }
		if (down) {
			Announcement saved = announcement;
			if (saved != null) {
				String again = build(saved.type(), () -> {
					Map<String, Object> payload = new LinkedHashMap<>(saved.payload().get());
					payload.put("reannounce", true);
					return payload;
				});
				if (again == null || !post(again)) return;
			}
			down = false;
		}
		post(body);
	}
	private boolean post(String body) {
		try { transport.accept(body); return true; }
		catch (RuntimeException ex) {
			failed.incrementAndGet();
			down = true;
			pausedUntil = System.currentTimeMillis() + BACKOFF_MILLIS;
			System.err.println("Inspector send failed; pausing events for 5s");
			return false;
		}
	}

	void awaitBackground() throws Exception { sender.submit(() -> { }).get(3, TimeUnit.SECONDS); }
	public String producerId() { return producerId; }
	public String runId() { return runId; }
	public String status() { return "pending=" + pending.get() + ", skipped=" + skipped.get() + ", failed=" + failed.get(); }
	@Override public void close() {
		sender.shutdown();
		try {
			if (!sender.awaitTermination(10, TimeUnit.SECONDS)) {
				System.err.println("Inspector shutdown deadline reached; " + status());
				sender.shutdownNow();
			}
		}
		catch (InterruptedException ex) { sender.shutdownNow(); Thread.currentThread().interrupt(); }
		System.err.println("Inspector " + producerId + ": " + status());
	}
}
