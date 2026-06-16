package com.system_gestion_soutenance.api.coordinator.document.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.pdf.DocumentGenerationService;
import com.system_gestion_soutenance.api.coordinator.document.dto.*;
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

	@MockitoBean
	private DocumentGenerationService documentGenerationService;

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
		when(documentDataService.evaluationSheets(any())).thenReturn(
				List.of(new com.system_gestion_soutenance.api.coordinator.document.dto.EvaluationSheetResponse(1L,
						"Projet Test", List.of(), "Supervisor", "2025-06-01", "09:00", "Room 1", List.of(), Map.of())));

		mockMvc.perform(post("/api/coordinator/documents/evaluation-sheets").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.size()").value(1))
				.andExpect(jsonPath("$.data[0].projectTitle").value("Projet Test"));
	}

	@Test
	void attendanceList_returnsData() throws Exception {
		when(documentDataService.attendanceList(1L)).thenReturn(
				new com.system_gestion_soutenance.api.coordinator.document.dto.AttendanceListResponse("Session PFE",
						List.of()));

		mockMvc.perform(post("/api/coordinator/documents/attendance-lists").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("defenseSessionId", 1)))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.defenseSessionName").value("Session PFE"));
	}

	@Test
	void juryConvocations_returnsData() throws Exception {
		when(documentDataService.juryConvocations(any())).thenReturn(List
				.of(new com.system_gestion_soutenance.api.coordinator.document.dto.JuryConvocationResponse("John Doe",
						"président", "Projet Test", List.of(), "2025-06-01", "09:00", "Room 1", "Session PFE")));

		mockMvc.perform(post("/api/coordinator/documents/jury-convocations").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.size()").value(1))
				.andExpect(jsonPath("$.data[0].teacherName").value("John Doe"));
	}

	@Test
	void schedule_returnsData() throws Exception {
		when(documentDataService.schedule(1L)).thenReturn(
				new com.system_gestion_soutenance.api.coordinator.document.dto.ScheduleDocResponse("Session PFE",
						List.of()));

		mockMvc.perform(post("/api/coordinator/documents/schedule").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("defenseSessionId", 1)))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.defenseSessionName").value("Session PFE"));
	}

	@Test
	void minutes_returnsData() throws Exception {
		when(documentDataService.minutes(1L))
				.thenReturn(new MinutesResponse(new MinutesResponse.Settings(null, null, null, null), null,
						List.of("Jane Smith"), "Supervisor", List.of()));

		mockMvc.perform(post("/api/coordinator/documents/proces-verbal").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.studentNames[0]").value("Jane Smith"));
	}

	@Test
	void evaluationSheetsPdf_returnsPdf() throws Exception {
		when(documentDataService.evaluationSheets(any())).thenReturn(List
				.of(new EvaluationSheetResponse(1L, "Projet", List.of("Alice"), "Super", "2025-06-01", "09:00", "Room1",
						List.of(new EvaluationSheetResponse.JuryMemberResponse("Rapporteur", "Dr. X", 3)), Map.of())));
		when(documentGenerationService.generatePdf(anyString(), anyMap()))
				.thenReturn(new byte[]{0x25, 0x50, 0x44, 0x46});

		mockMvc.perform(post("/api/coordinator/documents/evaluation-sheets/pdf").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF));
	}

	@Test
	void evaluationSheetsPdf_emptyData_returnsNotFound() throws Exception {
		when(documentDataService.evaluationSheets(any())).thenReturn(List.of());

		mockMvc.perform(post("/api/coordinator/documents/evaluation-sheets/pdf").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isNotFound());
	}

	@Test
	void evaluationSheetsPdf_withNullFields_handlesGracefully() throws Exception {
		when(documentDataService.evaluationSheets(any()))
				.thenReturn(List.of(new EvaluationSheetResponse(1L, null, null, null, null, null, null, null, null)));
		when(documentGenerationService.generatePdf(anyString(), anyMap()))
				.thenReturn(new byte[]{0x25, 0x50, 0x44, 0x46});

		mockMvc.perform(post("/api/coordinator/documents/evaluation-sheets/pdf").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isOk());
	}

	@Test
	void attendanceListPdf_returnsPdf() throws Exception {
		when(documentDataService.attendanceList(1L)).thenReturn(new AttendanceListResponse("Session PFE", List.of()));
		when(documentGenerationService.generatePdf(anyString(), anyMap()))
				.thenReturn(new byte[]{0x25, 0x50, 0x44, 0x46});

		mockMvc.perform(post("/api/coordinator/documents/attendance-lists/pdf").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("defenseSessionId", 1)))).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF));
	}

	@Test
	void attendanceListPdf_withNullFields_handlesGracefully() throws Exception {
		when(documentDataService.attendanceList(1L)).thenReturn(new AttendanceListResponse(null, null));
		when(documentGenerationService.generatePdf(anyString(), anyMap()))
				.thenReturn(new byte[]{0x25, 0x50, 0x44, 0x46});

		mockMvc.perform(post("/api/coordinator/documents/attendance-lists/pdf").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("defenseSessionId", 1)))).andExpect(status().isOk());
	}

	@Test
	void juryConvocationsPdf_returnsPdf() throws Exception {
		when(documentDataService.juryConvocations(any())).thenReturn(List.of(new JuryConvocationResponse("John",
				"président", "Projet", List.of("Alice"), "2025-06-01", "09:00", "Room1", "Session PFE")));
		when(documentGenerationService.generatePdf(anyString(), anyMap()))
				.thenReturn(new byte[]{0x25, 0x50, 0x44, 0x46});

		mockMvc.perform(post("/api/coordinator/documents/jury-convocations/pdf").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF));
	}

	@Test
	void juryConvocationsPdf_emptyData_returnsNotFound() throws Exception {
		when(documentDataService.juryConvocations(any())).thenReturn(List.of());

		mockMvc.perform(post("/api/coordinator/documents/jury-convocations/pdf").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isNotFound());
	}

	@Test
	void juryConvocationsPdf_withNullFields_handlesGracefully() throws Exception {
		when(documentDataService.juryConvocations(any()))
				.thenReturn(List.of(new JuryConvocationResponse(null, null, null, null, null, null, null, null)));
		when(documentGenerationService.generatePdf(anyString(), anyMap()))
				.thenReturn(new byte[]{0x25, 0x50, 0x44, 0x46});

		mockMvc.perform(post("/api/coordinator/documents/jury-convocations/pdf").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isOk());
	}

	@Test
	void schedulePdf_returnsPdf() throws Exception {
		when(documentDataService.schedule(1L)).thenReturn(new ScheduleDocResponse("Session PFE", List.of()));
		when(documentGenerationService.generatePdf(anyString(), anyMap()))
				.thenReturn(new byte[]{0x25, 0x50, 0x44, 0x46});

		mockMvc.perform(post("/api/coordinator/documents/schedule/pdf").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("defenseSessionId", 1)))).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF));
	}

	@Test
	void procesVerbalPdf_returnsPdf() throws Exception {
		when(documentDataService.minutes(1L)).thenReturn(new MinutesResponse(
				new MinutesResponse.Settings("Univ", "logo.png", "Europe/Paris", "dd/MM/yyyy"), null, List.of("Alice"),
				"Super", List.of(new MinutesResponse.JuryMemberDetails("Rapporteur", "Dr. X"))));
		when(documentGenerationService.generatePdf(anyString(), anyMap()))
				.thenReturn(new byte[]{0x25, 0x50, 0x44, 0x46});

		mockMvc.perform(post("/api/coordinator/documents/proces-verbal/pdf").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF));
	}

	@Test
	void procesVerbalPdf_withNullFields_handlesGracefully() throws Exception {
		when(documentDataService.minutes(1L)).thenReturn(new MinutesResponse(null, null, null, null, null));
		when(documentGenerationService.generatePdf(anyString(), anyMap()))
				.thenReturn(new byte[]{0x25, 0x50, 0x44, 0x46});

		mockMvc.perform(post("/api/coordinator/documents/proces-verbal/pdf").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isOk());
	}
}
