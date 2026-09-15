package dev.linhvu.example.graph_workflow_langgraph4j;

import java.util.Map;

import org.bsc.langgraph4j.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;

public class ClassifySupportRequestNode implements NodeAction<SupportState> {

	private static final Logger log = LoggerFactory.getLogger(ClassifySupportRequestNode.class);

	private final ChatClient chatClient;

	public ClassifySupportRequestNode(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@Override
	public Map<String, Object> apply(SupportState state) throws Exception {
		log.info("Classifying user question");

		String category = chatClient.prompt()
				.system("""
            Classify the customer support request as exactly one of:
            billing
            technical
            Respond with only the category name.
            """)
				.user(state.getUserQuestion())
				.call()
				.content()
				.trim()
				.toLowerCase();

		if (!category.equals("billing") && !category.equals("technical")) {
			log.info("Question category({}) unknown. Defaulting to technical.", category);
			category = "technical";
		}
		return Map.of("category", category);
	}
}
