package com.system_gestion_soutenance.api.coordinator.schedule.repository;

import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SlotAssignmentRepository extends JpaRepository<SlotAssignment, Long> {
	List<SlotAssignment> findByProjectId(Long projectId);

	boolean existsByProjectId(Long projectId);

	List<SlotAssignment> findByProjectIdIn(List<Long> projectIds);

	@Query("SELECT s FROM SlotAssignment s LEFT JOIN FETCH s.room")
	List<SlotAssignment> findAllWithRoom();
}
