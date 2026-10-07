package dev.linhvu.example.authserver;

import org.springaicommunity.mcp.security.authorizationserver.config.McpAuthorizationServerConfigurer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) {
		return http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
				.with(McpAuthorizationServerConfigurer.mcpAuthorizationServer(), Customizer.withDefaults())
				.formLogin(Customizer.withDefaults())
				.build();
	}
}
