package com.system_gestion_soutenance.api.coordinator.jury.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.system_gestion_soutenance.api.admin.config.grade.entity.Grade;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.TemplateRole;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseType;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
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
class JuryRepositoryTest {

	@Autowired
	private JuryRepository repository;

	@Autowired
	private TestEntityManager em;

	private Teacher savedTeacher;
	private Student savedStudent;
	private Project savedProject;

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

		Grade grade = new Grade();
		grade.setName("Professeur");
		em.persist(grade);

		Major major = new Major();
		major.setName("Génie Info");
		em.persist(major);

		Level level = new Level();
		level.setName("Master 2");
		em.persist(level);

		savedTeacher = new Teacher();
		savedTeacher.setEmail("teacher@test.com");
		savedTeacher.setPassword("pass");
		savedTeacher.setRole(Role.TEACHER);
		savedTeacher.setLastName("Martin");
		savedTeacher.setFirstName("Jean");
		savedTeacher.setActive(true);
		savedTeacher.setGrade(grade);
		savedTeacher.setDepartment(department);
		em.persist(savedTeacher);

		savedStudent = new Student();
		savedStudent.setEmail("student@test.com");
		savedStudent.setPassword("pass");
		savedStudent.setRole(Role.STUDENT);
		savedStudent.setLastName("Dupont");
		savedStudent.setFirstName("Marie");
		savedStudent.setActive(true);
		savedStudent.setCne("CNE123");
		savedStudent.setMajor(major);
		savedStudent.setLevel(level);
		em.persist(savedStudent);

		savedProject = new Project();
		savedProject.setTitle("Projet Test");
		savedProject.setDescription("Description");
		savedProject.setDefenseType("PFE");
		savedProject.setStatus("pending");
		savedProject.setSupervisor(savedTeacher);
		savedProject.setStudents(List.of(savedStudent));
		em.persist(savedProject);
	}

	@Test
	void findAllWithDetails_returnsJuriesWithProjectAndMembers() {
		JuryRoleTemplate template = createTemplate();
		em.persist(template);

		JuryMember member = new JuryMember();
		member.setRoleName("President");
		member.setTeacher(savedTeacher);

		Jury jury = new Jury();
		jury.setProject(savedProject);
		jury.setTemplate(template);
		jury.getMembers().add(member);
		member.setJury(jury);

		em.persist(jury);
		em.flush();
		em.clear();

		List<Jury> result = repository.findAllWithDetails();

		assertEquals(1, result.size());
		assertNotNull(result.get(0).getProject());
		assertEquals("Projet Test", result.get(0).getProject().getTitle());
		assertEquals(1, result.get(0).getMembers().size());
		assertEquals("President", result.get(0).getMembers().get(0).getRoleName());
	}

	@Test
	void findByProjectId_returnsJury() {
		JuryRoleTemplate template = createTemplate();
		em.persist(template);

		JuryMember member = new JuryMember();
		member.setRoleName("Rapporteur");
		member.setTeacher(savedTeacher);

		Jury jury = new Jury();
		jury.setProject(savedProject);
		jury.setTemplate(template);
		jury.getMembers().add(member);
		member.setJury(jury);

		em.persist(jury);
		em.flush();
		em.clear();

		List<Jury> result = repository.findByProjectId(savedProject.getId());

		assertEquals(1, result.size());
		assertEquals("Rapporteur", result.get(0).getMembers().get(0).getRoleName());
	}

	@Test
	void findByProjectId_noMatch_returnsEmpty() {
		List<Jury> result = repository.findByProjectId(999L);
		assertTrue(result.isEmpty());
	}

	private JuryRoleTemplate createTemplate() {
		JuryRoleTemplate template = new JuryRoleTemplate();
		template.setName("PFE Standard");
		template.setDefenseType(DefenseType.PFE);
		template.setRoles(List.of(new TemplateRole("President", 1, 2)));
		return template;
	}
}
