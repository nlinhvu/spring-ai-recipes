package dev.linhvu.example;

import java.util.Random;
import java.util.function.Function;

import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.ObjectProvider;
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
	public ApplicationRunner runner(OpenAiChatModel mainChatModel, OllamaChatModel ollamaChatModel,
			ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<ChatClientBuilderCustomizer> customizers) {
		return args -> {

			// Builders made from a model get neither the observation registry (which reports tool runs)
			// nor the registered customizers (e.g. the inspector's advisors), as the auto-configured one does.
			Function<ChatModel, ChatClient.Builder> builderFor = model -> {
				ChatClient.Builder builder = ChatClient.builder(model, observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP), null, null);
				customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
				return builder;
			};
			ChatClient.Builder mainBuilder = builderFor.apply(mainChatModel);
			ChatClient.Builder judgeBuilder = builderFor.apply(ollamaChatModel);

			ChatClient chatClient = mainBuilder
					.defaultTools(new MyTools())

					.defaultAdvisors(
							SelfRefineEvaluationAdvisor.builder()
									.order(Ordered.HIGHEST_PRECEDENCE + 100)
									.chatClientBuilder(judgeBuilder)
									.maxRepeatAttempts(3)
									.successRating(3)
									.build()
					)

					.defaultAdvisors(
							MyLoggingAdvisor.builder()
									.order(Ordered.HIGHEST_PRECEDENCE + 150)
									.build()
					)

					.build();

			String answer = chatClient
					.prompt("What is current weather in Paris?")
					.call()
					.content();

			log.info("\n -------------------------------- \n{}", answer);
		};
	}

	static class MyTools {

		final int[] temperatures = { -125, 15, -255 };

		private final Random random = new Random();

		@Tool(description = "Get the current weather for a given location")
		public String weather(String location) {
			int temperature = temperatures[random.nextInt(temperatures.length)];
			log.info("[TOOL] weather tool response temperature: {}", temperature);
			return "The current weather in " + location + " is sunny with a temperature of " + temperature + "°C.";
		}

	}

}
