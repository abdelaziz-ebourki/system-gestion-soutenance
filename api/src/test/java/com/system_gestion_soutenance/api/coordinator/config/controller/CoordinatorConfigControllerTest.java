package com.system_gestion_soutenance.api.coordinator.config.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.system_gestion_soutenance.api.admin.config.juryrole.dto.JuryRoleTemplateDto;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.service.JuryRoleTemplateService;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.ConfigMapper;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CoordinatorConfigController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class CoordinatorConfigControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JuryRoleTemplateService juryRoleTemplateService;

	@MockitoBean
	private ConfigMapper configMapper;

	@MockitoBean
	private DefenseSessionRepository defenseSessionRepository;

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
	void findAllJuryRoleTemplates_returns200WithMappedPage() throws Exception {
		JuryRoleTemplate template = new JuryRoleTemplate();
		when(juryRoleTemplateService.findAll(0, 10))
				.thenReturn(new PaginatedResponse<>(List.of(template), 1, 1, 0, 10));
		when(configMapper.toJuryRoleTemplateDto(any(JuryRoleTemplate.class)))
				.thenReturn(new JuryRoleTemplateDto(1L, "Standard", "CDM", List.of()));

		mockMvc.perform(get("/api/coordinator/config/jury-role-templates")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.total").value(1));
	}

	@Test
	void getSettings_withoutActiveSession_returnsNullData() throws Exception {
		when(defenseSessionRepository.findActiveSession(any(LocalDate.class))).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/coordinator/config/settings")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void getSettings_withActiveSession_returnsMappedSettings() throws Exception {
		DefenseSession session = new DefenseSession();
		session.setStartTime("08:00");
		session.setEndTime("18:00");
		session.setDefenseDuration(30);
		session.setBreakDuration(10);
		session.setGroupCreationStartDate("2026-03-01");
		session.setGroupCreationEndDate("2026-05-01");
		when(defenseSessionRepository.findActiveSession(any(LocalDate.class))).thenReturn(Optional.of(session));

		mockMvc.perform(get("/api/coordinator/config/settings")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.startTime").value("08:00"))
				.andExpect(jsonPath("$.data.defenseDuration").value(30));
	}

	@Test
	void paginationParams_areForwardedToService() throws Exception {
		when(juryRoleTemplateService.findAll(2, 25)).thenReturn(new PaginatedResponse<>(List.of(), 0, 0, 2, 25));

		mockMvc.perform(get("/api/coordinator/config/jury-role-templates").param("page", "2").param("limit", "25"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.data.currentPage").value(2));

		Mockito.verify(juryRoleTemplateService).findAll(2, 25);
	}
}
