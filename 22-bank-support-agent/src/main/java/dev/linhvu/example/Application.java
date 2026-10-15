package dev.linhvu.example;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agent.tools.SkillsTool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.mcp.McpToolFilter;
import org.springframework.ai.session.DefaultSessionService;
import org.springframework.ai.session.InMemorySessionRepository;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionMemoryAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	private static final String INSTRUCTIONS = """
			You are a support agent in our bank, give the \
			customer support and judge the risk level of their query. \
			Reply using the customer's name.

			The customer's name is '{customerName}'. Account balances are in USD.

			For refund related questions, first load the 'refunds' skill and follow it.""";

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	SessionService sessionService() {
		return DefaultSessionService.builder().sessionRepository(InMemorySessionRepository.builder().build()).build();
	}

	/**
	 * Hides the exchange-rate server's dataframe analytics tools, which are irrelevant for a
	 * support agent, from the auto-configured MCP tool callback provider.
	 */
	@Bean
	McpToolFilter mcpToolFilter() {
		return (connection, tool) -> !tool.name().startsWith("fx_dataframe_");
	}

	@Bean
	public ApplicationRunner runner(ChatClient.Builder chatClientBuilder, SessionService sessionService, DatabaseConn db,
			@Value("${agent.skills.dirs}") List<Resource> skillsDirs, ToolCallbackProvider mcpToolCallbackProvider) {
		return args -> {

			// The typed, per-run dependencies (like Pydantic's deps=SupportDependencies(...))
			SupportDependencies deps = new SupportDependencies(123, db);

			// Dynamic instructions: look up the customer name from the database
			String customerName = db.customerName(deps.customerId()).orElse("unknown");

			ChatClient supportAgent = chatClientBuilder
					.defaultSystem(INSTRUCTIONS)
					.defaultTools(
							new SupportTools(),
							SkillsTool.builder().addSkillsResources(skillsDirs).build(),
							mcpToolCallbackProvider
					)
					.defaultToolContext(Map.of(SupportDependencies.TOOL_CONTEXT_KEY, deps))
					.defaultAdvisors(
							SessionMemoryAdvisor.builder(sessionService).defaultUserId("john").build(),
							MyLoggingAdvisor.builder().build()
					)
					.build();

			String sessionId = "support-session-customer-" + deps.customerId();
			for (String query : List.of(
					"What is my balance?",
					"I just lost my card!",
					"I was charged twice for the same coffee. Where is my refund?",
					"What is my balance in euros?")) {

				SupportOutput result = supportAgent.prompt()
						.system(s -> s.param("customerName", customerName))
						.user(query)
						.advisors(a -> a.param(SessionMemoryAdvisor.SESSION_ID_CONTEXT_KEY, sessionId))
						.call()
						.entity(SupportOutput.class);

				log.info("\nUSER: " + query + "\n" + result + "\n");
			}
		};
	}
}
