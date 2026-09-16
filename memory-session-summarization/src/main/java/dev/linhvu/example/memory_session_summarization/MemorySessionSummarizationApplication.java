package dev.linhvu.example.memory_session_summarization;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.session.advisor.SessionMemoryAdvisor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MemorySessionSummarizationApplication {

	private static final Logger log = LoggerFactory.getLogger(MemorySessionSummarizationApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(MemorySessionSummarizationApplication.class, args);
	}

	@Bean
	ApplicationRunner runner(ChatClient chatClient) {
		return args -> {
			log.info("How can I help?\n");

			try (Scanner scanner = new Scanner(System.in)) {
				while (true) {
					log.info("> ");
					if (!scanner.hasNextLine()) break;
					var input = scanner.nextLine();
					if (input.isBlank()) continue;

					ChatResponse response = chatClient.prompt(input)
							.advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, "DEMO")) // Required to enable memory for specific user, here user is DEMO
							.call()
							.chatResponse();

					Usage tokens = response.getMetadata().getUsage();
					String answer = response.getResult().getOutput().getText();

					log.info("\n - {}", answer);
					log.info("          (Token usage: {} input, {} output, {} total)\n\n",
							tokens.getPromptTokens(), tokens.getCompletionTokens(), tokens.getTotalTokens());
				}
			}
		};
	}
}
