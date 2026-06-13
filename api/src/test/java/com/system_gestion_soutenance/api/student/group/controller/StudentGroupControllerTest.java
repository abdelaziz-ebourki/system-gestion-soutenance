package com.system_gestion_soutenance.api.student.group.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.mapper.StudentGroupMapper;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = StudentGroupController.class)
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
	private StudentGroupMapper studentGroupMapper;

	@BeforeEach
	void setUp() {
		// No more manual SecurityContextHolder setup
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getWorkspace_returns200() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setRole(com.system_gestion_soutenance.api.user.entity.Role.STUDENT);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
				List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")));
		when(studentGroupService.getWorkspace(1L))
				.thenReturn(new com.system_gestion_soutenance.api.student.group.dto.StudentGroupWorkspaceResponse(null,
						List.of(), null, null, true));
		mockMvc.perform(get("/api/student/groups").with(authentication(auth))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.isGroupCreationOpen").value(true));
	}

	@Test
	void createGroup_returns201() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setRole(com.system_gestion_soutenance.api.user.entity.Role.STUDENT);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
				List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")));
		Group group = new Group();
		group.setId(1L);
		group.setGroupName("Groupe de Alice");
		when(studentGroupService.createGroup(1L)).thenReturn(group);
		when(studentGroupMapper.toDetails(group, 1L))
				.thenReturn(new GroupDetailsResponse(1L, "Groupe de Alice", null, null, List.of()));
		mockMvc.perform(post("/api/student/groups").with(authentication(auth)).with(csrf()))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.data.groupName").value("Groupe de Alice"));
	}

	@Test
	void joinGroup_returns200() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setRole(com.system_gestion_soutenance.api.user.entity.Role.STUDENT);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
				List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")));
		Group group = new Group();
		group.setId(1L);
		group.setGroupName("Groupe Test");
		when(studentGroupService.joinGroup(anyLong(), eq(1L))).thenReturn(group);
		when(studentGroupMapper.toDetails(group, 1L))
				.thenReturn(new GroupDetailsResponse(1L, "Groupe Test", null, null, List.of()));
		mockMvc.perform(post("/api/student/groups/10/members").with(authentication(auth)).with(csrf()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.data.groupName").value("Groupe Test"));
	}

	@Test
	void leaveGroup_returns200() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setRole(com.system_gestion_soutenance.api.user.entity.Role.STUDENT);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
				List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")));
		doNothing().when(studentGroupService).leaveGroup(1L);

		mockMvc.perform(delete("/api/student/groups/leave").with(authentication(auth)).with(csrf()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
	}
}
