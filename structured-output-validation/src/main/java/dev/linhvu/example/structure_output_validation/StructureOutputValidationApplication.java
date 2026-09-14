package dev.linhvu.example.structure_output_validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StructureOutputValidationApplication {

	private static final Logger log = LoggerFactory.getLogger(StructureOutputValidationApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(StructureOutputValidationApplication.class, args);
	}

	@Bean
	ApplicationRunner runner(ChatClient chatClient, JsonMapper jsonMapper) {
		return args -> {

			ChatClientConfig.TopSongs topSongs = chatClient.prompt()
					.user("""
							What were the top 10 songs on the Billboard Year-End Hot 100 singles of 1985?
							""")
					.call()
					.entity(ChatClientConfig.TopSongs.class,
							spec -> spec
									.validateSchema()
									.useProviderStructuredOutput()); // for native structured output like OpenAI

			log.info(jsonMapper.writeValueAsString(topSongs));
		};
	}
}
