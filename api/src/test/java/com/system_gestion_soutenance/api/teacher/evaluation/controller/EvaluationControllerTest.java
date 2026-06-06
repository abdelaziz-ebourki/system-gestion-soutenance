package com.system_gestion_soutenance.api.teacher.evaluation.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.mapper.EvaluationMapper;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.teacher.evaluation.dto.EvaluationResponse;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import com.system_gestion_soutenance.api.teacher.evaluation.service.EvaluationService;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = EvaluationController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class EvaluationControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private EvaluationService evaluationService;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private SecurityService securityService;
	@MockitoBean
	private EvaluationMapper evaluationMapper;

	@BeforeEach
	void setUp() {
		User user = new User();
		user.setId(1L);
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
		when(securityService.getCurrentUserId()).thenReturn(1L);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void findByTeacher_returnsList() throws Exception {
		when(evaluationService.findByTeacher(1L)).thenReturn(List.of());
		when(evaluationService.buildProjectMap(any())).thenReturn(Map.of());
		mockMvc.perform(get("/api/teacher/evaluations")).andExpect(status().isOk());
	}

	@Test
	void submit_returns200() throws Exception {
		Evaluation evaluation = new Evaluation();
		evaluation.setId(1L);
		evaluation.setProjectId(1L);
		when(evaluationService.submit(anyLong(), any())).thenReturn(evaluation);
		when(evaluationService.buildProjectMap(any())).thenReturn(Map.of());
		when(evaluationMapper.toDto(eq(evaluation), any()))
				.thenReturn(new EvaluationResponse(1L, 1L, "Project", 15.0, "Good", "SUBMITTED"));
		mockMvc.perform(post("/api/teacher/evaluations/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"score\":15.0,\"comment\":\"Good\"}")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("SUBMITTED"));
	}
}
