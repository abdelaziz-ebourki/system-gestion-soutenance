package com.system_gestion_soutenance.api.admin.audit.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.admin.audit.service.AuditLogService;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuditLogController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class AuditLogControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private AuditLogService service;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private com.system_gestion_soutenance.api.common.mapper.AuditLogMapper auditLogMapper;

	@Test
	void findAll_returnsPaginated() throws Exception {
		when(service.getAuditLogs(0, 20)).thenReturn(new PaginatedResponse<>(List.of(), 0, 0, 0, 20));

		mockMvc.perform(get("/api/admin/audit-logs")).andExpect(status().isOk())
				.andExpect(jsonPath("$.items").isArray());
	}

	@Test
	void create_returns201() throws Exception {
		when(service.save(any())).thenReturn(mock());

		mockMvc.perform(post("/api/admin/audit-logs").contentType(MediaType.APPLICATION_JSON).content("""
				{"action":"DELETE","entity":"User","entityId":"1","adminEmail":"a@a.com"}
				""")).andExpect(status().isCreated());
	}

	@Test
	void create_withMissingFields_returns400() throws Exception {
		mockMvc.perform(post("/api/admin/audit-logs").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
	}
}
