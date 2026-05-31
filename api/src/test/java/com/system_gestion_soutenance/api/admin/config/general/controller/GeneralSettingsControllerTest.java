package com.system_gestion_soutenance.api.admin.config.general.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.admin.config.general.dto.UpdateGeneralSettingsRequest;
import com.system_gestion_soutenance.api.admin.config.general.entity.GeneralSettings;
import com.system_gestion_soutenance.api.admin.config.general.service.GeneralSettingsService;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GeneralSettingsController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class GeneralSettingsControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private GeneralSettingsService service;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private UserRepository userRepository;

	@Test
	void get_returnsSettings() throws Exception {
		GeneralSettings settings = new GeneralSettings();
		settings.setInstitutionName("Test Univ");
		when(service.get()).thenReturn(settings);

		mockMvc.perform(get("/api/admin/config/general")).andExpect(status().isOk())
				.andExpect(jsonPath("$.institutionName").value("Test Univ"));
	}

	@Test
	void update_returnsUpdatedSettings() throws Exception {
		GeneralSettings updated = new GeneralSettings();
		updated.setInstitutionName("Updated Univ");
		when(service.update(any(UpdateGeneralSettingsRequest.class))).thenReturn(updated);

		mockMvc.perform(put("/api/admin/config/general").contentType(MediaType.APPLICATION_JSON).content("""
				{"institutionName":"Updated Univ"}
				""")).andExpect(status().isOk()).andExpect(jsonPath("$.institutionName").value("Updated Univ"));
	}

	@Test
	void update_passesRequestToService() throws Exception {
		mockMvc.perform(put("/api/admin/config/general").contentType(MediaType.APPLICATION_JSON).content("""
				{"institutionName":"Univ","setupCompleted":true}
				""")).andExpect(status().isOk());

		verify(service).update(argThat(req -> req.institutionName().equals("Univ") && req.setupCompleted()));
	}
}
