package dev.linhvu.example.safeguard_output;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class ChatClientConfig {

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ChatClientBuilderCustomizer systemPromptCustomizer() {
		return builder -> builder.defaultSystem("""
        You are a helpful assistant able to answer all (or almost all)
        questions about the Disney movie Encanto. Don't answer questions
        about anything that doesn't pertain to that movie.
        """);
	}

	@Bean
	ChatClientBuilderCustomizer outputSafeguardCustomizer() {
		OutputSafeGuardAdvisor safeguard = OutputSafeGuardAdvisor.builder()
				.sensitiveWords("Bruno", "bruno", "BRUNO", "vision", "prophecy")
				.failureResponse("We don't talk about Bruno.")
				.order(Ordered.LOWEST_PRECEDENCE)
				.build();

		return builder -> builder.defaultAdvisors(safeguard);
	}
}
