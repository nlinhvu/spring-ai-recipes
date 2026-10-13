package dev.linhvu.example;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agent.tools.BraveWebSearchTool;
import org.springaicommunity.agent.tools.ShellTools;
import org.springaicommunity.agent.tools.TodoWriteTool;
import org.springaicommunity.agent.utils.AgentEnvironment;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public ApplicationRunner runner(ChatClient.Builder chatClientBuilder, ApplicationEventPublisher applicationEventPublisher) {
		return args -> {

			TodoWriteTool todoWriteTool = TodoWriteTool.builder()
					// (optional) publish events
					.todoEventHandler(event ->
							applicationEventPublisher.publishEvent(new TodoUpdateEvent(this, event.todos())))
					.build();

			ChatClient chatClient = chatClientBuilder
					.defaultTools(
							todoWriteTool,
							ShellTools.builder().build()
					)
					.defaultAdvisors(MyLoggingAdvisor.builder().build())
					.build();

			String answer = chatClient.prompt()
					.system("Always use the available tools to assist the user in their requests.")
					.user("Create a file named hello_world.txt in a temporary directory and then remove it.")
					.call()
					.content();

			log.info("The Answer: {}", answer);
		};
	}

	public class TodoUpdateEvent extends ApplicationEvent {

		private final List<TodoWriteTool.Todos.TodoItem> todos;

		public TodoUpdateEvent(Object source, List<TodoWriteTool.Todos.TodoItem> todos) {
			super(source);
			this.todos = List.copyOf(todos);
		}

		public List<TodoWriteTool.Todos.TodoItem> getTodos() {
			return todos;
		}

	}

	@Component
	public class TodoProgressListener {

		@EventListener
		public void onTodoUpdate(TodoUpdateEvent event) {
			int completed = (int) event.getTodos().stream().filter(t -> t.status() == TodoWriteTool.Todos.Status.completed).count();
			int total = event.getTodos().size();

			System.out.printf("\nProgress: %d/%d tasks completed (%.0f%%)\n", completed, total,
					(completed * 100.0 / total));

			for (TodoWriteTool.Todos.TodoItem item : event.getTodos()) {
				String statusIcon = switch (item.status()) {
					case completed -> "[✓]";
					case in_progress -> "[→]";
					case pending -> "[ ]";
				};
				System.out.printf("  %s %s\n", statusIcon, item.content());
			}
		}

	}

}
