package dev.linhvu.example.graph_workflow_langgraph4j;

import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import org.bsc.langgraph4j.CompiledGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GraphWorkflowLanggraph4jApplication {

	private static final Logger log = LoggerFactory.getLogger(GraphWorkflowLanggraph4jApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(GraphWorkflowLanggraph4jApplication.class, args);
	}

	@Bean
	ApplicationRunner runner(CompiledGraph<SupportState> compiledGraph) {
		return args -> {
			log.info("How can I help?\n");

			try (Scanner scanner = new Scanner(System.in)) {
				while (true) {
					log.info("> ");
					if (!scanner.hasNextLine()) break;
					var input = scanner.nextLine();
					if (input.isBlank()) continue;

					Optional<SupportState> response = compiledGraph.invoke(Map.of("user_question", input));
					String resolution = response.get().getResolution();

					log.info("\n - {}", resolution);
				}
			}
		};
	}
}
