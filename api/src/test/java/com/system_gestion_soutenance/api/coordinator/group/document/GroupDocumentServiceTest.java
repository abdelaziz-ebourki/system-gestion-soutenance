package com.system_gestion_soutenance.api.coordinator.group.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.exception.UnauthorizedAccessException;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GroupDocumentServiceTest {

	@Mock
	private GroupDocumentRepository repository;

	@Mock
	private GroupRepository groupRepository;

	@TempDir
	private Path tempDir;

	private GroupDocumentService service;

	@BeforeEach
	void setUp() {
		service = new GroupDocumentService(repository, groupRepository);
		ReflectionTestUtils.setField(service, "maxFileSizeMb", 10L);
		ReflectionTestUtils.setField(service, "allowedExtensions", "pdf,doc,docx");
		ReflectionTestUtils.setField(service, "uploadDir", tempDir);
	}

	private Group leaderGroup() {
		Group group = new Group();
		group.setId(1L);
		group.setLeaderId(42L);
		return group;
	}

	private GroupDocument pendingDoc() {
		GroupDocument doc = new GroupDocument();
		doc.setId(5L);
		doc.setGroupId(1L);
		doc.setType(GroupDocumentType.REPORT);
		doc.setName("Rapport PFE");
		doc.setDeadline(LocalDate.now().plusDays(10));
		doc.setStatus("missing");
		return doc;
	}

	private MockMultipartFile pdfFile() {
		return new MockMultipartFile("file", "rapport.pdf", "application/pdf", "content".getBytes());
	}

	@Test
	void findByGroup_delegatesToRepository() {
		when(repository.findByGroupId(1L)).thenReturn(List.of(pendingDoc()));

		assertEquals(1, service.findByGroup(1L).size());
	}

	@Test
	void upload_happyPath_marksSubmitted() {
		when(groupRepository.findById(1L)).thenReturn(Optional.of(leaderGroup()));
		GroupDocument doc = pendingDoc();
		when(repository.findByGroupIdAndType(1L, GroupDocumentType.REPORT)).thenReturn(Optional.of(doc));
		when(repository.save(any(GroupDocument.class))).thenAnswer(inv -> inv.getArgument(0));

		GroupDocument result = service.upload(1L, GroupDocumentType.REPORT, 42L, pdfFile());

		assertEquals("submitted", result.getStatus());
		assertTrue(result.getFilePath().contains("rapport.pdf"));
		assertTrue(result.getSubmittedAt() != null);
	}

	@Test
	void upload_unknownGroup_throws404() {
		when(groupRepository.findById(9L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.upload(9L, GroupDocumentType.REPORT, 42L, pdfFile()));
	}

	@Test
	void upload_nonLeader_throws403() {
		when(groupRepository.findById(1L)).thenReturn(Optional.of(leaderGroup()));

		assertThrows(UnauthorizedAccessException.class,
				() -> service.upload(1L, GroupDocumentType.REPORT, 7L, pdfFile()));
	}

	@Test
	void upload_missingDocument_throws404() {
		when(groupRepository.findById(1L)).thenReturn(Optional.of(leaderGroup()));
		when(repository.findByGroupIdAndType(1L, GroupDocumentType.REPORT)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.upload(1L, GroupDocumentType.REPORT, 42L, pdfFile()));
	}

	@Test
	void upload_pastDeadline_throws400() {
		when(groupRepository.findById(1L)).thenReturn(Optional.of(leaderGroup()));
		GroupDocument doc = pendingDoc();
		doc.setDeadline(LocalDate.now().minusDays(1));
		when(repository.findByGroupIdAndType(1L, GroupDocumentType.REPORT)).thenReturn(Optional.of(doc));

		assertThrows(InvalidBusinessStateException.class,
				() -> service.upload(1L, GroupDocumentType.REPORT, 42L, pdfFile()));
	}

	@Test
	void upload_oversizeFile_throws400() {
		when(groupRepository.findById(1L)).thenReturn(Optional.of(leaderGroup()));
		when(repository.findByGroupIdAndType(1L, GroupDocumentType.REPORT)).thenReturn(Optional.of(pendingDoc()));
		byte[] big = new byte[11 * 1024 * 1024];
		MockMultipartFile huge = new MockMultipartFile("file", "big.pdf", "application/pdf", big);

		assertThrows(IllegalArgumentException.class, () -> service.upload(1L, GroupDocumentType.REPORT, 42L, huge));
	}

	@Test
	void upload_forbiddenExtension_throws400() {
		when(groupRepository.findById(1L)).thenReturn(Optional.of(leaderGroup()));
		when(repository.findByGroupIdAndType(1L, GroupDocumentType.REPORT)).thenReturn(Optional.of(pendingDoc()));
		MockMultipartFile exe = new MockMultipartFile("file", "run.exe", "application/octet-stream", "x".getBytes());

		Exception e = assertThrows(IllegalArgumentException.class,
				() -> service.upload(1L, GroupDocumentType.REPORT, 42L, exe));
		assertTrue(e.getMessage().contains("Extension non autorisée"));
	}

	@Test
	void upload_extensionlessFileName_throws400() {
		when(groupRepository.findById(1L)).thenReturn(Optional.of(leaderGroup()));
		when(repository.findByGroupIdAndType(1L, GroupDocumentType.REPORT)).thenReturn(Optional.of(pendingDoc()));
		MockMultipartFile noExt = new MockMultipartFile("file", "README", "text/plain", "x".getBytes());

		assertThrows(IllegalArgumentException.class, () -> service.upload(1L, GroupDocumentType.REPORT, 42L, noExt));
	}

	@Test
	void upload_emptyFileName_rejectedAsUnknownExtension() {
		when(groupRepository.findById(1L)).thenReturn(Optional.of(leaderGroup()));
		when(repository.findByGroupIdAndType(1L, GroupDocumentType.REPORT)).thenReturn(Optional.of(pendingDoc()));
		MockMultipartFile nameless = new MockMultipartFile("file", "", "application/pdf", "x".getBytes());

		assertThrows(IllegalArgumentException.class, () -> service.upload(1L, GroupDocumentType.REPORT, 42L, nameless));
	}

	@Test
	void download_roundTrip_returnsUploadedBytes() throws Exception {
		when(groupRepository.findById(1L)).thenReturn(Optional.of(leaderGroup()));
		GroupDocument doc = pendingDoc();
		when(repository.findByGroupIdAndType(1L, GroupDocumentType.REPORT)).thenReturn(Optional.of(doc));
		when(repository.save(any(GroupDocument.class))).thenAnswer(inv -> inv.getArgument(0));

		service.upload(1L, GroupDocumentType.REPORT, 42L, pdfFile());

		assertEquals("content", new String(service.download(1L, GroupDocumentType.REPORT)));
	}

	@Test
	void download_unreadableFile_throws500() throws Exception {
		when(groupRepository.findById(1L)).thenReturn(Optional.of(leaderGroup()));
		GroupDocument doc = pendingDoc();
		when(repository.findByGroupIdAndType(1L, GroupDocumentType.REPORT)).thenReturn(Optional.of(doc));
		when(repository.save(any(GroupDocument.class))).thenAnswer(inv -> inv.getArgument(0));

		service.upload(1L, GroupDocumentType.REPORT, 42L, pdfFile());
		Files.delete(Path.of(doc.getFilePath()));

		assertThrows(RuntimeException.class, () -> service.download(1L, GroupDocumentType.REPORT));
	}

	@Test
	void download_missingDocument_throws404() {
		when(repository.findByGroupIdAndType(1L, GroupDocumentType.REPORT)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.download(1L, GroupDocumentType.REPORT));
	}

	@Test
	void download_noFileUploaded_throws404() {
		GroupDocument doc = pendingDoc();
		doc.setFilePath(null);
		when(repository.findByGroupIdAndType(1L, GroupDocumentType.REPORT)).thenReturn(Optional.of(doc));

		assertThrows(EntityNotFoundException.class, () -> service.download(1L, GroupDocumentType.REPORT));
	}

	@Test
	void createDefaultDocuments_usesProvidedDeadlineAndAllTypes() {
		service.createDefaultDocuments(3L, LocalDate.of(2027, 6, 1));

		verify(repository, org.mockito.Mockito.times(3)).save(any(GroupDocument.class));
	}

	@Test
	void createDefaultDocuments_nullDeadline_defaultsTo30Days() {
		service.createDefaultDocuments(3L, null);

		org.mockito.ArgumentCaptor<GroupDocument> captor = org.mockito.ArgumentCaptor.forClass(GroupDocument.class);
		verify(repository, org.mockito.Mockito.times(3)).save(captor.capture());
		assertEquals(LocalDate.now().plusDays(30), captor.getValue().getDeadline());
	}

	@Test
	void updateStatus_missingDocument_throws404() {
		when(repository.findById(9L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.updateStatus(9L, "validated"));
	}

	@Test
	void updateStatus_persistsNewStatus() {
		GroupDocument doc = pendingDoc();
		when(repository.findById(5L)).thenReturn(Optional.of(doc));
		when(repository.save(doc)).thenReturn(doc);

		assertEquals("validated", service.updateStatus(5L, "validated").getStatus());
	}
}
