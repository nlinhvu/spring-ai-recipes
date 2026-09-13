package dev.linhvu.example.logging_googleai;

import com.google.genai.Client;
import com.google.genai.types.ClientOptions;
import okhttp3.OkHttpClient;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.okhttp.LogbookInterceptor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.model.google.genai.autoconfigure.chat.GoogleGenAiConnectionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}

	@Bean
	ChatClientBuilderCustomizer loggingCustomizer() {
		return builder -> builder.defaultAdvisors(
				SimpleLoggerAdvisor.builder().build()
		);
	}

	@Bean
	Client googleGenAiClient(GoogleGenAiConnectionProperties properties, Logbook logbook) {
		// ApiClient#createHttpClient:274
		OkHttpClient httpClient = new OkHttpClient.Builder()
				.addInterceptor(new LogbookInterceptor(logbook))
				.build();

		return Client.builder()
				.apiKey(properties.getApiKey())
				.clientOptions(ClientOptions.builder()
						.customHttpClient(httpClient)
						.build())
				.build();
	}
}
