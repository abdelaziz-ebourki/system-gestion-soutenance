package com.system_gestion_soutenance.api.student.defense.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
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
class StudentDefenseServiceTest {

	@Mock
	private GroupRepository groupRepository;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private JuryRepository juryRepository;
	@Mock
	private SlotAssignmentRepository slotAssignmentRepository;

	@InjectMocks
	private StudentDefenseService service;

	@Test
	void getDefense_noGroup_throws() {
		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> service.getDefense(1L));
	}

	@Test
	void getDefense_noProject_throws() {
		Group group = new Group();
		group.setStudents(List.of(student(1L)));
		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.of(group));
		assertThrows(ResponseStatusException.class, () -> service.getDefense(1L));
	}

	@Test
	void getDefense_withProjectAndSchedule_returnsDefense() {
		Teacher supervisor = new Teacher();
		supervisor.setFirstName("John");
		supervisor.setLastName("Doe");

		Project project = new Project();
		project.setId(10L);
		project.setTitle("Projet Test");
		project.setDescription("Description");
		project.setSupervisor(supervisor);

		Group group = new Group();
		group.setProject(project);
		group.setStudents(List.of(student(1L)));

		SlotAssignment slot = new SlotAssignment();
		slot.setProjectId(10L);
		slot.setDate("2026-06-15");
		slot.setTime("09:00");

		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.of(group));
		when(juryRepository.findByProjectId(10L)).thenReturn(List.of());
		when(slotAssignmentRepository.findByProjectId(10L)).thenReturn(List.of(slot));

		Map<String, Object> result = service.getDefense(1L);

		assertEquals("Projet Test", result.get("projectTitle"));
		assertEquals("John Doe", result.get("supervisorName"));
		assertEquals("2026-06-15", result.get("date"));
		assertEquals("09:00", result.get("startTime"));
		assertEquals("scheduled", result.get("status"));
	}

	@Test
	void getDefense_withNullSupervisor_returnsNullSupervisorName() {
		Project project = new Project();
		project.setId(10L);
		project.setTitle("Projet Test");
		project.setSupervisor(null);

		Group group = new Group();
		group.setProject(project);
		group.setStudents(List.of(student(1L)));

		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.of(group));
		when(juryRepository.findByProjectId(10L)).thenReturn(List.of());
		when(slotAssignmentRepository.findByProjectId(10L)).thenReturn(List.of());

		Map<String, Object> result = service.getDefense(1L);

		assertNull(result.get("supervisorName"));
	}

	@Test
	void getDefense_withNullJuryTeacher_skipsMember() {
		Project project = new Project();
		project.setId(10L);
		project.setTitle("Projet Test");

		JuryMember member = new JuryMember();
		member.setTeacher(null);

		Jury jury = new Jury();
		jury.setMembers(List.of(member));

		Group group = new Group();
		group.setProject(project);
		group.setStudents(List.of(student(1L)));

		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.of(group));
		when(juryRepository.findByProjectId(10L)).thenReturn(List.of(jury));
		when(slotAssignmentRepository.findByProjectId(10L)).thenReturn(List.of());

		Map<String, Object> result = service.getDefense(1L);

		assertTrue(((List<?>) result.get("juryMembers")).isEmpty());
	}

	@Test
	void getDefense_withNullRoom_returnsEmptyRoomName() {
		Project project = new Project();
		project.setId(10L);
		project.setTitle("Projet Test");

		Group group = new Group();
		group.setProject(project);
		group.setStudents(List.of(student(1L)));

		SlotAssignment slot = new SlotAssignment();
		slot.setProjectId(10L);
		slot.setRoom(null);

		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.of(group));
		when(juryRepository.findByProjectId(10L)).thenReturn(List.of());
		when(slotAssignmentRepository.findByProjectId(10L)).thenReturn(List.of(slot));

		Map<String, Object> result = service.getDefense(1L);

		assertEquals("", result.get("roomName"));
	}

	@Test
	void findGroupForStudent_withNullStudents_returnsNull() {
		Group group = new Group();
		group.setStudents(null);

		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.of(group));

		assertThrows(ResponseStatusException.class, () -> service.getDefense(1L));
	}

	@Test
	void getDefense_withoutSchedule_returnsPending() {
		Project project = new Project();
		project.setId(10L);
		project.setTitle("Projet Test");
		project.setDescription("Description");

		Group group = new Group();
		group.setProject(project);
		group.setStudents(List.of(student(1L)));

		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.of(group));
		when(juryRepository.findByProjectId(10L)).thenReturn(List.of());
		when(slotAssignmentRepository.findByProjectId(10L)).thenReturn(List.of());

		Map<String, Object> result = service.getDefense(1L);

		assertEquals("pending", result.get("status"));
	}

	private static Student student(Long id) {
		Student s = new Student();
		s.setId(id);
		return s;
	}
}
