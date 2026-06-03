package com.system_gestion_soutenance.api.admin.config.email.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.admin.config.email.entity.EmailConfig;
import com.system_gestion_soutenance.api.admin.config.email.service.EmailConfigService;
import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = EmailConfigController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class EmailConfigControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private EmailConfigService service;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;

	@Test
	void get_returnsConfig() throws Exception {
		EmailConfig config = new EmailConfig();
		config.setHost("smtp.test.com");
		when(service.get()).thenReturn(config);

		mockMvc.perform(get("/api/admin/config/email")).andExpect(status().isOk())
				.andExpect(jsonPath("$.host").value("smtp.test.com"));
	}

	@Test
	void update_returnsConfig() throws Exception {
		EmailConfig config = new EmailConfig();
		config.setHost("new.host.com");
		when(service.update(any())).thenReturn(config);

		mockMvc.perform(put("/api/admin/config/email").contentType(MediaType.APPLICATION_JSON).content(
				"""
						{"host":"new.host.com","port":587,"username":"u","senderName":"S","senderEmail":"s@s.com","encryption":"tls"}
						"""))
				.andExpect(status().isOk()).andExpect(jsonPath("$.host").value("new.host.com"));
	}
}
