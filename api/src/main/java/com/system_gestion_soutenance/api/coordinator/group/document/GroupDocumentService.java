package com.system_gestion_soutenance.api.coordinator.group.document;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.exception.UnauthorizedAccessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GroupDocumentService {

	private final GroupDocumentRepository repository;
	private final GroupRepository groupRepository;
	private final Path uploadDir = Paths.get("uploads/group-documents");

	@Value("${app.document.max-file-size-mb:10}")
	private long maxFileSizeMb;

	@Value("${app.document.allowed-extensions:pdf,doc,docx}")
	private String allowedExtensions;

	public GroupDocumentService(GroupDocumentRepository repository, GroupRepository groupRepository) {
		this.repository = repository;
		this.groupRepository = groupRepository;
	}

	@Transactional(readOnly = true)
	public List<GroupDocument> findByGroup(Long groupId) {
		return repository.findByGroupId(groupId);
	}

	@Transactional
	public GroupDocument upload(Long groupId, GroupDocumentType type, Long currentUserId, MultipartFile file) {
		Group group = groupRepository.findById(groupId)
				.orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé"));

		if (!group.getLeaderId().equals(currentUserId)) {
			throw new UnauthorizedAccessException("Seul le chef de groupe peut télécharger des documents");
		}

		GroupDocument doc = repository.findByGroupIdAndType(groupId, type)
				.orElseThrow(() -> new EntityNotFoundException("Document non trouvé pour ce groupe et ce type"));

		if (doc.getDeadline() != null && LocalDate.now().isAfter(doc.getDeadline())) {
			throw new InvalidBusinessStateException(
					"Le délai de soumission est expiré. Date limite: " + doc.getDeadline());
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
						"Extension non autorisée. Extensions acceptées: " + allowedExtensions);
			}
		}

		try {
			Files.createDirectories(uploadDir);
			String filename = groupId + "_" + type.name().toLowerCase() + "_" + file.getOriginalFilename();
			Path target = uploadDir.resolve(filename);
			file.transferTo(target.toFile());

			doc.setFilePath(target.toString());
			doc.setSubmittedAt(LocalDateTime.now());
			doc.setStatus("submitted");
			return repository.save(doc);
		} catch (IOException e) {
			throw new RuntimeException("Erreur lors du téléchargement du fichier");
		}
	}

	@Transactional(readOnly = true)
	public byte[] download(Long groupId, GroupDocumentType type) {
		GroupDocument doc = repository.findByGroupIdAndType(groupId, type)
				.orElseThrow(() -> new EntityNotFoundException("Document non trouvé pour ce groupe et ce type"));

		if (doc.getFilePath() == null) {
			throw new EntityNotFoundException("Aucun fichier téléchargé pour ce document");
		}
		try {
			return Files.readAllBytes(Path.of(doc.getFilePath()));
		} catch (IOException e) {
			throw new RuntimeException("Erreur lors de la lecture du fichier");
		}
	}

	@Transactional
	public void createDefaultDocuments(Long groupId, LocalDate deadline) {
		LocalDate effectiveDeadline = deadline != null ? deadline : LocalDate.now().plusDays(30);

		for (GroupDocumentType type : GroupDocumentType.values()) {
			GroupDocument doc = new GroupDocument();
			doc.setGroupId(groupId);
			doc.setType(type);
			doc.setName(typeToName(type));
			doc.setDeadline(effectiveDeadline);
			doc.setStatus("missing");
			repository.save(doc);
		}
	}

	@Transactional
	public GroupDocument updateStatus(Long documentId, String status) {
		GroupDocument doc = repository.findById(documentId)
				.orElseThrow(() -> new EntityNotFoundException("Document non trouvé"));
		doc.setStatus(status);
		return repository.save(doc);
	}

	private String typeToName(GroupDocumentType type) {
		return switch (type) {
			case REPORT -> "Rapport PFE";
			case PRESENTATION -> "Fiche de présentation";
			case DIVERSE -> "Divers";
		};
	}
}
