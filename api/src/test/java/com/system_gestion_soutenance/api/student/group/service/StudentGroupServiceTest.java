package com.system_gestion_soutenance.api.student.group.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.settings.defense.entity.DefenseSettings;
import com.system_gestion_soutenance.api.admin.config.settings.defense.repository.DefenseSettingsRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class StudentGroupServiceTest {

	@Mock
	private GroupRepository groupRepository;
	@Mock
	private StudentRepository studentRepository;
	@Mock
	private DefenseSettingsRepository defenseSettingsRepository;

	@InjectMocks
	private StudentGroupService service;

	@Test
	void getWorkspace_withNullDefenseSettings_usesDefaults() {
		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.empty());
		when(groupRepository.findAllWithDetails()).thenReturn(List.of());
		when(defenseSettingsRepository.findById(1L)).thenReturn(Optional.empty());

		com.system_gestion_soutenance.api.student.group.dto.StudentGroupWorkspaceResponse result = service
				.getWorkspace(1L);

		assertEquals("", result.groupCreationStartDate());
		assertEquals("", result.groupCreationEndDate());
		assertFalse(result.isGroupCreationOpen());
	}

	@Test
	void getWorkspace_withDefenseSettings_returnsDates() {
		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.empty());
		when(groupRepository.findAllWithDetails()).thenReturn(List.of());
		when(defenseSettingsRepository.findById(1L))
				.thenReturn(Optional.of(new DefenseSettings(1L, null, null, 0, 0, "2025-01-01", "2025-12-31")));

		com.system_gestion_soutenance.api.student.group.dto.StudentGroupWorkspaceResponse result = service
				.getWorkspace(1L);

		assertEquals("2025-01-01", result.groupCreationStartDate());
		assertEquals("2025-12-31", result.groupCreationEndDate());
	}

	@Test
	void getWorkspace_noGroup_returnsAvailable() {
		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.empty());
		when(groupRepository.findAllWithDetails()).thenReturn(List.of());
		when(defenseSettingsRepository.findById(1L)).thenReturn(Optional.empty());

		com.system_gestion_soutenance.api.student.group.dto.StudentGroupWorkspaceResponse result = service
				.getWorkspace(1L);

		assertNull(result.currentGroup());
		assertEquals(0, result.availableGroups().size());
	}

	@SuppressWarnings("unchecked")
	@Test
	void getWorkspace_inGroup_returnsCurrentGroup() {
		Student student = student(1L, "Alice", "Test");
		Group group = new Group();
		group.setId(10L);
		group.setGroupName("Groupe Test");
		group.setStudents(List.of(student));

		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.of(group));
		when(groupRepository.findAllWithDetails()).thenReturn(List.of(group));
		when(defenseSettingsRepository.findById(1L)).thenReturn(Optional.empty());

		com.system_gestion_soutenance.api.student.group.dto.StudentGroupWorkspaceResponse result = service
				.getWorkspace(1L);

		assertNotNull(result.currentGroup());
		assertEquals("Groupe Test", result.currentGroup().groupName());
	}

	@Test
	void createGroup_studentNotFound_throws() {
		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.empty());
		when(defenseSettingsRepository.findById(1L))
				.thenReturn(Optional.of(new DefenseSettings(1L, null, null, 0, 0, "2000-01-01", "2099-12-31")));
		when(studentRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> service.createGroup(1L));
	}

	@Test
	void joinGroup_studentNotFound_throws() {
		Group group = new Group();
		group.setId(10L);
		group.setStudents(new ArrayList<>());

		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.empty());
		when(defenseSettingsRepository.findById(1L))
				.thenReturn(Optional.of(new DefenseSettings(1L, null, null, 0, 0, "2000-01-01", "2099-12-31")));
		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(studentRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> service.joinGroup(10L, 1L));
	}

	@Test
	void joinGroup_withNullStudentList_initializesList() {
		Student student = student(1L, "Alice", "Test");
		Group group = new Group();
		group.setId(10L);
		group.setGroupName("Groupe Test");
		group.setStudents(null);

		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.empty());
		when(defenseSettingsRepository.findById(1L))
				.thenReturn(Optional.of(new DefenseSettings(1L, null, null, 0, 0, "2000-01-01", "2099-12-31")));
		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		com.system_gestion_soutenance.api.student.group.dto.GroupDetailsResponse result = service.joinGroup(10L, 1L);

		assertEquals(1, result.members().size());
	}

	@Test
	void joinGroup_alreadyInSpecificGroup_throws() {
		Student student = student(1L, "Alice", "Test");
		Group group = new Group();
		group.setId(10L);
		group.setStudents(new ArrayList<>(List.of(student)));

		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.of(group));

		assertThrows(ResponseStatusException.class, () -> service.joinGroup(10L, 1L));
	}

	@Test
	void createGroup_success() {
		Student student = student(1L, "Alice", "Test");
		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.empty());
		when(defenseSettingsRepository.findById(1L))
				.thenReturn(Optional.of(new DefenseSettings(1L, null, null, 0, 0, "2000-01-01", "2099-12-31")));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		com.system_gestion_soutenance.api.student.group.dto.GroupDetailsResponse result = service.createGroup(1L);

		assertEquals("Groupe de Alice Test", result.groupName());
	}

	@Test
	void createGroup_alreadyInGroup_throws() {
		Group group = new Group();
		group.setStudents(List.of(student(1L, "A", "B")));
		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.of(group));

		assertThrows(ResponseStatusException.class, () -> service.createGroup(1L));
	}

	@Test
	void createGroup_creationClosed_throws() {
		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.empty());
		when(defenseSettingsRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> service.createGroup(1L));
	}

	@Test
	void joinGroup_success() {
		Student student = student(1L, "Alice", "Test");
		Group group = new Group();
		group.setId(10L);
		group.setGroupName("Groupe Test");
		group.setStudents(new ArrayList<>(List.of(student(2L, "Bob", "Test"))));

		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.empty());
		when(defenseSettingsRepository.findById(1L))
				.thenReturn(Optional.of(new DefenseSettings(1L, null, null, 0, 0, "2000-01-01", "2099-12-31")));
		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		com.system_gestion_soutenance.api.student.group.dto.GroupDetailsResponse result = service.joinGroup(10L, 1L);

		assertEquals(2, result.members().size());
	}

	@Test
	void joinGroup_alreadyInGroup_throws() {
		Group group = new Group();
		group.setStudents(List.of(student(1L, "A", "B")));
		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.of(group));

		assertThrows(ResponseStatusException.class, () -> service.joinGroup(10L, 1L));
	}

	@Test
	void getWorkspace_groupWithNullProject_returnsNullProjectTitle() {
		Student student = student(1L, "Alice", "Test");
		Group group = new Group();
		group.setId(10L);
		group.setGroupName("Groupe Test");
		group.setStudents(List.of(student));
		group.setProject(null);

		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.of(group));
		when(groupRepository.findAllWithDetails()).thenReturn(List.of(group));
		when(defenseSettingsRepository.findById(1L)).thenReturn(Optional.empty());

		com.system_gestion_soutenance.api.student.group.dto.StudentGroupWorkspaceResponse result = service
				.getWorkspace(1L);

		com.system_gestion_soutenance.api.student.group.dto.GroupDetailsResponse current = result.currentGroup();
		assertNull(current.projectTitle());
		assertNull(current.supervisorName());
	}

	@Test
	void joinGroup_groupNotFound_throws() {
		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.empty());
		when(defenseSettingsRepository.findById(1L))
				.thenReturn(Optional.of(new DefenseSettings(1L, null, null, 0, 0, "2000-01-01", "2099-12-31")));
		when(groupRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> service.joinGroup(99L, 1L));
	}

	private static Student student(Long id, String firstName, String lastName) {
		Student s = new Student();
		s.setId(id);
		s.setFirstName(firstName);
		s.setLastName(lastName);
		return s;
	}
}
