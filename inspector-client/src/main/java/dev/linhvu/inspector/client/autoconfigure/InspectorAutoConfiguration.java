package dev.linhvu.inspector.client.autoconfigure;

import java.net.URI;

import dev.linhvu.inspector.client.InspectorAdvisor;
import dev.linhvu.inspector.client.InspectorClient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnClass(ChatClient.class)
@ConditionalOnProperty(name = "spring.ai.inspector.active", havingValue = "true")
public class InspectorAutoConfiguration {

	@Bean(destroyMethod = "close")
	InspectorClient inspectorClient(Environment environment) {
		return new InspectorClient(
				URI.create(environment.getProperty("spring.ai.inspector.url", "http://127.0.0.1:9002"))
		);
	}

	@Bean
	ChatClientBuilderCustomizer inspectorClientBuilderCustomizer(InspectorClient inspectorClient) {
		return builder -> builder.defaultAdvisors(new InspectorAdvisor(inspectorClient));
	}


}
