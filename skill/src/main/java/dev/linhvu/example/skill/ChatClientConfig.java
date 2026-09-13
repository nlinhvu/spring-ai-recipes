package dev.linhvu.example.skill;

import java.util.List;

import org.springaicommunity.agent.tools.SkillsTool;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ChatClientConfig {

	@Value("${agent.skills.path}")
	List<Resource> skillResources;

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ChatClientBuilderCustomizer skillsCustomizer() {
		ToolCallback skills = SkillsTool.builder().addSkillsResources(skillResources).build();
		return builder -> builder.defaultTools(skills);
	}

	@Bean
	ChatClientBuilderCustomizer weatherToolsCustomizer(WeatherTools tools) { // Skill will leverage this tool, that's why it's here
		return builder -> builder.defaultTools(tools);
	}

	@Bean
	ChatClientBuilderCustomizer forceUsingSkillCustomizer() {
		return builder ->
				builder.defaultSystem("""
						IMPORTANT: Always use the available skills to assist the user in their requests. When available follow skills instructions exactly.
						""");
	}

}
