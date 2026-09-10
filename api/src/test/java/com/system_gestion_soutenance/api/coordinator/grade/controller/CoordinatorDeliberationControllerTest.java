package com.system_gestion_soutenance.api.coordinator.grade.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.coordinator.grade.dto.DeliberationRequest;
import com.system_gestion_soutenance.api.coordinator.grade.dto.DeliberationStateResponse;
import com.system_gestion_soutenance.api.coordinator.grade.dto.ScoreAdjustRequest;
import com.system_gestion_soutenance.api.coordinator.grade.service.CoordinatorDeliberationService;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.time.LocalDateTime;
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

@WebMvcTest(controllers = CoordinatorDeliberationController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class CoordinatorDeliberationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private CoordinatorDeliberationService deliberationService;

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
	void getDeliberationState_returnsState() throws Exception {
		DeliberationStateResponse response = new DeliberationStateResponse(1L, "Session PFE", null, null, null, null,
				false, List.of());

		when(deliberationService.getDeliberationState(1L)).thenReturn(response);

		mockMvc.perform(get("/api/coordinator/sessions/1/deliberation")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.sessionId").value(1L))
				.andExpect(jsonPath("$.data.sessionName").value("Session PFE"));
	}

	@Test
	void deliberate_returnsUpdatedState() throws Exception {
		DeliberationRequest request = new DeliberationRequest(Map.of(1L, 15.0, 2L, 12.5), "Session validée");
		DeliberationStateResponse response = new DeliberationStateResponse(1L, "Session PFE", 10L, LocalDateTime.now(),
				null, null, false, List.of());

		when(deliberationService.deliberate(eq(1L), any())).thenReturn(response);

		mockMvc.perform(post("/api/coordinator/sessions/1/deliberate").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.deliberatedBy").value(10L));
	}

	@Test
	void deliberate_withEmptyScores_returnsBadRequest() throws Exception {
		DeliberationRequest request = new DeliberationRequest(null, "Commentaire");

		mockMvc.perform(post("/api/coordinator/sessions/1/deliberate").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
	}

	@Test
	void adjustScore_returnsSuccess() throws Exception {
		ScoreAdjustRequest request = new ScoreAdjustRequest(14.0, "Ajustement après délibération");

		doNothing().when(deliberationService).adjustScore(eq(1L), any());

		mockMvc.perform(patch("/api/coordinator/defenses/1/adjust-score").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void adjustScore_withNullScore_returnsBadRequest() throws Exception {
		ScoreAdjustRequest request = new ScoreAdjustRequest(null, "Commentaire");

		mockMvc.perform(patch("/api/coordinator/defenses/1/adjust-score").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
	}

	@Test
	void adjustScore_withScoreTooHigh_returnsBadRequest() throws Exception {
		ScoreAdjustRequest request = new ScoreAdjustRequest(25.0, "Trop haut");

		mockMvc.perform(patch("/api/coordinator/defenses/1/adjust-score").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
	}
}
