package com.system_gestion_soutenance.api.admin.config.level.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.level.service.LevelConfigService;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = LevelConfigController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class LevelConfigControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private LevelConfigService levelConfigService;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private com.system_gestion_soutenance.api.common.mapper.ConfigMapper configMapper;

	@Test
	void findAll_returnsList() throws Exception {
		when(levelConfigService.findAll()).thenReturn(List.of(new Level()));
		mockMvc.perform(get("/api/admin/config/levels")).andExpect(status().isOk());
	}

	@Test
	void create_returns201() throws Exception {
		when(levelConfigService.create(any())).thenReturn(new Level());
		mockMvc.perform(
				post("/api/admin/config/levels").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"S6\"}"))
				.andExpect(status().isCreated());
	}

	@Test
	void update_returns200() throws Exception {
		when(levelConfigService.update(anyLong(), any())).thenReturn(new Level());
		mockMvc.perform(
				put("/api/admin/config/levels/1").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"S7\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void delete_returns204() throws Exception {
		doNothing().when(levelConfigService).delete(1L);
		mockMvc.perform(delete("/api/admin/config/levels/1")).andExpect(status().isNoContent());
	}
}
