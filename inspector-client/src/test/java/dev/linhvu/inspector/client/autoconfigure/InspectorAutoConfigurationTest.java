package dev.linhvu.inspector.client.autoconfigure;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.model.chat.client.autoconfigure.ChatClientAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.junit.jupiter.api.Assertions.*;

class InspectorAutoConfigurationTest {
	private final ApplicationContextRunner runner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(InspectorAutoConfiguration.class, ChatClientAutoConfiguration.class))
			.withBean(ChatModel.class, () -> prompt -> new ChatResponse(
					List.of(new Generation(new AssistantMessage("fixture answer")))));

	@Test
	void inactiveByDefaultAndWhenFalse() {
		runner.run(context -> {
			assertFalse(context.containsBean("inspectorClient"));
			assertEquals("fixture answer", context.getBean(ChatClient.Builder.class).build().prompt("test").call().content());
		});
		runner.withPropertyValues("spring.ai.inspector.active=false").run(context -> {
			assertFalse(context.containsBean("inspectorClient"));
			assertTrue(context.getBeansOfType(ChatClientBuilderCustomizer.class).isEmpty());
		});
	}

	@Test
	void classpathGuardBacksOffWhenChatClientIsMissing() {
		new ApplicationContextRunner().withPropertyValues("spring.ai.inspector.active=true")
				.withClassLoader(new FilteredClassLoader(ChatClient.class))
				.withConfiguration(AutoConfigurations.of(InspectorAutoConfiguration.class)).run(context -> {
					assertNull(context.getStartupFailure());
					assertFalse(context.containsBean("inspectorClient"));
				});
	}

	@Test
	void invalidUrlFailsConfigurationRatherThanSilentlyDisablingCapture() {
		runner.withPropertyValues("spring.ai.inspector.active=true", "spring.ai.inspector.url=https://example.com")
				.run(context -> assertNotNull(context.getStartupFailure()));
	}

	@Test
	void importMetadataContainsThisConfiguration() {
		List<String> imports = ImportCandidates.load(AutoConfiguration.class, getClass().getClassLoader()).getCandidates();
		assertTrue(imports.contains(InspectorAutoConfiguration.class.getName()));
	}
}