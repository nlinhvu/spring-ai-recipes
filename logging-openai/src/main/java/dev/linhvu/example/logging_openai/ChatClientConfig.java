package dev.linhvu.example.logging_openai;

import org.zalando.logbook.Logbook;
import org.zalando.logbook.okhttp.LogbookInterceptor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.http.okhttp.OpenAiHttpClientBuilderCustomizer;
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
	OpenAiHttpClientBuilderCustomizer logbookCustomizer(Logbook logbook) {
		return builder -> builder.interceptor(new LogbookInterceptor(logbook)); // OkHttp Interceptor
	}

	// In the past, Spring OpenAI use RestClient instead of OpenAiSdk.
	//	@Bean
//	RestClientCustomizer logbookCustomizer(
//			LogbookClientHttpRequestInterceptor interceptor) {
//		return restClient -> restClient.requestInterceptor(interceptor); // RestClient Interceptor
//	}

}
