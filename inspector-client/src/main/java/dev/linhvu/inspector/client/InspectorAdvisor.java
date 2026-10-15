package dev.linhvu.inspector.client;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import dev.linhvu.inspector.Event;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.core.Ordered;

public final class InspectorAdvisor implements CallAdvisor, StreamAdvisor {

	private static final String OPERATION_KEY = InspectorAdvisor.class.getName() + ".operation";
	private final InspectorClient client;

	public InspectorAdvisor(InspectorClient client) { this.client = client; }

	@Override
	public String getName() {
		return "RecipeInspector";
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
		String operationId = UUID.randomUUID().toString();
		client.emit(operationId, Event.Kind.START, "ChatClient.call started");
		long start = System.nanoTime();

		try {
			ChatClientResponse response = chain.nextCall(request);
			long elapsed = (System.nanoTime() - start) / 1_000_000;
			client.emit(operationId, Event.Kind.END, "ChatClient.call completed in " + elapsed + " ms");
			return response;
		} catch (RuntimeException ex) {
			client.emit(operationId, Event.Kind.ERROR, "ChatClient.call failed: " + ex.getClass().getSimpleName());
			throw ex;
		}
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
		return Flux.deferContextual(context -> {
			String operationId = UUID.randomUUID().toString();
			String parentId = context.getOrDefault(OPERATION_KEY, null);
			long start = System.nanoTime();
			AtomicLong chunks = new AtomicLong();
			AtomicBoolean terminated = new AtomicBoolean();
			client.emit(operationId, parentId, Event.Kind.START, "ChatClient.stream started");
			return chain.nextStream(request)
					.doOnNext(response -> client.emit(operationId, parentId, Event.Kind.SNAPSHOT,
							"Stream response chunk " + chunks.incrementAndGet()))
					.doOnComplete(() -> terminal(terminated, operationId, parentId, Event.Kind.END,
							"Stream completed in " + elapsed(start) + " ms"))
					.doOnError(ex -> terminal(terminated, operationId, parentId, Event.Kind.ERROR,
							"Stream failed: " + ex.getClass().getSimpleName()))
					.doOnCancel(() -> terminal(terminated, operationId, parentId, Event.Kind.CANCEL,
							"Stream cancelled after " + chunks.get() + " response chunks"))
					.contextWrite(upstream -> upstream.put(OPERATION_KEY, operationId));
		});
	}

	private void terminal(AtomicBoolean terminated, String operationId, String parentId,
			Event.Kind kind, String summary) {
		if (terminated.compareAndSet(false, true)) client.emit(operationId, parentId, kind, summary);
	}

	private static long elapsed(long start) { return (System.nanoTime() - start) / 1_000_000; }

}
