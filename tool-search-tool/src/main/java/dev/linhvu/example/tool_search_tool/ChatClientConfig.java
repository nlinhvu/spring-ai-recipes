package dev.linhvu.example.tool_search_tool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.toolsearch.ToolSearchToolCallingAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.tool.toolsearch.ToolIndex;
import org.springframework.ai.tool.toolsearch.index.lucene.LuceneToolIndex;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ChatClientBuilderCustomizer weatherToolsCustomizer(Toolbox tools) {
		return builder -> builder.defaultTools(tools);
	}

	// ToolSearchTool requires
	@Bean
	ChatClientBuilderCustomizer chatMemoryCustomizer() {
		return builder ->
			builder.defaultAdvisors(
					MessageChatMemoryAdvisor.builder(
									MessageWindowChatMemory.builder()
											.maxMessages(500)
											.build())
							.build());
	}

	@Bean
	LuceneToolIndex luceneToolIndex() {
		return new LuceneToolIndex(0.4f);
	}

	@Bean
	ChatClientBuilderCustomizer toolSearchToolsCustomizer(ToolIndex toolIndex) {
		ToolSearchToolCallingAdvisor toolSearchAdvisor =
				ToolSearchToolCallingAdvisor.builder()
						.toolIndex(toolIndex)
						.maxResults(5)
						.build();

		return builder -> builder.defaultAdvisors(toolSearchAdvisor);
	}

}
