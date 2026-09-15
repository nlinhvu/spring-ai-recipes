package dev.linhvu.example.rag_hyde;

import org.zalando.logbook.Logbook;
import org.zalando.logbook.okhttp.LogbookInterceptor;

import org.springframework.ai.chat.client.ChatClientBuilderCustomizer;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.http.okhttp.OpenAiHttpClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientLoggingConfig {

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
}
