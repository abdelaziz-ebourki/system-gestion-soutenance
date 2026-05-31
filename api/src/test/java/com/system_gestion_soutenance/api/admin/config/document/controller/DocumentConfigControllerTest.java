package com.system_gestion_soutenance.api.admin.config.document.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.admin.config.document.entity.DocumentConfig;
import com.system_gestion_soutenance.api.admin.config.document.service.DocumentConfigService;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = DocumentConfigController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class DocumentConfigControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private DocumentConfigService service;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;

	@Test
	void get_returns200() throws Exception {
		when(service.get()).thenReturn(new DocumentConfig(1L, 10, "pdf", 5));
		mockMvc.perform(get("/api/admin/config/documents")).andExpect(status().isOk())
				.andExpect(jsonPath("$.maxFileSizeMb").value(10));
	}

	@Test
	void update_returns200() throws Exception {
		when(service.update(any())).thenReturn(new DocumentConfig(1L, 20, "pdf,doc", 3));
		mockMvc.perform(put("/api/admin/config/documents").contentType(MediaType.APPLICATION_JSON)
				.content("{\"maxFileSizeMb\":20,\"allowedExtensions\":\"pdf,doc\",\"versionLimit\":3}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.maxFileSizeMb").value(20));
	}
}
