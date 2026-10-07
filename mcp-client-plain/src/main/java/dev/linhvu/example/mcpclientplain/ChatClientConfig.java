package dev.linhvu.example.mcpclientplain;

import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ChatClientBuilderCustomizer addMcpTools(ToolCallbackProvider mcpTools) {
		return builder -> builder.defaultTools(mcpTools);
	}

//	@Bean
//	McpSyncHttpClientRequestCustomizer addMcpHeaders() {
//		return (builder, method, endpoint, body, context) ->  {
//			builder.header("X-MCP-API-KEY", "ApiKeyId.Secret"); // This is for mcp-server-api-key, they apiKey is <id>.<secret>
//		};
//	}
}
