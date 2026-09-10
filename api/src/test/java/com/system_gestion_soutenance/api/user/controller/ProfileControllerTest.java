package com.system_gestion_soutenance.api.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.mapper.UserMapper;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ProfileController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class ProfileControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SecurityService securityService;

	@MockitoBean
	private UserProfileService userProfileService;

	@MockitoBean
	private UserMapper userMapper;

	private User mockUser() {
		User user = new User();
		user.setId(1L);
		user.setEmail("test@example.com");
		user.setFirstName("John");
		user.setLastName("Doe");
		return user;
	}

	@Test
	void getProfile_returnsUserDto() throws Exception {
		User user = mockUser();
		UserDto dto = new UserDto(1L, "test@example.com", "student", "Doe", "John", true, null, null, null, null, null,
				null, null, null, null, null);
		when(securityService.getCurrentUser()).thenReturn(user);
		when(userMapper.toDto(user)).thenReturn(dto);

		mockMvc.perform(get("/api/me")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value("test@example.com"));
	}

	@Test
	void updateProfile_returnsUpdatedUserDto() throws Exception {
		User user = mockUser();
		UserDto dto = new UserDto(1L, "test@example.com", "student", "Updated", "Jane", true, null, null, null, null,
				null, null, null, null, null, null);
		when(securityService.getCurrentUser()).thenReturn(user);
		when(userMapper.toDto(user)).thenReturn(dto);

		mockMvc.perform(patch("/api/me").contentType(MediaType.APPLICATION_JSON).content("""
				{"firstName":"Jane","lastName":"Updated"}
				""")).andExpect(status().isOk()).andExpect(jsonPath("$.data.firstName").value("Jane"))
				.andExpect(jsonPath("$.data.lastName").value("Updated"));
	}

	@Test
	void changePassword_withValidPasswords_returns200() throws Exception {
		User user = mockUser();
		when(securityService.getCurrentUser()).thenReturn(user);

		mockMvc.perform(put("/api/me/password").contentType(MediaType.APPLICATION_JSON).content("""
				{"currentPassword":"OldP@ss1","newPassword":"NewP@ss2"}
				""")).andExpect(status().isOk());

		verify(userProfileService).changePassword(eq(user), any());
	}

	@Test
	void changePassword_withMissingFields_returns400() throws Exception {
		mockMvc.perform(put("/api/me/password").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void changePassword_withWeakPassword_returns400() throws Exception {
		User user = mockUser();
		when(securityService.getCurrentUser()).thenReturn(user);
		doThrow(new InvalidBusinessStateException(
				"Le mot de passe doit contenir au moins 8 caractères, inclure une majuscule, "
						+ "une minuscule, un chiffre et un caractère spécial."))
				.when(userProfileService).changePassword(eq(user), any());

		mockMvc.perform(put("/api/me/password").contentType(MediaType.APPLICATION_JSON).content("""
				{"currentPassword":"OldP@ss1","newPassword":"weak"}
				""")).andExpect(status().isBadRequest());
	}

	@Test
	void getProfile_unauthenticated_returns500() throws Exception {
		when(securityService.getCurrentUser()).thenThrow(new IllegalStateException("No authenticated user"));

		mockMvc.perform(get("/api/me")).andExpect(status().isInternalServerError());
	}
}
