package com.system_gestion_soutenance.api.teacher.unavailability.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.coordinator.unavailability.entity.Unavailability;
import com.system_gestion_soutenance.api.teacher.unavailability.service.TeacherUnavailabilityService;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TeacherUnavailabilityController.class)
class TeacherUnavailabilityControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private TeacherUnavailabilityService service;
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
	void get_returns200() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setRole(com.system_gestion_soutenance.api.user.entity.Role.TEACHER);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
				List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_TEACHER")));
		when(service.getByTeacher(1L)).thenReturn(List.of());
		mockMvc.perform(get("/api/teacher/unavailabilities").with(authentication(auth))).andExpect(status().isOk());
	}

	@Test
	void save_withFlatBody_returns200() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setRole(com.system_gestion_soutenance.api.user.entity.Role.TEACHER);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
				List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_TEACHER")));
		when(service.saveForTeacher(anyLong(), any())).thenReturn(List.of());
		mockMvc.perform(post("/api/teacher/unavailabilities").contentType(MediaType.APPLICATION_JSON)
				.content("{\"slots\":[{\"date\":\"2026-06-01\",\"slots\":[\"08:00\"]}]}").with(authentication(auth))
				.with(csrf())).andExpect(status().isOk());
	}

	@Test
	void save_returns200() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setRole(com.system_gestion_soutenance.api.user.entity.Role.TEACHER);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
				List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_TEACHER")));
		when(service.saveForTeacher(anyLong(), any())).thenReturn(List.of());
		mockMvc.perform(post("/api/teacher/unavailabilities").contentType(MediaType.APPLICATION_JSON)
				.content("{\"slots\":[{\"date\":\"2026-06-01\",\"slots\":[\"08:00\"]}]}").with(authentication(auth))
				.with(csrf())).andExpect(status().isOk());
	}
}
