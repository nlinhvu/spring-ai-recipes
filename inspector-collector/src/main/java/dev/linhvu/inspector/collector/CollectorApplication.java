package dev.linhvu.inspector.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(InspectorProperties.class)
public class CollectorApplication {
	public static void main(String[] args) {
		SpringApplication.run(CollectorApplication.class, args);
	}
}
