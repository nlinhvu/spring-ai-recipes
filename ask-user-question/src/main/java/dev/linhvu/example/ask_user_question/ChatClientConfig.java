package dev.linhvu.example.ask_user_question;

import org.springaicommunity.agent.tools.AskUserQuestionTool;
import org.springaicommunity.agent.utils.CommandLineQuestionHandler;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ChatClientBuilderCustomizer askUserQuestionCustomizer() {
		AskUserQuestionTool askUserQuestionTool = AskUserQuestionTool.builder()
				// CommandLineQuestionHandler:
				// * display questions via standard output
				// * accept responses via standard input
				.questionHandler(new CommandLineQuestionHandler())
				.build();

		return builder -> builder.defaultTools(askUserQuestionTool);
	}
}
