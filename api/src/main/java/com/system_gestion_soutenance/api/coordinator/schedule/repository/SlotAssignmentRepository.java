package com.system_gestion_soutenance.api.coordinator.schedule.repository;

import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotAssignmentRepository extends JpaRepository<SlotAssignment, Long> {
	List<SlotAssignment> findByProjectId(Long projectId);
}
