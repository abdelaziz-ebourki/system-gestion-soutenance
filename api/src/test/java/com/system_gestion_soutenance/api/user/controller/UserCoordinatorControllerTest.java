package com.system_gestion_soutenance.api.user.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.UserMapper;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import com.system_gestion_soutenance.api.user.service.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserCoordinatorController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class UserCoordinatorControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private UserMapper userMapper;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private UserRepository userRepository;

	@Test
	void listUsers_withTeacherRole_returnsPaginatedResponse() throws Exception {
		when(userService.listUsers(eq("teacher"), anyInt(), anyInt(), any())).thenReturn(Page.empty());

		mockMvc.perform(get("/api/coordinator/users").param("role", "teacher")).andExpect(status().isOk())
				.andExpect(jsonPath("$.items").isArray());

		verify(userService).listUsers(eq("teacher"), eq(0), eq(5000), isNull());
	}

	@Test
	void listUsers_withTeacherRoleAndSearch_passesSearchParam() throws Exception {
		when(userService.listUsers(eq("teacher"), anyInt(), anyInt(), any())).thenReturn(Page.empty());

		mockMvc.perform(get("/api/coordinator/users").param("role", "teacher").param("search", "ali"))
				.andExpect(status().isOk());

		verify(userService).listUsers(eq("teacher"), eq(0), eq(5000), eq("ali"));
	}

	@Test
	void listUsers_withStudentRole_returnsPaginatedResponse() throws Exception {
		when(userService.listUsers(eq("student"), anyInt(), anyInt(), any())).thenReturn(Page.empty());

		mockMvc.perform(get("/api/coordinator/users").param("role", "student")).andExpect(status().isOk())
				.andExpect(jsonPath("$.items").isArray());

		verify(userService).listUsers(eq("student"), eq(0), eq(5000), isNull());
	}
}
