package dev.linhvu.example.rag_hyde;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

@Configuration
public class ChatClientConfig {

	@Bean
	@Primary
	ChatClient.Builder openAiChatClientBuilder(
			OpenAiChatModel openAiChatModel,
			ObjectProvider<ChatClientBuilderCustomizer> customizers) {

		ChatClient.Builder builder = ChatClient.builder(openAiChatModel);
		customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
		return builder;
	}

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ChatClient.Builder compressionChatClientBuilder(OpenAiChatModel openAiChatModel) {
		return ChatClient.builder(openAiChatModel)
				.defaultOptions(
						OpenAiChatOptions.builder().model("gemini-3.1-flash-lite")
				);
	}

	@Bean
	@Order(1)
	QueryTransformer queryTransformer(
			@Qualifier("compressionChatClientBuilder") ChatClient.Builder chatClientBuilder) {
		return CompressionQueryTransformer.builder()
				.chatClientBuilder(chatClientBuilder)
				.build();
	}

	@Bean
	@Order(2)
	QueryTransformer hydeQueryTransformer(
			@Qualifier("compressionChatClientBuilder") ChatClient.Builder chatClientBuilder) {
		return HydeQueryTransformer.builder()
				.chatClientBuilder(chatClientBuilder)
				.build();
	}

	@Bean
	ChatClientBuilderCustomizer ragAdvisorCustomizer(QueryTransformer[] queryTransformers, VectorStore vectorStore) {
		VectorStoreDocumentRetriever documentRetriever =
				VectorStoreDocumentRetriever.builder()
						.vectorStore(vectorStore)
						.build();

		RetrievalAugmentationAdvisor ragAdvisor =
				RetrievalAugmentationAdvisor.builder()
						.queryTransformers(queryTransformers)
						.documentRetriever(documentRetriever)
						.build();

		return builder -> builder.defaultAdvisors(ragAdvisor);
	}
}
