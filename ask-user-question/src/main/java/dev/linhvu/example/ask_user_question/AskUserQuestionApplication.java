package dev.linhvu.example.ask_user_question;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AskUserQuestionApplication {

	private static final Logger log = LoggerFactory.getLogger(AskUserQuestionApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(AskUserQuestionApplication.class, args);
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
					log.info("\n - {}", chatClient.prompt(input)
							.call()
							.content());
				}
			}
		};
	}
}
