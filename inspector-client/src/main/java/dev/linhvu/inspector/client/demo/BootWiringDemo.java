package dev.linhvu.inspector.client.demo;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import dev.linhvu.inspector.client.InspectorClient;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
@EnableAutoConfiguration
public class BootWiringDemo {
	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(BootWiringDemo.class);
		application.setDefaultProperties(Map.of("spring.main.web-application-type", "none",
				"spring.main.banner-mode", "off"));
		try (var context = application.run(args)) {
			System.out.println("Demo complete; closing application context");
		}
	}

	@Bean
	ChatModel fixtureModel() {
		return new ChatModel() {
			@Override public ChatResponse call(Prompt prompt) { return response("fixture answer"); }
			@Override public Flux<ChatResponse> stream(Prompt prompt) {
				return Flux.just(response("one "), response("two "), response("three"))
						.delayElements(Duration.ofMillis(300));
			}
			private ChatResponse response(String text) {
				return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
			}
		};
	}

	@Bean
	ApplicationRunner fixtureRunner(ObjectProvider<ChatClient.Builder> builders,
			ObjectProvider<InspectorClient> sender) {
		return args -> {
			System.out.println(sender.getIfAvailable() == null ? "Inspector inactive" : "Run: " + sender.getObject().runId() + " / Producer: " + sender.getObject().producerId());
			System.out.println(builders.getObject().build().prompt("first").call().content());
			if (args.containsOption("recovery") && args.getOptionValues("recovery").contains("true")) {
				System.out.println("Recovery window: start the collector within 8 seconds");
				Thread.sleep(8000);
			}
			System.out.println(builders.getObject().build().prompt("second").call().content());
			builders.getObject().build().prompt("stream").stream().content()
					.doOnNext(System.out::println).blockLast(Duration.ofSeconds(10));
		};
	}
}
