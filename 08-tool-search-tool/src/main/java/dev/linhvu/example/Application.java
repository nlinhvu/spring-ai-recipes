package dev.linhvu.example;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.toolsearch.ToolSearchToolCallingAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.toolsearch.ToolIndex;
import org.springframework.ai.tool.toolsearch.index.lucene.LuceneToolIndex;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	VectorStore vectorStore(EmbeddingModel embeddingModel) {
		return SimpleVectorStore.builder(embeddingModel).build();
	}

//	// This needs Embedded Models
//	@Bean
//	ToolIndex vectorToolSearcher(VectorStore vectorStore) {
//		return new VectorToolIndex(vectorStore);
//	}

	// Don't need Embedded Models
	@Bean
	LuceneToolIndex luceneToolIndex() {
		return new LuceneToolIndex(0.4f);
	}

	@Bean
	public ApplicationRunner runner(ChatClient.Builder chatClientBuilder, ToolIndex toolIndex) {
		return args -> {

			MyLoggingAdvisor loggingAdvisor = MyLoggingAdvisor.builder()
					.order(Ordered.HIGHEST_PRECEDENCE + 2000)
					.showAvailableTools(true)
					.build();


			ToolSearchToolCallingAdvisor searchToolCallingAdvisor = ToolSearchToolCallingAdvisor.builder()
					.toolIndex(toolIndex)
					.referenceToolNameAccumulation(false)
//					.maxResults(5)
					.build();


			ChatClient chatClient = chatClientBuilder
					.defaultTools(new MyTools(), new DummyTools())
					.defaultAdvisors(loggingAdvisor, searchToolCallingAdvisor)
					.defaultAdvisors(a -> a.param(ChatMemory.CONVERSATION_ID, "abcdef")) // required by searchToolCallingAdvisor
					.build();

			String answer = chatClient.prompt("""
							Help me plan what to wear today in Landsmeer, NL.
							Please suggest clothing shops that are open right now in the area.
							
							Do not make assumptions about the date, time. Use the tools for getting the current time.
							""")
					.call().content();

			log.info(answer);

		};
	}

	static class MyTools {

		@Tool(description = "Get the weather for a given location and at a given time")
		public String weather(String location, @ToolParam(description = "YYYY-MM-DDTHH:mm:ss") String atTime) {
			return "The current weather in " + location + " is sunny with a temperature of 25°C.";
		}

		@Tool(description = "Get of clothing shops names for a given location and at a given time")
		public List<String> clothing(String location,
				@ToolParam(description = "YYYY-MM-DDTHH:mm:ss") String openAtTime) {
			return List.of("Foo", "Bar", "Baz");
		}

		@Tool(description = "Provides the current date and time (as date-time string) for a given location")
		public String currentTime(String location) {
			return LocalDateTime.now().toString();
		}

	}
}
