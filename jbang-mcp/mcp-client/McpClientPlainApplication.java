///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25
//BOM org.springframework.boot:spring-boot-dependencies:4.1.1
//DEPS org.springframework.ai:spring-ai-starter-model-openai:2.0.1
//DEPS org.springframework.ai:spring-ai-starter-mcp-client:2.0.1
//DEPS org.zalando:logbook-spring-boot-starter:4.1.0
//DEPS org.zalando:logbook-okhttp:4.1.0
//DEPS com.fasterxml.jackson.core:jackson-annotations:2.21
//SOURCES ChatClientConfig.java
//SOURCES ChatClientLoggingConfig.java

package dev.linhvu.example.mcpclientplain;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpClientPlainApplication {

	private static final Logger log = LoggerFactory.getLogger(McpClientPlainApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(McpClientPlainApplication.class, args);
	}

	@Bean
	ApplicationRunner runner(ChatClient chatClient) {
		return args -> {
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
