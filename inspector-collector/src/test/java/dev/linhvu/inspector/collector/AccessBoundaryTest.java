package dev.linhvu.inspector.collector;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccessBoundaryTest {
	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(InspectorProperties.class)
	static class BindingConfiguration { }

	private final ApplicationContextRunner context = new ApplicationContextRunner()
			.withUserConfiguration(BindingConfiguration.class);

	@Test
	void bindsDefaultsAndOverrides() {
		context.withPropertyValues("spring.ai.inspector.upstreams.openai=https://api.openai.com")
				.run(ctx -> {
					InspectorProperties p = ctx.getBean(InspectorProperties.class);
					assertEquals(512_000, p.maxBodyChars());
					assertEquals(268_435_456L, p.maxTotalBytes());
					assertEquals(16_777_216L, p.maxRequestBytes());
					assertEquals(268_435_456L, p.maxImportBytes());
					assertEquals(67_108_864L, p.maxBlobBytes());
					assertEquals(16_777_216, p.maxBlobSize());
					assertEquals("https://api.openai.com", p.upstreams().get("openai"));
					assertNull(p.allowedHosts());
					assertFalse(p.hasToken());
				});
		context.withPropertyValues("spring.ai.inspector.token=local-demo-token",
				"spring.ai.inspector.allowed-hosts[0]=inspector.local",
				"spring.ai.inspector.max-request-bytes=64").run(ctx -> {
			InspectorProperties p = ctx.getBean(InspectorProperties.class);
			assertTrue(p.hasToken());
			assertEquals(List.of("inspector.local"), p.allowedHosts());
			assertEquals(64, p.maxRequestBytes());
		});
	}

	private InspectorProperties properties(String token, List<String> hosts) {
		return new InspectorProperties(Map.of(), null, token, hosts,
				512_000, 268_435_456L, 64, 128, 256, 32);
	}

	private boolean passes(AccessFilter filter, String path, String host, String header,
			String query, int length, int status) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
		request.setServerName(host);
		if (header != null) request.addHeader("X-Inspector-Token", header);
		if (query != null) request.setParameter("token", query);
		if (length >= 0) request.setContent(new byte[length]);
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicBoolean called = new AtomicBoolean();
		FilterChain chain = (req, res) -> called.set(true);
		filter.doFilter(request, response, chain);
		assertEquals(status, response.getStatus());
		return called.get();
	}

	@Test
	void checksHostBeforeTokenAndAllowsConfiguredNames() throws Exception {
		AccessFilter local = new AccessFilter(properties("local-demo-token", null), "127.0.0.1");
		assertFalse(passes(local, "/api/runs", "evil.example", null, null, 0, 403));
		assertFalse(passes(local, "/api/runs", "localhost", null, null, 0, 401));
		assertTrue(passes(local, "/api/runs", "127.0.0.1", "local-demo-token", null, 0, 200));
		AccessFilter alias = new AccessFilter(properties(null, List.of("inspector.local")), "127.0.0.1");
		assertTrue(passes(alias, "/api/runs", "INSPECTOR.LOCAL", null, null, 0, 200));
		assertFalse(passes(alias, "/", "evil.example", null, null, 0, 403));
		AccessFilter exposed = new AccessFilter(properties(null, null), "0.0.0.0");
		assertTrue(passes(exposed, "/api/runs", "any.example", null, null, 0, 200));
		AccessFilter explicitEmpty = new AccessFilter(properties(null, List.of()), "0.0.0.0");
		assertFalse(passes(explicitEmpty, "/api/runs", "any.example", null, null, 0, 403));
	}

	@Test
	void acceptsQueryTokenButHeaderTakesPrecedence() throws Exception {
		AccessFilter filter = new AccessFilter(properties("local-demo-token", null), "127.0.0.1");
		assertTrue(passes(filter, "/api/runs", "localhost", null, "local-demo-token", 0, 200));
		assertFalse(passes(filter, "/api/runs", "localhost", "wrong", "local-demo-token", 0, 401));
		assertTrue(passes(filter, "/api/ping", "localhost", null, null, 0, 200));
		assertTrue(passes(filter, "/", "localhost", null, null, 0, 200));
	}

	@Test
	void appliesPathBudgetsOnlyToKnownContentLength() throws Exception {
		AccessFilter filter = new AccessFilter(properties(null, null), "127.0.0.1");
		assertTrue(passes(filter, "/api/events", "localhost", null, null, 64, 200));
		assertFalse(passes(filter, "/api/events", "localhost", null, null, 65, 413));
		assertTrue(passes(filter, "/api/import", "localhost", null, null, 128, 200));
		assertFalse(passes(filter, "/api/import", "localhost", null, null, 129, 413));
		assertFalse(passes(filter, "/api/blobs/media", "localhost", null, null, 33, 413));
		assertTrue(passes(filter, "/api/events", "localhost", null, null, -1, 200));
	}

	@Test
	void protectsExistingMvcEndpoints() throws Exception {
		var mvc = MockMvcBuilders.standaloneSetup(new EventController())
				.addFilters(new AccessFilter(properties("local-demo-token", null), "127.0.0.1"))
				.build();
		mvc.perform(get("/api/runs")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/runs").header("X-Inspector-Token", "local-demo-token"))
				.andExpect(status().isOk()).andExpect(content().json("[]"));
		mvc.perform(get("/api/ping")).andExpect(status().isNotFound());
	}
}
