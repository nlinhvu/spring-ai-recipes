package dev.linhvu.inspector.client.autoconfigure;

import java.net.URI;
import java.util.Map;

import dev.linhvu.inspector.client.InspectorAdvisor;
import dev.linhvu.inspector.client.InspectorClient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
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
		return new InspectorClient(URI.create(environment.getProperty(
				"spring.ai.inspector.url", "http://127.0.0.1:9002")),
				environment.getProperty("spring.ai.inspector.run-id"),
				environment.getProperty("spring.ai.inspector.token"));
	}
	@Bean
	ChatClientBuilderCustomizer inspectorChatClientCustomizer(InspectorClient client) {
		return builder -> builder.defaultAdvisors(new InspectorAdvisor(client));
	}
	@Bean
	RunLifecycle inspectorRunLifecycle(InspectorClient client, Environment environment) {
		return new RunLifecycle(client, environment);
	}
	static class RunLifecycle implements InitializingBean, DisposableBean {
		private final InspectorClient client;
		private final Environment environment;
		RunLifecycle(InspectorClient client, Environment environment) {
			this.client = client; this.environment = environment;
		}
		@Override public void afterPropertiesSet() {
			client.announce("run-start", () -> Map.of(
					"app", environment.getProperty("spring.ai.inspector.app",
							environment.getProperty("spring.application.name", "recipe-demo")),
					"pid", ProcessHandle.current().pid(), "upstreams", Map.of(), "routed", java.util.List.of()));
		}
		@Override public void destroy() { client.send("run-end", Map.of()); }
	}
}
