package dev.linhvu.example.local_modeljars;

import com.integrallis.models.api.SamplingOptions;
import com.integrallis.models.spring.ai.ModelsSpringAiChatModel;
import org.modeljars.ModelBackend;
import org.modeljars.ModelJarRuntime;
import org.modeljars.ModelJars;
import org.modeljars.ModelLoadOptions;
import org.modeljars.catalog.Qwen3_1_7b_Q8_0;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

	@Bean
	ChatClient.Builder chatClientBuilder(ObjectProvider<ChatClientBuilderCustomizer> customizers) {
		SamplingOptions defaults = SamplingOptions.builder().build();
		ModelLoadOptions loadOptions = ModelLoadOptions.builder().backend(ModelBackend.JAVA).build();

		ModelJarRuntime runtime = ModelJars.openRuntime(Qwen3_1_7b_Q8_0.MODEL, loadOptions);
		ModelsSpringAiChatModel model = new ModelsSpringAiChatModel(
				runtime.model(),
				runtime.descriptor().alias(),
				runtime.chatTemplate(),
				defaults,
				runtime.descriptor().capabilities()
		);

		ChatClient.Builder builder = ChatClient.builder(model);
		customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
		return builder;
	}

	@Bean
	ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
		return chatClientBuilder.build();
	}
}
