package com.system_gestion_soutenance.api.coordinator.jury.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.repository.JuryRoleTemplateRepository;
import com.system_gestion_soutenance.api.coordinator.jury.dto.CreateJuryRequest;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class JuryServiceTest {

	private final JuryRepository juryRepository = mock(JuryRepository.class);
	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final TeacherRepository teacherRepository = mock(TeacherRepository.class);
	private final JuryRoleTemplateRepository juryRoleTemplateRepository = mock(JuryRoleTemplateRepository.class);

	private final JuryService service = new JuryService(juryRepository, projectRepository, teacherRepository,
			juryRoleTemplateRepository);

	@Test
	void findAll_returnsAllJuries() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Projet Test");
		when(project.getDefenseType()).thenReturn("PFE");

		JuryRoleTemplate template = mock(JuryRoleTemplate.class);
		when(template.getId()).thenReturn(10L);
		when(template.getName()).thenReturn("Template Standard");

		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(5L);
		when(teacher.getFirstName()).thenReturn("John");
		when(teacher.getLastName()).thenReturn("Doe");

		JuryMember member = mock(JuryMember.class);
		when(member.getRoleName()).thenReturn("président");
		when(member.getTeacher()).thenReturn(teacher);

		Jury jury = mock(Jury.class);
		when(jury.getId()).thenReturn(1L);
		when(jury.getProject()).thenReturn(project);
		when(jury.getTemplateId()).thenReturn(10L);
		when(jury.getTemplateName()).thenReturn("Template Standard");
		when(jury.getMembers()).thenReturn(List.of(member));

		when(juryRepository.findAllWithDetails()).thenReturn(List.of(jury));

		var result = service.findAll();

		assertEquals(1, result.size());
		assertEquals("Projet Test", result.get(0).projectTitle());
	}

	@Test
	void create_withValidRequest_returnsJury() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Projet");
		when(project.getDefenseType()).thenReturn("PFE");

		JuryRoleTemplate template = mock(JuryRoleTemplate.class);
		when(template.getId()).thenReturn(10L);
		when(template.getName()).thenReturn("Template");

		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(5L);
		when(teacher.getFirstName()).thenReturn("John");
		when(teacher.getLastName()).thenReturn("Doe");

		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(juryRoleTemplateRepository.findById(10L)).thenReturn(Optional.of(template));
		when(teacherRepository.findById(5L)).thenReturn(Optional.of(teacher));

		Jury savedJury = mock(Jury.class);
		when(savedJury.getId()).thenReturn(1L);
		when(savedJury.getProject()).thenReturn(project);
		when(savedJury.getTemplateId()).thenReturn(10L);
		when(savedJury.getTemplateName()).thenReturn("Template");
		when(savedJury.getMembers()).thenReturn(List.of());

		when(juryRepository.save(any(Jury.class))).thenReturn(savedJury);

		CreateJuryRequest.MemberEntry member = new CreateJuryRequest.MemberEntry(5L, "président");
		CreateJuryRequest request = new CreateJuryRequest(1L, 10L, List.of(member));
		var result = service.create(request);

		assertEquals("Projet", result.projectTitle());
	}

	@Test
	void create_projectNotFound_throwsException() {
		when(projectRepository.findById(99L)).thenReturn(Optional.empty());

		CreateJuryRequest request = new CreateJuryRequest(99L, 1L, List.of());

		assertThrows(ResponseStatusException.class, () -> service.create(request));
	}

	@Test
	void create_templateNotFound_throwsException() {
		when(projectRepository.findById(1L)).thenReturn(Optional.of(mock(Project.class)));
		when(juryRoleTemplateRepository.findById(99L)).thenReturn(Optional.empty());

		CreateJuryRequest request = new CreateJuryRequest(1L, 99L, List.of());

		assertThrows(ResponseStatusException.class, () -> service.create(request));
	}

	@Test
	void create_duplicateTeacher_throwsException() {
		when(projectRepository.findById(1L)).thenReturn(Optional.of(mock(Project.class)));
		when(juryRoleTemplateRepository.findById(1L)).thenReturn(Optional.of(mock(JuryRoleTemplate.class)));

		CreateJuryRequest.MemberEntry m1 = new CreateJuryRequest.MemberEntry(5L, "président");
		CreateJuryRequest.MemberEntry m2 = new CreateJuryRequest.MemberEntry(5L, "examinateur");
		CreateJuryRequest request = new CreateJuryRequest(1L, 1L, List.of(m1, m2));

		assertThrows(ResponseStatusException.class, () -> service.create(request));
	}

	@Test
	void create_teacherNotFound_throwsException() {
		Project project = mock(Project.class);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(juryRoleTemplateRepository.findById(1L)).thenReturn(Optional.of(mock(JuryRoleTemplate.class)));
		when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

		CreateJuryRequest.MemberEntry member = new CreateJuryRequest.MemberEntry(99L, "président");
		CreateJuryRequest request = new CreateJuryRequest(1L, 1L, List.of(member));

		assertThrows(ResponseStatusException.class, () -> service.create(request));
	}

	@Test
	void update_withProjectId_updatesProject() {
		Jury jury = mock(Jury.class);
		Project newProject = mock(Project.class);
		when(newProject.getId()).thenReturn(2L);
		when(newProject.getTitle()).thenReturn("New Proj");
		when(newProject.getDefenseType()).thenReturn("Stage");

		when(juryRepository.findById(1L)).thenReturn(Optional.of(jury));
		when(projectRepository.findById(2L)).thenReturn(Optional.of(newProject));

		when(juryRepository.save(jury)).thenReturn(jury);
		when(jury.getId()).thenReturn(1L);
		when(jury.getProject()).thenReturn(newProject);
		when(jury.getTemplateId()).thenReturn(null);
		when(jury.getTemplateName()).thenReturn(null);
		when(jury.getMembers()).thenReturn(List.of());

		var result = service.update(1L,
				new com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest(2L, null, List.of()));

		verify(jury).setProject(newProject);
		assertEquals("New Proj", result.projectTitle());
	}

	@Test
	void update_withTemplateId_updatesTemplate() {
		Jury jury = mock(Jury.class);
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("P");
		when(project.getDefenseType()).thenReturn("PFE");

		JuryRoleTemplate template = mock(JuryRoleTemplate.class);
		when(template.getId()).thenReturn(20L);
		when(template.getName()).thenReturn("Template B");

		when(juryRepository.findById(1L)).thenReturn(Optional.of(jury));
		when(juryRoleTemplateRepository.findById(20L)).thenReturn(Optional.of(template));

		when(juryRepository.save(jury)).thenReturn(jury);
		when(jury.getId()).thenReturn(1L);
		when(jury.getProject()).thenReturn(project);
		when(jury.getTemplateId()).thenReturn(20L);
		when(jury.getTemplateName()).thenReturn("Template B");
		when(jury.getMembers()).thenReturn(List.of());

		var result = service.update(1L,
				new com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest(null, 20L, List.of()));

		verify(jury).setTemplate(template);
		assertEquals("Template B", result.templateName());
	}

	@Test
	void update_withNewMembers_updatesMembers() {
		Jury jury = mock(Jury.class);
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("P");
		when(project.getDefenseType()).thenReturn("PFE");

		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(5L);
		when(teacher.getFirstName()).thenReturn("John");
		when(teacher.getLastName()).thenReturn("Doe");

		List<JuryMember> existingMembers = new ArrayList<>();
		when(jury.getMembers()).thenReturn(existingMembers);

		when(juryRepository.findById(1L)).thenReturn(Optional.of(jury));
		when(teacherRepository.findById(5L)).thenReturn(Optional.of(teacher));

		when(juryRepository.save(jury)).thenReturn(jury);
		when(jury.getId()).thenReturn(1L);
		when(jury.getProject()).thenReturn(project);
		when(jury.getTemplateId()).thenReturn(null);
		when(jury.getTemplateName()).thenReturn(null);
		when(jury.getMembers()).thenReturn(existingMembers);

		com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest updates = new com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest(
				null, null,
				List.of(new com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest.MemberEntry(5L,
						"président")));
		service.update(1L, updates);

		assertEquals(1, existingMembers.size());
		assertEquals("président", existingMembers.get(0).getRoleName());
	}

	@Test
	void update_juryNotFound_throwsException() {
		when(juryRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> service.update(99L,
				new com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest(1L, null, List.of())));
	}

	@Test
	void delete_existingJury_deletes() {
		when(juryRepository.existsById(1L)).thenReturn(true);

		service.delete(1L);

		verify(juryRepository).deleteById(1L);
	}

	@Test
	void delete_juryNotFound_throwsException() {
		when(juryRepository.existsById(99L)).thenReturn(false);

		assertThrows(ResponseStatusException.class, () -> service.delete(99L));
	}

	@Test
	void update_duplicateMembersInUpdate_throwsException() {
		Jury jury = mock(Jury.class);
		when(jury.getMembers()).thenReturn(new ArrayList<>());

		when(juryRepository.findById(1L)).thenReturn(Optional.of(jury));
		when(teacherRepository.findById(5L)).thenReturn(Optional.of(mock(Teacher.class)));

		com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest updates = new com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest(
				null, null,
				List.of(new com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest.MemberEntry(5L,
						"président"),
						new com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest.MemberEntry(5L,
								"examinateur")));

		assertThrows(ResponseStatusException.class, () -> service.update(1L, updates));
	}
}
