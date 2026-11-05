package dev.linhvu.example.tool_use;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.openai.responses.OpenAiResponsesMetadata;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ToolUseApplication {

	private static final Logger log = LoggerFactory.getLogger(ToolUseApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ToolUseApplication.class, args);
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
					ChatClientResponse ccr = chatClient.prompt(input)
							.call()
							.chatClientResponse();

					String reasoningSummary = ccr.chatResponse().getResult().getMetadata().get(OpenAiResponsesMetadata.REASONING_CONTENT);

					log.info("====\n{}\n====", reasoningSummary);

					String content = ccr.chatResponse().getResult().getOutput().getText();
					log.info("\n - {}", content);
				}
			}
		};
	}
}
