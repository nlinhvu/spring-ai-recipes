package dev.linhvu.inspector.client;

import java.net.URI;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

public class SmokeDemo {

	public static void main(String[] args) throws Exception {
		boolean fail = args.length > 0 && args[0].equals("error");
		ChatModel model = prompt -> {
			if (fail) throw new IllegalStateException("Synthetic model failure");
			return new ChatResponse(List.of(new Generation(new AssistantMessage("fixture answer"))));
		};
		try (InspectorClient sender = new InspectorClient(URI.create("http://127.0.0.1:9002"))) {
			System.out.println("Producer: " + sender.producerId());
			ChatClient client = ChatClient.builder(model).defaultAdvisors(new InspectorAdvisor(sender)).build();
			System.out.println(client.prompt("first synthetic request").call().content());
			System.out.println("Run the second call after 8 seconds");
			Thread.sleep(8000);
			System.out.println(client.prompt("second synthetic request").call().content());
		}
	}
}
