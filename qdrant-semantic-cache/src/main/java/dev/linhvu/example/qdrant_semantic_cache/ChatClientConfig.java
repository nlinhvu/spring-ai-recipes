package dev.linhvu.example.qdrant_semantic_cache;

import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
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
	ChatClientBuilderCustomizer Customizer(SemanticCacheAdvisor semanticCacheAdvisor) {
		return builder ->builder.defaultAdvisors(semanticCacheAdvisor);
	}
}
