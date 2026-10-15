package dev.linhvu.inspector.client;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

public class StreamSmokeDemo {

	public static void main(String[] args) {
		String mode = args.length == 0 ? "normal" : args[0];
		try (InspectorClient sender = new InspectorClient(URI.create("http://127.0.0.1:9002"))) {
			System.out.println("Producer: " + sender.producerId());
			FixtureModel model = new FixtureModel();
			ChatClient client = ChatClient.builder(model).defaultAdvisors(new InspectorAdvisor(sender)).build();
			model.client = client;
			run(client, mode);
		}
	}

	static final class FixtureModel implements ChatModel {
		ChatClient client;
		@Override public ChatResponse call(Prompt prompt) { return response("sync answer"); }
		@Override public Flux<ChatResponse> stream(Prompt prompt) {
			String mode = prompt.getUserMessage().getText();
			if ("setup-error".equals(mode)) throw new IllegalStateException("Synthetic setup failure");
			if ("outer".equals(mode)) return client.prompt("inner").stream().chatResponse();
			Flux<ChatResponse> first = Flux.just(response("one ")).delayElements(Duration.ofMillis(500));
			if ("error".equals(mode)) return first.concatWith(
					Flux.error(new IllegalStateException("Synthetic stream failure")));
			return Flux.just(response("one "), response("two "), response("three"))
					.delayElements(Duration.ofMillis(500));
		}
		private static ChatResponse response(String text) {
			return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
		}
	}

	static Flux<String> fresh(ChatClient client, String prompt) {
		return Flux.defer(() -> client.prompt(prompt).stream().content());
	}

	static void run(ChatClient client, String mode) {
		switch (mode) {
			case "normal" -> fresh(client, "normal").doOnNext(System.out::println).blockLast(Duration.ofSeconds(10));
			case "error", "setup-error" -> fresh(client, mode).blockLast(Duration.ofSeconds(10));
			case "cancel" -> fresh(client, "normal").take(2).doOnNext(System.out::println)
					.blockLast(Duration.ofSeconds(10));
			case "parallel" -> Flux.merge(fresh(client, "A"), fresh(client, "B"))
					.doOnNext(value -> System.out.println(Thread.currentThread().getName() + ": " + value))
					.blockLast(Duration.ofSeconds(10));
			case "repeat" -> {
				Flux<String> stream = fresh(client, "normal");
				stream.blockLast(Duration.ofSeconds(10));
				stream.blockLast(Duration.ofSeconds(10));
			}
			case "cold" -> {
				fresh(client, "normal");
				System.out.println("Publisher assembled without subscription");
			}
			case "nested" -> fresh(client, "outer").doOnNext(System.out::println)
					.blockLast(Duration.ofSeconds(10));
			case "async" -> {
				try (var callers = Executors.newVirtualThreadPerTaskExecutor()) {
					var a = CompletableFuture.supplyAsync(() -> client.prompt("A").call().content(), callers);
					var b = CompletableFuture.supplyAsync(() -> client.prompt("B").call().content(), callers);
					System.out.println(a.join());
					System.out.println(b.join());
				}
			}
			default -> throw new IllegalArgumentException("Unknown scenario: " + mode);
		}
	}
}
