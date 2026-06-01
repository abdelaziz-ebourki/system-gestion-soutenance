package com.system_gestion_soutenance.api.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import com.system_gestion_soutenance.api.user.service.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserAdminController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class UserAdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private UserRepository userRepository;

	@Test
	void listUsers_returnsPaginatedResponse() throws Exception {
		when(userService.listUsers(any(), anyInt(), anyInt(), any()))
				.thenReturn(new PaginatedResponse<>(List.of(), 0, 0, 0, 10));

		mockMvc.perform(get("/api/admin/users")).andExpect(status().isOk()).andExpect(jsonPath("$.items").isArray());
	}

	@Test
	void listUsers_withRoleParam() throws Exception {
		when(userService.listUsers(eq("student"), anyInt(), anyInt(), any()))
				.thenReturn(new PaginatedResponse<>(List.of(), 0, 0, 0, 10));

		mockMvc.perform(get("/api/admin/users").param("role", "student")).andExpect(status().isOk());

		verify(userService).listUsers(eq("student"), eq(0), eq(10), isNull());
	}

	@Test
	void listAllTeachers_returnsList() throws Exception {
		when(userService.listAllByRole("teacher")).thenReturn(List.of());

		mockMvc.perform(get("/api/admin/users/teachers-list")).andExpect(status().isOk());
	}

	@Test
	void listAllStudents_returnsList() throws Exception {
		when(userService.listAllByRole("student")).thenReturn(List.of());

		mockMvc.perform(get("/api/admin/users/students-list")).andExpect(status().isOk());
	}

	@Test
	void listStudents_returnsPaginatedResponse() throws Exception {
		when(userService.listUsers(eq("student"), anyInt(), anyInt(), any()))
				.thenReturn(new PaginatedResponse<>(List.of(), 0, 0, 0, 10));

		mockMvc.perform(get("/api/admin/students")).andExpect(status().isOk());
	}

	@Test
	void listTeachers_returnsPaginatedResponse() throws Exception {
		when(userService.listUsers(eq("teacher"), anyInt(), anyInt(), any()))
				.thenReturn(new PaginatedResponse<>(List.of(), 0, 0, 0, 10));

		mockMvc.perform(get("/api/admin/teachers")).andExpect(status().isOk());
	}

	@Test
	void listCoordinators_returnsPaginatedResponse() throws Exception {
		when(userService.listUsers(eq("coordinator"), anyInt(), anyInt(), any()))
				.thenReturn(new PaginatedResponse<>(List.of(), 0, 0, 0, 10));

		mockMvc.perform(get("/api/admin/coordinators")).andExpect(status().isOk());
	}

	@Test
	void createUser_returns201() throws Exception {
		when(userService.createUser(any())).thenReturn(mock(UserDto.class));

		mockMvc.perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content("""
				{"lastName":"Doe","firstName":"John","email":"j@t.com","role":"student"}
				""")).andExpect(status().isCreated());
	}

	@Test
	void createUser_withMissingFields_returns400() throws Exception {
		mockMvc.perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createStudent_withBlankRole_setsDefaultRole() throws Exception {
		when(userService.createUser(any())).thenReturn(mock(UserDto.class));

		mockMvc.perform(post("/api/admin/students").contentType(MediaType.APPLICATION_JSON).content("""
				{"lastName":"D","firstName":"J","email":"j@t.com","role":" "}
				""")).andExpect(status().isCreated());

		verify(userService).createUser(argThat(req -> "student".equals(req.role())));
	}

	@Test
	void createStudent_withRole_keepsRole() throws Exception {
		when(userService.createUser(any())).thenReturn(mock(UserDto.class));

		mockMvc.perform(post("/api/admin/students").contentType(MediaType.APPLICATION_JSON).content("""
				{"lastName":"D","firstName":"J","email":"j@t.com","role":"student"}
				""")).andExpect(status().isCreated());

		verify(userService).createUser(argThat(req -> "student".equals(req.role())));
	}

	@Test
	void createStudent_setsDefaultRole() throws Exception {
		when(userService.createUser(any())).thenReturn(mock(UserDto.class));

		mockMvc.perform(post("/api/admin/students").contentType(MediaType.APPLICATION_JSON).content("""
				{"lastName":"Doe","firstName":"John","email":"j@t.com"}
				""")).andExpect(status().isCreated());

		verify(userService).createUser(argThat(req -> "student".equals(req.role())));
	}

	@Test
	void createTeacher_withBlankRole_setsDefaultRole() throws Exception {
		when(userService.createUser(any())).thenReturn(mock(UserDto.class));

		mockMvc.perform(post("/api/admin/teachers").contentType(MediaType.APPLICATION_JSON).content("""
				{"lastName":"S","firstName":"T","email":"t@t.com","departmentId":1,"role":" "}
				""")).andExpect(status().isCreated());

		verify(userService).createUser(argThat(req -> "teacher".equals(req.role())));
	}

	@Test
	void createTeacher_withRole_keepsRole() throws Exception {
		when(userService.createUser(any())).thenReturn(mock(UserDto.class));

		mockMvc.perform(post("/api/admin/teachers").contentType(MediaType.APPLICATION_JSON).content("""
				{"lastName":"S","firstName":"T","email":"t@t.com","departmentId":1,"role":"teacher"}
				""")).andExpect(status().isCreated());

		verify(userService).createUser(argThat(req -> "teacher".equals(req.role())));
	}

	@Test
	void createTeacher_setsDefaultRole() throws Exception {
		when(userService.createUser(any())).thenReturn(mock(UserDto.class));

		mockMvc.perform(post("/api/admin/teachers").contentType(MediaType.APPLICATION_JSON).content("""
				{"lastName":"S","firstName":"T","email":"t@t.com","departmentId":1}
				""")).andExpect(status().isCreated());

		verify(userService).createUser(argThat(req -> "teacher".equals(req.role())));
	}

	@Test
	void createCoordinator_withBlankRole_setsDefaultRole() throws Exception {
		when(userService.createUser(any())).thenReturn(mock(UserDto.class));

		mockMvc.perform(post("/api/admin/coordinators").contentType(MediaType.APPLICATION_JSON).content("""
				{"lastName":"C","firstName":"C","email":"c@t.com","role":" "}
				""")).andExpect(status().isCreated());

		verify(userService).createUser(argThat(req -> "coordinator".equals(req.role())));
	}

	@Test
	void createCoordinator_withRole_keepsRole() throws Exception {
		when(userService.createUser(any())).thenReturn(mock(UserDto.class));

		mockMvc.perform(post("/api/admin/coordinators").contentType(MediaType.APPLICATION_JSON).content("""
				{"lastName":"C","firstName":"C","email":"c@t.com","role":"coordinator"}
				""")).andExpect(status().isCreated());

		verify(userService).createUser(argThat(req -> "coordinator".equals(req.role())));
	}

	@Test
	void createCoordinator_setsDefaultRole() throws Exception {
		when(userService.createUser(any())).thenReturn(mock(UserDto.class));

		mockMvc.perform(post("/api/admin/coordinators").contentType(MediaType.APPLICATION_JSON).content("""
				{"lastName":"C","firstName":"C","email":"c@t.com"}
				""")).andExpect(status().isCreated());

		verify(userService).createUser(argThat(req -> "coordinator".equals(req.role())));
	}

	@Test
	void bulkCreate_returns201() throws Exception {
		when(userService.bulkCreate(any())).thenReturn(List.of());

		mockMvc.perform(post("/api/admin/users/bulk").contentType(MediaType.APPLICATION_JSON).content("""
				{"users":[{"lastName":"D","firstName":"J","email":"j@t.com"}],"role":"student"}
				""")).andExpect(status().isCreated());
	}

	@Test
	void updateUser_returnsUpdatedUser() throws Exception {
		when(userService.updateUser(anyLong(), any())).thenReturn(mock(UserDto.class));

		mockMvc.perform(put("/api/admin/users/1").contentType(MediaType.APPLICATION_JSON).content("""
				{"lastName":"Updated"}
				""")).andExpect(status().isOk());
	}

	@Test
	void deleteUser_returns204() throws Exception {
		doNothing().when(userService).deleteUser(1L);

		mockMvc.perform(delete("/api/admin/users/1")).andExpect(status().isNoContent());

		verify(userService).deleteUser(1L);
	}
}
