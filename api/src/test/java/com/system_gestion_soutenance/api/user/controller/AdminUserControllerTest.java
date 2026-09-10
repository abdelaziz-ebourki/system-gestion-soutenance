package com.system_gestion_soutenance.api.user.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.mapper.UserMapper;
import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import com.system_gestion_soutenance.api.user.service.UserService;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AdminUserController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class AdminUserControllerTest {

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
	void listStudents_returnsPagedStudents() throws Exception {
		com.system_gestion_soutenance.api.user.entity.User user = new com.system_gestion_soutenance.api.user.entity.User();
		Page<com.system_gestion_soutenance.api.user.entity.User> page = new PageImpl<>(List.of(user));

		when(userService.listUsers("STUDENT", 0, 10, null)).thenReturn(page);
		when(userMapper.toDto(user)).thenReturn(new UserDto(1L, "alice@test.com", "STUDENT", "Martin", "Alice", true,
				null, null, null, null, null, null, null, null, null, null));

		mockMvc.perform(get("/api/admin/students")).andExpect(status().isOk())
				.andExpect(jsonPath("$.items.size()").value(1)).andExpect(jsonPath("$.items[0].role").value("STUDENT"));
	}

	@Test
	void listTeachers_returnsPagedTeachers() throws Exception {
		com.system_gestion_soutenance.api.user.entity.User user = new com.system_gestion_soutenance.api.user.entity.User();
		Page<com.system_gestion_soutenance.api.user.entity.User> page = new PageImpl<>(List.of(user));

		when(userService.listUsers("TEACHER", 0, 10, null)).thenReturn(page);
		when(userMapper.toDto(user)).thenReturn(new UserDto(2L, "bob@test.com", "TEACHER", "Durand", "Bob", true, null,
				null, null, null, null, null, null, null, null, null));

		mockMvc.perform(get("/api/admin/teachers")).andExpect(status().isOk())
				.andExpect(jsonPath("$.items.size()").value(1)).andExpect(jsonPath("$.items[0].role").value("TEACHER"));
	}

	@Test
	void listCoordinators_returnsPagedCoordinators() throws Exception {
		com.system_gestion_soutenance.api.user.entity.User user = new com.system_gestion_soutenance.api.user.entity.User();
		Page<com.system_gestion_soutenance.api.user.entity.User> page = new PageImpl<>(List.of(user));

		when(userService.listUsers("COORDINATOR", 0, 10, null)).thenReturn(page);
		when(userMapper.toDto(user)).thenReturn(new UserDto(3L, "coord@test.com", "COORDINATOR", "Admin", "Charles",
				true, null, null, null, null, null, null, null, null, null, null));

		mockMvc.perform(get("/api/admin/coordinators")).andExpect(status().isOk())
				.andExpect(jsonPath("$.items.size()").value(1))
				.andExpect(jsonPath("$.items[0].role").value("COORDINATOR"));
	}

	@Test
	void listStudents_withSearch_returnsFiltered() throws Exception {
		when(userService.listUsers("STUDENT", 0, 10, "alice")).thenReturn(Page.empty());

		mockMvc.perform(get("/api/admin/students?search=alice")).andExpect(status().isOk())
				.andExpect(jsonPath("$.items.size()").value(0));
	}

	@Test
	void listAllTeachers_returnsSimpleList() throws Exception {
		com.system_gestion_soutenance.api.user.entity.User user = new com.system_gestion_soutenance.api.user.entity.User();
		Page<com.system_gestion_soutenance.api.user.entity.User> page = new PageImpl<>(List.of(user));

		when(userService.listUsers("TEACHER", 0, 1000, null)).thenReturn(page);
		when(userMapper.toDto(user)).thenReturn(new UserDto(2L, "bob@test.com", "TEACHER", "Durand", "Bob", true, null,
				null, null, null, null, null, null, null, null, null));

		mockMvc.perform(get("/api/admin/users/teachers-list")).andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(1));
	}

	@Test
	void listAllStudents_returnsSimpleList() throws Exception {
		com.system_gestion_soutenance.api.user.entity.User user = new com.system_gestion_soutenance.api.user.entity.User();
		Page<com.system_gestion_soutenance.api.user.entity.User> page = new PageImpl<>(List.of(user));

		when(userService.listUsers("STUDENT", 0, 1000, null)).thenReturn(page);
		when(userMapper.toDto(user)).thenReturn(new UserDto(1L, "alice@test.com", "STUDENT", "Martin", "Alice", true,
				null, null, null, null, null, null, null, null, null, null));

		mockMvc.perform(get("/api/admin/users/students-list")).andExpect(status().isOk())
				.andExpect(jsonPath("$.size()").value(1));
	}
}
