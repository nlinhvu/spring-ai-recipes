package dev.linhvu.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.session.DefaultSessionService;
import org.springframework.ai.session.InMemorySessionRepository;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionMemoryAdvisor;
import org.springframework.ai.session.compaction.SlidingWindowCompactionStrategy;
import org.springframework.ai.session.compaction.TurnCountTrigger;
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
	SessionService sessionService() {
		return DefaultSessionService.builder().sessionRepository(InMemorySessionRepository.builder().build()).build();
	}

	@Bean
	SessionMemoryAdvisor sessionMemoryAdvisor(SessionService sessionService) {
		return SessionMemoryAdvisor.builder(sessionService)
				.defaultUserId("cloud")
				// Compact when 3 turns accumulate, keeping the last 2 events
				.compactionTrigger(new TurnCountTrigger(3))
				.compactionStrategy(SlidingWindowCompactionStrategy.builder().maxEvents(2).build())
				.build();
	}

	@Bean
	public ApplicationRunner runner(ChatClient.Builder chatClientBuilder, SessionMemoryAdvisor sessionMemoryAdvisor) {
		return args -> {

			ChatClient chatClient = chatClientBuilder
					.defaultAdvisors(sessionMemoryAdvisor)
					.defaultAdvisors(MyLoggingAdvisor.builder().build())
					.build();

			log.info("Name introduction: {}", chatClient.prompt("My name is Linh Vu")
					.advisors(a -> a.param(SessionMemoryAdvisor.SESSION_ID_CONTEXT_KEY, "cloud"))
					.call().content());

			log.info("Asking for the name: {}", chatClient.prompt("What is my name?")
					.advisors(a -> a.param(SessionMemoryAdvisor.SESSION_ID_CONTEXT_KEY, "cloud"))
					.call().content());

		};
	}
}
