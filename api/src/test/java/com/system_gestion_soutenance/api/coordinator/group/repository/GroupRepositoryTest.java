package com.system_gestion_soutenance.api.coordinator.group.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.system_gestion_soutenance.api.admin.config.teacherrank.entity.TeacherRank;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus;
import com.system_gestion_soutenance.api.user.entity.Role;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class GroupRepositoryTest {

	@Autowired
	private GroupRepository repository;

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
	void findAllWithDetails_returnsGroupsWithProjectAndStudents() {
		Project project = createProject("Projet A");
		em.persist(project);

		Group group = new Group();
		group.setGroupName("Groupe 1");
		group.setProject(project);
		group.setStudents(List.of(savedStudent));
		em.persist(group);

		em.flush();
		em.clear();

		List<Group> result = repository.findAllWithDetails();

		assertEquals(1, result.size());
		assertNotNull(result.get(0).getProject());
		assertEquals("Projet A", result.get(0).getProject().getTitle());
		assertEquals(1, result.get(0).getStudents().size());
	}

	@Test
	void findFirstByStudentsIdOrderByIdAsc_returnsGroup() {
		Project project = createProject("Projet B");
		em.persist(project);

		Group group = new Group();
		group.setGroupName("Groupe 2");
		group.setProject(project);
		group.setStudents(List.of(savedStudent));
		em.persist(group);

		em.flush();
		em.clear();

		Optional<Group> result = repository.findFirstByStudentsIdOrderByIdAsc(savedStudent.getId());

		assertTrue(result.isPresent());
		assertEquals("Groupe 2", result.get().getGroupName());
	}

	@Test
	void findFirstByStudentsIdOrderByIdAsc_noMatch_returnsEmpty() {
		Optional<Group> result = repository.findFirstByStudentsIdOrderByIdAsc(999L);
		assertTrue(result.isEmpty());
	}

	@Test
	void findByProjectId_returnsGroups() {
		Project project = createProject("Projet C");
		em.persist(project);

		Group group = new Group();
		group.setGroupName("Groupe 3");
		group.setProject(project);
		group.setStudents(List.of(savedStudent));
		em.persist(group);

		em.flush();
		em.clear();

		List<Group> result = repository.findByProjectId(project.getId());

		assertEquals(1, result.size());
		assertEquals("Groupe 3", result.get(0).getGroupName());
	}

	private Project createProject(String title) {
		Project project = new Project();
		project.setTitle(title);
		project.setDescription("Description");
		project.setDefenseType("PFE");
		project.setStatus(ProjectStatus.PENDING);
		project.setSupervisor(savedTeacher);
		project.setStudents(List.of(savedStudent));
		return project;
	}
}
