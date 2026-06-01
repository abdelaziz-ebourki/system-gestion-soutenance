package com.system_gestion_soutenance.api.coordinator.schedule.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.coordinator.conflict.service.ConflictDetectionService;
import com.system_gestion_soutenance.api.coordinator.schedule.service.ScheduleService;
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

@WebMvcTest(controllers = ScheduleController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class ScheduleControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ScheduleService scheduleService;

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
	void get_returnsSchedule() throws Exception {
		when(scheduleService.getSchedule()).thenReturn(Map.of("1", Map.of("id", 1L, "title", "Slot 1")));

		mockMvc.perform(get("/api/coordinator/schedule")).andExpect(status().isOk())
				.andExpect(jsonPath("$['1'].title").value("Slot 1"));
	}

	@Test
	void save_noConflicts_returnsOk() throws Exception {
		Map<String, Object> body = Map.of("schedule", Map.of("1", Map.of("title", "Slot 1")), "defenseSessionId", "1");

		when(conflictDetectionService.validate(any(), eq("1"))).thenReturn(List.of());
		when(scheduleService.saveSchedule(any())).thenReturn(Map.of("1", Map.of("title", "Slot 1")));

		mockMvc.perform(post("/api/coordinator/schedule").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))).andExpect(status().isOk());
	}

	@Test
	void save_withConflicts_returnsBadRequest() throws Exception {
		Map<String, Object> body = Map.of("schedule", Map.of("1", Map.of("title", "Slot 1")), "defenseSessionId", "1");

		when(conflictDetectionService.validate(any(), eq("1")))
				.thenReturn(List.of(Map.of("severity", "error", "message", "Conflict detected")));

		mockMvc.perform(post("/api/coordinator/schedule").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.conflicts").isArray());
	}

	@Test
	void save_withWarningsOnly_returnsOk() throws Exception {
		Map<String, Object> body = Map.of("schedule", Map.of("1", Map.of("title", "Slot 1")), "defenseSessionId", "1");

		when(conflictDetectionService.validate(any(), eq("1")))
				.thenReturn(List.of(Map.of("severity", "warning", "message", "Minor issue")));
		when(scheduleService.saveSchedule(any())).thenReturn(Map.of());

		mockMvc.perform(post("/api/coordinator/schedule").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))).andExpect(status().isOk());
	}

	@Test
	void autoGenerate_missingDefenseSessionId_throwsBadRequest() throws Exception {
		mockMvc.perform(post("/api/coordinator/schedule/auto-generate").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of()))).andExpect(status().isBadRequest());
	}

	@Test
	void autoGenerate_withDefenseSessionId_returnsSchedule() throws Exception {
		when(scheduleService.autoGenerate(1L)).thenReturn(Map.of("1", Map.of("title", "Generated Slot")));

		mockMvc.perform(post("/api/coordinator/schedule/auto-generate").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("defenseSessionId", "1")))).andExpect(status().isOk())
				.andExpect(jsonPath("$.schedule['1'].title").value("Generated Slot"));
	}

	@Test
	void publish_missingDefenseSessionId_throwsBadRequest() throws Exception {
		mockMvc.perform(post("/api/coordinator/schedule/publish").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of()))).andExpect(status().isBadRequest());
	}

	@Test
	void publish_withDefenseSessionId_returnsOk() throws Exception {
		doNothing().when(scheduleService).publish(1L);

		mockMvc.perform(post("/api/coordinator/schedule/publish").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("defenseSessionId", "1")))).andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Planning publié avec succès."));
	}

	@Test
	void save_missingSchedule_throwsBadRequest() throws Exception {
		mockMvc.perform(post("/api/coordinator/schedule").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("defenseSessionId", "1"))))
				.andExpect(status().isBadRequest());
	}
}
