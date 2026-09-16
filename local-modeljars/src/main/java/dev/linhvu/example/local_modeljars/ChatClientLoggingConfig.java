package dev.linhvu.example.local_modeljars;

import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientLoggingConfig {

	@Bean
	ChatClientBuilderCustomizer loggingCustomizer() {
		return builder -> builder.defaultAdvisors(
				SimpleLoggerAdvisor.builder().build()
		);
	}
}
