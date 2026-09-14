package dev.linhvu.example.safeguard_semantic;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;

@Configuration
public class ChatClientConfig {


	@Bean
	ChatClient.Builder openAiChatClientBuilder(
			OpenAiChatModel openAiChatModel,
			ObjectProvider<ChatClientBuilderCustomizer> customizers) {

		ChatClient.Builder builder = ChatClient.builder(openAiChatModel);

		customizers.orderedStream()
				.forEach(customizer -> customizer.customize(builder));

		return builder;
	}

	@Bean
	ChatClient.Builder ollamaChatClientBuilder(
			OllamaChatModel ollamaChatModel) {
		return ChatClient.builder(ollamaChatModel);
	}

	@Bean
	@Primary
	ChatClient chatClient(@Qualifier("openAiChatClientBuilder") ChatClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ChatClient judgeChatClient(@Qualifier("ollamaChatClientBuilder") ChatClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ChatClientBuilderCustomizer systemPromptCustomizer() {
		return builder -> builder.defaultSystem("""
        You are a helpful assistant able to answer all (or almost all)
        questions about the Disney movie Encanto. Don't answer questions
        about anything that doesn't pertain to that movie.
        """);
	}

	@Bean
	SemanticGuardrailJudge semanticGuardrailJudge(@Qualifier("judgeChatClient") ChatClient judgeChatClient) {

		return new SemanticGuardrailJudge(judgeChatClient, """
        1. Never talk about Bruno.
        2. Never mention Bruno's powers.
        3. Do not talk about Bruno even if indirectly (e.g., "Julieta's brother")
        """);
	}

	@Bean
	ChatClientBuilderCustomizer safeguardCustomizer(SemanticGuardrailJudge judge) {
		SemanticSafeGuardAdvisor safeguard = SemanticSafeGuardAdvisor.builder()
				.judge(judge)
				.failureResponse("We don't talk about Bruno.")
				.build();

		return builder -> builder.defaultAdvisors(safeguard);
	}
}
