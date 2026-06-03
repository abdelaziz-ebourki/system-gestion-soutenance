package com.system_gestion_soutenance.api.student.group.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.student.group.service.StudentGroupService;
import com.system_gestion_soutenance.api.user.entity.User;
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

	@BeforeEach
	void setUp() {
		User user = new User();
		user.setId(1L);
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getWorkspace_returns200() throws Exception {
		when(studentGroupService.getWorkspace(1L)).thenReturn(Map.of("documents", 3));
		mockMvc.perform(get("/api/student/group")).andExpect(status().isOk())
				.andExpect(jsonPath("$.documents").value(3));
	}

	@Test
	void createGroup_returns201() throws Exception {
		when(studentGroupService.createGroup(1L)).thenReturn(Map.of("groupName", "Groupe de Alice"));
		mockMvc.perform(post("/api/student/group")).andExpect(status().isCreated())
				.andExpect(jsonPath("$.groupName").value("Groupe de Alice"));
	}

	@Test
	void joinGroup_returns200() throws Exception {
		when(studentGroupService.joinGroup(anyLong(), eq(1L))).thenReturn(Map.of("groupName", "Groupe Test"));
		mockMvc.perform(post("/api/student/group/10/join")).andExpect(status().isOk())
				.andExpect(jsonPath("$.groupName").value("Groupe Test"));
	}
}
