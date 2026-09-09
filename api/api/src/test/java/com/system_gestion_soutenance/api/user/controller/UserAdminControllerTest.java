package com.system_gestion_soutenance.api.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.UserMapper;
import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import com.system_gestion_soutenance.api.user.service.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
	private UserMapper userMapper;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private UserRepository userRepository;

	@Test
	void listUsers_returnsPaginatedResponse() throws Exception {
		when(userService.listUsers(any(), anyInt(), anyInt(), any())).thenReturn(Page.empty());

		mockMvc.perform(get("/api/admin/users")).andExpect(status().isOk()).andExpect(jsonPath("$.items").isArray());
	}

	@Test
	void listUsers_withRoleParam() throws Exception {
		when(userService.listUsers(eq("student"), anyInt(), anyInt(), any())).thenReturn(Page.empty());

		mockMvc.perform(get("/api/admin/users").param("role", "student")).andExpect(status().isOk());

		verify(userService).listUsers(eq("student"), eq(0), eq(10), isNull());
	}

	@Test
	void createUser_returns201() throws Exception {
		when(userService.createUser(any())).thenReturn(new User());
		when(userMapper.toDto(any(User.class))).thenReturn(mock(UserDto.class));

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
	void createUser_withSpecifiedRole_createsUserWithThatRole() throws Exception {
		when(userService.createUser(any())).thenReturn(new User());
		when(userMapper.toDto(any(User.class))).thenReturn(mock(UserDto.class));

		mockMvc.perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content("""
				{"lastName":"D","firstName":"J","email":"j@t.com","role":"teacher"}
				""")).andExpect(status().isCreated());

		verify(userService).createUser(argThat(req -> "teacher".equals(req.role())));
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
		when(userService.updateUser(anyLong(), any())).thenReturn(new User());
		when(userMapper.toDto(any(User.class))).thenReturn(mock(UserDto.class));

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
