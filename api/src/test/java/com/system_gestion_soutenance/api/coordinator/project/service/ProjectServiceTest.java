package com.system_gestion_soutenance.api.coordinator.project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.project.dto.CreateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.exception.ResourceConflictException;
import com.system_gestion_soutenance.api.coordinator.project.dto.*;
import com.system_gestion_soutenance.api.notification.event.ProjectStatusChangedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class ProjectServiceTest {

	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final TeacherRepository teacherRepository = mock(TeacherRepository.class);
	private final StudentRepository studentRepository = mock(StudentRepository.class);
	private final GroupRepository groupRepository = mock(GroupRepository.class);
	private final DefenseRepository defenseRepository = mock(DefenseRepository.class);
	private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
	private final SecurityService securityService = mock(SecurityService.class);
	private final UserRepository userRepository = mock(UserRepository.class);

	private final ProjectService service = new ProjectService(projectRepository, teacherRepository, groupRepository,
			defenseRepository, eventPublisher, securityService, userRepository);

	@Test
	void findAll_returnsAllProjects() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Projet Test");
		when(project.getDescription()).thenReturn("Description");
		when(project.getDefenseType()).thenReturn("PFE");
		when(project.getStatus()).thenReturn(ProjectStatus.PENDING);
		when(project.getSupervisor()).thenReturn(null);
		when(projectRepository.findAllWithDetails()).thenReturn(List.of(project));

		var result = service.findAll();

		assertEquals(1, result.size());
		assertEquals("Projet Test", result.get(0).getTitle());
	}

	@Test
	void create_withValidRequest_returnsProject() {
		Teacher supervisor = mock(Teacher.class);
		when(supervisor.getId()).thenReturn(1L);
		when(supervisor.getFirstName()).thenReturn("John");
		when(supervisor.getLastName()).thenReturn("Doe");

		Student student = mock(Student.class);
		when(student.getId()).thenReturn(10L);
		when(student.getFirstName()).thenReturn("Jane");
		when(student.getLastName()).thenReturn("Smith");

		Project savedProject = mock(Project.class);
		when(savedProject.getId()).thenReturn(1L);
		when(savedProject.getTitle()).thenReturn("New Project");
		when(savedProject.getDescription()).thenReturn("A description");
		when(savedProject.getDefenseType()).thenReturn("PFE");
		when(savedProject.getStatus()).thenReturn(ProjectStatus.PENDING);
		when(savedProject.getSupervisor()).thenReturn(supervisor);

		when(teacherRepository.findById(1L)).thenReturn(Optional.of(supervisor));
		when(studentRepository.findAllById(List.of(10L))).thenReturn(List.of(student));
		when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

		CreateProjectRequest request = new CreateProjectRequest("New Project", "A description", 1L, "PFE", List.of(10L),
				null);
		var result = service.create(request);

		assertEquals("New Project", result.getTitle());
		assertEquals("John", result.getSupervisor().getFirstName());
	}

	@Test
	void create_supervisorNotFound_throwsException() {
		when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

		CreateProjectRequest request = new CreateProjectRequest("Title", "Desc", 99L, "PFE", List.of(), null);

		assertThrows(InvalidBusinessStateException.class, () -> service.create(request));
	}

	@Test
	void create_withNullStudentIds_createsWithoutStudents() {
		Teacher supervisor = mock(Teacher.class);
		when(supervisor.getId()).thenReturn(1L);

		Project savedProject = mock(Project.class);
		when(savedProject.getId()).thenReturn(1L);
		when(savedProject.getTitle()).thenReturn("Project");
		when(savedProject.getDescription()).thenReturn("Desc");
		when(savedProject.getDefenseType()).thenReturn("PFE");
		when(savedProject.getStatus()).thenReturn(ProjectStatus.PENDING);
		when(savedProject.getSupervisor()).thenReturn(supervisor);

		when(teacherRepository.findById(1L)).thenReturn(Optional.of(supervisor));
		when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

		CreateProjectRequest request = new CreateProjectRequest("Project", "Desc", 1L, "PFE", null, null);
		var result = service.create(request);

		assertEquals("Project", result.getTitle());
	}

	@Test
	void update_withTitle_updatesTitle() {
		Project project = mock(Project.class);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(projectRepository.save(project)).thenReturn(project);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Updated");
		when(project.getDescription()).thenReturn("Desc");
		when(project.getDefenseType()).thenReturn("PFE");
		when(project.getStatus()).thenReturn(ProjectStatus.PENDING);
		when(project.getSupervisor()).thenReturn(null);

		var result = service.update(1L,
				new com.system_gestion_soutenance.api.coordinator.project.dto.UpdateProjectRequest("Updated", "Desc",
						"PFE", null));

		verify(project).setTitle("Updated");
		assertEquals("Updated", result.getTitle());
	}

	@Test
	void update_projectNotFound_throwsException() {
		when(projectRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class,
				() -> service.update(99L,
						new com.system_gestion_soutenance.api.coordinator.project.dto.UpdateProjectRequest("X", "Desc",
								"PFE", null)));
	}

	@Test
	void delete_withNoReferences_deletesProject() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.empty());
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());

		service.delete(1L);

		verify(projectRepository).delete(project);
	}

	@Test
	void delete_projectNotFound_throwsException() {
		when(projectRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.delete(99L));
	}

	@Test
	void delete_withDefenseAttached_throwsException() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(mock(Defense.class)));

		assertThrows(ResourceConflictException.class, () -> service.delete(1L));
	}

	@Test
	void delete_withGroupAttached_throwsException() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.empty());
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of(mock(Group.class)));

		assertThrows(ResourceConflictException.class, () -> service.delete(1L));
	}

	@Test
	void findAll_withPagination_returnsPaginatedResponse() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Projet");
		when(project.getStatus()).thenReturn(ProjectStatus.PENDING);
		when(project.getSupervisor()).thenReturn(null);

		Page<Project> page = new PageImpl<>(List.of(project));
		when(projectRepository.findAllWithDetails(PageRequest.of(0, 10))).thenReturn(page);

		var result = service.findAll(0, 10);

		assertEquals(1, result.items().size());
		assertEquals(1, result.total());
	}

	@Test
	void buildProjectGroupIdMap_withProjects_returnsMap() {
		Project p1 = mock(Project.class);
		when(p1.getId()).thenReturn(1L);
		Project p2 = mock(Project.class);
		when(p2.getId()).thenReturn(2L);

		Group g = mock(Group.class);
		when(g.getProject()).thenReturn(p1);
		when(g.getId()).thenReturn(10L);

		when(groupRepository.findByProjectIdIn(List.of(1L, 2L))).thenReturn(List.of(g));

		var result = service.buildProjectGroupIdMap(List.of(p1, p2));

		assertEquals(10L, result.get(1L));
		assertNull(result.get(2L));
	}

	@Test
	void buildProjectGroupIdMap_withEmptyList_returnsEmptyMap() {
		var result = service.buildProjectGroupIdMap(List.of());
		assertTrue(result.isEmpty());
	}

	@Test
	void updateStatus_fromPendingToApproved_succeeds() {
		Project project = mock(Project.class);
		when(project.getStatus()).thenReturn(ProjectStatus.PENDING);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(projectRepository.save(any())).thenAnswer(i -> {
			Project p = i.getArgument(0);
			when(p.getStatus()).thenReturn(ProjectStatus.APPROVED);
			return p;
		});
		when(project.getTitle()).thenReturn("Projet");
		when(project.getId()).thenReturn(1L);
		when(securityService.getCurrentUserEmail()).thenReturn("admin@test.com");

		var result = service.updateStatus(1L, ProjectStatus.APPROVED);

		assertEquals(ProjectStatus.APPROVED, result.getStatus());
		verify(eventPublisher).publishEvent(any(ProjectStatusChangedEvent.class));
	}

	@Test
	void updateStatus_fromPendingToRejected_succeeds() {
		Project project = mock(Project.class);
		when(project.getStatus()).thenReturn(ProjectStatus.PENDING);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(projectRepository.save(any())).thenAnswer(i -> {
			Project p = i.getArgument(0);
			when(p.getStatus()).thenReturn(ProjectStatus.REJECTED);
			return p;
		});
		when(project.getTitle()).thenReturn("Projet");
		when(project.getId()).thenReturn(1L);
		when(securityService.getCurrentUserEmail()).thenReturn("admin@test.com");

		var result = service.updateStatus(1L, ProjectStatus.REJECTED);

		assertEquals(ProjectStatus.REJECTED, result.getStatus());
	}

	@Test
	void updateStatus_sameStatus_throwsException() {
		Project project = mock(Project.class);
		when(project.getStatus()).thenReturn(ProjectStatus.PENDING);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

		assertThrows(InvalidBusinessStateException.class, () -> service.updateStatus(1L, ProjectStatus.PENDING));
	}

	@Test
	void updateStatus_fromApprovedToPending_succeeds() {
		Project project = mock(Project.class);
		when(project.getStatus()).thenReturn(ProjectStatus.APPROVED);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(projectRepository.save(any())).thenAnswer(i -> {
			Project p = i.getArgument(0);
			when(p.getStatus()).thenReturn(ProjectStatus.PENDING);
			return p;
		});
		when(project.getTitle()).thenReturn("Projet");
		when(project.getId()).thenReturn(1L);
		when(securityService.getCurrentUserEmail()).thenReturn("admin@test.com");

		var result = service.updateStatus(1L, ProjectStatus.PENDING);

		assertEquals(ProjectStatus.PENDING, result.getStatus());
	}

	@Test
	void updateStatus_fromApprovedToRejected_throwsException() {
		Project project = mock(Project.class);
		when(project.getStatus()).thenReturn(ProjectStatus.APPROVED);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

		assertThrows(InvalidBusinessStateException.class, () -> service.updateStatus(1L, ProjectStatus.REJECTED));
	}

	@Test
	void updateStatus_fromRejectedToPending_succeeds() {
		Project project = mock(Project.class);
		when(project.getStatus()).thenReturn(ProjectStatus.REJECTED);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(projectRepository.save(any())).thenAnswer(i -> {
			Project p = i.getArgument(0);
			when(p.getStatus()).thenReturn(ProjectStatus.PENDING);
			return p;
		});
		when(project.getTitle()).thenReturn("Projet");
		when(project.getId()).thenReturn(1L);
		when(securityService.getCurrentUserEmail()).thenReturn("admin@test.com");

		var result = service.updateStatus(1L, ProjectStatus.PENDING);

		assertEquals(ProjectStatus.PENDING, result.getStatus());
	}

	@Test
	void updateStatus_fromRejectedToApproved_throwsException() {
		Project project = mock(Project.class);
		when(project.getStatus()).thenReturn(ProjectStatus.REJECTED);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

		assertThrows(InvalidBusinessStateException.class, () -> service.updateStatus(1L, ProjectStatus.APPROVED));
	}

	@Test
	void bulkImport_withValidEntries_importsSuccessfully() {
		Teacher supervisor = mock(Teacher.class);
		when(supervisor.getId()).thenReturn(1L);

		Student student = mock(Student.class);
		when(student.getId()).thenReturn(10L);

		Project saved = mock(Project.class);
		when(saved.getId()).thenReturn(1L);
		when(saved.getTitle()).thenReturn("Projet 1");

		when(teacherRepository.findById(1L)).thenReturn(Optional.of(supervisor));
		when(studentRepository.findAllById(List.of(10L))).thenReturn(List.of(student));
		when(projectRepository.save(any())).thenReturn(saved);

		BulkProjectRequest request = new BulkProjectRequest(
				List.of(new BulkProjectEntry("Projet 1", "Description", 1L, null, "PFE", List.of(10L))));

		var result = service.bulkImport(request);

		assertEquals(1, result.imported());
		assertEquals(0, result.errors().size());
	}

	@Test
	void bulkImport_withInvalidSupervisor_addsError() {
		when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

		BulkProjectRequest request = new BulkProjectRequest(
				List.of(new BulkProjectEntry("Projet", "Desc", 99L, null, "PFE", List.of())));

		var result = service.bulkImport(request);

		assertEquals(0, result.imported());
		assertEquals(1, result.errors().size());
	}

	@Test
	void bulkImport_withMissingStudents_addsError() {
		Teacher supervisor = mock(Teacher.class);
		when(supervisor.getId()).thenReturn(1L);
		when(teacherRepository.findById(1L)).thenReturn(Optional.of(supervisor));
		when(studentRepository.findAllById(List.of(10L, 20L))).thenReturn(List.of());

		BulkProjectRequest request = new BulkProjectRequest(
				List.of(new BulkProjectEntry("Projet", "Desc", 1L, null, "PFE", List.of(10L, 20L))));

		var result = service.bulkImport(request);

		assertEquals(0, result.imported());
		assertTrue(result.errors().size() > 0);
	}

	@Test
	void bulkImport_withException_duringSave_addsError() {
		Teacher supervisor = mock(Teacher.class);
		when(supervisor.getId()).thenReturn(1L);
		when(teacherRepository.findById(1L)).thenReturn(Optional.of(supervisor));
		when(projectRepository.save(any())).thenThrow(new RuntimeException("DB error"));

		BulkProjectRequest request = new BulkProjectRequest(
				List.of(new BulkProjectEntry("Projet", "Desc", 1L, null, "PFE", List.of())));

		var result = service.bulkImport(request);

		assertEquals(0, result.imported());
		assertEquals(1, result.errors().size());
	}
}
