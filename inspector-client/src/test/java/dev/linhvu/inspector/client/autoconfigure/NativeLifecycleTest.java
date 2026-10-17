package dev.linhvu.inspector.client.autoconfigure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.linhvu.inspector.client.InspectorClient;
import dev.linhvu.inspector.client.NativeTestClients;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.util.JsonHelper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class NativeLifecycleTest {
	@Test
	void beanDependencyDestroysRunLifecycleBeforeClosingSender() {
		List<String> wire = new ArrayList<>();
		InspectorAutoConfiguration configuration = new InspectorAutoConfiguration();
		try (var context = new AnnotationConfigApplicationContext()) {
			context.registerBean("inspectorClient", InspectorClient.class, () -> NativeTestClients.recording(wire::add),
					definition -> definition.setDestroyMethodName("close"));
			context.registerBean("lifecycle", InspectorAutoConfiguration.RunLifecycle.class,
					() -> configuration.inspectorRunLifecycle(context.getBean(InspectorClient.class), new MockEnvironment()),
					definition -> definition.setDependsOn("inspectorClient"));
			context.refresh();
			ChatModel model = new ChatModel() {
				private ChatResponse response() { return new ChatResponse(List.of(new Generation(new AssistantMessage("ok")))); }
				@Override public ChatResponse call(Prompt prompt) { return response(); }
				@Override public Flux<ChatResponse> stream(Prompt prompt) { return Flux.just(response(), response(), response()); }
			};
			ChatClient.Builder builder = ChatClient.builder(model);
			configuration.inspectorChatClientCustomizer(context.getBean(InspectorClient.class)).customize(builder);
			ChatClient client = builder.build();
			client.prompt("one").call().content();
			client.prompt("two").call().content();
			client.prompt("stream").stream().content().blockLast();
		}
		JsonHelper json = new JsonHelper();
		List<Map> events = wire.stream().map(body -> json.fromJson(body, Map.class)).toList();
		assertEquals(11, events.size());
		assertEquals("run-start", events.getFirst().get("type"));
		assertEquals("run-end", events.getLast().get("type"));
		assertEquals(1, events.stream().map(e -> e.get("runId")).distinct().count());
	}
}
