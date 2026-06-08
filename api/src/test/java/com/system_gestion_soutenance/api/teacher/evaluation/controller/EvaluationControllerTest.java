package com.system_gestion_soutenance.api.teacher.evaluation.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.mapper.EvaluationMapper;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
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

@WebMvcTest(controllers = EvaluationController.class)
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
	private EvaluationMapper evaluationMapper;

	@BeforeEach
	void setUp() {
		// No more manual SecurityContextHolder setup
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void findByTeacher_returnsList() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setRole(com.system_gestion_soutenance.api.user.entity.Role.TEACHER);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
				List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_TEACHER")));
		when(evaluationService.findByTeacher(1L)).thenReturn(List.of());
		when(evaluationService.buildProjectMap(any())).thenReturn(Map.of());
		mockMvc.perform(get("/api/teacher/evaluations").with(authentication(auth))).andExpect(status().isOk());
	}

	@Test
	void submit_returns200() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setRole(com.system_gestion_soutenance.api.user.entity.Role.TEACHER);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
				List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_TEACHER")));
		Defense defense = mock(Defense.class);
		when(defense.getId()).thenReturn(1L);

		Evaluation evaluation = new Evaluation();
		evaluation.setId(1L);
		evaluation.setDefense(defense);
		when(evaluationService.submit(anyLong(), any())).thenReturn(evaluation);
		when(evaluationService.buildProjectMap(any())).thenReturn(Map.of());
		when(evaluationMapper.toDto(eq(evaluation), any()))
				.thenReturn(new EvaluationResponse(1L, 1L, "Project", 15.0, "Good", "SUBMITTED"));
		mockMvc.perform(post("/api/teacher/evaluations/1").contentType(MediaType.APPLICATION_JSON)
				.content("{\"score\":15.0,\"comment\":\"Good\"}").with(authentication(auth)).with(csrf()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("SUBMITTED"));
	}
}
