package com.system_gestion_soutenance.api.student.document.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.student.document.entity.StudentDocument;
import com.system_gestion_soutenance.api.student.document.repository.StudentDocumentRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.UnauthorizedAccessException;
import com.system_gestion_soutenance.api.common.service.SecurityService;

@ExtendWith(MockitoExtension.class)
class StudentDocumentServiceTest {

	@Mock
	private StudentDocumentRepository repository;
	@Mock
	private StudentRepository studentRepository;
	@Mock
	private ApplicationEventPublisher eventPublisher;
	@Mock
	private SecurityService securityService;

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
		when(studentRepository.findById(1L)).thenReturn(Optional.of(new Student()));
		when(securityService.getCurrentUserEmail()).thenReturn("test@test.com");

		StudentDocument doc = new StudentDocument();
		doc.setId(1L);
		doc.setStatus("missing");
		doc.setStudentId(1L);

		MultipartFile file = mock(MultipartFile.class);
		when(file.getOriginalFilename()).thenReturn("report.pdf");

		when(repository.findById(1L)).thenReturn(Optional.of(doc));
		when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

		StudentDocument result = service.upload(1L, 1L, file);

		assertEquals("submitted", result.getStatus());
		assertNotNull(result.getSubmittedAt());
		assertTrue(result.getFilePath().endsWith("report.pdf"));
	}

	@Test
	void upload_notFound_throws() {
		when(repository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(EntityNotFoundException.class, () -> service.upload(99L, 1L, mock(MultipartFile.class)));
	}

	@Test
	void upload_wrongOwner_throws() {
		StudentDocument doc = new StudentDocument();
		doc.setId(1L);
		doc.setStudentId(1L);

		when(repository.findById(1L)).thenReturn(Optional.of(doc));

		assertThrows(UnauthorizedAccessException.class, () -> service.upload(1L, 99L, mock(MultipartFile.class)));
	}

	@Test
	void upload_pastDeadline_throws() {
		StudentDocument doc = new StudentDocument();
		doc.setId(1L);
		doc.setStudentId(1L);
		doc.setDeadline(LocalDate.of(2020, 1, 1));

		when(repository.findById(1L)).thenReturn(Optional.of(doc));

		assertThrows(IllegalArgumentException.class, () -> service.upload(1L, 1L, mock(MultipartFile.class)));
	}
}
