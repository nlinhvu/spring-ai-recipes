package dev.linhvu.example.voicechat_tts;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class VoicechatTtsApplication {

	private static final Logger log = LoggerFactory.getLogger(VoicechatTtsApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(VoicechatTtsApplication.class, args);
	}

	@Bean
	ApplicationRunner runner(ChatClient chatClient, SpeechService speechService) {
		return args -> {
			log.info("How can I help?\n");

			try (Scanner scanner = new Scanner(System.in)) {
				while (true) {
					log.info("> ");
					if (!scanner.hasNextLine()) break;
					var input = scanner.nextLine();
					if (input.isBlank()) continue;
					String answer = chatClient.prompt(input)
							.call()
							.content();
					log.info("\n - {}", answer);
					speechService.speak(answer);
				}
			}
		};
	}
}
