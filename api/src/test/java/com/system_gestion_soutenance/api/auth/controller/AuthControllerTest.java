package com.system_gestion_soutenance.api.auth.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.dto.*;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.auth.service.AuthService;
import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private UserRepository userRepository;

	@Test
	void login_withValidCredentials_returns200() throws Exception {
		LoginResponse response = new LoginResponse(mock(UserDto.class), "jwt-token", 9999999999L);
		when(authService.login(any(LoginRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON).content("""
				{"email":"admin@test.com","password":"password"}
				""")).andExpect(status().isOk()).andExpect(jsonPath("$.token").value("jwt-token"));
	}

	@Test
	void login_withMissingEmail_returns400() throws Exception {
		mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON).content("""
				{"password":"password"}
				""")).andExpect(status().isBadRequest());
	}

	@Test
	void login_withInvalidEmail_returns400() throws Exception {
		mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON).content("""
				{"email":"not-an-email","password":"password"}
				""")).andExpect(status().isBadRequest());
	}

	@Test
	void forgotPassword_withValidEmail_returns200() throws Exception {
		doNothing().when(authService).forgotPassword(any(ForgotPasswordRequest.class));

		mockMvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content("""
				{"email":"user@test.com"}
				""")).andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.notNullValue()));
	}

	@Test
	void forgotPassword_withEmptyEmail_returns400() throws Exception {
		mockMvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void resetPassword_withValidRequest_returns200() throws Exception {
		doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

		mockMvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content("""
				{"token":"valid-token","password":"new-password"}
				""")).andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Mot de passe réinitialisé avec succès."));
	}

	@Test
	void verifyAccount_withValidRequest_returns200() throws Exception {
		doNothing().when(authService).verifyAccount(any(VerifyRequest.class));

		mockMvc.perform(post("/api/auth/verify-account").contentType(MediaType.APPLICATION_JSON).content("""
				{"token":"valid-token","password":"secure-pass"}
				""")).andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Compte vérifié avec succès."));
	}
}
