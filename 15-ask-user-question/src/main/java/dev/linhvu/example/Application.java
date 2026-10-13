package dev.linhvu.example;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agent.tools.AskUserQuestionTool;
import org.springaicommunity.agent.utils.CommandLineQuestionHandler;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
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
	public ApplicationRunner runner(ChatClient.Builder chatClientBuilder) {
		return args -> {

			AskUserQuestionTool askUserQuestionTool = AskUserQuestionTool.builder()
					.questionHandler(new CommandLineQuestionHandler())
					.answersValidation(false)
					.build();

			ChatClient chatClient = chatClientBuilder
					.defaultTools(askUserQuestionTool)
					.defaultAdvisors(MyLoggingAdvisor.builder().build())
					.build();


			String answer = chatClient.prompt("I want to build a website, ask me 3 questions to verify my requirements.").call().content();

			log.info("Answer: {}", answer);
		};
	}
}
