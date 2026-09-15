package dev.linhvu.example.rag_reranking;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;

public class RerankingDocumentPostProcessor implements DocumentPostProcessor {

	private static final Logger log = LoggerFactory.getLogger(RerankingDocumentPostProcessor.class);

	private final ChatClient chatClient;
	private final int topK;

	public RerankingDocumentPostProcessor(ChatClient.Builder chatClientBuilder, int topK) {
		this.chatClient = chatClientBuilder.build();
		this.topK = topK;
	}

	@Override
	public List<Document> process(Query query, List<Document> documents) {
		Ranking ranking = chatClient.prompt()
				.user(rankingPrompt(query, documents))
				.call()
				.entity(Ranking.class);

		logResults("PRE-RERANKING", documents);

		List<Document> rankedDocuments = ranking.documentIndexes().stream()
				.limit(topK)
				.map(documents::get)
				.toList();

		logResults("RERANKED",  rankedDocuments);

		return rankedDocuments;
	}

	private String rankingPrompt(
			Query query, List<Document> documents) {

		StringBuilder prompt = new StringBuilder("""
            Rank the following documents by their relevance to the query.
            Assign each document a relevance score from 0.0 to 1.0.

            Query:
            %s

            Documents:
            """.formatted(query.text()));

		for (int i = 0; i < documents.size(); i++) {
			prompt.append("\n[%d]\n%s\n".formatted(
					i, documents.get(i).getText()));
		}

		return prompt.toString();
	}

	private void logResults(String label, List<Document> documents) {
		log.debug("{}", label);

		for (int i = 0; i < documents.size(); i++) {
			Document document = documents.get(i);

			String text = document.getText()
					.replaceAll("\\s+", " ")
					.trim();

			String snippet = text.substring(
					0, Math.min(text.length(), 80));

			log.debug(
					"{}. {} - {}",
					i + 1,
					document.getId(),
					snippet);
		}
	}

	record Ranking(List<Integer> documentIndexes) {}
}
