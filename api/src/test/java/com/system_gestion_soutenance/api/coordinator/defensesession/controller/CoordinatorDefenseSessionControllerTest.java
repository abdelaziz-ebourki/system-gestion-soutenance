package com.system_gestion_soutenance.api.coordinator.defensesession.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSessionStatus;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.coordinator.defensesession.dto.CreateDefenseSessionRequest;
import com.system_gestion_soutenance.api.coordinator.defensesession.service.CoordinatorDefenseSessionService;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
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

@WebMvcTest(controllers = CoordinatorDefenseSessionController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class CoordinatorDefenseSessionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private CoordinatorDefenseSessionService service;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private com.system_gestion_soutenance.api.common.mapper.DefenseSessionMapper defenseSessionMapper;

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
	void findAll_returnsSessions() throws Exception {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setName("Session 1");
		when(service.findAll(0, 10)).thenReturn(new PaginatedResponse<>(List.of(ds), 1, 1, 0, 10));
		when(defenseSessionMapper.toDto(ds))
				.thenReturn(new com.system_gestion_soutenance.api.admin.defensesession.dto.DefenseSessionDto(1L,
						"Session 1", "PFE", "ACTIVE", 3, 30, 15, null, null, null, null, null, false, false, null, null,
						null, null, null, null, 30, 70));

		mockMvc.perform(get("/api/coordinator/defense-sessions")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items").isArray())
				.andExpect(jsonPath("$.data.items[0].name").value("Session 1"));
	}

	@Test
	void create_returnsCreated() throws Exception {
		CreateDefenseSessionRequest request = new CreateDefenseSessionRequest("Session PFE", "PFE", null, 3, 30, 15,
				null, null, null, "2025-06-01", "2025-06-30", null, null, null, null);
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setName("Session PFE");
		when(service.create(any())).thenReturn(ds);
		when(defenseSessionMapper.toDto(ds))
				.thenReturn(new com.system_gestion_soutenance.api.admin.defensesession.dto.DefenseSessionDto(1L,
						"Session PFE", "PFE", "CREATED", 3, 30, 15, null, null, null, null, null, false, false, null,
						null, null, null, null, null, 30, 70));

		mockMvc.perform(post("/api/coordinator/defense-sessions").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.name").value("Session PFE"));
	}

	@Test
	void update_returnsSession() throws Exception {
		CreateDefenseSessionRequest request = new CreateDefenseSessionRequest("Updated Session", "MEMOIRE", "ACTIVE", 4,
				20, 10, null, null, null, "2025-07-01", "2025-07-31", null, null, null, null);
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setName("Updated Session");
		when(service.update(eq(1L), any())).thenReturn(ds);
		when(defenseSessionMapper.toDto(ds))
				.thenReturn(new com.system_gestion_soutenance.api.admin.defensesession.dto.DefenseSessionDto(1L,
						"Updated Session", "MEMOIRE", "ACTIVE", 4, 20, 10, null, null, null, null, null, false, false,
						null, null, null, null, null, null, 30, 70));

		mockMvc.perform(put("/api/coordinator/defense-sessions/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("Updated Session"));
	}

	@Test
	void delete_returnsOk() throws Exception {
		doNothing().when(service).delete(1L);

		mockMvc.perform(delete("/api/coordinator/defense-sessions/1")).andExpect(status().isOk());
	}

	@Test
	void transition_returnsUpdatedSession() throws Exception {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setStatus(DefenseSessionStatus.ACTIVE);
		when(service.transition(1L, "ACTIVE")).thenReturn(ds);
		when(defenseSessionMapper.toDto(ds))
				.thenReturn(new com.system_gestion_soutenance.api.admin.defensesession.dto.DefenseSessionDto(1L,
						"Session 1", "PFE", "ACTIVE", 3, 30, 15, null, null, null, null, null, false, false, null, null,
						null, null, null, null, 30, 70));

		mockMvc.perform(post("/api/coordinator/defense-sessions/1/transition").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("toStatus", "ACTIVE")))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("ACTIVE"));
	}

	@Test
	void approve_returnsSessionWithApproval() throws Exception {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setApprovedBy(1L);
		ds.setApprovedAt(java.time.LocalDateTime.of(2026, 6, 13, 9, 0));
		when(service.approve(eq(1L), any())).thenReturn(ds);
		when(defenseSessionMapper.toDto(ds))
				.thenReturn(new com.system_gestion_soutenance.api.admin.defensesession.dto.DefenseSessionDto(1L,
						"Session 1", "PFE", "ACTIVE", 3, 30, 15, null, null, null, null, null, false, false, 1L,
						java.time.LocalDateTime.of(2026, 6, 13, 9, 0), null, null, null, null, 30, 70));

		mockMvc.perform(patch("/api/coordinator/defense-sessions/1/approve")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.approvedBy").value(1));
	}

	@Test
	void revokeApproval_returnsSessionWithoutApproval() throws Exception {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setApprovedBy(null);
		ds.setApprovedAt(null);
		when(service.revokeApproval(1L)).thenReturn(ds);
		when(defenseSessionMapper.toDto(ds))
				.thenReturn(new com.system_gestion_soutenance.api.admin.defensesession.dto.DefenseSessionDto(1L,
						"Session 1", "PFE", "ACTIVE", 3, 30, 15, null, null, null, null, null, false, false, null, null,
						null, null, null, null, 30, 70));

		mockMvc.perform(patch("/api/coordinator/defense-sessions/1/revoke-approval")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.approvedBy").isEmpty());
	}
}
