package com.system_gestion_soutenance.api.coordinator.stats.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.coordinator.stats.dto.CoordinatorStatsResponse;
import com.system_gestion_soutenance.api.coordinator.stats.service.CoordinatorStatsService;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CoordinatorStatsController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class CoordinatorStatsControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CoordinatorStatsService statsService;

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
	void getStats_returnsStats() throws Exception {
		when(statsService.getStats()).thenReturn(new CoordinatorStatsResponse(10L, 5L, 8L, 3L));

		mockMvc.perform(get("/api/coordinator/stats")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalProjects").value(10))
				.andExpect(jsonPath("$.data.totalGroups").value(5));
	}
}
