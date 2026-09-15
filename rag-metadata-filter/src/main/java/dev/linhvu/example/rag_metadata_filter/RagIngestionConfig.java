package dev.linhvu.example.rag_metadata_filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class RagIngestionConfig {

	private static final Logger log = LoggerFactory.getLogger(RagIngestionConfig.class);

	@Value("${rag.documents}")
	Resource[] documentResources;

	@Bean
	ApplicationRunner load(VectorStore vectorStore) {
		return args -> {
			for(Resource documentResource : documentResources) {
				String filename = documentResource.getFilename();
				log.info("Loading document from {}.", filename);

				TikaDocumentReader reader = new TikaDocumentReader(documentResource);
				TokenTextSplitter splitter = TokenTextSplitter.builder().build();

				String titleTag = filename.substring(0, filename.lastIndexOf('.'));

				vectorStore.accept(
						splitter.apply(
								reader.get().stream()
										.peek(document ->
												document.getMetadata().put("title", titleTag)
										)
										.toList()
						));
			}

			log.info("Document loading complete.");
		};
	}
}
