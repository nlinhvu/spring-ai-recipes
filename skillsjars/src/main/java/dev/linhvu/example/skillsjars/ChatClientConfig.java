package dev.linhvu.example.skillsjars;

import java.util.List;

import org.springaicommunity.agent.tools.FileSystemTools;
import org.springaicommunity.agent.tools.ShellTools;
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

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}

	@Value("${agent.skills.paths}")
	List<Resource> skillPaths;

	@Bean
	ChatClientBuilderCustomizer skillsToolCustomizer() {
		ToolCallback skills = SkillsTool.builder().addSkillsResources(skillPaths).build();
		return builder -> builder.defaultTools(skills);
	}

	@Bean
	ChatClientBuilderCustomizer shellAndFilesystemToolsCustomizer(){ // PDF skill requires
		ShellTools shellTools = ShellTools.builder().build();
		FileSystemTools fileSystemTools = FileSystemTools.builder().build();

		return builder -> builder.defaultTools(shellTools, fileSystemTools);
	}
}
