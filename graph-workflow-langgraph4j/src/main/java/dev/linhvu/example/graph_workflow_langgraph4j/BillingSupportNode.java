package dev.linhvu.example.graph_workflow_langgraph4j;

import java.util.Map;

import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;

public class BillingSupportNode implements NodeAction<SupportState> {

	private static final Logger log = LoggerFactory.getLogger(BillingSupportNode.class);

	private ChatClient chatClient;

	public BillingSupportNode(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@Override
	public Map<String, Object> apply(SupportState state) throws Exception {
		log.info("Handling billing question.");

		String resolution = chatClient.prompt()
				.system("""
            You are a billing support assistant.
            Write a brief, helpful response to the customer.
            Do not promise refunds. Say that the billing team will 
            review the account.
            """)
				.user(state.getUserQuestion())
				.call()
				.content();

		return Map.of("resolution", resolution);
	}
}
