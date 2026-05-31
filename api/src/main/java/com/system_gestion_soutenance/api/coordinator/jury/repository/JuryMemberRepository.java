package com.system_gestion_soutenance.api.coordinator.jury.repository;

import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JuryMemberRepository extends JpaRepository<JuryMember, Long> {
	List<JuryMember> findByTeacher_Id(Long teacherId);

	List<JuryMember> findByJuryId(Long juryId);
}
