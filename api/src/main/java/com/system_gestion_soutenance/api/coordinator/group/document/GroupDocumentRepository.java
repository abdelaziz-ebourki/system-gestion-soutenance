package com.system_gestion_soutenance.api.coordinator.group.document;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupDocumentRepository extends JpaRepository<GroupDocument, Long> {
	List<GroupDocument> findByGroupId(Long groupId);

	Optional<GroupDocument> findByGroupIdAndType(Long groupId, GroupDocumentType type);
}
