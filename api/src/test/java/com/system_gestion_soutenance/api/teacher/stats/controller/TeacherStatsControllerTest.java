package com.system_gestion_soutenance.api.teacher.stats.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.teacher.stats.service.TeacherStatsService;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TeacherStatsController.class)
class TeacherStatsControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private TeacherStatsService statsService;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		// No more manual SecurityContextHolder setup
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getStats_returns200() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setRole(com.system_gestion_soutenance.api.user.entity.Role.TEACHER);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
				List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_TEACHER")));
		when(statsService.getStats(1L))
				.thenReturn(new com.system_gestion_soutenance.api.teacher.stats.dto.TeacherStatsResponse(0, 0, 0, 0));
		mockMvc.perform(get("/api/teacher/stats").with(authentication(auth))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.upcomingDefenses").value(0));
	}
}
