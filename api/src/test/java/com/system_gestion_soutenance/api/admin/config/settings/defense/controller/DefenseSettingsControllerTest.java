package com.system_gestion_soutenance.api.admin.config.settings.defense.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.admin.config.settings.defense.entity.DefenseSettings;
import com.system_gestion_soutenance.api.admin.config.settings.defense.service.DefenseSettingsService;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = DefenseSettingsController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class DefenseSettingsControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private DefenseSettingsService service;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;

	@Test
	void get_returns200() throws Exception {
		when(service.get()).thenReturn(new DefenseSettings(1L, "08:00", "18:00", 30, 15, null, null));
		mockMvc.perform(get("/api/admin/config/settings")).andExpect(status().isOk())
				.andExpect(jsonPath("$.startTime").value("08:00"));
	}

	@Test
	void update_returns200() throws Exception {
		when(service.update(any())).thenReturn(new DefenseSettings(1L, "09:00", "17:00", 45, 10, null, null));
		mockMvc.perform(put("/api/admin/config/settings").contentType(MediaType.APPLICATION_JSON)
				.content("{\"startTime\":\"09:00\",\"endTime\":\"17:00\",\"defenseDuration\":45,\"breakDuration\":10}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.startTime").value("09:00"));
	}
}
