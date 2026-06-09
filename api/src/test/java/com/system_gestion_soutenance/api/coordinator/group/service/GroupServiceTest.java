package com.system_gestion_soutenance.api.coordinator.group.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.group.dto.CreateGroupRequest;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;

class GroupServiceTest {

	private final GroupRepository groupRepository = mock(GroupRepository.class);
	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final StudentRepository studentRepository = mock(StudentRepository.class);
	private final DefenseSessionRepository defenseSessionRepository = mock(DefenseSessionRepository.class);

	private final GroupService service = new GroupService(groupRepository, projectRepository, studentRepository,
			defenseSessionRepository);

	@Test
	void findAll_returnsAllGroups() {
		Group group = mock(Group.class);
		when(group.getId()).thenReturn(1L);
		when(group.getGroupName()).thenReturn("Groupe A");
		when(group.getProject()).thenReturn(null);
		when(group.getStudents()).thenReturn(List.of());
		when(group.getSessionId()).thenReturn(null);
		when(groupRepository.findAllWithDetails()).thenReturn(List.of(group));

		var result = service.findAll();

		assertEquals(1, result.size());
		assertEquals("Groupe A", result.get(0).getGroupName());
	}

	@Test
	void create_withValidRequest_returnsGroup() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(10L);
		when(project.getTitle()).thenReturn("Projet Test");

		Student student = mock(Student.class);
		when(student.getId()).thenReturn(1L);
		when(student.getFirstName()).thenReturn("Jane");
		when(student.getLastName()).thenReturn("Smith");

		Group savedGroup = mock(Group.class);
		when(savedGroup.getId()).thenReturn(1L);
		when(savedGroup.getGroupName()).thenReturn("Groupe A");
		when(savedGroup.getProject()).thenReturn(project);
		when(savedGroup.getStudents()).thenReturn(List.of(student));
		when(savedGroup.getSessionId()).thenReturn(100L);
		when(savedGroup.getLeaderId()).thenReturn(1L);

		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
		when(studentRepository.findAllById(List.of(1L))).thenReturn(List.of(student));
		when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);

		CreateGroupRequest request = new CreateGroupRequest("Groupe A", 10L, List.of(1L), 100L, 1L);
		var result = service.create(request);

		assertEquals("Groupe A", result.getGroupName());
		assertEquals(Long.valueOf(10L), result.getProject().getId());
		assertEquals(1L, result.getLeaderId());
	}

	@Test
	void create_projectNotFound_throwsException() {
		when(projectRepository.findById(99L)).thenReturn(Optional.empty());

		CreateGroupRequest request = new CreateGroupRequest("Groupe", 99L, List.of(), null, null);

		assertThrows(InvalidBusinessStateException.class, () -> service.create(request));
	}

	@Test
	void create_withNullStudentIds_createsWithoutStudents() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Title");

		Group savedGroup = mock(Group.class);
		when(savedGroup.getId()).thenReturn(1L);
		when(savedGroup.getGroupName()).thenReturn("Groupe");
		when(savedGroup.getProject()).thenReturn(project);
		when(savedGroup.getStudents()).thenReturn(List.of());
		when(savedGroup.getSessionId()).thenReturn(null);

		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);

		CreateGroupRequest request = new CreateGroupRequest("Groupe", 1L, null, null, null);
		var result = service.create(request);

		assertEquals("Groupe", result.getGroupName());
	}

	@Test
	void delete_existingGroup_deletes() {
		when(groupRepository.existsById(1L)).thenReturn(true);

		service.delete(1L);

		verify(groupRepository).deleteById(1L);
	}

	@Test
	void delete_groupNotFound_throwsException() {
		when(groupRepository.existsById(99L)).thenReturn(false);

		assertThrows(EntityNotFoundException.class, () -> service.delete(99L));
	}

	@Test
	void create_leaderNotInStudentIds_throws() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(10L);

		Student s1 = mock(Student.class);
		when(s1.getId()).thenReturn(1L);

		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
		when(studentRepository.findAllById(List.of(1L))).thenReturn(List.of(s1));

		CreateGroupRequest request = new CreateGroupRequest("Groupe A", 10L, List.of(1L), null, 99L);

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> service.create(request));
		assertEquals("Le leader doit être membre du groupe", ex.getMessage());
	}

	@Test
	void create_exceedsMaxGroupSize_throws() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(10L);

		Student s1 = mock(Student.class);
		when(s1.getId()).thenReturn(1L);
		Student s2 = mock(Student.class);
		when(s2.getId()).thenReturn(2L);
		Student s3 = mock(Student.class);
		when(s3.getId()).thenReturn(3L);

		DefenseSession session = mock(DefenseSession.class);
		when(session.getMaxGroupSize()).thenReturn(2);

		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
		when(studentRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of(s1, s2, s3));
		when(defenseSessionRepository.findById(100L)).thenReturn(Optional.of(session));

		CreateGroupRequest request = new CreateGroupRequest("Groupe A", 10L, List.of(1L, 2L, 3L), 100L, null);

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> service.create(request));
		assertEquals("Le groupe a atteint sa taille maximale", ex.getMessage());
	}
}
