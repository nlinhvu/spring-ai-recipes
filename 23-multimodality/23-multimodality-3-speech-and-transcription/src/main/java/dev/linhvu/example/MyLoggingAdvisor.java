package dev.linhvu.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.util.JsonHelper;
import org.springframework.util.StringUtils;

public class MyLoggingAdvisor implements BaseAdvisor {

	private final Logger log = LoggerFactory.getLogger(MyLoggingAdvisor.class);

	private static final String ORANGE = "\033[38;5;208m";

	private static final String RESET = "\033[0m";

	private final int order;

	public final boolean showSystemMessage;

	public final boolean showAvailableTools;

	public final boolean showConversationHistory;

	public final String labelPrefix;

	private MyLoggingAdvisor(int order, boolean showSystemMessage, boolean showAvailableTools,  boolean showConversationHistory, String labelPrefix) {
		this.order = order;
		this.showSystemMessage = showSystemMessage;
		this.showAvailableTools = showAvailableTools;
		this.showConversationHistory = showConversationHistory;
		this.labelPrefix = labelPrefix;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {

		StringBuilder sb = new StringBuilder("\n" + this.labelPrefix + "USER: ");

		// Print system prompt
		if (this.showSystemMessage && chatClientRequest.prompt().getSystemMessage() != null
				&& StringUtils.hasText(chatClientRequest.prompt().getSystemMessage().getText())) {
			sb.append("\n - SYSTEM: " + first(chatClientRequest.prompt().getSystemMessage().getText(), 50));
		}

		// Print tools
		if (this.showAvailableTools) {
			Object tools = "No Tools";

			if (chatClientRequest.prompt().getOptions() instanceof ToolCallingChatOptions toolOptions
					&& toolOptions.getToolCallbacks() != null) {
				tools = toolOptions.getToolCallbacks().stream().map(tc -> tc.getToolDefinition().name()).toList();
			}

			sb.append("\n - TOOLS: " + new JsonHelper().toJson(tools));
		}

		Message lastMessage = chatClientRequest.prompt().getLastUserOrToolResponseMessage();
		if (lastMessage.getMessageType() == MessageType.TOOL) {
			ToolResponseMessage toolResponseMessage = (ToolResponseMessage) lastMessage;
			for (ToolResponseMessage.ToolResponse toolResponse : toolResponseMessage.getResponses()) {
				String tr = toolResponse.name() + ": " + first(toolResponse.responseData(), 300);
				sb.append("\n - TOOL-RESPONSE: " + tr);
			}
		}
		else if (lastMessage.getMessageType() == MessageType.USER) {
			if (StringUtils.hasText(lastMessage.getText())) {
				sb.append("\n - TEXT: " + first(lastMessage.getText(), 300));
			}
		}

		if (this.showConversationHistory) {
			sb.append("\n - [ALL INSTRUCTIONS]: " + chatClientRequest.prompt()
					.getInstructions()
					.stream()
					.map(m -> m.getMessageType() + ": " + messageContent(m))
					.toList());
		}

		log.info(ORANGE + sb.toString() + RESET);

		return chatClientRequest;
	}

	@Override
	public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {

		StringBuilder sb = new StringBuilder("\n" + this.labelPrefix + "ASSISTANT: ");

		if (chatClientResponse.chatResponse() == null || chatClientResponse.chatResponse().getResults() == null) {
			sb.append(" No chat response ");
			log.info(sb.toString());
			return chatClientResponse;
		}

		for (Generation generation : chatClientResponse.chatResponse().getResults()) {
			AssistantMessage message = generation.getOutput();
			if (message.getToolCalls() != null) {
				for (AssistantMessage.ToolCall toolCall : message.getToolCalls()) {
					sb.append("\n - TOOL-CALL: ")
							.append(toolCall.name())
							.append(" (")
							.append(toolCall.arguments())
							.append(")");
				}
			}

			if (message.getText() != null) {
				if (StringUtils.hasText(message.getText())) {
					sb.append("\n - TEXT: " + first(message.getText(), 200));
				}
			}
		}

		log.info(ORANGE + sb.toString() + RESET);

		return chatClientResponse;
	}

	private String first(String text, int n) {
		if (text.length() <= n) {
			return text;
		}
		return text.substring(0, n) + "...";
	}

	private String messageContent(Message message) {
		if (message instanceof ToolResponseMessage toolResponseMessage) {
			return toolResponseMessage.getResponses()
					.stream()
					.map(r -> first(r.name() + ": " + r.responseData(), 30))
					.reduce((a, b) -> a + ", " + b)
					.orElse("");
		}
		else if (message instanceof AssistantMessage assistantMessage) {
			if (StringUtils.hasText(assistantMessage.getText())) {
				return first(assistantMessage.getText(), 20);
			}
			return assistantMessage.getToolCalls() != null ? assistantMessage.getToolCalls()
					.stream()
					.map(tc -> first(tc.name() + ": " + tc.arguments(), 30))
					.reduce((a, b) -> a + ", " + b)
					.orElse("") : "";
		}
		else {
			if (message.getText() != null) {
				return first(message.getText(), 30);
			}
			return "";
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private int order = 0;
		private boolean showSystemMessage = true;
		private boolean showAvailableTools = true;
		private boolean showConversationHistory = true;
		private String labelPrefix = "";

		public Builder order(int order) {
			this.order = order;
			return this;
		}

		public Builder showSystemMessage(boolean showSystemMessage) {
			this.showSystemMessage = showSystemMessage;
			return this;
		}

		public Builder showAvailableTools(boolean showAvailableTools) {
			this.showAvailableTools = showAvailableTools;
			return this;
		}

		public Builder showConversationHistory(boolean showConversationHistory) {
			this.showConversationHistory = showConversationHistory;
			return this;
		}

		public Builder labelPrefix(String labelPrefix) {
			this.labelPrefix = labelPrefix;
			return this;
		}

		public MyLoggingAdvisor build() {
			return new MyLoggingAdvisor(order, showSystemMessage, showAvailableTools, showConversationHistory, labelPrefix);
		}
	}
}
