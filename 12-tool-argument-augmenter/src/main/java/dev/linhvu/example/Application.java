package dev.linhvu.example;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.augment.AugmentedToolCallbackProvider;
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

	public record AgentThinking(
			@ToolParam(description = "Your step-by-step reasoning for why you're calling this tool and what you expect",
					required = true) String innerThought,

			@ToolParam(description = "Confidence level (low, medium, high) in this tool choice",
					required = false) String confidence,

			@ToolParam(description = "Key insights to remember for future interactions",
					required = true) List<String> memoryNotes) {
	}

	@Bean
	public ApplicationRunner runner(ChatClient.Builder chatClientBuilder) {
		return args -> {

			AugmentedToolCallbackProvider<AgentThinking> augmentedTools = AugmentedToolCallbackProvider
					.<AgentThinking>builder()
					.toolObject(new MyTools())
					.argumentType(AgentThinking.class)
					.argumentConsumer(event -> {
						// Access the extended arguments via event.arguments()
						AgentThinking thinking = event.arguments();

						// Log the LLM's reasoning - great for debugging and observability
						log.info("LLM Reasoning: {}", thinking.innerThought());
						log.info("Confidence: {}", thinking.confidence());
						log.info("Thinking notes: {}", thinking.memoryNotes());

						// Access additional context from the event
						log.info("Tool: {}", event.toolDefinition().name());
					})
					// Remove before calling actual tool
					.removeExtraArgumentsAfterProcessing(true)
					.build();

			ChatClient chatClient = chatClientBuilder
					.defaultTools(augmentedTools)
					.defaultAdvisors(MyLoggingAdvisor.builder().build())
					.build();

			String answer = chatClient
					.prompt("What is current weather in Paris?")
					.call()
					.content();

			log.info("\n -------------------------------- \n{}", answer);

		};
	}

	static class MyTools {

		@Tool(description = "Get the current weather for a given location")
		public String weather(String location) {
			return "The current weather in " + location + " is sunny with a temperature of 25°C.";
		}

	}
}
