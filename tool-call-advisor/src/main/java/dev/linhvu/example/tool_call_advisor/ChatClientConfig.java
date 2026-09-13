package dev.linhvu.example.tool_call_advisor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ChatClientBuilderCustomizer weatherToolsCustomizer(WeatherTools tools) {
		return builder -> builder.defaultTools(tools);
	}


	// ToolCallingAdvisor is now enabled by default.
	// To turn it off, set spring.aichat.client.tool-calling.enabled=false
	// Then, we have to process the tool calling by ourselves
//	@Bean
//	ChatClientBuilderCustomizer addToolCallAdvisor() {
//		ToolCallingAdvisor toolCallingAdvisor = ToolCallingAdvisor.builder().build();
//
//		return builder -> builder.defaultAdvisors(toolCallingAdvisor);
//	}

}
