package dev.linhvu.example.tool_use;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
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

	// Create vector-store
	// curl https://api.openai.com/v1/vector_stores \
	//  -H "Authorization: Bearer $OPENAI_API_KEY" \
	//  -H "Content-Type: application/json" \
	//  -d '{
	//    "name": "Spring AI Recipe Documents"
	//  }'

	// Upload a document
	// curl https://api.openai.com/v1/files \
	//  -H "Authorization: Bearer $OPENAI_API_KEY" \
	//  -F "purpose=assistants" \
	//  -F "file=@ClockworkCove.pdf"

	// Attack the document to the vector-store
	// curl https://api.openai.com/v1/vector_stores/{vs_id}/files \
	//  -H "Authorization: Bearer $OPENAI_API_KEY" \
	//  -H "Content-Type: application/json" \
	//  -d '{
	//    "file_id": "{file_id}"
	//  }'

	// Check status the async command above
	// curl https://api.openai.com/v1/vector_stores/{vs_id}/files/{file_id} \
	//  -H "Authorization: Bearer $OPENAI_API_KEY"

	// Clean up
	// curl https://api.openai.com/v1/vector_stores \
	//  -H "Authorization: Bearer $OPENAI_API_KEY"

	// curl https://api.openai.com/v1/vector_stores/{vs_id}/files \
	//  -H "Authorization: Bearer $OPENAI_API_KEY"

	// curl -X DELETE \
	//  https://api.openai.com/v1/vector_stores/{vs_id} \
	//  -H "Authorization: Bearer $OPENAI_API_KEY"

	// curl -X DELETE \
	//  https://api.openai.com/v1/files/{file_id} \
	//  -H "Authorization: Bearer $OPENAI_API_KEY"

	@Bean
	ApplicationRunner runner(ChatClient.Builder builder) {
		return args -> {
			ChatClient chatClient = builder.build();

			log.info("How can I help?\n");

			try (Scanner scanner = new Scanner(System.in)) {
				while (true) {
					log.info("> ");
					if (!scanner.hasNextLine()) break;
					var input = scanner.nextLine();
					if (input.isBlank()) continue;
					log.info("\n - {}", chatClient.prompt(input)
							.call()
							.content());
				}
			}
		};
	}
}
