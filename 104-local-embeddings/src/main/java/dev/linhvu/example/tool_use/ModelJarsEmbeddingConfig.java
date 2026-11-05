package dev.linhvu.example.tool_use;

import com.integrallis.models.spring.ai.ModelsSpringAiEmbeddingModel;
import io.micrometer.observation.ObservationRegistry;
import org.modeljars.ModelJars;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("modeljars")
public class ModelJarsEmbeddingConfig {

	@Bean
	EmbeddingModel embeddingModel() {
		return new ModelsSpringAiEmbeddingModel(
				ModelJars.openEmbedding(
						"org.modeljars.huggingface:" +
								"second-state.all-minilm-l6-v2-embedding-gguf.q4_k_m:" +
								"2.0.0-q4_k_m.2"
				),
				"all-minilm-l6-v2",
				ObservationRegistry.NOOP
		);
	}
}
