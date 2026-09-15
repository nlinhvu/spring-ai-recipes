package dev.linhvu.example.graph_workflow_langgraph4j;

import java.util.Map;

import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;

public class TechnicalSupportNode implements NodeAction<SupportState> {

	private static final Logger log = LoggerFactory.getLogger(TechnicalSupportNode.class);

	private final ChatClient chatClient;

	public TechnicalSupportNode(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@Override
	public Map<String, Object> apply(SupportState state) throws Exception {
		log.info("Handling technical question.");

		String resolution = chatClient.prompt()
				.system("""
            You are a technical support assistant.
            Write a brief, helpful response to the customer.
            Suggest one or two practical troubleshooting steps or
            ask if they have tried turning it off and then back on again.
            """)
				.user(state.getUserQuestion())
				.call()
				.content();

		return Map.of("resolution", resolution);
	}
}
