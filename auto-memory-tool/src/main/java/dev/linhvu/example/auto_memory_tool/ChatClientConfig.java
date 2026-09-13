package dev.linhvu.example.auto_memory_tool;

import org.springaicommunity.agent.advisors.AutoMemoryToolsAdvisor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}

	@Value("${agent.memories.dir}")
	String memoriesRootDirectory;



	@Bean
	ChatClientBuilderCustomizer autoMemoryToolCustomizer() {
		//	AutoMemoryToolsAdvisor leverages the LLM to:
		//
		//	determine which facts are important and durable
		//	extract those facts from conversation
		//	persist them as structured Markdown files
		//	reintroduce them into future prompts
		AutoMemoryToolsAdvisor autoMemoryToolsAdvisor = AutoMemoryToolsAdvisor.builder().memoriesRootDirectory(memoriesRootDirectory).build();

		return builder -> builder.defaultAdvisors(autoMemoryToolsAdvisor);
	}
}
