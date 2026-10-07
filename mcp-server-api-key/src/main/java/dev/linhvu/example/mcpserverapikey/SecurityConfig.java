package dev.linhvu.example.mcpserverapikey;

import java.util.List;

import org.springaicommunity.mcp.security.server.apikey.ApiKeyEntityRepository;
import org.springaicommunity.mcp.security.server.apikey.memory.ApiKeyEntityImpl;
import org.springaicommunity.mcp.security.server.apikey.memory.InMemoryApiKeyEntityRepository;
import org.springaicommunity.mcp.security.server.config.McpApiKeyConfigurer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) {
		return http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
				.with(
						McpApiKeyConfigurer.mcpServerApiKey().headerName("X-MCP-API-KEY"), // Default "X-API-KEY"
						apiKey -> apiKey.apiKeyRepository(apiKeyRepository()))
				.build();
	}

	private ApiKeyEntityRepository<ApiKeyEntityImpl> apiKeyRepository() {
		ApiKeyEntityImpl apiKey = ApiKeyEntityImpl.builder().name("DemoApiKey").id("ApiKeyId").secret("Secret").build();
		return new InMemoryApiKeyEntityRepository<>(List.of(apiKey));
	}
}
