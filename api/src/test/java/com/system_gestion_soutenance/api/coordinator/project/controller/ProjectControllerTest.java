package com.system_gestion_soutenance.api.coordinator.project.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.ProjectMapper;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.coordinator.project.dto.CreateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.dto.ProjectResponse;
import com.system_gestion_soutenance.api.coordinator.project.dto.ProjectStatusUpdateRequest;
import com.system_gestion_soutenance.api.coordinator.project.dto.UpdateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus;
import com.system_gestion_soutenance.api.coordinator.project.service.ProjectService;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.Collections;
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
	@SuppressWarnings("unchecked")
	void findAll_returnsProjects() throws Exception {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Projet Test");

		ProjectResponse dto = new ProjectResponse(1L, "Projet Test", "Desc", "PFE", "PENDING", 1L, "Supervisor",
				List.of());

		when(projectService.findAll(0, 10)).thenReturn(new PaginatedResponse<>(List.of(project), 1, 1, 0, 10));
		when(projectService.buildProjectGroupIdMap(anyList())).thenReturn(Map.of(1L, 1L));
		when(projectMapper.toDto(project, Map.of(1L, 1L))).thenReturn(dto);

		mockMvc.perform(get("/api/coordinator/projects")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items").isArray())
				.andExpect(jsonPath("$.data.items[0].title").value("Projet Test"));
	}

	@Test
	void create_returnsCreated() throws Exception {
		CreateProjectRequest request = new CreateProjectRequest("New Project", "Description", 1L, "PFE", List.of(1L));
		Project project = mock(Project.class);
		ProjectResponse dto = new ProjectResponse(1L, "New Project", "Desc", "PFE", "PENDING", 1L, "Supervisor",
				List.of());

		when(projectService.create(any())).thenReturn(project);
		when(projectMapper.toDto(project, Collections.emptyMap())).thenReturn(dto);

		mockMvc.perform(post("/api/coordinator/projects").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.title").value("New Project"));
	}

	@Test
	void update_returnsProject() throws Exception {
		UpdateProjectRequest updates = new UpdateProjectRequest("Updated Project", "Desc", "PFE");
		Project project = mock(Project.class);
		ProjectResponse dto = new ProjectResponse(1L, "Updated Project", "Desc", "PFE", "PENDING", 1L, "Supervisor",
				List.of());

		when(projectService.update(eq(1L), any())).thenReturn(project);
		when(projectMapper.toDto(project, Collections.emptyMap())).thenReturn(dto);

		mockMvc.perform(put("/api/coordinator/projects/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updates))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.title").value("Updated Project"));
	}

	@Test
	void delete_returns200() throws Exception {
		doNothing().when(projectService).delete(1L);

		mockMvc.perform(delete("/api/coordinator/projects/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void updateStatus_approve_returns200() throws Exception {
		Project project = mock(Project.class);
		ProjectResponse dto = new ProjectResponse(1L, "Projet Test", "Desc", "PFE", "APPROVED", 1L, "Supervisor",
				List.of());

		when(projectService.updateStatus(eq(1L), eq(ProjectStatus.APPROVED))).thenReturn(project);
		when(projectMapper.toDto(project, Collections.emptyMap())).thenReturn(dto);

		ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest(ProjectStatus.APPROVED);

		mockMvc.perform(patch("/api/coordinator/projects/1/status").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("APPROVED"));
	}

	@Test
	void updateStatus_reject_returns200() throws Exception {
		Project project = mock(Project.class);
		ProjectResponse dto = new ProjectResponse(1L, "Projet Test", "Desc", "PFE", "REJECTED", 1L, "Supervisor",
				List.of());

		when(projectService.updateStatus(eq(1L), eq(ProjectStatus.REJECTED))).thenReturn(project);
		when(projectMapper.toDto(project, Collections.emptyMap())).thenReturn(dto);

		ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest(ProjectStatus.REJECTED);

		mockMvc.perform(patch("/api/coordinator/projects/1/status").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("REJECTED"));
	}

	@Test
	void updateStatus_invalidTransition_returns400() throws Exception {
		when(projectService.updateStatus(eq(1L), eq(ProjectStatus.APPROVED)))
				.thenThrow(new InvalidBusinessStateException("Transition invalide"));

		ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest(ProjectStatus.APPROVED);

		mockMvc.perform(patch("/api/coordinator/projects/1/status").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}
}
