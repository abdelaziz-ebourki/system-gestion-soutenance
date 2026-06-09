package com.system_gestion_soutenance.api.admin.defensesession.repository;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DefenseSessionRepository extends JpaRepository<DefenseSession, Long> {
	List<DefenseSession> findByJuryRoleTemplate_Id(Long juryRoleTemplateId);

	@Query("SELECT ds FROM DefenseSession ds WHERE ds.status = com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSessionStatus.ACTIVE AND :today BETWEEN ds.startDate AND ds.endDate")
	Optional<DefenseSession> findActiveSession(LocalDate today);
}
