package dev.linhvu.example;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

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
					.defaultAdvisors(MyLoggingAdvisor.builder().order(Ordered.HIGHEST_PRECEDENCE + 2000).build())
					.build();

			record ActorsFilms(String actor, List<String> movies) {}

			ActorsFilms actorsFilms = chatClient.prompt()
					.user("Generate the filmography of 5 movies for Tom Hanks.")
					.call()
					.entity(ActorsFilms.class, e -> e.useProviderStructuredOutput().validateSchema());

			log.info(actorsFilms.toString());

		};
	}
}
