package dev.linhvu.example;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
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

			MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(10).build();
			MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

			String sessionId = UUID.randomUUID().toString();

			ChatClient chatClient = chatClientBuilder
					.defaultAdvisors(MyLoggingAdvisor.builder().build())
					.defaultAdvisors(a -> a.advisors(memoryAdvisor)
							.param(ChatMemory.CONVERSATION_ID, sessionId))
					.build();

			log.info("Name introduction: {}", chatClient.prompt("My name is Linh Vu").call().content());

			log.info("Asking for the name: {}", chatClient.prompt("What is my name?").call().content());

		};
	}
}
