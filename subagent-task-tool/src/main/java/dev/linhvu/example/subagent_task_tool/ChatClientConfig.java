package dev.linhvu.example.subagent_task_tool;

import java.util.List;

import org.springaicommunity.agent.tools.task.TaskTool;
import org.springaicommunity.agent.tools.task.claude.ClaudeSubagentReferences;
import org.springaicommunity.agent.tools.task.claude.ClaudeSubagentType;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

@Configuration
public class ChatClientConfig {

	@Bean
	@Primary
	ChatClient.Builder openAiChatClientBuilder(
			OpenAiChatModel openAiChatModel,
			ObjectProvider<ChatClientBuilderCustomizer> customizers) {

		ChatClient.Builder builder = ChatClient.builder(openAiChatModel);

		customizers.forEach(customizer -> customizer.customize(builder));

		return builder;
	}

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ChatClient.Builder subagentChatClientBuilder(
			OpenAiChatModel openAiChatModel) {
		return ChatClient.builder(openAiChatModel);
	}

	@Bean
	ChatClientBuilderCustomizer subagentChatClientBuilderCustomizer(
			@Value("${agent.tasks.paths}") List<Resource> agentPaths,
			@Qualifier("subagentChatClientBuilder") ChatClient.Builder subagentChatClientBuilder) {
		ToolCallback toolCallback = TaskTool.builder()
				.subagentReferences(ClaudeSubagentReferences.fromResources(agentPaths))
				.subagentTypes(
						ClaudeSubagentType.builder()
								.chatClientBuilder("default", subagentChatClientBuilder)
								.build()
				)
				.build();

		return builder -> builder.defaultTools(toolCallback)
				.defaultSystem("When telling jokes, always use the joke-teller agent.");
	}
}
