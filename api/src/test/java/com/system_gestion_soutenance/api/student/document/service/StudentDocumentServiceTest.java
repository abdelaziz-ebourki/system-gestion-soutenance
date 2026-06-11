package com.system_gestion_soutenance.api.student.document.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.student.document.entity.StudentDocument;
import com.system_gestion_soutenance.api.student.document.repository.StudentDocumentRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class StudentDocumentServiceTest {

	@Mock
	private StudentDocumentRepository repository;

	@InjectMocks
	private StudentDocumentService service;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(service, "maxFileSizeMb", 10L);
		ReflectionTestUtils.setField(service, "allowedExtensions", "pdf,doc,docx");
		ReflectionTestUtils.setField(service, "versionLimit", 5);
	}

	@Test
	void findByStudent_returnsDocuments() {
		when(repository.findByStudentId(1L)).thenReturn(List.of(new StudentDocument()));
		assertEquals(1, service.findByStudent(1L).size());
	}

	@Test
	void upload_success() throws Exception {
		StudentDocument doc = new StudentDocument();
		doc.setId(1L);
		doc.setStatus("missing");

		MultipartFile file = mock(MultipartFile.class);
		when(file.getOriginalFilename()).thenReturn("report.pdf");

		when(repository.findById(1L)).thenReturn(Optional.of(doc));
		when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

		StudentDocument result = service.upload(1L, file);

		assertEquals("submitted", result.getStatus());
		assertNotNull(result.getSubmittedAt());
		assertTrue(result.getFilePath().endsWith("report.pdf"));
	}

	@Test
	void upload_notFound_throws() {
		when(repository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(EntityNotFoundException.class, () -> service.upload(99L, mock(MultipartFile.class)));
	}
}
