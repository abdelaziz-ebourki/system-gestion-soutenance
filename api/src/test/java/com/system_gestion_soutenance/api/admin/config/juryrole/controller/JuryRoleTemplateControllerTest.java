package com.system_gestion_soutenance.api.admin.config.juryrole.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.service.JuryRoleTemplateService;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseType;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = JuryRoleTemplateController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class JuryRoleTemplateControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private JuryRoleTemplateService juryRoleTemplateService;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private com.system_gestion_soutenance.api.common.mapper.ConfigMapper configMapper;

	@Test
	void findAll_returnsList() throws Exception {
		when(juryRoleTemplateService.findAll()).thenReturn(List.of());
		mockMvc.perform(get("/api/admin/config/jury-role-templates")).andExpect(status().isOk());
	}

	@Test
	void create_returns201() throws Exception {
		JuryRoleTemplate template = new JuryRoleTemplate(1L, "Template PFE", DefenseType.PFE, new ArrayList<>());
		when(juryRoleTemplateService.create(any())).thenReturn(template);
		when(configMapper.toJuryRoleTemplateDto(template))
				.thenReturn(new com.system_gestion_soutenance.api.admin.config.juryrole.dto.JuryRoleTemplateDto(1L,
						"Template PFE", "PFE", List.of()));
		mockMvc.perform(post("/api/admin/config/jury-role-templates").contentType(MediaType.APPLICATION_JSON).content(
				"{\"name\":\"Template PFE\",\"defenseType\":\"PFE\",\"roles\":[{\"name\":\"Président\",\"count\":1,\"coefficient\":2}]}"))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Template PFE"));
	}

	@Test
	void update_returns200() throws Exception {
		JuryRoleTemplate template = new JuryRoleTemplate(1L, "Updated", DefenseType.MEMOIRE, new ArrayList<>());
		when(juryRoleTemplateService.update(anyLong(), any())).thenReturn(template);
		when(configMapper.toJuryRoleTemplateDto(template))
				.thenReturn(new com.system_gestion_soutenance.api.admin.config.juryrole.dto.JuryRoleTemplateDto(1L,
						"Updated", "MEMOIRE", List.of()));
		mockMvc.perform(put("/api/admin/config/jury-role-templates/1").contentType(MediaType.APPLICATION_JSON).content(
				"{\"name\":\"Updated\",\"defenseType\":\"MEMOIRE\",\"roles\":[{\"name\":\"Rapporteur\",\"count\":1,\"coefficient\":1}]}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated"));
	}

	@Test
	void delete_returns204() throws Exception {
		doNothing().when(juryRoleTemplateService).delete(1L);
		mockMvc.perform(delete("/api/admin/config/jury-role-templates/1")).andExpect(status().isNoContent());
	}
}
