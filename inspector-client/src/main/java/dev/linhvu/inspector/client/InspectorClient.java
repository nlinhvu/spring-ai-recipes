package dev.linhvu.inspector.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import dev.linhvu.inspector.Event;

import org.springframework.ai.util.JsonHelper;

public final class InspectorClient implements AutoCloseable{

	private final String producerId = UUID.randomUUID().toString();
	private final JsonHelper json = new JsonHelper();

	private final Consumer<String> transport;

	private final ExecutorService executorService = Executors.newSingleThreadExecutor(task -> {
		Thread thread = new Thread(task, "recipe-inspector-sender");
		thread.setDaemon(true);
		return thread;
	});

	private final AtomicLong pending = new AtomicLong();
	private final AtomicLong skipped = new AtomicLong();
	private final AtomicLong failed = new AtomicLong();
	private volatile long pausedUntil;
	private long sequence;

	public InspectorClient(URI collectorUri) {
		this(httpTransport(collectorUri));
	}

	InspectorClient(Consumer<String> transport) {
		this.transport = transport;
	}

	private static Consumer<String> httpTransport(URI collectorUri) {
		if (!"http".equals(collectorUri.getScheme()) || !"127.0.0.1".equals(collectorUri.getHost())) {
			throw new IllegalArgumentException("Use an http://127.0.0.1 collector URL");
		}
		URI eventsUri = collectorUri.resolve("/api/events");
		HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(500)).build();
		return body -> {
			try {
				HttpRequest request = HttpRequest.newBuilder(eventsUri)
						.timeout(Duration.ofSeconds(1))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString(body))
						.build();

				int status = httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
				if (status < 200 || status >= 300) throw new IllegalStateException("Collector HTTP " + status);
			}
			catch (IOException e) {
				throw new IllegalStateException("Collector unavailable", e);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Sender interrupted", e);
			}
		};
	}

	public void emit(String operationId, Event.Kind kind, String summary) {
		emit(operationId, null, kind, summary);
	}

	public synchronized void emit(String operationId, String parentOperationId,
			Event.Kind kind, String summary) {
		long seq = ++sequence;
		if (System.currentTimeMillis() < pausedUntil) {
			skipped.incrementAndGet();
			return;
		}
		String body = build(seq, operationId, parentOperationId, kind, summary);
		if (body != null) background(body);
	}

	private String build(long seq, String operationId, String parentOperationId,
			Event.Kind kind, String summary) {
		try {
			return json.toJson(new Event(producerId, seq, Instant.now(),
					operationId, parentOperationId, kind, summary));
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
			executorService.execute(() -> {
				try {
					deliver(body);
				}
				finally {
					pending.decrementAndGet();
				}
			});
		}
		catch (RejectedExecutionException _) {
			pending.decrementAndGet();
			skipped.incrementAndGet();
		}
	}

	private void deliver(String body) {
		if (System.currentTimeMillis() < pausedUntil) {
			skipped.incrementAndGet();
			return;
		}
		try {
			transport.accept(body);
		}
		catch (RuntimeException _) {
			failed.incrementAndGet();
			pausedUntil = System.currentTimeMillis() + 5000;
			System.err.println("Inspector send failed; pausing events for 5s");
		}
	}

	public String producerId() { return producerId; }

	public String status() {
		return "pending=" + pending.get() + ", skipped=" + skipped.get() + ", failed=" + failed.get();
	}

	@Override
	public void close() {
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
				System.err.println("Inspector shutdown deadline reached; " + status());
				executorService.shutdownNow();
			}
		}
		catch (InterruptedException ex) {
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
		}
		System.err.println("Inspector " + producerId + ": " + status());
	}
}
