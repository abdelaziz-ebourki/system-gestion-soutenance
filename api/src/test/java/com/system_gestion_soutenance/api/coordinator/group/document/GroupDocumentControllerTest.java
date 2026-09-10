package com.system_gestion_soutenance.api.coordinator.group.document;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.system_gestion_soutenance.api.auth.jwt.JwtTokenProvider;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GroupDocumentController.class, excludeAutoConfiguration = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
		org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class})
class GroupDocumentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GroupDocumentService groupDocumentService;

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
	void findByGroup_returnsDocuments() throws Exception {
		GroupDocument doc = createDocument(1L, 10L, GroupDocumentType.REPORT, "Rapport PFE", "missing");
		when(groupDocumentService.findByGroup(10L)).thenReturn(List.of(doc));

		mockMvc.perform(get("/api/groups/10/documents")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.size()").value(1)).andExpect(jsonPath("$.data[0].type").value("REPORT"))
				.andExpect(jsonPath("$.data[0].name").value("Rapport PFE"));
	}

	@Test
	void findByGroup_whenEmpty_returnsEmptyList() throws Exception {
		when(groupDocumentService.findByGroup(10L)).thenReturn(List.of());

		mockMvc.perform(get("/api/groups/10/documents")).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.size()").value(0));
	}

	@Test
	void upload_returnsDocument() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "rapport.pdf", "application/pdf",
				"fake pdf content".getBytes());

		GroupDocument doc = createDocument(1L, 10L, GroupDocumentType.REPORT, "Rapport PFE", "submitted");
		when(groupDocumentService.upload(eq(10L), eq(GroupDocumentType.REPORT), anyLong(), any())).thenReturn(doc);

		mockMvc.perform(multipart("/api/groups/10/documents/REPORT/attachments").file(file)).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.type").value("REPORT"))
				.andExpect(jsonPath("$.data.status").value("submitted"));
	}

	@Test
	void download_returnsFile() throws Exception {
		when(groupDocumentService.download(10L, GroupDocumentType.REPORT)).thenReturn("file content".getBytes());

		mockMvc.perform(get("/api/groups/10/documents/REPORT/download")).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
	}

	@Test
	void download_whenNotFound_returnsNotFound() throws Exception {
		when(groupDocumentService.download(10L, GroupDocumentType.REPORT))
				.thenThrow(new com.system_gestion_soutenance.api.common.exception.EntityNotFoundException("Not found"));

		mockMvc.perform(get("/api/groups/10/documents/REPORT/download")).andExpect(status().isNotFound());
	}

	private GroupDocument createDocument(Long id, Long groupId, GroupDocumentType type, String name, String status) {
		GroupDocument doc = new GroupDocument();
		doc.setId(id);
		doc.setGroupId(groupId);
		doc.setType(type);
		doc.setName(name);
		doc.setDeadline(LocalDate.now().plusDays(30));
		doc.setStatus(status);
		doc.setSubmittedAt(status.equals("submitted") ? LocalDateTime.now() : null);
		doc.setFilePath(status.equals("submitted") ? "/path/to/file.pdf" : null);
		return doc;
	}
}
