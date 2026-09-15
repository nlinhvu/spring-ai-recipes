package dev.linhvu.example.rag_hybrid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

public class HybridDocumentRetriever implements DocumentRetriever {

	private static final Logger log = LoggerFactory.getLogger(HybridDocumentRetriever.class);

	private final VectorStore vectorStore;
	private final LuceneSearch luceneSearch;

	public HybridDocumentRetriever(VectorStore vectorStore, LuceneSearch search) {
		this.vectorStore = vectorStore;
		this.luceneSearch = search;
	}

	@Override
	public List<Document> retrieve(Query query) {
		List<Document> vectorResults = vectorStore.similaritySearch(
				SearchRequest.builder()
						.query(query.text())
						.topK(10)
						.build());

		List<Document> bm25Results = luceneSearch.search(query.text(), 10);

		List<Document> fusedResults = reciprocalRankFusion(vectorResults, bm25Results, 10);

		logResults("VECTOR", vectorResults);
		logResults("BM25", bm25Results);
		logResults("FUSED", fusedResults);

		return fusedResults;
	}

	private List<Document> reciprocalRankFusion(
			List<Document> vectorResults,
			List<Document> bm25Results,
			int limit) {

		Map<String, Double> scores = new HashMap<>();
		Map<String, Document> documents = new HashMap<>();

		addRrfScores(vectorResults, scores, documents);
		addRrfScores(bm25Results, scores, documents);

		return scores.entrySet().stream()
				.sorted(Map.Entry.<String, Double>comparingByValue().reversed())
				.limit(limit)
				.map(entry -> documents.get(entry.getKey()))
				.toList();
	}

	private void addRrfScores(
			List<Document> results,
			Map<String, Double> scores,
			Map<String, Document> documents) {

		final int k = 60;

		for (int i = 0; i < results.size(); i++) {
			Document document = results.get(i);
			int rank = i + 1;

			documents.putIfAbsent(document.getId(), document);

			scores.merge(
					document.getId(),
					1.0 / (k + rank),
					Double::sum);
		}
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
}
