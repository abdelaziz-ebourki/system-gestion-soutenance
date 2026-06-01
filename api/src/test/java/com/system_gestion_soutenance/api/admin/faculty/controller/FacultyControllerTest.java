package com.system_gestion_soutenance.api.admin.faculty.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.admin.faculty.service.FacultyService;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = FacultyController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class FacultyControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private FacultyService facultyService;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private com.system_gestion_soutenance.api.common.mapper.ConfigMapper configMapper;

	private static Faculty createFaculty(Long id, String name, String code) {
		Faculty f = new Faculty();
		f.setId(id);
		f.setName(name);
		f.setCode(code);
		return f;
	}

	@Test
	void findAll_returnsList() throws Exception {
		Faculty faculty = createFaculty(1L, "FS", "FS");
		when(facultyService.findAll()).thenReturn(List.of(faculty));
		when(configMapper.toFacultyDto(faculty)).thenReturn(
				new com.system_gestion_soutenance.api.admin.faculty.dto.FacultyDto(1L, "FS", "FS", null, null));
		mockMvc.perform(get("/api/admin/faculties")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("FS"));
	}

	@Test
	void findById_returns200() throws Exception {
		Faculty faculty = createFaculty(1L, "FS", "FS");
		when(facultyService.findById(1L)).thenReturn(faculty);
		when(configMapper.toFacultyDto(faculty)).thenReturn(
				new com.system_gestion_soutenance.api.admin.faculty.dto.FacultyDto(1L, "FS", "FS", null, null));
		mockMvc.perform(get("/api/admin/faculties/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("FS"));
	}

	@Test
	void create_returns201() throws Exception {
		Faculty faculty = createFaculty(1L, "FS", "FS");
		when(facultyService.create(any())).thenReturn(faculty);
		when(configMapper.toFacultyDto(faculty)).thenReturn(
				new com.system_gestion_soutenance.api.admin.faculty.dto.FacultyDto(1L, "FS", "FS", null, null));
		mockMvc.perform(post("/api/admin/faculties").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"FS\",\"code\":\"FS\"}")).andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("FS"));
	}

	@Test
	void update_returns200() throws Exception {
		Faculty faculty = createFaculty(1L, "New", "N");
		when(facultyService.update(anyLong(), any())).thenReturn(faculty);
		when(configMapper.toFacultyDto(faculty)).thenReturn(
				new com.system_gestion_soutenance.api.admin.faculty.dto.FacultyDto(1L, "New", "N", null, null));
		mockMvc.perform(put("/api/admin/faculties/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"New\",\"code\":\"N\"}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("New"));
	}

	@Test
	void delete_returns204() throws Exception {
		doNothing().when(facultyService).delete(1L);
		mockMvc.perform(delete("/api/admin/faculties/1")).andExpect(status().isNoContent());
	}
}
