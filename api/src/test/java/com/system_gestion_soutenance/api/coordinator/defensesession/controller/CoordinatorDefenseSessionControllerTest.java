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
		when(service.findAll()).thenReturn(List.of(ds));

		mockMvc.perform(get("/api/coordinator/defense-sessions")).andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(1)).andExpect(jsonPath("$[0].name").value("Session 1"));
	}

	@Test
	void create_returnsCreated() throws Exception {
		CreateDefenseSessionRequest request = new CreateDefenseSessionRequest("Session PFE", "PFE", null, 3, 30, 15,
				null, null, null, "2025-06-01", "2025-06-30");
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setName("Session PFE");
		when(service.create(any())).thenReturn(ds);

		mockMvc.perform(post("/api/coordinator/defense-sessions").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Session PFE"));
	}

	@Test
	void update_returnsSession() throws Exception {
		CreateDefenseSessionRequest request = new CreateDefenseSessionRequest("Updated Session", "MEMOIRE", "ACTIVE", 4,
				20, 10, null, null, null, "2025-07-01", "2025-07-31");
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setName("Updated Session");
		when(service.update(eq(1L), any())).thenReturn(ds);

		mockMvc.perform(put("/api/coordinator/defense-sessions/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Updated Session"));
	}

	@Test
	void delete_returnsNoContent() throws Exception {
		doNothing().when(service).delete(1L);

		mockMvc.perform(delete("/api/coordinator/defense-sessions/1")).andExpect(status().isNoContent());
	}

	@Test
	void transition_returnsUpdatedSession() throws Exception {
		DefenseSession ds = new DefenseSession();
		ds.setId(1L);
		ds.setStatus(DefenseSessionStatus.ACTIVE);
		when(service.transition(1L, "ACTIVE")).thenReturn(ds);

		mockMvc.perform(post("/api/coordinator/defense-sessions/1/transition").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of("toStatus", "ACTIVE")))).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}
}
