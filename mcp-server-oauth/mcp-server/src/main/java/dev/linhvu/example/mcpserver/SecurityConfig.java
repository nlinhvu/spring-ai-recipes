package dev.linhvu.example.mcpserver;

import org.springaicommunity.mcp.security.server.config.McpServerOAuth2Configurer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
	private String issuerUrl;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) {
		return http.authorizeHttpRequests(auth ->
						auth.requestMatchers("/mcp").permitAll()
								.anyRequest().authenticated())
				.with(
						McpServerOAuth2Configurer.mcpServerOAuth2(),
						mcpAuthorization -> mcpAuthorization.authorizationServer(issuerUrl))
				.build();
	}

}
