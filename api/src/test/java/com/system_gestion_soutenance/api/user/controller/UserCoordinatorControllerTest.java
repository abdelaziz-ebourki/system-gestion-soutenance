package com.system_gestion_soutenance.api.user.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import com.system_gestion_soutenance.api.user.service.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
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
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private com.system_gestion_soutenance.api.user.service.UserCacheService userCacheService;

	@Test
	void listTeachers_returnsPaginatedResponse() throws Exception {
		when(userService.listUsers(eq("teacher"), anyInt(), anyInt(), any()))
				.thenReturn(new PaginatedResponse<>(List.of(), 0, 0, 0, 5000));

		mockMvc.perform(get("/api/coordinator/teachers")).andExpect(status().isOk())
				.andExpect(jsonPath("$.items").isArray());

		verify(userService).listUsers(eq("teacher"), eq(0), eq(5000), isNull());
	}

	@Test
	void listTeachers_withSearch_passesSearchParam() throws Exception {
		when(userService.listUsers(eq("teacher"), anyInt(), anyInt(), any()))
				.thenReturn(new PaginatedResponse<>(List.of(), 0, 0, 0, 5000));

		mockMvc.perform(get("/api/coordinator/teachers").param("search", "ali")).andExpect(status().isOk());

		verify(userService).listUsers(eq("teacher"), eq(0), eq(5000), eq("ali"));
	}

	@Test
	void listStudents_returnsPaginatedResponse() throws Exception {
		when(userService.listUsers(eq("student"), anyInt(), anyInt(), any()))
				.thenReturn(new PaginatedResponse<>(List.of(), 0, 0, 0, 5000));

		mockMvc.perform(get("/api/coordinator/students")).andExpect(status().isOk())
				.andExpect(jsonPath("$.items").isArray());

		verify(userService).listUsers(eq("student"), eq(0), eq(5000), isNull());
	}
}
