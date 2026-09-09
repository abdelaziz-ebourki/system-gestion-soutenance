package com.system_gestion_soutenance.api.auth.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.user.entity.Role;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.service.UserCacheService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private UserCacheService userCacheService;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private FilterChain filterChain;

	@InjectMocks
	private JwtAuthFilter filter;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void doFilter_withValidToken_setsAuthentication() throws Exception {
		when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
		when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
		when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn("1");

		User user = new User();
		user.setId(1L);
		user.setEmail("admin@test.com");
		user.setRole(Role.ADMIN);
		user.setActive(true);
		when(userCacheService.getUserById(1L)).thenReturn(Optional.of(user));

		filter.doFilter(request, response, filterChain);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertNotNull(auth);
		assertEquals(user, auth.getPrincipal());
		assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void doFilter_withNoAuthHeader_skipsAuthentication() throws Exception {
		when(request.getHeader("Authorization")).thenReturn(null);

		filter.doFilter(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
		verifyNoInteractions(jwtTokenProvider);
	}

	@Test
	void doFilter_withInvalidToken_skipsAuthentication() throws Exception {
		when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
		when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

		filter.doFilter(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void doFilter_withBearerPrefixOnly_skipsAuthentication() throws Exception {
		when(request.getHeader("Authorization")).thenReturn("Bearer ");

		filter.doFilter(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void doFilter_withUserNotFound_skipsAuthentication() throws Exception {
		when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
		when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
		when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn("999");
		when(userCacheService.getUserById(999L)).thenReturn(Optional.empty());

		filter.doFilter(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void doFilter_withValidTokenInCookie_setsAuthentication() throws Exception {
		when(request.getHeader("Authorization")).thenReturn(null);
		when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("jwt_token", "valid-token")});
		when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
		when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn("1");

		User user = new User();
		user.setId(1L);
		user.setEmail("admin@test.com");
		user.setRole(Role.ADMIN);
		user.setActive(true);
		when(userCacheService.getUserById(1L)).thenReturn(Optional.of(user));

		filter.doFilter(request, response, filterChain);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		assertNotNull(auth);
		assertEquals(user, auth.getPrincipal());
		assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void doFilter_withValidTokenButInactiveUser_returns403() throws Exception {
		when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
		when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
		when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn("1");

		User user = new User();
		user.setId(1L);
		user.setEmail("inactive@test.com");
		user.setRole(Role.ADMIN);
		user.setActive(false);
		when(userCacheService.getUserById(1L)).thenReturn(Optional.of(user));

		filter.doFilter(request, response, filterChain);

		verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Compte désactivé");
		verify(filterChain, never()).doFilter(any(), any());
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void doFilter_withNonBearerHeader_skipsAuthentication() throws Exception {
		when(request.getHeader("Authorization")).thenReturn("Basic xxx");

		filter.doFilter(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
		verifyNoInteractions(jwtTokenProvider);
	}
}
