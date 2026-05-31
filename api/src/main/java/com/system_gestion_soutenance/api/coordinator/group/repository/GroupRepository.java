package com.system_gestion_soutenance.api.coordinator.group.repository;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
	List<Group> findByProjectId(Long projectId);
}
