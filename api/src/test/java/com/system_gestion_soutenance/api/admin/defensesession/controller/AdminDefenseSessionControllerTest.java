package com.system_gestion_soutenance.api.admin.defensesession.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureJson
@WebMvcTest(controllers = AdminDefenseSessionController.class, excludeAutoConfiguration = {
		org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration.class,
		org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration.class})
class AdminDefenseSessionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DefenseSessionRepository defenseSessionRepository;

	@MockitoBean
	private SecurityService securityService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
				new com.system_gestion_soutenance.api.user.entity.User(), null, List.of()));
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void approveResults_unknownSession_returns404() throws Exception {
		when(defenseSessionRepository.findById(99L)).thenReturn(Optional.empty());

		mockMvc.perform(patch("/api/admin/sessions/99/approve-results")).andExpect(status().isNotFound());
	}

	@Test
	void approveResults_notDeliberated_returns400() throws Exception {
		DefenseSession session = new DefenseSession();
		session.setId(1L);
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(session));

		mockMvc.perform(patch("/api/admin/sessions/1/approve-results")).andExpect(status().isBadRequest());
	}

	@Test
	void approveResults_deliberated_publishesResults() throws Exception {
		DefenseSession session = new DefenseSession();
		session.setId(1L);
		session.setDeliberatedAt(LocalDateTime.now().minusDays(1));
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(securityService.getCurrentUserId()).thenReturn(7L);

		mockMvc.perform(patch("/api/admin/sessions/1/approve-results")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));

		verify(defenseSessionRepository).save(session);
		assertTrue(session.isResultsPublished());
		assertEquals(7L, session.getValidatedBy());
		assertNotNull(session.getValidatedAt());
	}
}
