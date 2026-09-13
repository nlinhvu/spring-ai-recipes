package dev.linhvu.example.rag_tool;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RagTools {

	private static final Logger log = LoggerFactory.getLogger(RagTools.class);

	private final VectorStore vectorStore;

	public RagTools(VectorStore vectorStore) {
		this.vectorStore = vectorStore;
	}

	@Tool(name = "sagrada-helper",
			description = "Returns the rules of the game Sagrada for a given query.")
	public List<Document> findBoardGameRules(@ToolParam(description = "The original user query") String query) {
		log.info("Querying board game rules: {}", query);
		return this.vectorStore.similaritySearch(query);
	}

}
