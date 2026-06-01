package com.system_gestion_soutenance.api.admin.config.major.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.service.MajorConfigService;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = MajorConfigController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class MajorConfigControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private MajorConfigService majorConfigService;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private com.system_gestion_soutenance.api.common.mapper.ConfigMapper configMapper;

	@Test
	void findAll_returnsList() throws Exception {
		when(majorConfigService.findAll()).thenReturn(List.of(new Major()));
		mockMvc.perform(get("/api/admin/config/majors")).andExpect(status().isOk());
	}

	@Test
	void create_returns201() throws Exception {
		when(majorConfigService.create(any())).thenReturn(new Major());
		mockMvc.perform(
				post("/api/admin/config/majors").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"GL\"}"))
				.andExpect(status().isCreated());
	}

	@Test
	void update_returns200() throws Exception {
		when(majorConfigService.update(anyLong(), any())).thenReturn(new Major());
		mockMvc.perform(
				put("/api/admin/config/majors/1").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"IIR\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void delete_returns204() throws Exception {
		doNothing().when(majorConfigService).delete(1L);
		mockMvc.perform(delete("/api/admin/config/majors/1")).andExpect(status().isNoContent());
	}
}
