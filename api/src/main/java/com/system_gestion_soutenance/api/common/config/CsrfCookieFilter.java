package com.system_gestion_soutenance.api.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that ensures the CSRF token is rendered into a cookie on every
 * request. Spring Security 6 uses deferred CSRF tokens by default, which means
 * the token is not generated or written to the response until it is accessed.
 * This filter forces the token to be generated so that the frontend SPA can
 * read the XSRF-TOKEN cookie.
 */
@SuppressWarnings("PMD")
public class CsrfCookieFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
		if (csrfToken != null) {
			csrfToken.getToken();
		}
		filterChain.doFilter(request, response);
	}
}