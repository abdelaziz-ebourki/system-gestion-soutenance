package com.system_gestion_soutenance.api.coordinator.project.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.dto.CreateProjectRequest;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ProjectServiceTest {

	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final TeacherRepository teacherRepository = mock(TeacherRepository.class);
	private final StudentRepository studentRepository = mock(StudentRepository.class);
	private final GroupRepository groupRepository = mock(GroupRepository.class);
	private final JuryRepository juryRepository = mock(JuryRepository.class);
	private final SlotAssignmentRepository slotAssignmentRepository = mock(SlotAssignmentRepository.class);

	private final ProjectService service = new ProjectService(projectRepository, teacherRepository, studentRepository,
			groupRepository, juryRepository, slotAssignmentRepository);

	@Test
	void findAll_returnsAllProjects() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Projet Test");
		when(project.getDescription()).thenReturn("Description");
		when(project.getDefenseType()).thenReturn("PFE");
		when(project.getStatus()).thenReturn("pending");
		when(project.getStudents()).thenReturn(List.of());
		when(project.getSupervisor()).thenReturn(null);
		when(projectRepository.findAllWithDetails()).thenReturn(List.of(project));

		var result = service.findAll();

		assertEquals(1, result.size());
		assertEquals("Projet Test", result.get(0).title());
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
		when(savedProject.getStatus()).thenReturn("pending");
		when(savedProject.getSupervisor()).thenReturn(supervisor);
		when(savedProject.getStudents()).thenReturn(List.of(student));

		when(teacherRepository.findById(1L)).thenReturn(Optional.of(supervisor));
		when(studentRepository.findAllById(List.of(10L))).thenReturn(List.of(student));
		when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

		CreateProjectRequest request = new CreateProjectRequest("New Project", "A description", 1L, "PFE",
				List.of(10L));
		var result = service.create(request);

		assertEquals("New Project", result.title());
		assertEquals("John Doe", result.supervisorName());
	}

	@Test
	void create_supervisorNotFound_throwsException() {
		when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

		CreateProjectRequest request = new CreateProjectRequest("Title", "Desc", 99L, "PFE", List.of());

		assertThrows(ResponseStatusException.class, () -> service.create(request));
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
		when(savedProject.getStatus()).thenReturn("pending");
		when(savedProject.getStudents()).thenReturn(List.of());
		when(savedProject.getSupervisor()).thenReturn(supervisor);

		when(teacherRepository.findById(1L)).thenReturn(Optional.of(supervisor));
		when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

		CreateProjectRequest request = new CreateProjectRequest("Project", "Desc", 1L, "PFE", null);
		var result = service.create(request);

		assertEquals("Project", result.title());
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
		when(project.getStatus()).thenReturn("pending");
		when(project.getStudents()).thenReturn(List.of());
		when(project.getSupervisor()).thenReturn(null);

		var result = service.update(1L,
				new com.system_gestion_soutenance.api.coordinator.project.dto.UpdateProjectRequest("Updated", "Desc",
						"PFE"));

		verify(project).setTitle("Updated");
		assertEquals("Updated", result.title());
	}

	@Test
	void update_projectNotFound_throwsException() {
		when(projectRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class,
				() -> service.update(99L,
						new com.system_gestion_soutenance.api.coordinator.project.dto.UpdateProjectRequest("X", "Desc",
								"PFE")));
	}

	@Test
	void delete_withNoReferences_deletesProject() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of());
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		service.delete(1L);

		verify(projectRepository).delete(project);
	}

	@Test
	void delete_projectNotFound_throwsException() {
		when(projectRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> service.delete(99L));
	}

	@Test
	void delete_withJuryAttached_throwsException() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of(mock(Jury.class)));

		assertThrows(ResponseStatusException.class, () -> service.delete(1L));
	}

	@Test
	void delete_withGroupAttached_throwsException() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of());
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of(mock(Group.class)));

		assertThrows(ResponseStatusException.class, () -> service.delete(1L));
	}

	@Test
	void delete_withSlotAssigned_throwsException() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of());
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());
		when(slotAssignmentRepository.existsByProjectId(1L)).thenReturn(true);

		assertThrows(ResponseStatusException.class, () -> service.delete(1L));
	}
}
