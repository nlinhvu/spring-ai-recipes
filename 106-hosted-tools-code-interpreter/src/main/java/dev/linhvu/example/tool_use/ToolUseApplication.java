package dev.linhvu.example.tool_use;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.part.UnknownPart;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.responses.OpenAiResponsesMetadata;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ToolUseApplication {

	private static final Logger log = LoggerFactory.getLogger(ToolUseApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ToolUseApplication.class, args);
	}

	@Bean
	ApplicationRunner runner(ChatClient.Builder builder) {
		return args -> {
			ChatClient chatClient = builder.build();

			String task = """
					Use Python to simulate rolling five six-sided dice
					100,000 times. Report the most common total and
					the probability of rolling at least 25.
					""";

			ChatResponse chatResponse = chatClient.prompt()
					.user(task)
					.call()
					.chatResponse();
			String answer = chatResponse.getResult().getOutput().getText();

			log.info(answer);

			Object toolCalls = chatResponse.getResult().getMetadata().get(OpenAiResponsesMetadata.HOSTED_TOOL_CALLS);
			log.info("Hosted tool calls: {}", toolCalls);

			JsonMapper jsonMapper = JsonMapper.builder().build();
			chatResponse.getResult().getOutput().getParts().stream()
					.filter(p -> p instanceof UnknownPart)
					.map(p -> (UnknownPart) p)
					.filter(p -> "code_interpreter_call".equals(p.kind()))
					.map(p -> jsonMapper.readTree(p.rawJson()).path("code").asString())
					.forEach(log::info);
		};
	}
}
