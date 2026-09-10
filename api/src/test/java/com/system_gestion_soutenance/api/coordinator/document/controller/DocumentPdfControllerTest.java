package com.system_gestion_soutenance.api.coordinator.document.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.service.PdfGenerationService;
import com.system_gestion_soutenance.api.coordinator.document.dto.AttendanceListResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.EvaluationSheetResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.JuryConvocationResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.MinutesResponse;
import com.system_gestion_soutenance.api.coordinator.document.dto.ScheduleDocResponse;
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

@WebMvcTest(controllers = DocumentPdfController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class DocumentPdfControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private DocumentDataService documentDataService;

	@MockitoBean
	private PdfGenerationService pdfGenerationService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private UserRepository userRepository;

	private final byte[] fakePdf = new byte[]{0x25, 0x50, 0x44, 0x46};

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
	void evaluationSheets_returnsPdf() throws Exception {
		when(documentDataService.evaluationSheets(any())).thenReturn(List.of(new EvaluationSheetResponse(1L, "Projet",
				List.of(), "Encadrant", "2025-06-01", "09:00", "Salle A", List.of(), Map.of())));
		when(pdfGenerationService.generatePdf(eq("evaluation-sheet"), anyMap())).thenReturn(fakePdf);

		mockMvc.perform(post("/api/coordinator/documents/pdf/evaluation-sheets").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF));
	}

	@Test
	void attendanceList_returnsPdf() throws Exception {
		when(documentDataService.attendanceList(1L)).thenReturn(new AttendanceListResponse("Session PFE", List.of()));
		when(pdfGenerationService.generatePdf(eq("attendance-list"), anyMap())).thenReturn(fakePdf);

		mockMvc.perform(post("/api/coordinator/documents/pdf/attendance-lists").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("defenseSessionId", 1)))).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF));
	}

	@Test
	void juryConvocations_returnsPdf() throws Exception {
		when(documentDataService.juryConvocations(any())).thenReturn(List.of(new JuryConvocationResponse("Jane Smith",
				"président", "Projet", List.of(), "2025-06-01", "09:00", "Salle A", "Session PFE")));
		when(pdfGenerationService.generatePdf(eq("jury-convocation"), anyMap())).thenReturn(fakePdf);

		mockMvc.perform(post("/api/coordinator/documents/pdf/jury-convocations").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF));
	}

	@Test
	void schedule_returnsPdf() throws Exception {
		when(documentDataService.schedule(1L)).thenReturn(new ScheduleDocResponse("Session PFE", List.of()));
		when(pdfGenerationService.generatePdf(eq("schedule"), anyMap())).thenReturn(fakePdf);

		mockMvc.perform(post("/api/coordinator/documents/pdf/schedule").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("defenseSessionId", 1)))).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF));
	}

	@Test
	void minutes_returnsPdf() throws Exception {
		when(documentDataService.minutes(1L)).thenReturn(new MinutesResponse(
				new MinutesResponse.Settings(null, null, null, null), null, List.of(), null, List.of()));
		when(pdfGenerationService.generatePdf(eq("proces-verbal"), anyMap())).thenReturn(fakePdf);

		mockMvc.perform(post("/api/coordinator/documents/pdf/proces-verbal").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("projectId", 1)))).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF));
	}

	@Test
	void evaluationSheets_missingProjectId_returns400() throws Exception {
		mockMvc.perform(post("/api/coordinator/documents/pdf/evaluation-sheets").contentType(MediaType.APPLICATION_JSON)
				.content("{}")).andExpect(status().isBadRequest());
	}

	@Test
	void attendanceList_missingSessionId_returns400() throws Exception {
		mockMvc.perform(post("/api/coordinator/documents/pdf/attendance-lists").contentType(MediaType.APPLICATION_JSON)
				.content("{}")).andExpect(status().isBadRequest());
	}
}
