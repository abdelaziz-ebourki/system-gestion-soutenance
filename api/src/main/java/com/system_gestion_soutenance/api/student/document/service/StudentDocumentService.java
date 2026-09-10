package com.system_gestion_soutenance.api.student.document.service;

import com.system_gestion_soutenance.api.student.document.entity.StudentDocument;
import com.system_gestion_soutenance.api.student.document.repository.StudentDocumentRepository;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.notification.event.StudentDocumentSubmittedEvent;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import org.springframework.context.ApplicationEventPublisher;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.UnauthorizedAccessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
@SuppressWarnings("PMD")

@Service
public class StudentDocumentService {

	private final StudentDocumentRepository repository;
	private final StudentRepository studentRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final SecurityService securityService;
	private final Path uploadDir = Paths.get("uploads");

	@Value("${app.document.max-file-size-mb:10}")
	private long maxFileSizeMb;

	@Value("${app.document.allowed-extensions:pdf,doc,docx}")
	private String allowedExtensions;

	@Value("${app.document.version-limit:5}")
	private int versionLimit;

	public StudentDocumentService(StudentDocumentRepository repository, StudentRepository studentRepository,
			ApplicationEventPublisher eventPublisher, SecurityService securityService) {
		this.repository = repository;
		this.studentRepository = studentRepository;
		this.eventPublisher = eventPublisher;
		this.securityService = securityService;
	}

	public List<StudentDocument> findByStudent(Long studentId) {
		return repository.findByStudentId(studentId);
	}

	public PaginatedResponse<StudentDocument> findByStudent(Long studentId, int page, int limit) {
		Page<StudentDocument> docPage = repository.findByStudentId(studentId, PageRequest.of(page, limit));
		return new PaginatedResponse<>(docPage.getContent(), docPage.getTotalElements(), docPage.getTotalPages(), page,
				limit);
	}

	public StudentDocument upload(Long id, Long currentUserId, MultipartFile file) {
		StudentDocument doc = repository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Document non trouve"));

		if (!doc.getStudentId().equals(currentUserId)) {
			throw new UnauthorizedAccessException("Vous ne pouvez modifier que vos propres documents");
		}

		if (doc.getDeadline() != null && LocalDate.now().isAfter(doc.getDeadline())) {
			throw new IllegalArgumentException("Le delai de soumission est expire. Date limite: " + doc.getDeadline());
		}

		long maxBytes = maxFileSizeMb * 1024L * 1024L;
		if (file.getSize() > maxBytes) {
			throw new IllegalArgumentException("Fichier trop volumineux. Taille maximale: " + maxFileSizeMb + " Mo");
		}
		String originalName = file.getOriginalFilename();
		if (originalName != null) {
			String ext = originalName.contains(".")
					? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
					: "";
			String allowed = allowedExtensions.toLowerCase();
			if (!List.of(allowed.split(",")).contains(ext)) {
				throw new IllegalArgumentException(
						"Extension non autorisee. Extensions acceptees: " + allowedExtensions);
			}
		}
		if (doc.getFilePath() != null) {
			String[] parts = doc.getFilePath().split("[\\\\/]");
			int currentVersion = 1;
			for (String p : parts) {
				if (p.matches("^v\\d+$")) {
					currentVersion = Integer.parseInt(p.substring(1)) + 1;
				}
			}
			if (currentVersion > versionLimit) {
				throw new IllegalArgumentException(
						"Limite de versions atteinte. Nombre maximal de versions: " + versionLimit);
			}
		}

		try {
			Files.createDirectories(uploadDir);
			String filename = id + "_" + file.getOriginalFilename();
			Path target = uploadDir.resolve(filename);
			file.transferTo(target.toFile());

			doc.setFilePath(target.toString());
			doc.setSubmittedAt(LocalDateTime.now());
			doc.setStatus("submitted");
			StudentDocument saved = repository.save(doc);

			com.system_gestion_soutenance.api.user.entity.Student student = studentRepository.findById(currentUserId)
					.orElseThrow(() -> new EntityNotFoundException("Étudiant introuvable"));

			eventPublisher.publishEvent(new StudentDocumentSubmittedEvent(securityService.getCurrentUserEmail(),
					saved.getId(), saved.getName(), student.getFirstName() + " " + student.getLastName()));

			return saved;
		} catch (IOException e) {
			throw new RuntimeException("Erreur lors du telechargement du fichier");
		}
	}

	public byte[] download(Long id, Long currentUserId) {
		StudentDocument doc = repository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Document non trouve"));

		if (!doc.getStudentId().equals(currentUserId)) {
			throw new UnauthorizedAccessException("Vous ne pouvez télécharger que vos propres documents");
		}
		if (doc.getFilePath() == null) {
			throw new EntityNotFoundException("Aucun fichier telecharge pour ce document");
		}
		try {
			return Files.readAllBytes(Path.of(doc.getFilePath()));
		} catch (IOException e) {
			throw new RuntimeException("Erreur lors de la lecture du fichier");
		}
	}
}
