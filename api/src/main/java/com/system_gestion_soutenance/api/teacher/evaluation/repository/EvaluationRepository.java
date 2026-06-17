package com.system_gestion_soutenance.api.teacher.evaluation.repository;

import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
	List<Evaluation> findByTeacherId(Long teacherId);

	Page<Evaluation> findByTeacherId(Long teacherId, Pageable pageable);

	List<Evaluation> findByDefense(Defense defense);

	List<Evaluation> findByDefenseIn(List<Defense> defenses);

	List<Evaluation> findByDefenseAndType(Defense defense,
			com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationType type);

	java.util.Optional<Evaluation> findByDefenseAndTeacherIdAndType(Defense defense, Long teacherId,
			com.system_gestion_soutenance.api.teacher.evaluation.entity.EvaluationType type);

}
