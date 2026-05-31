package com.system_gestion_soutenance.api.coordinator.conflict.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.coordinator.conflict.service.ConflictDetectionService;
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

@WebMvcTest(controllers = ConflictController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class ConflictControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ConflictDetectionService conflictDetectionService;

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
	void validate_returnsConflicts() throws Exception {
		Map<String, Object> body = Map.of("schedule", Map.of("1", Map.of("title", "Slot 1")), "defenseSessionId", "1");

		when(conflictDetectionService.validate(any(), eq("1")))
				.thenReturn(List.of(Map.of("type", "project_already_scheduled", "severity", "error")));

		mockMvc.perform(post("/api/coordinator/schedule/validate").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))).andExpect(status().isOk())
				.andExpect(jsonPath("$.conflicts.size()").value(1))
				.andExpect(jsonPath("$.conflicts[0].type").value("project_already_scheduled"));
	}

	@Test
	void validate_noConflicts_returnsEmpty() throws Exception {
		Map<String, Object> body = Map.of("schedule", Map.of("1", Map.of("title", "Slot 1")), "defenseSessionId", "1");

		when(conflictDetectionService.validate(any(), eq("1"))).thenReturn(List.of());

		mockMvc.perform(post("/api/coordinator/schedule/validate").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))).andExpect(status().isOk())
				.andExpect(jsonPath("$.conflicts.size()").value(0));
	}
}
