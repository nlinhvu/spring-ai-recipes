package dev.linhvu.example;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agent.tools.task.TaskTool;
import org.springaicommunity.agent.tools.task.claude.ClaudeSubagentReferences;
import org.springaicommunity.agent.tools.task.claude.ClaudeSubagentType;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public ApplicationRunner runner(ChatClient.Builder chatClientBuilder,
			@Value("${agent.tasks.paths}") List<Resource> agentPaths,
			@Value("${agent.skills.paths}") List<Resource> skillPaths) {
		return args -> {

			ToolCallback taskTools = TaskTool.builder()
					.subagentReferences(ClaudeSubagentReferences.fromResources(agentPaths))
					.subagentTypes(
							ClaudeSubagentType.builder()
									.skillsResources(skillPaths)
									// configuration used by the local Claude subagents
									.chatClientBuilder("default", chatClientBuilder.clone())
									.chatClientBuilder("webfetch", chatClientBuilder.clone())
									// Web search
//									.braveApiKey(braveApiKey)
									.build()
					)
					.build();

			ChatClient chatClient = chatClientBuilder
					.defaultSystem("When telling jokes, always use the joke-teller agent.")
					.defaultTools(taskTools)
					.defaultAdvisors(MyLoggingAdvisor.builder().build())
					.build();

			String answer = chatClient.prompt("Tell me a joke about the weather!").call().content();

			log.info("Answer: {}", answer);
		};
	}
}
