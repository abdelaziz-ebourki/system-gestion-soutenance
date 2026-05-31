package com.system_gestion_soutenance.api.coordinator.document.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.coordinator.document.service.DocumentDataService;
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

@WebMvcTest(controllers = DocumentDataController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class DocumentDataControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private DocumentDataService documentDataService;

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
	void evaluationSheets_returnsData() throws Exception {
		when(documentDataService.evaluationSheets(any()))
				.thenReturn(List.of(Map.of("projectId", 1L, "projectTitle", "Projet Test")));

		mockMvc.perform(post("/api/coordinator/documents/evaluation-sheets").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(1)).andExpect(jsonPath("$[0].projectTitle").value("Projet Test"));
	}

	@Test
	void attendanceList_returnsData() throws Exception {
		when(documentDataService.attendanceList(1L)).thenReturn(Map.of("defenseSessionName", "Session PFE"));

		mockMvc.perform(post("/api/coordinator/documents/attendance-lists").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("defenseSessionId", 1)))).andExpect(status().isOk())
				.andExpect(jsonPath("$.defenseSessionName").value("Session PFE"));
	}

	@Test
	void juryConvocations_returnsData() throws Exception {
		when(documentDataService.juryConvocations(any()))
				.thenReturn(List.of(Map.of("teacherName", "John Doe", "role", "président")));

		mockMvc.perform(post("/api/coordinator/documents/jury-convocations").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(1)).andExpect(jsonPath("$[0].teacherName").value("John Doe"));
	}

	@Test
	void schedule_returnsData() throws Exception {
		when(documentDataService.schedule(1L)).thenReturn(Map.of("defenseSessionName", "Session PFE"));

		mockMvc.perform(post("/api/coordinator/documents/schedule").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("defenseSessionId", 1)))).andExpect(status().isOk())
				.andExpect(jsonPath("$.defenseSessionName").value("Session PFE"));
	}

	@Test
	void procesVerbal_returnsData() throws Exception {
		when(documentDataService.procesVerbal(1L)).thenReturn(Map.of("studentNames", List.of("Jane Smith")));

		mockMvc.perform(post("/api/coordinator/documents/proces-verbal").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isOk())
				.andExpect(jsonPath("$.studentNames[0]").value("Jane Smith"));
	}
}
