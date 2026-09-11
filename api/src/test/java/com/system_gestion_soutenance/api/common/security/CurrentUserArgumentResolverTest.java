package com.system_gestion_soutenance.api.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.system_gestion_soutenance.api.common.exception.UnauthorizedException;
import com.system_gestion_soutenance.api.user.entity.User;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class CurrentUserArgumentResolverTest {

	private final CurrentUserArgumentResolver resolver = new CurrentUserArgumentResolver();

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private MethodParameter userParameter() throws NoSuchMethodException {
		return new MethodParameter(SampleController.class.getMethod("handle", User.class), 0);
	}

	private MethodParameter stringParameter() throws NoSuchMethodException {
		return new MethodParameter(SampleController.class.getMethod("other", String.class), 0);
	}

	@Test
	void supportsParameter_matchesAnnotatedUserParam() throws Exception {
		assertTrue(resolver.supportsParameter(userParameter()));
	}

	@Test
	void supportsParameter_rejectsUnannotatedOrOtherTypes() throws Exception {
		assertFalse(resolver.supportsParameter(stringParameter()));
	}

	@Test
	void resolveArgument_returnsPrincipalFromHolder() throws Exception {
		User user = new User();
		user.setId(3L);
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null,
				List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))));

		assertEquals(user, resolver.resolveArgument(userParameter(), null, null, null));
	}

	@Test
	void resolveArgument_missingAuthentication_throws401() throws Exception {
		assertThrows(UnauthorizedException.class, () -> resolver.resolveArgument(userParameter(), null, null, null));
	}

	@Test
	void resolveArgument_wrongPrincipalType_throws401() throws Exception {
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken("anonymous", null, List.of()));

		assertThrows(UnauthorizedException.class, () -> resolver.resolveArgument(userParameter(), null, null, null));
	}

	static class SampleController {
		@SuppressWarnings("unused")
		public void handle(@CurrentUser User user) {
		}

		@SuppressWarnings("unused")
		public void other(String value) {
		}
	}
}
