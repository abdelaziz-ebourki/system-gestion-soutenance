package com.system_gestion_soutenance.api.coordinator.report.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.service.PdfGenerationService;
import com.system_gestion_soutenance.api.coordinator.report.dto.GradeHistoryResponse;
import com.system_gestion_soutenance.api.coordinator.report.dto.SessionReportResponse;
import com.system_gestion_soutenance.api.coordinator.report.service.ReportService;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ReportController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class ReportControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ReportService reportService;

	@MockitoBean
	private PdfGenerationService pdfGenerationService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private UserRepository userRepository;

	@Test
	void getSessionReport_returnsPdf() throws Exception {
		SessionReportResponse data = new SessionReportResponse("Session", "PFE", "2025-06-01", "2025-06-30", 5, 4, 80.0,
				List.of());
		when(reportService.getSessionReport(1L)).thenReturn(data);
		when(pdfGenerationService.generatePdf(eq("session-report"), any())).thenReturn(new byte[]{1, 2, 3});
		doNothing().when(reportService).recordDocumentGeneration(anyString(), anyLong(), anyLong());

		mockMvc.perform(get("/api/coordinator/sessions/1/report")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_PDF));
	}

	@Test
	void getTeacherWorkload_returnsWorkload() throws Exception {
		when(reportService.getTeacherWorkload()).thenReturn(List.of());

		mockMvc.perform(get("/api/coordinator/reports/teacher-workload")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void getGradeHistory_returnsHistory() throws Exception {
		when(reportService.getGradeHistory(1L)).thenReturn(List.of());

		mockMvc.perform(get("/api/coordinator/reports/grade-history/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}
}
