package dev.linhvu.example.augmented_tool_callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.tool.augment.AugmentedToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

	private static final Logger log = LoggerFactory.getLogger(ChatClientConfig.class);

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ChatClientBuilderCustomizer augmentedToolCallbackProviderCustomizer(DateTimeTools tools) {
		AugmentedToolCallbackProvider<ToolChoiceExplanation> provider = AugmentedToolCallbackProvider.<ToolChoiceExplanation>builder()
				.toolObject(tools)
				.argumentType(ToolChoiceExplanation.class)
				.argumentConsumer(event -> {
					ToolChoiceExplanation thinking = event.arguments();
					log.debug("Tool called : {}", event.toolDefinition().name());
					log.debug("Reasoning   : {}", thinking.innerThought());
					log.debug("Confidence  : {}", thinking.confidence());
					log.debug("MemoryNotes : {}", thinking.memoryNotes());
				})
				.build();

		return builder -> builder.defaultTools(provider);
	}

}
