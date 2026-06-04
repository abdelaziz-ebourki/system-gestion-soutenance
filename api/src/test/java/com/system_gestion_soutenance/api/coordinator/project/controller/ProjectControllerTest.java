package com.system_gestion_soutenance.api.coordinator.project.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.coordinator.project.dto.CreateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.dto.ProjectResponse;
import com.system_gestion_soutenance.api.coordinator.project.dto.UpdateProjectRequest;
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

@WebMvcTest(controllers = ProjectController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class ProjectControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ProjectService projectService;

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
	void findAll_returnsProjects() throws Exception {
		ProjectResponse response = new ProjectResponse(1L, "Projet Test", "Desc", "PFE", 1L, "Supervisor", List.of());
		when(projectService.findAll()).thenReturn(List.of(response));

		mockMvc.perform(get("/api/coordinator/projects")).andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(1)).andExpect(jsonPath("$[0].title").value("Projet Test"));
	}

	@Test
	void create_returnsCreated() throws Exception {
		CreateProjectRequest request = new CreateProjectRequest("New Project", "Description", 1L, "PFE", List.of(1L));
		ProjectResponse response = new ProjectResponse(1L, "New Project", "Desc", "PFE", 1L, "Supervisor", List.of());
		when(projectService.create(any())).thenReturn(response);

		mockMvc.perform(post("/api/coordinator/projects").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("New Project"));
	}

	@Test
	void update_returnsProject() throws Exception {
		UpdateProjectRequest updates = new UpdateProjectRequest("Updated Project", "Desc", "PFE");
		ProjectResponse response = new ProjectResponse(1L, "Updated Project", "Desc", "PFE", 1L, "Supervisor",
				List.of());
		when(projectService.update(eq(1L), any())).thenReturn(response);

		mockMvc.perform(put("/api/coordinator/projects/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updates))).andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Updated Project"));
	}

	@Test
	void delete_returnsNoContent() throws Exception {
		doNothing().when(projectService).delete(1L);

		mockMvc.perform(delete("/api/coordinator/projects/1")).andExpect(status().isNoContent());
	}
}
