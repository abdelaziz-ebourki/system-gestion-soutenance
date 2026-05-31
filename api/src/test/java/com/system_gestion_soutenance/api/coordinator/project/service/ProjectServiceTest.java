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
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
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
		when(projectRepository.findAll()).thenReturn(List.of(project));

		var result = service.findAll();

		assertEquals(1, result.size());
		assertEquals("Projet Test", result.get(0).get("title"));
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

		CreateProjectRequest request = new CreateProjectRequest("New Project", "A description", 1L, List.of(10L),
				"PFE");
		var result = service.create(request);

		assertEquals("New Project", result.get("title"));
		assertEquals("John Doe", result.get("supervisorName"));
	}

	@Test
	void create_supervisorNotFound_throwsException() {
		when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

		CreateProjectRequest request = new CreateProjectRequest("Title", "Desc", 99L, List.of(), "PFE");

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

		CreateProjectRequest request = new CreateProjectRequest("Project", "Desc", 1L, null, "PFE");
		var result = service.create(request);

		assertEquals("Project", result.get("title"));
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

		var result = service.update(1L, Map.of("title", "Updated"));

		verify(project).setTitle("Updated");
		assertEquals("Updated", result.get("title"));
	}

	@Test
	void update_projectNotFound_throwsException() {
		when(projectRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> service.update(99L, Map.of("title", "X")));
	}

	@Test
	void update_withSupervisorId_updatesSupervisor() {
		Project project = mock(Project.class);
		Teacher supervisor = mock(Teacher.class);
		when(supervisor.getId()).thenReturn(2L);
		when(supervisor.getFirstName()).thenReturn("John");
		when(supervisor.getLastName()).thenReturn("Doe");

		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(teacherRepository.findById(2L)).thenReturn(Optional.of(supervisor));
		when(projectRepository.save(project)).thenReturn(project);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Title");
		when(project.getDescription()).thenReturn("Desc");
		when(project.getDefenseType()).thenReturn("PFE");
		when(project.getStatus()).thenReturn("pending");
		when(project.getStudents()).thenReturn(List.of());
		when(project.getSupervisor()).thenReturn(supervisor);

		service.update(1L, Map.of("supervisorId", "2"));

		verify(project).setSupervisor(supervisor);
	}

	@Test
	void update_withStudentIds_updatesStudents() {
		Project project = mock(Project.class);
		Student student = mock(Student.class);
		when(student.getId()).thenReturn(5L);
		when(student.getFirstName()).thenReturn("Jane");
		when(student.getLastName()).thenReturn("Smith");

		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(studentRepository.findAllById(List.of(5L))).thenReturn(List.of(student));
		when(projectRepository.save(project)).thenReturn(project);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Title");
		when(project.getDescription()).thenReturn("Desc");
		when(project.getDefenseType()).thenReturn("PFE");
		when(project.getStatus()).thenReturn("pending");
		when(project.getStudents()).thenReturn(List.of(student));
		when(project.getSupervisor()).thenReturn(null);

		service.update(1L, Map.of("studentIds", List.of(5)));

		verify(project).setStudents(List.of(student));
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
	void update_withStudentIdsAsNumbers_parsesCorrectly() {
		Project project = mock(Project.class);
		Student student = mock(Student.class);
		when(student.getId()).thenReturn(5L);
		when(student.getFirstName()).thenReturn("Jane");
		when(student.getLastName()).thenReturn("Smith");

		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(studentRepository.findAllById(List.of(5L))).thenReturn(List.of(student));
		when(projectRepository.save(project)).thenReturn(project);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Title");
		when(project.getDescription()).thenReturn("Desc");
		when(project.getDefenseType()).thenReturn("PFE");
		when(project.getStatus()).thenReturn("pending");
		when(project.getStudents()).thenReturn(List.of(student));
		when(project.getSupervisor()).thenReturn(null);

		Map<String, Object> updates = new LinkedHashMap<>();
		updates.put("studentIds", List.of(5));
		service.update(1L, updates);

		verify(project).setStudents(List.of(student));
	}

	@Test
	void update_withStatus_updatesStatus() {
		Project project = mock(Project.class);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(projectRepository.save(project)).thenReturn(project);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Title");
		when(project.getDescription()).thenReturn("Desc");
		when(project.getDefenseType()).thenReturn("PFE");
		when(project.getStatus()).thenReturn("approved");
		when(project.getStudents()).thenReturn(List.of());
		when(project.getSupervisor()).thenReturn(null);

		service.update(1L, Map.of("status", "approved"));

		verify(project).setStatus("approved");
	}

	@Test
	void delete_withSlotAssigned_throwsException() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of());
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());
		SlotAssignment slot = mock(SlotAssignment.class);
		when(slot.getProjectId()).thenReturn(1L);
		when(slotAssignmentRepository.findAll()).thenReturn(List.of(slot));

		assertThrows(ResponseStatusException.class, () -> service.delete(1L));
	}
}
