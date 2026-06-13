package com.system_gestion_soutenance.api.coordinator.jury.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.coordinator.jury.dto.CreateJuryRequest;
import com.system_gestion_soutenance.api.coordinator.jury.dto.JuryResponse;
import com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.service.DefenseService;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.JuryMapper;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
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

@WebMvcTest(controllers = JuryController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class JuryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private DefenseService defenseService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private JuryMapper juryMapper;

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
	void findAll_returnsJuries() throws Exception {
		Defense defense = mock(Defense.class);
		when(defense.getId()).thenReturn(1L);
		when(defenseService.getSchedule(0, 10)).thenReturn(new PaginatedResponse<>(List.of(defense), 1, 1, 0, 10));
		when(juryMapper.toDto(defense)).thenReturn(new JuryResponse(1L, 1L, "Projet Test", "Soutenance", List.of()));

		mockMvc.perform(get("/api/coordinator/juries")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items").isArray())
				.andExpect(jsonPath("$.data.items[0].projectTitle").value("Projet Test"));
	}

	@Test
	void create_returnsCreated() throws Exception {
		CreateJuryRequest.MemberEntry member = new CreateJuryRequest.MemberEntry(1L, "président");
		CreateJuryRequest request = new CreateJuryRequest(1L, List.of(member));
		Defense defense = mock(Defense.class);
		when(defense.getId()).thenReturn(1L);
		when(defenseService.createJury(any())).thenReturn(defense);
		when(juryMapper.toDto(defense)).thenReturn(new JuryResponse(1L, 1L, "Projet Test", "Soutenance", List.of()));

		mockMvc.perform(post("/api/coordinator/juries").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.projectId").value(1L));
	}

	@Test
	void update_returnsJury() throws Exception {
		UpdateJuryRequest updates = new UpdateJuryRequest(2L, List.of());
		Defense defense = mock(Defense.class);
		when(defense.getId()).thenReturn(1L);
		when(defenseService.updateJury(eq(1L), any())).thenReturn(defense);
		when(juryMapper.toDto(defense)).thenReturn(new JuryResponse(1L, 2L, "Projet Test", "Soutenance", List.of()));

		mockMvc.perform(put("/api/coordinator/juries/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updates))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.projectId").value(2L));
	}

	@Test
	void delete_returns200() throws Exception {
		Defense defense = mock(Defense.class);
		when(defense.getId()).thenReturn(1L);
		when(defenseService.clearJuryMembers(1L)).thenReturn(defense);

		mockMvc.perform(delete("/api/coordinator/juries/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}
}
