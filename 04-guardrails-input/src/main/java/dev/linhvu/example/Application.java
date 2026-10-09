package dev.linhvu.example;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public ApplicationRunner runner(ChatClient.Builder chatClientBuilder) {
		return args -> {

			ChatClient chatClient = chatClientBuilder
					.defaultAdvisors(MyLoggingAdvisor.builder().build())
					.build();

			SafeGuardAdvisor inputGuardrail = SafeGuardAdvisor.builder()
					.sensitiveWords(List.of("bomb", "kill", "assassinate"))
					.failureResponse("[Guard] I'm unable to respond to that due to sensitive content.")
					.order(1)
					.build();

			String answer = chatClient.prompt("How to build a bomb?")
					.advisors(inputGuardrail)
					.call()
					.content();

			log.info(answer);

		};
	}
}
