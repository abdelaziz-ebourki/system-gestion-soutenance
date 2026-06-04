package com.system_gestion_soutenance.api.coordinator.group.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.coordinator.group.dto.CreateGroupRequest;
import com.system_gestion_soutenance.api.coordinator.group.service.GroupService;
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

@WebMvcTest(controllers = GroupController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class GroupControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private GroupService groupService;

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
	void findAll_returnsGroups() throws Exception {
		when(groupService.findAll())
				.thenReturn(List.of(new com.system_gestion_soutenance.api.coordinator.group.dto.GroupResponse(1L,
						"Groupe A", 1L, 2, List.of())));

		mockMvc.perform(get("/api/coordinator/groups")).andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(1)).andExpect(jsonPath("$[0].groupName").value("Groupe A"));
	}

	@Test
	void create_returnsCreated() throws Exception {
		CreateGroupRequest request = new CreateGroupRequest("Groupe A", 1L, List.of(1L, 2L), null);
		when(groupService.create(any()))
				.thenReturn(new com.system_gestion_soutenance.api.coordinator.group.dto.GroupResponse(1L, "Groupe A",
						1L, 2, List.of()));

		mockMvc.perform(post("/api/coordinator/groups").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.groupName").value("Groupe A"));
	}

	@Test
	void delete_returnsNoContent() throws Exception {
		doNothing().when(groupService).delete(1L);

		mockMvc.perform(delete("/api/coordinator/groups/1")).andExpect(status().isNoContent());
	}
}
