package dev.linhvu.example.rag_tool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ChatClientBuilderCustomizer systemPromptCustomizer() {
		return builder ->
				builder.defaultSystem("""
            Always use the sagrada-helper tool when answering questions
            about the board game called Sagrada."""
				);
	}

	@Bean
	ChatClientBuilderCustomizer addRagTools(RagTools ragTools) {
		return builder ->
				builder.defaultTools(ragTools);
	}
}
