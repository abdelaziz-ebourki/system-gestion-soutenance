package com.system_gestion_soutenance.api.student.defense.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class StudentDefenseServiceTest {

	@Mock
	private GroupRepository groupRepository;
	@Mock
	private DefenseRepository defenseRepository;

	@InjectMocks
	private StudentDefenseService service;

	@Test
	void getDefense_noGroup_throws() {
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		assertThrows(EntityNotFoundException.class, () -> service.getDefense(1L));
	}

	@Test
	void getDefense_noProject_throws() {
		Group group = new Group();
		group.setStudents(List.of(student(1L)));
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		assertThrows(EntityNotFoundException.class, () -> service.getDefense(1L));
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

		Defense defense = new Defense();
		defense.setDate(LocalDate.of(2026, 6, 15));
		defense.setTime(LocalTime.of(9, 0));

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));

		com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse result = service.getDefense(1L);

		assertEquals("Projet Test", result.projectTitle());
		assertEquals("John Doe", result.supervisorName());
		assertEquals("2026-06-15", result.date());
		assertEquals("09:00", result.startTime());
		assertEquals("scheduled", result.status());
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

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.empty());

		com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse result = service.getDefense(1L);

		assertNull(result.supervisorName());
	}

	@Test
	void getDefense_withNullJuryTeacher_skipsMember() {
		Project project = new Project();
		project.setId(10L);
		project.setTitle("Projet Test");

		JuryMember member = new JuryMember();
		member.setTeacher(null);

		Defense defense = new Defense();
		defense.setDate(LocalDate.of(2026, 6, 15));
		defense.setTime(LocalTime.of(9, 0));
		defense.setMembers(List.of(member));

		Group group = new Group();
		group.setProject(project);
		group.setStudents(List.of(student(1L)));

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));

		com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse result = service.getDefense(1L);

		assertTrue(result.juryMembers().isEmpty());
	}

	@Test
	void getDefense_withNullRoom_returnsEmptyRoomName() {
		Project project = new Project();
		project.setId(10L);
		project.setTitle("Projet Test");

		Defense defense = new Defense();
		defense.setDate(LocalDate.of(2026, 6, 15));
		defense.setTime(LocalTime.of(9, 0));
		defense.setRoom(null);

		Group group = new Group();
		group.setProject(project);
		group.setStudents(List.of(student(1L)));

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));

		com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse result = service.getDefense(1L);

		assertEquals("", result.roomName());
	}

	@Test
	void findGroupForStudent_withNullStudents_returnsNull() {
		Group group = new Group();
		group.setStudents(null);

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));

		assertThrows(EntityNotFoundException.class, () -> service.getDefense(1L));
	}

	@Test
	void getDefense_withJuryTeacher_putsMemberInResult() {
		Project project = new Project();
		project.setId(10L);
		project.setTitle("Projet Test");

		Teacher teacher = new Teacher();
		teacher.setFirstName("Jane");
		teacher.setLastName("Smith");

		JuryMember member = new JuryMember(null, teacher, "Président", null, null, null, null);

		Defense defense = new Defense();
		defense.setDate(LocalDate.of(2026, 6, 15));
		defense.setTime(LocalTime.of(9, 0));
		defense.setMembers(List.of(member));

		Group group = new Group();
		group.setProject(project);
		group.setStudents(List.of(student(1L)));

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));

		com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse result = service.getDefense(1L);

		List<com.system_gestion_soutenance.api.student.defense.dto.JuryMemberResponse> juryMembers = result
				.juryMembers();
		assertEquals(1, juryMembers.size());
		assertEquals("Jane Smith", juryMembers.get(0).name());
		assertEquals("Président", juryMembers.get(0).role());
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

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.empty());

		com.system_gestion_soutenance.api.student.defense.dto.StudentDefenseResponse result = service.getDefense(1L);

		assertEquals("pending", result.status());
	}

	private static Student student(Long id) {
		Student s = new Student();
		s.setId(id);
		return s;
	}
}
