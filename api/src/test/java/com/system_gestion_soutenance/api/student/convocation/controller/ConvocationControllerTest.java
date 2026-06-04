package com.system_gestion_soutenance.api.student.convocation.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.student.defense.service.StudentDefenseService;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(controllers = ConvocationController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class ConvocationControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private StudentDefenseService studentDefenseService;
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
	void getConvocation_scheduled_returnsPdf() throws Exception {
		when(studentDefenseService.getDefense(1L))
				.thenReturn(new com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse(null, null,
						null, List.of(), null, null, null, null, "scheduled", null, null));
		mockMvc.perform(get("/api/student/convocation")).andExpect(status().isOk())
				.andExpect(content().contentType("application/pdf"));
	}

	@Test
	void getConvocation_defenseNotFound_returns404() throws Exception {
		when(studentDefenseService.getDefense(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
		mockMvc.perform(get("/api/student/convocation")).andExpect(status().isNotFound());
	}

	@Test
	void getConvocation_notScheduled_returns404() throws Exception {
		when(studentDefenseService.getDefense(1L))
				.thenReturn(new com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse(null, null,
						null, List.of(), null, null, null, null, "pending", null, null));
		mockMvc.perform(get("/api/student/convocation")).andExpect(status().isNotFound());
	}
}
