package com.system_gestion_soutenance.api.teacher.unavailability.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.teacher.unavailability.service.TeacherUnavailabilityService;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
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

@WebMvcTest(controllers = TeacherUnavailabilityController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class TeacherUnavailabilityControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private TeacherUnavailabilityService service;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		User user = new User();
		user.setId(1L);
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void get_returns200() throws Exception {
		when(service.getByTeacher(1L)).thenReturn(Map.of("slotsByDate", Map.of()));
		mockMvc.perform(get("/api/teacher/unavailability")).andExpect(status().isOk());
	}

	@Test
	void save_withFlatBody_returns200() throws Exception {
		when(service.saveForTeacher(anyLong(), any())).thenReturn(Map.of("slotsByDate", Map.of()));
		mockMvc.perform(post("/api/teacher/unavailability").contentType(MediaType.APPLICATION_JSON)
				.content("{\"2026-06-01\":[\"08:00\"]}")).andExpect(status().isOk());
	}

	@Test
	void save_returns200() throws Exception {
		when(service.saveForTeacher(anyLong(), any())).thenReturn(Map.of("slotsByDate", Map.of()));
		mockMvc.perform(post("/api/teacher/unavailability").contentType(MediaType.APPLICATION_JSON)
				.content("{\"slotsByDate\":{\"2026-06-01\":[\"08:00\"]}}")).andExpect(status().isOk());
	}
}
