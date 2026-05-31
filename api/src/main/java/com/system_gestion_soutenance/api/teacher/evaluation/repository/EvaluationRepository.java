package com.system_gestion_soutenance.api.teacher.evaluation.repository;

import com.system_gestion_soutenance.api.teacher.evaluation.entity.Evaluation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
	List<Evaluation> findByTeacherId(Long teacherId);

	List<Evaluation> findByProjectId(Long projectId);
}
