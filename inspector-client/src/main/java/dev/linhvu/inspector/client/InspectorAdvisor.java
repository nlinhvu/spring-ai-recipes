package dev.linhvu.inspector.client;

import java.util.UUID;

import dev.linhvu.inspector.Event;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.core.Ordered;

public final class InspectorAdvisor implements CallAdvisor {

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


}
