package dev.linhvu.example.todo_write_tool;

import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientMemoryConfig {

	@Bean
	ChatClientBuilderCustomizer chatMemoryCustomizer() {
		return builder ->
				builder.defaultAdvisors(
						MessageChatMemoryAdvisor.builder(
										MessageWindowChatMemory.builder()
												.maxMessages(500)
												.build())
								.build());
	}
}
