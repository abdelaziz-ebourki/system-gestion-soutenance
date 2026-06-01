package com.system_gestion_soutenance.api.coordinator.group.repository;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GroupRepository extends JpaRepository<Group, Long> {
	List<Group> findByProjectId(Long projectId);

	@Query("SELECT g FROM Group g JOIN g.students s WHERE s.id = :studentId")
	Optional<Group> findByStudentId(Long studentId);

	@Query("SELECT DISTINCT g FROM Group g JOIN FETCH g.project JOIN FETCH g.students")
	List<Group> findAllWithDetails();
}
