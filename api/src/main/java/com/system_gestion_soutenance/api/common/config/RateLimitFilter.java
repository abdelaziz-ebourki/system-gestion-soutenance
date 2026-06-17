package com.system_gestion_soutenance.api.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper;
	private final ConcurrentHashMap<String, RequestCounter> counters = new ConcurrentHashMap<>();
	private final boolean trustProxyHeaders;
	private ScheduledExecutorService evictor;

	private final int defaultMaxRequests;
	private final boolean generalEnabled;
	private static final long WINDOW_MS = 60_000;
	private static final long EVICT_INTERVAL_MS = 120_000;

	public RateLimitFilter(ObjectMapper objectMapper,
			@Value("${app.security.trust-proxy-headers:false}") boolean trustProxyHeaders,
			@Value("${app.security.rate-limit.max-requests:60}") int defaultMaxRequests,
			@Value("${app.security.rate-limit.general-enabled:false}") boolean generalEnabled) {
		this.objectMapper = objectMapper;
		this.trustProxyHeaders = trustProxyHeaders;
		this.defaultMaxRequests = defaultMaxRequests;
		this.generalEnabled = generalEnabled;
	}

	@PostConstruct
	void startEviction() {
		evictor = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread t = new Thread(r, "rate-limit-evictor");
			t.setDaemon(true);
			return t;
		});
		evictor.scheduleWithFixedDelay(this::evictStale, EVICT_INTERVAL_MS, EVICT_INTERVAL_MS, TimeUnit.MILLISECONDS);
	}

	@PreDestroy
	void stopEviction() {
		if (evictor != null) {
			evictor.shutdownNow();
		}
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String path = request.getRequestURI();

		if (!path.startsWith("/api/")) {
			filterChain.doFilter(request, response);
			return;
		}

		if (!isAuthEndpoint(path) && !isBulkEndpoint(path) && !generalEnabled) {
			filterChain.doFilter(request, response);
			return;
		}

		int effectiveMax = resolveMaxRequests(path);

		String clientIp = getClientIp(request);
		long now = System.currentTimeMillis();
		RequestCounter counter = counters.compute(clientIp,
				(key, existing) -> (existing == null || now - existing.windowStart > WINDOW_MS)
						? new RequestCounter(now)
						: existing);

		int currentCount = counter.count.incrementAndGet();
		if (currentCount > effectiveMax) {
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			objectMapper.writeValue(response.getWriter(),
					Map.of("message", "Trop de requêtes. Veuillez réessayer plus tard."));
			return;
		}

		filterChain.doFilter(request, response);
	}

	private boolean isAuthEndpoint(String path) {
		return path.startsWith("/api/auth/") || "/api/login".equals(path);
	}

	private boolean isBulkEndpoint(String path) {
		return path.startsWith("/api/coordinator/projects/bulk") || path.startsWith("/api/coordinator/schedules");
	}

	private int resolveMaxRequests(String path) {
		if (isAuthEndpoint(path)) {
			return Math.min(defaultMaxRequests, 10);
		}
		if (isBulkEndpoint(path)) {
			return Math.min(defaultMaxRequests, 10);
		}
		return defaultMaxRequests;
	}

	private String getClientIp(HttpServletRequest request) {
		if (trustProxyHeaders) {
			String xff = request.getHeader("X-Forwarded-For");
			if (xff != null && !xff.isEmpty()) {
				return xff.split(",")[0].trim();
			}
		}
		return request.getRemoteAddr();
	}

	private void evictStale() {
		long now = System.currentTimeMillis();
		Iterator<Map.Entry<String, RequestCounter>> it = counters.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, RequestCounter> entry = it.next();
			if (now - entry.getValue().windowStart > 2 * WINDOW_MS) {
				it.remove();
			}
		}
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
