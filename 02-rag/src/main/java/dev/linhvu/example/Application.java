package dev.linhvu.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
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

	@Value("classpath:wikipedia-hurricane-milton-page.pdf")
	Resource hurricaneDocs;

	@Bean
	VectorStore vectorStore(EmbeddingModel embeddingModel) {
		return SimpleVectorStore.builder(embeddingModel).build();
	}

	@Bean
	ApplicationRunner runner(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
		return args -> {

			// Load the PDF document, split it into chunks, and add to the vector store
			// (Onetime, offline operation)
			vectorStore.add(
					TokenTextSplitter.builder().build().split(
							new PagePdfDocumentReader(hurricaneDocs).read()
					)
			);

			ChatClient chatClient = chatClientBuilder
					.defaultAdvisors(
							MyLoggingAdvisor.builder()
									.order(1) // after the QuestionAnswerAdvisor = 0
									.build())
					.build();

			String content = chatClient.prompt()
					.advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
					.user("Was Florida hit by the Hurricane Milton?")
					.call()
					.content();

			log.info(content);
		};
	}
}
