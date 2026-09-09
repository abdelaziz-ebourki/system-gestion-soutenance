package com.system_gestion_soutenance.api.teacher.project.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.mapper.ProjectMapper;
import com.system_gestion_soutenance.api.coordinator.project.dto.ProjectResponse;
import com.system_gestion_soutenance.api.coordinator.project.dto.TeacherProposeProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.service.ProjectService;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TeacherProjectController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class TeacherProjectControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

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
	void propose_createsProject() throws Exception {
		TeacherProposeProjectRequest request = new TeacherProposeProjectRequest("Projet IA", "Description IA", "PFE",
				4);

		Project project = new Project();
		project.setId(1L);
		project.setTitle("Projet IA");

		when(projectService.proposeByTeacher(any())).thenReturn(project);
		when(projectMapper.toDto(project, Map.of(), Map.of())).thenReturn(
				new ProjectResponse(1L, "Projet IA", "Description IA", "PFE", 4, "PENDING", null, null, null, null));

		mockMvc.perform(post("/api/teacher/projects").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.title").value("Projet IA"));
	}

	@Test
	void propose_withBlankTitle_returnsBadRequest() throws Exception {
		TeacherProposeProjectRequest request = new TeacherProposeProjectRequest("", "Description", "PFE", 4);

		mockMvc.perform(post("/api/teacher/projects").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
	}

	@Test
	void propose_withNullDefenseType_returnsBadRequest() throws Exception {
		TeacherProposeProjectRequest request = new TeacherProposeProjectRequest("Projet", "Description", null, 4);

		mockMvc.perform(post("/api/teacher/projects").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
	}
}
