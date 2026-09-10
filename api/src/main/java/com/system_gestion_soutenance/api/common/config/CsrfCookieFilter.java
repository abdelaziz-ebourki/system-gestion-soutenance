package com.system_gestion_soutenance.api.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

public class CsrfCookieFilter extends OncePerRequestFilter {

	private static final Logger LOG = LoggerFactory.getLogger(CsrfCookieFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

		LOG.info("=== CsrfCookieFilter ===");
		LOG.info("Method: {}", request.getMethod());
		LOG.info("URI: {}", request.getRequestURI());
		LOG.info("Cookies: {}", Arrays.toString(request.getCookies()));
		LOG.info("X-XSRF-TOKEN header: {}", request.getHeader("X-XSRF-TOKEN"));

		if (csrfToken != null) {
			LOG.info("CSRF token found in request attribute");
			String tokenStr = csrfToken.getToken();
			LOG.info("Token value: {}", tokenStr);
			LOG.info("Header name expected: {}", csrfToken.getHeaderName());
			LOG.info("Parameter name expected: {}", csrfToken.getParameterName());
		} else {
			LOG.info("CSRF token NOT found in request attribute");
		}

		filterChain.doFilter(request, response);
	}
}
