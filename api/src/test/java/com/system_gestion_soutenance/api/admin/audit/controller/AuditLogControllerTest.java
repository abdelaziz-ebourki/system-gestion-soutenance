package com.system_gestion_soutenance.api.admin.audit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.admin.audit.dto.AuditLogDto;
import com.system_gestion_soutenance.api.admin.audit.entity.AuditLog;
import com.system_gestion_soutenance.api.admin.audit.service.AuditLogService;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuditLogController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class AuditLogControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private AuditLogService service;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private com.system_gestion_soutenance.api.common.mapper.AuditLogMapper auditLogMapper;

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void findAll_returnsPaginated() throws Exception {
		when(service.getAuditLogs(0, 20)).thenReturn(new PaginatedResponse<>(List.of(), 0, 0, 0, 20));

		mockMvc.perform(get("/api/admin/audit-logs")).andExpect(status().isOk())
				.andExpect(jsonPath("$.items").isArray());
	}

	@Test
	void findAll_returnsPaginatedWithBody() throws Exception {
		AuditLog auditLog = new AuditLog();
		auditLog.setId(1L);
		auditLog.setAction("CREATE");
		auditLog.setEntity("User");
		when(service.getAuditLogs(0, 20)).thenReturn(new PaginatedResponse<>(List.of(auditLog), 1, 1, 0, 20));
		when(auditLogMapper.toDto(auditLog)).thenReturn(
				new AuditLogDto(1L, "CREATE", "User", 1L, "admin@test.com", "details", LocalDateTime.now()));

		mockMvc.perform(get("/api/admin/audit-logs")).andExpect(status().isOk())
				.andExpect(jsonPath("$.items").isArray()).andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].id").value(1)).andExpect(jsonPath("$.items[0].action").value("CREATE"))
				.andExpect(jsonPath("$.items[0].entity").value("User")).andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.currentPage").value(0)).andExpect(jsonPath("$.size").value(20));
	}

	@Test
	void create_returns201() throws Exception {
		when(service.save(any())).thenReturn(mock());

		mockMvc.perform(post("/api/admin/audit-logs").contentType(MediaType.APPLICATION_JSON).content("""
				{"action":"DELETE","entity":"User","entityId":1,"adminEmail":"a@a.com"}
				""")).andExpect(status().isCreated());
	}

	@Test
	void create_returns201_withBody() throws Exception {
		AuditLog savedLog = new AuditLog();
		savedLog.setId(1L);
		when(service.save(any())).thenReturn(savedLog);

		AuditLogDto dto = new AuditLogDto(1L, "DELETE", "User", 1L, "a@a.com", null, LocalDateTime.now());
		when(auditLogMapper.toDto(savedLog)).thenReturn(dto);

		mockMvc.perform(post("/api/admin/audit-logs").contentType(MediaType.APPLICATION_JSON).content("""
				{"action":"DELETE","entity":"User","entityId":1,"adminEmail":"a@a.com"}
				""")).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.action").value("DELETE")).andExpect(jsonPath("$.entity").value("User"))
				.andExpect(jsonPath("$.entityId").value(1)).andExpect(jsonPath("$.adminEmail").value("a@a.com"));
	}

	@Test
	void create_withMissingFields_returns400() throws Exception {
		mockMvc.perform(post("/api/admin/audit-logs").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void create_withAuthenticatedStringPrincipal_usesPrincipalEmail() throws Exception {
		Authentication auth = mock(Authentication.class);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getPrincipal()).thenReturn("admin@test.com");
		SecurityContextHolder.getContext().setAuthentication(auth);

		ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
		when(service.save(captor.capture())).thenReturn(mock());

		mockMvc.perform(post("/api/admin/audit-logs").contentType(MediaType.APPLICATION_JSON).content("""
				{"action":"DELETE","entity":"User","entityId":1,"adminEmail":"fallback@test.com"}
				""")).andExpect(status().isCreated());

		assertThat(captor.getValue().getAdminEmail()).isEqualTo("admin@test.com");
	}

	@Test
	void create_withAuthenticatedUserPrincipal_usesUserEmail() throws Exception {
		User user = new User();
		user.setEmail("user@test.com");

		Authentication auth = mock(Authentication.class);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getPrincipal()).thenReturn(user);
		SecurityContextHolder.getContext().setAuthentication(auth);

		ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
		when(service.save(captor.capture())).thenReturn(mock());

		mockMvc.perform(post("/api/admin/audit-logs").contentType(MediaType.APPLICATION_JSON).content("""
				{"action":"DELETE","entity":"User","entityId":1,"adminEmail":"fallback@test.com"}
				""")).andExpect(status().isCreated());

		assertThat(captor.getValue().getAdminEmail()).isEqualTo("user@test.com");
	}

	@Test
	void create_withoutAuthentication_usesRequestAdminEmail() throws Exception {
		SecurityContextHolder.clearContext();

		ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
		when(service.save(captor.capture())).thenReturn(mock());

		mockMvc.perform(post("/api/admin/audit-logs").contentType(MediaType.APPLICATION_JSON).content("""
				{"action":"DELETE","entity":"User","entityId":1,"adminEmail":"explicit@test.com"}
				""")).andExpect(status().isCreated());

		assertThat(captor.getValue().getAdminEmail()).isEqualTo("explicit@test.com");
	}
}
