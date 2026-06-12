package com.system_gestion_soutenance.api.coordinator.project.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.system_gestion_soutenance.api.admin.config.teacherrank.entity.TeacherRank;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus;
import com.system_gestion_soutenance.api.user.entity.Role;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class ProjectRepositoryTest {

	@Autowired
	private ProjectRepository repository;

	@Autowired
	private TestEntityManager em;

	private Teacher savedTeacher;
	private Student savedStudent;

	@BeforeEach
	void setUp() {
		Faculty faculty = new Faculty();
		faculty.setName("FS");
		faculty.setCode("FS");
		em.persist(faculty);

		Department department = new Department();
		department.setName("Informatique");
		department.setCode("INFO");
		department.setFaculty(faculty);
		em.persist(department);

		TeacherRank teacherRank = new TeacherRank();
		teacherRank.setName("Professeur");
		em.persist(teacherRank);

		Major major = new Major();
		major.setName("Génie Info");
		em.persist(major);

		Level level = new Level();
		level.setName("Master 2");
		em.persist(level);

		Teacher teacher = new Teacher();
		teacher.setEmail("teacher@test.com");
		teacher.setPassword("pass");
		teacher.setRole(Role.TEACHER);
		teacher.setLastName("Martin");
		teacher.setFirstName("Jean");
		teacher.setActive(true);
		teacher.setTeacherRank(teacherRank);
		teacher.setDepartment(department);
		savedTeacher = em.persist(teacher);

		Student student = new Student();
		student.setEmail("student@test.com");
		student.setPassword("pass");
		student.setRole(Role.STUDENT);
		student.setLastName("Dupont");
		student.setFirstName("Marie");
		student.setActive(true);
		student.setCne("CNE123");
		student.setMajor(major);
		student.setLevel(level);
		savedStudent = em.persist(student);
	}

	@Test
	void findAllWithDetails_returnsProjectsWithStudentsAndSupervisor() {
		Project project = new Project();
		project.setTitle("Projet Test");
		project.setDescription("Description");
		project.setDefenseType("PFE");
		project.setStatus(ProjectStatus.PENDING);
		project.setSupervisor(savedTeacher);
		project.setStudents(List.of(savedStudent));
		em.persist(project);

		em.flush();
		em.clear();

		List<Project> result = repository.findAllWithDetails();

		assertEquals(1, result.size());
		assertNotNull(result.get(0).getSupervisor());
		assertEquals("Martin", result.get(0).getSupervisor().getLastName());
		assertEquals(1, result.get(0).getStudents().size());
	}

	@Test
	void findAllWithDetails_noStudents_returnsProjectWithNullCollections() {
		Project project = new Project();
		project.setTitle("Solo Project");
		project.setDescription("No students yet");
		project.setDefenseType("PFE");
		project.setStatus(ProjectStatus.PENDING);
		project.setSupervisor(savedTeacher);
		project.setStudents(List.of());
		em.persist(project);

		em.flush();
		em.clear();

		List<Project> result = repository.findAllWithDetails();

		assertEquals(1, result.size());
		assertEquals("Solo Project", result.get(0).getTitle());
	}

	@Test
	void findBySupervisorId_returnsProjects() {
		Project project = new Project();
		project.setTitle("Teacher Project");
		project.setDescription("Desc");
		project.setDefenseType("MEMOIRE");
		project.setStatus(ProjectStatus.PENDING);
		project.setSupervisor(savedTeacher);
		project.setStudents(List.of(savedStudent));
		em.persist(project);

		em.flush();
		em.clear();

		List<Project> result = repository.findBySupervisorId(savedTeacher.getId());

		assertEquals(1, result.size());
		assertEquals("Teacher Project", result.get(0).getTitle());
	}

	@Test
	void findByStudentsId_returnsProject() {
		Project project = new Project();
		project.setTitle("Student Project");
		project.setDescription("Desc");
		project.setDefenseType("PFE");
		project.setStatus(ProjectStatus.PENDING);
		project.setSupervisor(savedTeacher);
		project.setStudents(List.of(savedStudent));
		em.persist(project);

		em.flush();
		em.clear();

		List<Project> result = repository.findByStudentsId(savedStudent.getId());

		assertEquals(1, result.size());
		assertEquals("Student Project", result.get(0).getTitle());
	}
}
