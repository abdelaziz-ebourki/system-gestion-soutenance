package com.system_gestion_soutenance.api.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

	private static final int MAX_REQUESTS = 60;

	private RateLimitFilter filter(boolean trustProxyHeaders, boolean generalEnabled) {
		return new RateLimitFilter(new ObjectMapper(), trustProxyHeaders, MAX_REQUESTS, generalEnabled);
	}

	private MockHttpServletRequest request(String uri, String remoteAddr) {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
		request.setRemoteAddr(remoteAddr);
		return request;
	}

	private void passThrough(RateLimitFilter filter, MockHttpServletRequest request) throws Exception {
		FilterChain chain = mock(FilterChain.class);
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request, response, chain);
		verify(chain, times(1)).doFilter(request, response);
		assertEquals(200, response.getStatus());
	}

	@Test
	void nonApiPath_alwaysPassesThrough() throws Exception {
		RateLimitFilter filter = filter(false, true);
		for (int i = 0; i < MAX_REQUESTS + 10; i++) {
			passThrough(filter, request("/actuator/health", "10.0.0.1"));
		}
	}

	@Test
	void nonLimitedEndpoint_withGeneralDisabled_passesThrough() throws Exception {
		RateLimitFilter filter = filter(false, false);
		for (int i = 0; i < MAX_REQUESTS + 10; i++) {
			passThrough(filter, request("/api/admin/users", "10.0.0.2"));
		}
	}

	@Test
	void authEndpoint_overLimit_returns429() throws Exception {
		RateLimitFilter filter = filter(false, false);
		int effectiveMax = Math.min(MAX_REQUESTS, 30);
		for (int i = 0; i < effectiveMax; i++) {
			passThrough(filter, request("/api/auth/login", "10.0.0.3"));
		}

		FilterChain chain = mock(FilterChain.class);
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request("/api/auth/login", "10.0.0.3"), response, chain);

		assertEquals(429, response.getStatus());
		assertTrue(response.getContentAsString().contains("Trop de requ"));
		verify(chain, times(0)).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
	}

	@Test
	void bulkEndpoint_overLimit_returns429() throws Exception {
		RateLimitFilter filter = filter(false, false);
		int effectiveMax = Math.min(MAX_REQUESTS, 30);
		for (int i = 0; i < effectiveMax; i++) {
			passThrough(filter, request("/api/coordinator/schedules", "10.0.0.4"));
		}

		FilterChain chain = mock(FilterChain.class);
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request("/api/coordinator/schedules", "10.0.0.4"), response, chain);

		assertEquals(429, response.getStatus());
	}

	@Test
	void generalEnabled_limitsAnyApiEndpoint() throws Exception {
		RateLimitFilter filter = filter(false, true);
		for (int i = 0; i < MAX_REQUESTS; i++) {
			passThrough(filter, request("/api/admin/users", "10.0.0.5"));
		}

		FilterChain chain = mock(FilterChain.class);
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request("/api/admin/users", "10.0.0.5"), response, chain);

		assertEquals(429, response.getStatus());
	}

	@Test
	void trustedProxyHeader_countsByForwardedIp() throws Exception {
		RateLimitFilter filter = filter(true, false);
		int effectiveMax = Math.min(MAX_REQUESTS, 30);
		for (int i = 0; i < effectiveMax; i++) {
			MockHttpServletRequest req = request("/api/auth/login", "10.9.9." + (i % 250 + 1));
			req.addHeader("X-Forwarded-For", "203.0.113.7, 70.41.3.18");
			passThrough(filter, req);
		}

		FilterChain chain = mock(FilterChain.class);
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest blocked = request("/api/auth/login", "10.9.9.200");
		blocked.addHeader("X-Forwarded-For", "203.0.113.7");
		filter.doFilter(blocked, response, chain);

		assertEquals(429, response.getStatus());
	}

	@Test
	void untrustedProxyHeader_countsByRemoteAddr() throws Exception {
		RateLimitFilter filter = filter(false, false);
		int effectiveMax = Math.min(MAX_REQUESTS, 30);
		for (int i = 0; i < effectiveMax; i++) {
			MockHttpServletRequest req = request("/api/auth/login", "10.0.0.6");
			req.addHeader("X-Forwarded-For", "203.0.113.99");
			passThrough(filter, req);
		}

		FilterChain chain = mock(FilterChain.class);
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest otherSpoof = request("/api/auth/login", "10.0.0.7");
		otherSpoof.addHeader("X-Forwarded-For", "203.0.113.99");
		filter.doFilter(otherSpoof, response, chain);

		assertEquals(200, response.getStatus());
		verify(chain, times(1)).doFilter(otherSpoof, response);
	}
}
