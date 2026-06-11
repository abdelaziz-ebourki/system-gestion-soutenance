package com.system_gestion_soutenance.api.student.convocation.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.service.PdfGenerationService;
import com.system_gestion_soutenance.api.student.defense.service.StudentDefenseService;
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
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;

@WebMvcTest(controllers = ConvocationController.class)
class ConvocationControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private StudentDefenseService studentDefenseService;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private PdfGenerationService pdfGenerationService;
	@MockitoBean
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		// No more manual SecurityContextHolder setup
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getConvocation_scheduled_returnsPdf() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setRole(com.system_gestion_soutenance.api.user.entity.Role.STUDENT);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
				List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")));
		when(studentDefenseService.getDefense(1L)).thenReturn(
				new com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse("Project Title",
						"Description", "Supervisor", List.of(), "2026-06-15", "10:00", "12:00", "Room 1", "scheduled",
						null, null));
		mockMvc.perform(get("/api/student/convocations").with(authentication(auth))).andExpect(status().isOk())
				.andExpect(content().contentType("application/pdf"));
	}

	@Test
	void getConvocation_defenseNotFound_returns404() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setRole(com.system_gestion_soutenance.api.user.entity.Role.STUDENT);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
				List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")));
		when(studentDefenseService.getDefense(1L)).thenThrow(new EntityNotFoundException("Not found"));
		mockMvc.perform(get("/api/student/convocations").with(authentication(auth))).andExpect(status().isNotFound());
	}

	@Test
	void getConvocation_notScheduled_returns404() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setRole(com.system_gestion_soutenance.api.user.entity.Role.STUDENT);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
				List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")));
		when(studentDefenseService.getDefense(1L))
				.thenReturn(new com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse(null, null,
						null, List.of(), null, null, null, null, "pending", null, null));
		mockMvc.perform(get("/api/student/convocations").with(authentication(auth))).andExpect(status().isNotFound());
	}
}
