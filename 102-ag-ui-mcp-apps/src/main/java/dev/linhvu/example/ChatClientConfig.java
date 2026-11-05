package dev.linhvu.example;

import java.util.List;

import com.agui.community.core.agent.Agent;
import com.agui.community.spring.ai.SpringAiAgent;
import io.modelcontextprotocol.client.McpSyncClient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.McpToolNamePrefixGenerator;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

	@Bean
	Agent mcpAppAgent(ChatClient.Builder builder, List<McpSyncClient> mcpClients) {
		SyncMcpToolCallbackProvider mcpTools =
				SyncMcpToolCallbackProvider.builder()
						.mcpClients(mcpClients)
						.toolNamePrefixGenerator(McpToolNamePrefixGenerator.noPrefix())
						.build();

		SpringAiAgent springAiAgent =
				SpringAiAgent.builder(builder.build())
						.tools(List.of(mcpTools.getToolCallbacks()))
						.build();

		return new McpAppsAgent(springAiAgent, mcpClients);
	}

}
