package dev.linhvu.example.voicechat_stt;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class VoicechatSttApplication {

	private static final Logger log = LoggerFactory.getLogger(VoicechatSttApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(VoicechatSttApplication.class, args);
	}

	@Bean
	ApplicationRunner runner(ChatClient chatClient, AudioRecorder recorder, Transcriber transcriber) {
		return args -> {
			log.info("How can I help?\n");

			try (Scanner scanner = new Scanner(System.in)) {
				while (true) {
					log.info("> ");
					if (!scanner.hasNextLine()) break;
					var input = scanner.nextLine();
					if (input.isBlank()) continue;

					if (input.equalsIgnoreCase("/record")) {
						recorder.start();
						continue;
					}

					if (input.equalsIgnoreCase("/stop")) {
						byte[] audio = recorder.stop();
						input = transcriber.transcribe(audio);
						log.info("> {}", input);
					}

					log.info("\n - {}", chatClient.prompt(input)
							.call()
							.content());
				}
			}
		};
	}
}
