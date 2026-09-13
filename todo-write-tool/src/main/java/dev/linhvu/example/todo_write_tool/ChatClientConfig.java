package dev.linhvu.example.todo_write_tool;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agent.tools.TodoWriteTool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

	private static final Logger log = LoggerFactory.getLogger(ChatClientConfig.class);

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ChatClientBuilderCustomizer todoWriteToolCustomizer() {
		TodoWriteTool todoWriteTool = TodoWriteTool.builder()
				.todoEventHandler(event -> {
					List<TodoWriteTool.Todos.TodoItem> todos = event.todos();

					long completeCount = todos.stream()
							.filter(todo -> todo.status().equals(TodoWriteTool.Todos.Status.completed))
							.count();

					long percentageComplete = Math.round((completeCount * 100.0) / todos.size());

					log.info("Event ({}/{} : {}%):",
							completeCount,
							event.todos().size(),
							percentageComplete);

					event.todos().forEach(todoItem -> {
						log.info("   -- TODO Item: {} - {}",
								todoItem.status(),
								todoItem.content());
					});
				})
				.build();

		return builder -> builder.defaultTools(todoWriteTool);
	}
}
