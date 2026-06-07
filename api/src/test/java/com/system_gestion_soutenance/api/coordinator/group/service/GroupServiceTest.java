package com.system_gestion_soutenance.api.coordinator.group.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

	private final GroupService service = new GroupService(groupRepository, projectRepository, studentRepository);

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

		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
		when(studentRepository.findAllById(List.of(1L))).thenReturn(List.of(student));
		when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);

		CreateGroupRequest request = new CreateGroupRequest("Groupe A", 10L, List.of(1L), 100L);
		var result = service.create(request);

		assertEquals("Groupe A", result.getGroupName());
		assertEquals(Long.valueOf(10L), result.getProject().getId());
	}

	@Test
	void create_projectNotFound_throwsException() {
		when(projectRepository.findById(99L)).thenReturn(Optional.empty());

		CreateGroupRequest request = new CreateGroupRequest("Groupe", 99L, List.of(), null);

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

		CreateGroupRequest request = new CreateGroupRequest("Groupe", 1L, null, null);
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
}
