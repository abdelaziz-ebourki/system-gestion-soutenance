package com.system_gestion_soutenance.api.coordinator.schedule.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.common.mapper.ScheduleMapper;
import com.system_gestion_soutenance.api.coordinator.conflict.dto.ConflictDetailResponse;
import com.system_gestion_soutenance.api.coordinator.conflict.service.ConflictDetectionService;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleRequest;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleResponse;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
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
	private ScheduleMapper scheduleMapper;

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
	@SuppressWarnings("unchecked")
	void get_returnsSchedule() throws Exception {
		SlotAssignment slot = mock(SlotAssignment.class);
		when(slot.getId()).thenReturn(1L);
		when(slot.getTitle()).thenReturn("Slot 1");

		ScheduleResponse dto = new ScheduleResponse(1L, "Slot 1", "2025-06-01", "09:00", 1L, 1L, "Salle 1", "Projet 1",
				List.of("S1"), "supervisor", "Approved");

		when(scheduleService.getSchedule()).thenReturn(List.of(slot));
		when(scheduleService.buildProjectMap(anyList())).thenReturn(Map.of());
		when(scheduleService.buildStudentNamesMap(anyMap())).thenReturn(Map.of());
		when(scheduleMapper.toDto(eq(slot), anyMap(), anyMap())).thenReturn(dto);

		mockMvc.perform(get("/api/coordinator/schedules")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.size()").value(1)).andExpect(jsonPath("$.data[0].title").value("Slot 1"));
	}

	@Test
	@SuppressWarnings("unchecked")
	void save_noConflicts_returnsOk() throws Exception {
		com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleRequest body = new com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleRequest(
				1L,
				List.of(new com.system_gestion_soutenance.api.coordinator.schedule.dto.SlotAssignmentRequest("Slot 1",
						"2025-06-01", "09:00", 1L, 1L)));

		SlotAssignment slot = mock(SlotAssignment.class);
		when(slot.getId()).thenReturn(1L);
		when(slot.getTitle()).thenReturn("Slot 1");

		ScheduleResponse dto = new ScheduleResponse(1L, "Slot 1", "2025-06-01", "09:00", 1L, 1L, "Salle 1", "Projet 1",
				List.of("S1"), "supervisor", "Approved");

		when(conflictDetectionService.validate(any(ScheduleRequest.class), anyString())).thenReturn(List.of());
		when(scheduleService.saveSchedule(any())).thenReturn(List.of(slot));
		when(scheduleService.buildProjectMap(anyList())).thenReturn(Map.of());
		when(scheduleService.buildStudentNamesMap(anyMap())).thenReturn(Map.of());
		when(scheduleMapper.toDto(eq(slot), anyMap(), anyMap())).thenReturn(dto);

		mockMvc.perform(post("/api/coordinator/schedules").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void save_withConflicts_returnsBadRequest() throws Exception {
		com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleRequest body = new com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleRequest(
				1L,
				List.of(new com.system_gestion_soutenance.api.coordinator.schedule.dto.SlotAssignmentRequest("Slot 1",
						"2025-06-01", "09:00", 1L, 1L)));

		when(conflictDetectionService.validate(any(ScheduleRequest.class), anyString())).thenReturn(
				List.of(new ConflictDetailResponse("type", "error", "Conflict detected", "slotId", "suggestion")));

		mockMvc.perform(post("/api/coordinator/schedules").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors").isArray());
	}

	@Test
	@SuppressWarnings("unchecked")
	void save_withWarningsOnly_returnsOk() throws Exception {
		com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleRequest body = new com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleRequest(
				1L,
				List.of(new com.system_gestion_soutenance.api.coordinator.schedule.dto.SlotAssignmentRequest("Slot 1",
						"2025-06-01", "09:00", 1L, 1L)));

		SlotAssignment slot = mock(SlotAssignment.class);

		when(conflictDetectionService.validate(any(ScheduleRequest.class), anyString())).thenReturn(
				List.of(new ConflictDetailResponse("type", "warning", "Minor issue", "slotId", "suggestion")));
		when(scheduleService.saveSchedule(any())).thenReturn(List.of(slot));
		when(scheduleService.buildProjectMap(anyList())).thenReturn(Map.of());
		when(scheduleService.buildStudentNamesMap(anyMap())).thenReturn(Map.of());
		when(scheduleMapper.toDto(eq(slot), anyMap(), anyMap())).thenReturn(null);

		mockMvc.perform(post("/api/coordinator/schedules").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))).andExpect(status().isOk());
	}

	@Test
	void autoGenerate_missingDefenseSessionId_throwsBadRequest() throws Exception {
		mockMvc.perform(post("/api/coordinator/schedules/generation").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of()))).andExpect(status().isBadRequest());
	}

	@Test
	void autoGenerate_withDefenseSessionId_returnsSchedule() throws Exception {
		when(scheduleService.autoGenerate(1L))
				.thenReturn(List.of(new com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleResponse(1L,
						"Generated Slot", "2025-06-01", "09:00", 1L, 1L, "Salle 1", "Projet 1", List.of("S1"),
						"supervisor", "Approved")));

		var body = new com.system_gestion_soutenance.api.coordinator.schedule.dto.DefenseSessionIdRequest(1L);
		mockMvc.perform(post("/api/coordinator/schedules/generation").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].title").value("Generated Slot"));
	}

	@Test
	void publish_missingDefenseSessionId_throwsBadRequest() throws Exception {
		mockMvc.perform(patch("/api/coordinator/schedules/publication").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of()))).andExpect(status().isBadRequest());
	}

	@Test
	void publish_withDefenseSessionId_returnsOk() throws Exception {
		doNothing().when(scheduleService).publish(1L);

		var body = new com.system_gestion_soutenance.api.coordinator.schedule.dto.DefenseSessionIdRequest(1L);
		mockMvc.perform(patch("/api/coordinator/schedules/publication").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))).andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Planning publié avec succès."));
	}

	@Test
	void save_missingSchedule_throwsBadRequest() throws Exception {
		mockMvc.perform(post("/api/coordinator/schedules").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("defenseSessionId", "1"))))
				.andExpect(status().isBadRequest());
	}
}
