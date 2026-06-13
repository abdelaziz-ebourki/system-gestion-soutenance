package com.system_gestion_soutenance.api.student.document.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.student.document.entity.StudentDocument;
import com.system_gestion_soutenance.api.student.document.dto.StudentDocumentDto;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.student.document.service.StudentDocumentService;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = StudentDocumentController.class)
class StudentDocumentControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockitoBean
	private StudentDocumentService studentDocumentService;
	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;
	@MockitoBean
	private UserRepository userRepository;
	@MockitoBean
	private com.system_gestion_soutenance.api.common.mapper.StudentDocumentMapper studentDocumentMapper;

	@BeforeEach
	void setUp() {
		// No more manual SecurityContextHolder setup
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void findByStudent_returnsList() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setRole(com.system_gestion_soutenance.api.user.entity.Role.STUDENT);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
				List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")));
		when(studentDocumentService.findByStudent(1L, 0, 10))
				.thenReturn(new PaginatedResponse<>(List.of(), 0, 0, 0, 10));
		mockMvc.perform(get("/api/student/documents").with(authentication(auth))).andExpect(status().isOk());
	}

	@Test
	void upload_returns200() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setRole(com.system_gestion_soutenance.api.user.entity.Role.STUDENT);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
				List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")));
		StudentDocument doc = new StudentDocument();
		doc.setId(1L);
		doc.setStatus("submitted");
		when(studentDocumentService.upload(anyLong(), anyLong(), any())).thenReturn(doc);

		StudentDocumentDto dto = new StudentDocumentDto(1L, 1L, "test.pdf", "pdf", "2026-06-01", "submitted", null,
				"/path/to/file");
		when(studentDocumentMapper.toDto(doc)).thenReturn(dto);

		MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
		mockMvc.perform(
				multipart("/api/student/documents/1/attachments").file(file).with(authentication(auth)).with(csrf()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("submitted"));
	}
}
