package dev.linhvu.example.rag_reranking;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.ai.document.Document;

public class LuceneSearch implements AutoCloseable {

	private static final String ID_FIELD = "id";
	private static final String CONTENT_FIELD = "content";
	private static final String METADATA_FIELD = "metadata";

	private final Analyzer analyzer;
	private final Directory directory;
	private final DirectoryReader reader;
	private final IndexSearcher searcher;
	private final JsonMapper jsonMapper;

	public LuceneSearch(Path indexPath) throws IOException {
		this.analyzer = new StandardAnalyzer();
		this.directory = FSDirectory.open(indexPath);
		this.reader = DirectoryReader.open(directory);
		this.searcher = new IndexSearcher(reader);

		searcher.setSimilarity(new BM25Similarity());

		this.jsonMapper = new JsonMapper();
	}

	public List<Document> search(String queryText, int topK) {
		try {
			QueryParser parser = new QueryParser(CONTENT_FIELD, analyzer);
			Query query = parser.parse(
					QueryParser.escape(queryText));

			TopDocs hits = searcher.search(query, topK);
			StoredFields storedFields = searcher.storedFields();

			ArrayList<Document> results = new ArrayList<>();
			for (ScoreDoc hit : hits.scoreDocs) {
				org.apache.lucene.document.Document luceneDocument =
						storedFields.document(hit.doc);

				Map<String, Object> metadata = jsonMapper.readValue(
						luceneDocument.get(METADATA_FIELD),
						new TypeReference<>() {
						});

				results.add(new Document(
						luceneDocument.get(ID_FIELD),
						luceneDocument.get(CONTENT_FIELD),
						metadata));
			}

			return results;
		} catch (IOException | ParseException e) {
			throw new IllegalStateException("Failed to search Lucene index", e);
		}
	}

	@Override
	public void close() throws IOException {
		reader.close();
		directory.close();
		analyzer.close();
	}
}
