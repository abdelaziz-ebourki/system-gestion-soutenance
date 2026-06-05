package com.system_gestion_soutenance.api.admin.stats.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.admin.stats.dto.GlobalStatsResponse;
import com.system_gestion_soutenance.api.admin.stats.service.StatsService;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = StatsController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class StatsControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private StatsService statsService;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;

	@Test
	void getStats_returnsGlobalStats() throws Exception {
		when(statsService.getStats()).thenReturn(new GlobalStatsResponse(100L, 0L, 0L, 0L, 0L));

		mockMvc.perform(get("/api/admin/stats")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalStudents").value(100));
	}
}
