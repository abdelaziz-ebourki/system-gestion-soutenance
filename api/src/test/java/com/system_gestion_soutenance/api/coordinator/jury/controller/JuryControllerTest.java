package com.system_gestion_soutenance.api.coordinator.jury.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.coordinator.jury.dto.CreateJuryRequest;
import com.system_gestion_soutenance.api.coordinator.jury.dto.JuryResponse;
import com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest;
import com.system_gestion_soutenance.api.coordinator.jury.service.JuryService;
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

@WebMvcTest(controllers = JuryController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class JuryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private JuryService juryService;

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
	void findAll_returnsJuries() throws Exception {
		when(juryService.findAll())
				.thenReturn(List.of(new JuryResponse(1L, 1L, "Projet Test", "Soutenance", 1L, "Template", List.of())));

		mockMvc.perform(get("/api/coordinator/juries")).andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(1)).andExpect(jsonPath("$[0].projectTitle").value("Projet Test"));
	}

	@Test
	void create_returnsCreated() throws Exception {
		CreateJuryRequest.MemberEntry member = new CreateJuryRequest.MemberEntry(1L, "président");
		CreateJuryRequest request = new CreateJuryRequest(1L, 1L, List.of(member));
		JuryResponse response = new JuryResponse(1L, 1L, "Projet Test", "Soutenance", 1L, "Template", List.of());
		when(juryService.create(any())).thenReturn(response);

		mockMvc.perform(post("/api/coordinator/juries").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.projectId").value(1L));
	}

	@Test
	void update_returnsJury() throws Exception {
		UpdateJuryRequest updates = new UpdateJuryRequest(2L, 1L, List.of());
		JuryResponse response = new JuryResponse(1L, 2L, "Projet Test", "Soutenance", 1L, "Template", List.of());
		when(juryService.update(eq(1L), any())).thenReturn(response);

		mockMvc.perform(put("/api/coordinator/juries/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updates))).andExpect(status().isOk())
				.andExpect(jsonPath("$.projectId").value(2L));
	}

	@Test
	void delete_returnsNoContent() throws Exception {
		doNothing().when(juryService).delete(1L);

		mockMvc.perform(delete("/api/coordinator/juries/1")).andExpect(status().isNoContent());
	}
}
