package dev.linhvu.example.rag_conversation_aware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagIngestionConfig {

	private static final Logger log = LoggerFactory.getLogger(RagIngestionConfig.class);

	@Value("${rag.document.url}")
	String documentUrl;

	@Bean
	ApplicationRunner load(VectorStore vectorStore) {
		return args -> {
			log.info("Loading document from {}.", documentUrl);

			TikaDocumentReader reader = new TikaDocumentReader(documentUrl);
			TokenTextSplitter splitter = TokenTextSplitter.builder().build();

			vectorStore.accept(splitter.apply(reader.get())); // pipeline, pdf -> reader -> processor -> writer -> vectorStore

			log.info("Document loading complete.");
		};
	}
}
