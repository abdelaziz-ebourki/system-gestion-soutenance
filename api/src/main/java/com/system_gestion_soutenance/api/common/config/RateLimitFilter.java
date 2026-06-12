package com.system_gestion_soutenance.api.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper;
	private final ConcurrentHashMap<String, RequestCounter> counters = new ConcurrentHashMap<>();
	private static final int MAX_REQUESTS = 10;
	private static final long WINDOW_MS = 60_000;

	public RateLimitFilter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String path = request.getRequestURI();
		if (!path.startsWith("/api/auth/") && !"/api/login".equals(path)) {
			filterChain.doFilter(request, response);
			return;
		}

		String clientIp = getClientIp(request);
		long now = System.currentTimeMillis();
		RequestCounter counter = counters.compute(clientIp,
				(key, existing) -> (existing == null || now - existing.windowStart > WINDOW_MS)
						? new RequestCounter(now)
						: existing);

		int currentCount = counter.count.incrementAndGet();
		if (currentCount > MAX_REQUESTS) {
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			objectMapper.writeValue(response.getWriter(),
					Map.of("message", "Trop de requêtes. Veuillez réessayer plus tard."));
			return;
		}

		filterChain.doFilter(request, response);
	}

	private String getClientIp(HttpServletRequest request) {
		String xff = request.getHeader("X-Forwarded-For");
		if (xff != null && !xff.isEmpty()) {
			return xff.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private static class RequestCounter {
		final long windowStart;
		final AtomicInteger count;

		RequestCounter(long windowStart) {
			this.windowStart = windowStart;
			this.count = new AtomicInteger(0);
		}
	}
}
