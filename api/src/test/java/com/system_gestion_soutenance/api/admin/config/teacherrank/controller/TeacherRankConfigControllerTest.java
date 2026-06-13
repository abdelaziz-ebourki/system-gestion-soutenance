package com.system_gestion_soutenance.api.admin.config.teacherrank.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.admin.config.teacherrank.entity.TeacherRank;
import com.system_gestion_soutenance.api.admin.config.teacherrank.service.TeacherRankConfigService;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TeacherRankConfigController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class TeacherRankConfigControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private TeacherRankConfigService teacherRankConfigService;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private com.system_gestion_soutenance.api.common.mapper.ConfigMapper configMapper;

	@Test
	void findAll_returnsList() throws Exception {
		TeacherRank teacherRank = new TeacherRank(1L, "Prof");
		when(teacherRankConfigService.findAll(0, 10))
				.thenReturn(new PaginatedResponse<>(List.of(teacherRank), 1, 1, 0, 10));
		when(configMapper.toTeacherRankDto(teacherRank)).thenReturn(
				new com.system_gestion_soutenance.api.admin.config.teacherrank.dto.TeacherRankDto(1L, "Prof"));
		mockMvc.perform(get("/api/admin/config/teacher-ranks")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items[0].name").value("Prof"));
	}

	@Test
	void create_returns201() throws Exception {
		TeacherRank teacherRank = new TeacherRank(1L, "Prof");
		when(teacherRankConfigService.create(any())).thenReturn(teacherRank);
		when(configMapper.toTeacherRankDto(teacherRank)).thenReturn(
				new com.system_gestion_soutenance.api.admin.config.teacherrank.dto.TeacherRankDto(1L, "Prof"));
		mockMvc.perform(post("/api/admin/config/teacher-ranks").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Prof\"}")).andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.name").value("Prof"));
	}

	@Test
	void update_returns200() throws Exception {
		TeacherRank teacherRank = new TeacherRank(1L, "Updated");
		when(teacherRankConfigService.update(anyLong(), any())).thenReturn(teacherRank);
		when(configMapper.toTeacherRankDto(teacherRank)).thenReturn(
				new com.system_gestion_soutenance.api.admin.config.teacherrank.dto.TeacherRankDto(1L, "Updated"));
		mockMvc.perform(put("/api/admin/config/teacher-ranks/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Updated\"}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("Updated"));
	}

	@Test
	void delete_returns200() throws Exception {
		doNothing().when(teacherRankConfigService).delete(1L);
		mockMvc.perform(delete("/api/admin/config/teacher-ranks/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void patch_returns200() throws Exception {
		TeacherRank teacherRank = new TeacherRank(1L, "Updated");
		when(teacherRankConfigService.updatePartial(anyLong(), any())).thenReturn(teacherRank);
		when(configMapper.toTeacherRankDto(teacherRank)).thenReturn(
				new com.system_gestion_soutenance.api.admin.config.teacherrank.dto.TeacherRankDto(1L, "Updated"));
		mockMvc.perform(patch("/api/admin/config/teacher-ranks/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Updated\"}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("Updated"));
	}
}
