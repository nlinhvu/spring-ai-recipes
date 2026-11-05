package dev.linhvu.example.tool_use;

import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	private static final String PROMPT = "Why is the sky blue?";
	private static final int WARMUP_RUNS = 20;
	private static final int MEASURED_RUNS = 10;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	ApplicationRunner go(EmbeddingModel embeddingModel,
			@Value("${spring.profiles.active}") String activeProfile) {
		return args -> {
			LOGGER.info(" === {} ===", activeProfile.toUpperCase());

			// Warm up the model
			IntStream.range(0, WARMUP_RUNS).forEach(i ->
					LOGGER.info("WARMUP TIME: {} ms", format(time(() -> embeddingModel.embed(PROMPT)))));

			// Measure subsequent embeddings
			double average = IntStream.range(0, MEASURED_RUNS)
					.mapToDouble(i -> {
						double elapsed = time(() -> embeddingModel.embed(PROMPT));
						LOGGER.info("RUN {}: {} ms", i + 1, format(elapsed));
						return elapsed;
					})
					.average()
					.orElse(Double.NaN);

			LOGGER.info("AVERAGE TIME: {} ms", format(average));
		};
	}

	/** Returns the elapsed time of the action in milliseconds. */
	private static double time(Runnable action) {
		long start = System.nanoTime();
		action.run();
		return (System.nanoTime() - start) / 1_000_000.0;
	}

	private static String format(double millis) {
		return String.format("%.3f", millis);
	}
}
