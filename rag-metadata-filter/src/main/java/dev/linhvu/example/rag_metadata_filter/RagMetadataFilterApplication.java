package dev.linhvu.example.rag_metadata_filter;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RagMetadataFilterApplication {

	private static final Logger log = LoggerFactory.getLogger(RagMetadataFilterApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(RagMetadataFilterApplication.class, args);
	}

	@Bean
	ApplicationRunner runner(ChatClient chatClient, TitleHelper titleHelper) {
		return args -> {
			log.info("How can I help?\n");

			try (Scanner scanner = new Scanner(System.in)) {
				while (true) {
					log.info("> ");
					if (!scanner.hasNextLine()) break;
					var input = scanner.nextLine();
					if (input.isBlank()) continue;

					String gameTitle = titleHelper.determineGameTitle(input);

					String answer = chatClient.prompt(input)
							.advisors(spec -> {
								if (gameTitle != null) {
									spec.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, String.format("title == '%s'", gameTitle));
								}
							})
							.call()
							.content();

					log.info("\n - {}", answer);
				}
			}
		};
	}
}
