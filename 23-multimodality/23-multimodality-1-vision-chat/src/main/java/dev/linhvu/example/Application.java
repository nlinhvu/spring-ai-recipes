package dev.linhvu.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public ApplicationRunner runner(ChatClient.Builder chatClientBuilder) {
		return args -> {

			ChatClient chatClient = chatClientBuilder.build();

			String answer1 = chatClient.prompt()
					.user(
							u -> u.text("This chart shows ticket sales per day of a conference. Which day sold the most, and roughly how many? Answer in one sentence.")
									.media(MimeTypeUtils.IMAGE_PNG, new ClassPathResource("ticket-sales-per-day.png"))
					)
					.call()
					.content();
			log.info("\nchart: {}", answer1);

			String answer2 = chatClient.prompt()
					.user(
							u -> u.text("In two sentences: where and when did this hurricane make landfall?")
									.media(MimeType.valueOf("application/pdf"), new ClassPathResource("wikipedia-hurricane-milton-page.pdf"))
					)
					.call()
					.content();
			log.info("\npdf: {}", answer2);

		};
	}
}
