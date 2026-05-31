package com.system_gestion_soutenance.api.admin.config.grade.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.admin.config.grade.entity.Grade;
import com.system_gestion_soutenance.api.admin.config.grade.service.GradeConfigService;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GradeConfigController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class GradeConfigControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private GradeConfigService gradeConfigService;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;

	@Test
	void findAll_returnsList() throws Exception {
		when(gradeConfigService.findAll()).thenReturn(List.of(new Grade(1L, "Prof")));
		mockMvc.perform(get("/api/admin/config/grades")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Prof"));
	}

	@Test
	void create_returns201() throws Exception {
		when(gradeConfigService.create(any())).thenReturn(new Grade(1L, "Prof"));
		mockMvc.perform(
				post("/api/admin/config/grades").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Prof\"}"))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Prof"));
	}

	@Test
	void update_returns200() throws Exception {
		when(gradeConfigService.update(anyLong(), any())).thenReturn(new Grade(1L, "Updated"));
		mockMvc.perform(put("/api/admin/config/grades/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Updated\"}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Updated"));
	}

	@Test
	void delete_returns204() throws Exception {
		doNothing().when(gradeConfigService).delete(1L);
		mockMvc.perform(delete("/api/admin/config/grades/1")).andExpect(status().isNoContent());
	}
}
