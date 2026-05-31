package com.system_gestion_soutenance.api.coordinator.unavailability.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.coordinator.unavailability.entity.Unavailability;
import com.system_gestion_soutenance.api.coordinator.unavailability.repository.UnavailabilityRepository;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UnavailabilityController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class UnavailabilityControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UnavailabilityRepository repository;

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
	void findAll_returnsUnavailabilityRecords() throws Exception {
		Unavailability record = new Unavailability(1L, 10L, "2025-06-01", List.of("08:00", "10:00"));
		when(repository.findAll()).thenReturn(List.of(record));

		mockMvc.perform(get("/api/coordinator/unavailability")).andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(1)).andExpect(jsonPath("$[0].teacherId").value(10L))
				.andExpect(jsonPath("$[0].date").value("2025-06-01"));
	}

	@Test
	void findAll_noRecords_returnsEmptyList() throws Exception {
		when(repository.findAll()).thenReturn(List.of());

		mockMvc.perform(get("/api/coordinator/unavailability")).andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(0));
	}
}
