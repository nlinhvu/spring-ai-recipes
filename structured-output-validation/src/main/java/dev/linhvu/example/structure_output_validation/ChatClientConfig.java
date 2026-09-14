package dev.linhvu.example.structure_output_validation;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ChatClientBuilderCustomizer structuredOutputValidationCustomizer() {
		StructuredOutputValidationAdvisor advisor = StructuredOutputValidationAdvisor.builder()
				.outputType(TopSongs.class)
				.maxRepeatAttempts(2)
				.build();

		return builder -> builder.defaultAdvisors(advisor);
	}

	public record TopSongs(String year, List<Song> songs) {}
	public record Song(String title, String artist) {}

}
