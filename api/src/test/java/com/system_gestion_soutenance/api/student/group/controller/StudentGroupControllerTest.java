package com.system_gestion_soutenance.api.student.group.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.mapper.StudentGroupMapper;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.student.group.dto.GroupDetailsResponse;
import com.system_gestion_soutenance.api.student.group.service.StudentGroupService;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = StudentGroupController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class StudentGroupControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private StudentGroupService studentGroupService;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private SecurityService securityService;
	@MockitoBean
	private StudentGroupMapper studentGroupMapper;

	@BeforeEach
	void setUp() {
		User user = new User();
		user.setId(1L);
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
		when(securityService.getCurrentUserId()).thenReturn(1L);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getWorkspace_returns200() throws Exception {
		when(studentGroupService.getWorkspace(1L))
				.thenReturn(new com.system_gestion_soutenance.api.student.group.dto.StudentGroupWorkspaceResponse(null,
						List.of(), null, null, true));
		mockMvc.perform(get("/api/student/group")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.isGroupCreationOpen").value(true));
	}

	@Test
	void createGroup_returns201() throws Exception {
		Group group = new Group();
		group.setId(1L);
		group.setGroupName("Groupe de Alice");
		when(studentGroupService.createGroup(1L)).thenReturn(group);
		when(studentGroupMapper.toDetails(group, 1L))
				.thenReturn(new GroupDetailsResponse(1L, "Groupe de Alice", null, null, List.of()));
		mockMvc.perform(post("/api/student/group")).andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.groupName").value("Groupe de Alice"));
	}

	@Test
	void joinGroup_returns200() throws Exception {
		Group group = new Group();
		group.setId(1L);
		group.setGroupName("Groupe Test");
		when(studentGroupService.joinGroup(anyLong(), eq(1L))).thenReturn(group);
		when(studentGroupMapper.toDetails(group, 1L))
				.thenReturn(new GroupDetailsResponse(1L, "Groupe Test", null, null, List.of()));
		mockMvc.perform(post("/api/student/group/10/join")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.groupName").value("Groupe Test"));
	}
}
