package dev.linhvu.inspector.client.autoconfigure;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import dev.linhvu.inspector.Event;
import dev.linhvu.inspector.client.InspectorClient;
import dev.linhvu.inspector.client.demo.BootWiringDemo;
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
					List.of(new Generation(new AssistantMessage("fixture answer")))
			));

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
	void customizesEachPrototypeBuilderButNotRawStaticBuilder() {
		runner.withPropertyValues("spring.ai.inspector.active=true").run(context -> {
			InspectorClient sender = context.getBean(InspectorClient.class);
			assertEquals(1, context.getBeansOfType(ChatClientBuilderCustomizer.class).size());
			ChatClient.Builder first = context.getBean(ChatClient.Builder.class);
			ChatClient.Builder second = context.getBean(ChatClient.Builder.class);
			assertNotSame(first, second);
			// Closing first makes advisor emissions observable without network traffic.
			sender.close();
			assertEquals("fixture answer", first.build().prompt("first").call().content());
			assertEquals("fixture answer", second.build().prompt("second").call().content());
			assertTrue(sender.status().contains("skipped=4"), sender.status());
			ChatClient raw = ChatClient.builder(context.getBean(ChatModel.class)).build();
			assertEquals("fixture answer", raw.prompt("raw").call().content());
			assertTrue(sender.status().contains("skipped=4"), sender.status());
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
	void contextCloseClosesOwnedSender() {
		AtomicReference<InspectorClient> reference = new AtomicReference<>();
		runner.withPropertyValues("spring.ai.inspector.active=true").run(context -> reference.set(context.getBean(InspectorClient.class)));
		InspectorClient sender = reference.get();
		sender.emit("after-close", Event.Kind.START, "test after close");
		assertEquals("pending=0, skipped=1, failed=0", sender.status());
	}

	@Test
	void invalidUrlFailsConfigurationRatherThanSilentlyDisablingCapture() {
		runner.withPropertyValues("spring.ai.inspector.active=true", "spring.ai.inspector.url=https://example.com")
				.run(context -> assertNotNull(context.getStartupFailure()));
	}

	@Test
	void enableAutoConfigurationDiscoversAndAppliesInspectorWithoutExplicitImport() {
		new ApplicationContextRunner()
				.withUserConfiguration(BootWiringDemo.class)
				.withPropertyValues("spring.ai.inspector.active=true").run(context -> {
					assertNull(context.getStartupFailure());
					InspectorClient sender = context.getBean(InspectorClient.class);
					sender.close();
					assertEquals("fixture answer", context.getBean(ChatClient.Builder.class)
							.build().prompt("discovery").call().content());
					assertEquals("pending=0, skipped=2, failed=0", sender.status());
				});
	}

	@Test
	void importMetadataContainsThisConfiguration() {
		List<String> imports = ImportCandidates.load(AutoConfiguration.class, getClass().getClassLoader()).getCandidates();
		assertTrue(imports.contains(InspectorAutoConfiguration.class.getName()));
	}
}