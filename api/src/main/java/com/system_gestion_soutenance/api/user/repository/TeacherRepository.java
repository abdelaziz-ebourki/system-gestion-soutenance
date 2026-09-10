package com.system_gestion_soutenance.api.user.repository;

import com.system_gestion_soutenance.api.user.entity.Teacher;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
	List<Teacher> findByTeacherRankId(Long teacherRankId);

	List<Teacher> findByDepartmentId(Long departmentId);
}
