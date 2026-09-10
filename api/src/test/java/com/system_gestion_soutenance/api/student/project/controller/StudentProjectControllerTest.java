package com.system_gestion_soutenance.api.student.project.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.mapper.ProjectMapper;
import com.system_gestion_soutenance.api.coordinator.project.dto.ProjectResponse;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus;
import com.system_gestion_soutenance.api.coordinator.project.service.ProjectService;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = StudentProjectController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class StudentProjectControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProjectService projectService;

	@MockitoBean
	private ProjectMapper projectMapper;

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
	void browse_returnsProjects() throws Exception {
		Project project = new Project();
		project.setId(1L);
		project.setTitle("Projet IA");

		when(projectService.findByStatus(ProjectStatus.PENDING)).thenReturn(List.of(project));
		when(projectService.buildProjectGroupIdMap(List.of(project))).thenReturn(Map.of());
		when(projectService.buildProjectStudentNamesMap(List.of(project))).thenReturn(Map.of());
		when(projectMapper.toDto(project, Map.of(), Map.of())).thenReturn(
				new ProjectResponse(1L, "Projet IA", "Description", "PFE", 4, "PENDING", null, null, null, null));

		mockMvc.perform(get("/api/student/projects")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].id").value(1L))
				.andExpect(jsonPath("$.data[0].title").value("Projet IA"));
	}

	@Test
	void browse_withCustomStatus_returnsFilteredProjects() throws Exception {
		when(projectService.findByStatus(ProjectStatus.APPROVED)).thenReturn(List.of());
		when(projectService.buildProjectGroupIdMap(List.of())).thenReturn(Map.of());
		when(projectService.buildProjectStudentNamesMap(List.of())).thenReturn(Map.of());

		mockMvc.perform(get("/api/student/projects?status=APPROVED")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.size()").value(0));
	}

	@Test
	void browse_withInvalidStatus_returnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/student/projects?status=INVALID")).andExpect(status().isBadRequest());
	}
}
