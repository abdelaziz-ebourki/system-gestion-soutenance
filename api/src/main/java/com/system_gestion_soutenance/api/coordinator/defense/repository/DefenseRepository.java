package com.system_gestion_soutenance.api.coordinator.defense.repository;

import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DefenseRepository extends JpaRepository<Defense, Long> {
	Optional<Defense> findByProject(Project project);

	boolean existsByProject_Id(Long projectId);

	boolean existsByMembers_Teacher_Id(Long teacherId);

	long countByMembers_Teacher_Id(Long teacherId);

	@Query("SELECT DISTINCT d FROM Defense d LEFT JOIN FETCH d.project LEFT JOIN FETCH d.room LEFT JOIN FETCH d.members")
	List<Defense> findAllWithMembers();
}
