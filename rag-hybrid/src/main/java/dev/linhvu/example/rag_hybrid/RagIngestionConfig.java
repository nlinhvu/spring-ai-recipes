package dev.linhvu.example.rag_hybrid;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.document.Document;
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
	LuceneDocumentWriter luceneDocumentWriter(
			@Value("${lucene.index.path}") Path luceneIndexPath) throws IOException {
		return new LuceneDocumentWriter(luceneIndexPath);
	}

	@Bean
	ApplicationRunner load(VectorStore vectorStore, LuceneDocumentWriter luceneDocumentWriter) {
		return args -> {
			for(Resource documentResource : documentResources) {
				String filename = documentResource.getFilename();
				log.info("Loading document from {}.", filename);

				TikaDocumentReader reader = new TikaDocumentReader(documentResource);
				TokenTextSplitter splitter = TokenTextSplitter.builder().build();

				String titleTag = filename.substring(0, filename.lastIndexOf('.'));

				List<Document> chunks = splitter.apply(
						reader.get().stream()
								.peek(document ->
										document.getMetadata().put("title", titleTag)
								)
								.toList()
				);

				vectorStore.accept(chunks);
				luceneDocumentWriter.add(chunks);
			}

			log.info("Document loading complete.");
		};
	}
}
