package com.system_gestion_soutenance.api.admin.config.teacherrank.repository;

import com.system_gestion_soutenance.api.admin.config.teacherrank.entity.TeacherRank;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRankRepository extends JpaRepository<TeacherRank, Long> {
	Optional<TeacherRank> findByName(String name);
}
