package com.system_gestion_soutenance.api.student.document.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.student.document.entity.StudentDocument;
import com.system_gestion_soutenance.api.student.document.repository.StudentDocumentRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

	@Test
	void upload_fileTooLarge_throws() {
		StudentDocument doc = new StudentDocument();
		doc.setId(1L);
		doc.setStudentId(1L);

		MultipartFile file = mock(MultipartFile.class);
		when(file.getSize()).thenReturn(20L * 1024 * 1024);

		when(repository.findById(1L)).thenReturn(Optional.of(doc));

		assertThrows(IllegalArgumentException.class, () -> service.upload(1L, 1L, file));
	}

	@Test
	void upload_badExtension_throws() {
		StudentDocument doc = new StudentDocument();
		doc.setId(1L);
		doc.setStudentId(1L);

		MultipartFile file = mock(MultipartFile.class);
		when(file.getSize()).thenReturn(100L);
		when(file.getOriginalFilename()).thenReturn("report.exe");

		when(repository.findById(1L)).thenReturn(Optional.of(doc));

		assertThrows(IllegalArgumentException.class, () -> service.upload(1L, 1L, file));
	}

	@Test
	void upload_versionLimitExceeded_throws() {
		StudentDocument doc = new StudentDocument();
		doc.setId(1L);
		doc.setStudentId(1L);
		doc.setFilePath("uploads/v6/report.pdf");

		MultipartFile file = mock(MultipartFile.class);
		when(file.getSize()).thenReturn(100L);
		when(file.getOriginalFilename()).thenReturn("report.pdf");

		when(repository.findById(1L)).thenReturn(Optional.of(doc));

		assertThrows(IllegalArgumentException.class, () -> service.upload(1L, 1L, file));
	}

	@Test
	void upload_noPreviousVersion_savesFirstVersion() throws Exception {
		Student student = new Student();
		student.setFirstName("Alice");
		student.setLastName("Smith");

		StudentDocument doc = new StudentDocument();
		doc.setId(1L);
		doc.setName("Report");
		doc.setStudentId(1L);

		MultipartFile file = mock(MultipartFile.class);
		when(file.getSize()).thenReturn(100L);
		when(file.getOriginalFilename()).thenReturn("report.pdf");

		when(repository.findById(1L)).thenReturn(Optional.of(doc));
		when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(securityService.getCurrentUserEmail()).thenReturn("student@test.com");

		StudentDocument result = service.upload(1L, 1L, file);

		assertEquals("submitted", result.getStatus());
		assertNotNull(result.getSubmittedAt());
		verify(eventPublisher)
				.publishEvent(any(com.system_gestion_soutenance.api.notification.event.DomainEvent.class));
	}

	@Test
	void upload_noExtension_throws() {
		StudentDocument doc = new StudentDocument();
		doc.setId(1L);
		doc.setStudentId(1L);

		MultipartFile file = mock(MultipartFile.class);
		when(file.getSize()).thenReturn(100L);
		when(file.getOriginalFilename()).thenReturn("report");

		when(repository.findById(1L)).thenReturn(Optional.of(doc));

		assertThrows(IllegalArgumentException.class, () -> service.upload(1L, 1L, file));
	}

	@Test
	void findByStudent_withPagination_returnsPaginatedResponse() {
		StudentDocument doc = new StudentDocument();
		Page<StudentDocument> page = new PageImpl<>(List.of(doc));
		when(repository.findByStudentId(1L, PageRequest.of(0, 10))).thenReturn(page);

		var result = service.findByStudent(1L, 0, 10);

		assertEquals(1, result.items().size());
	}

	@Test
	void download_success_returnsBytes() throws Exception {
		java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test-dl", ".txt");
		try {
			java.nio.file.Files.writeString(tempFile, "test content");
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}

		StudentDocument doc = new StudentDocument();
		doc.setId(1L);
		doc.setStudentId(1L);
		doc.setFilePath(tempFile.toString());

		when(repository.findById(1L)).thenReturn(Optional.of(doc));

		byte[] result = service.download(1L, 1L);

		assertArrayEquals("test content".getBytes(), result);
	}

	@Test
	void download_notFound_throws() {
		when(repository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.download(99L, 1L));
	}

	@Test
	void download_wrongOwner_throws() {
		StudentDocument doc = new StudentDocument();
		doc.setId(1L);
		doc.setStudentId(1L);

		when(repository.findById(1L)).thenReturn(Optional.of(doc));

		assertThrows(UnauthorizedAccessException.class, () -> service.download(1L, 99L));
	}

	@Test
	void download_noFilePath_throws() {
		StudentDocument doc = new StudentDocument();
		doc.setId(1L);
		doc.setStudentId(1L);
		doc.setFilePath(null);

		when(repository.findById(1L)).thenReturn(Optional.of(doc));

		assertThrows(EntityNotFoundException.class, () -> service.download(1L, 1L));
	}
}
