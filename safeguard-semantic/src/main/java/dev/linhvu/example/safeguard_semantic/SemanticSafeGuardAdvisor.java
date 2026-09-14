package dev.linhvu.example.safeguard_semantic;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.Ordered;

public class SemanticSafeGuardAdvisor implements CallAdvisor {

	private static final Logger log = LoggerFactory.getLogger(SemanticSafeGuardAdvisor.class);

	private final SemanticGuardrailJudge judge;
	private final boolean checkInput;
	private final boolean checkOutput;
	private final String failureResponse;
	private final int order;

	public SemanticSafeGuardAdvisor(SemanticGuardrailJudge judge, boolean checkInput, boolean checkOutput, String failureResponse, int order) {
		this.judge = judge;
		this.checkInput = checkInput;
		this.checkOutput = checkOutput;
		this.failureResponse = failureResponse;
		this.order = order;
	}

	public static Builder builder() {
		return new Builder();
	}



	@Override
	public String getName() {
		return "semantic-safe-guard-advisor";
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {

		if (this.checkInput) {
			String inputText = extractInputText(request);
			SemanticGuardrailJudge.GuardrailVerdict verdict = judge.judge(inputText, "user input");

			if (!verdict.allowed()) {
				log.error("Input text '{}' is not allowed.\nReason: {}\nViolated rule: {}",
						inputText, verdict.reason(), verdict.violatedRule());
				return createFailureResponse(request);
			}
		}

		ChatClientResponse response = chain.nextCall(request);

		if (checkOutput) {
			String outputText = extractOutputText(response);
			SemanticGuardrailJudge.GuardrailVerdict verdict = judge.judge(outputText, "model output");

			if (!verdict.allowed()) {
				log.error("Output text '{}' is not allowed.\nReason: {}\nViolated rule: {}",
						outputText, verdict.reason(), verdict.violatedRule());
				return createFailureResponse(request);
			}
		}

		return response;
	}

	private ChatClientResponse createFailureResponse(ChatClientRequest chatClientRequest) {
		return ChatClientResponse.builder()
				.chatResponse(ChatResponse.builder()
						.generations(List.of(new Generation(new AssistantMessage(this.failureResponse))))
						.build())
				.context(Map.copyOf(chatClientRequest.context()))
				.build();
	}

	private String extractInputText(ChatClientRequest request) {
		return request.prompt()
				.getUserMessage()
				.getText();
	}

	private static String extractOutputText(ChatClientResponse response) {
		return response.chatResponse()
				.getResult()
				.getOutput()
				.getText();
	}

	//
	// BUILDER
	//
	public static class Builder {
		private SemanticGuardrailJudge judge;
		private boolean checkInput = true;
		private boolean checkOutput = true;
		private String failureResponse = "Forbidden content";
		private int order = Ordered.LOWEST_PRECEDENCE;

		public SemanticSafeGuardAdvisor.Builder judge(SemanticGuardrailJudge judge) {
			this.judge = judge;
			return this;
		}

		public SemanticSafeGuardAdvisor.Builder failureResponse(String failureResponse) {
			this.failureResponse = failureResponse;
			return this;
		}

		public SemanticSafeGuardAdvisor.Builder order(int order) {
			this.order = order;
			return this;
		}

		public SemanticSafeGuardAdvisor.Builder checkInput(boolean checkInput) {
			this.checkInput = checkInput;
			return this;
		}

		public SemanticSafeGuardAdvisor.Builder checkOutput(boolean checkOutput) {
			this.checkOutput = checkOutput;
			return this;
		}

		public SemanticSafeGuardAdvisor build() {
			return new SemanticSafeGuardAdvisor(judge, checkInput, checkOutput, failureResponse, order);
		}
	}
}
