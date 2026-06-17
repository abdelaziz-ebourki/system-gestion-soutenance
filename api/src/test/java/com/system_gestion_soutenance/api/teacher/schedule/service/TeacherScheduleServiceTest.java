package com.system_gestion_soutenance.api.teacher.schedule.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TeacherScheduleServiceTest {

	private static Teacher teacher(Long id) {
		Teacher t = new Teacher();
		t.setId(id);
		t.setFirstName("John");
		t.setLastName("Doe");
		return t;
	}

	private static Project project(Long id, String title, Teacher supervisor) {
		Project p = new Project();
		p.setId(id);
		p.setTitle(title);
		p.setSupervisor(supervisor);
		return p;
	}

	private static Defense defense(Long projectId, String date, String time) {
		Defense d = new Defense();
		Project p = new Project();
		p.setId(projectId);
		d.setProject(p);
		d.setDate(LocalDate.parse(date));
		d.setTime(LocalTime.parse(time));
		return d;
	}

	private final DefenseRepository defenseRepository = mock(DefenseRepository.class);
	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final GroupRepository groupRepository = mock(GroupRepository.class);

	private final TeacherScheduleService service = new TeacherScheduleService(defenseRepository, projectRepository,
			groupRepository);

	@Test
	void getSchedule_noData_returnsEmpty() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());
		when(projectRepository.findAll()).thenReturn(List.of());
		when(groupRepository.findAll()).thenReturn(List.of());

		assertTrue(service.getSchedule(1L).slots().isEmpty());
	}

	@Test
	void getSchedule_teacherIsSupervisor_returnsSchedule() {
		Teacher supervisor = teacher(1L);
		Project project = project(10L, "Supervised Project", supervisor);

		Defense def = defense(10L, "2026-06-01", "09:00");

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(def));
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(groupRepository.findAll()).thenReturn(List.of());
		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

		var result = service.getSchedule(1L);

		assertEquals(1, result.slots().size());
		assertEquals("supervisor", result.slots().get(0).role());
	}

	@Test
	void getSchedule_withNullGroupStudents_returnsEmptyNames() {
		Teacher supervisor = teacher(1L);
		Project project = project(10L, "Projet", supervisor);

		Group group = mock(Group.class);
		when(group.getProject()).thenReturn(project);
		when(group.getStudents()).thenReturn(null);

		Defense def = defense(10L, "2026-06-01", "09:00");

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(def));
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(groupRepository.findAll()).thenReturn(List.of(group));
		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

		var result = service.getSchedule(1L);

		assertEquals(1, result.slots().size());
		assertTrue(result.slots().get(0).studentNames().isEmpty());
	}

	@Test
	void getSchedule_withNullRoom_returnsEmptyRoomName() {
		Teacher supervisor = teacher(1L);
		Project project = project(10L, "Projet", supervisor);

		Defense def = defense(10L, "2026-06-01", "09:00");
		def.setRoom(null);

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(def));
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(groupRepository.findAll()).thenReturn(List.of());
		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

		var result = service.getSchedule(1L);

		assertEquals("", result.slots().get(0).roomName());
	}

	@Test
	void getSchedule_withNoGroupStudents_returnsEmptyNames() {
		Teacher supervisor = teacher(1L);
		Project project = project(10L, "Projet", supervisor);

		Defense def = defense(10L, "2026-06-01", "09:00");

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(def));
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(groupRepository.findAll()).thenReturn(List.of());
		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

		var result = service.getSchedule(1L);

		assertEquals(1, result.slots().size());
		assertTrue(result.slots().get(0).studentNames().isEmpty());
	}

	@Test
	void getSchedule_withRoom_returnsRoomName() {
		Teacher supervisor = teacher(1L);
		Project project = project(10L, "Projet", supervisor);

		Defense def = defense(10L, "2026-06-01", "09:00");
		Room room = new Room();
		room.setName("Salle C");
		def.setRoom(room);

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(def));
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(groupRepository.findAll()).thenReturn(List.of());
		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

		var result = service.getSchedule(1L);

		assertEquals("Salle C", result.slots().get(0).roomName());
	}

	@Test
	void getSchedule_withSupervisorAndJuryRole_usesRole() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(10L);
		when(project.getTitle()).thenReturn("Projet");
		when(project.getSupervisor()).thenReturn(null);

		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(1L);

		Teacher supervisorTeacher = mock(Teacher.class);
		when(supervisorTeacher.getId()).thenReturn(1L);

		Project supervisedProject = mock(Project.class);
		when(supervisedProject.getId()).thenReturn(20L);
		when(supervisedProject.getTitle()).thenReturn("Supervised");
		when(supervisedProject.getSupervisor()).thenReturn(supervisorTeacher);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);
		when(member.getRoleName()).thenReturn("président");

		Defense defenseJury = mock(Defense.class);
		when(defenseJury.getId()).thenReturn(1L);
		when(defenseJury.getProject()).thenReturn(project);
		when(defenseJury.getDate()).thenReturn(LocalDate.of(2025, 6, 15));
		when(defenseJury.getTime()).thenReturn(LocalTime.of(9, 0));
		when(defenseJury.getMembers()).thenReturn(List.of(member));

		Defense defenseSup = mock(Defense.class);
		when(defenseSup.getId()).thenReturn(2L);
		when(defenseSup.getProject()).thenReturn(supervisedProject);
		when(defenseSup.getDate()).thenReturn(LocalDate.of(2025, 6, 15));
		when(defenseSup.getTime()).thenReturn(LocalTime.of(9, 0));
		when(defenseSup.getMembers()).thenReturn(List.of());

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defenseJury, defenseSup));
		when(projectRepository.findAll()).thenReturn(List.of(project, supervisedProject));
		when(groupRepository.findAll()).thenReturn(List.of());
		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
		when(projectRepository.findById(20L)).thenReturn(Optional.of(supervisedProject));

		var result = service.getSchedule(1L);

		assertEquals(2, result.slots().size());
	}

	@Test
	void getSchedule_withJuryAndSlot_returnsSchedule() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(10L);
		when(project.getTitle()).thenReturn("Projet Test");
		when(project.getSupervisor()).thenReturn(null);

		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(1L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);
		when(member.getRoleName()).thenReturn("président");

		Defense defense = mock(Defense.class);
		when(defense.getProject()).thenReturn(project);
		when(defense.getDate()).thenReturn(LocalDate.of(2025, 6, 15));
		when(defense.getTime()).thenReturn(LocalTime.of(9, 0));
		when(defense.getMembers()).thenReturn(List.of(member));

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense));
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
		when(groupRepository.findAll()).thenReturn(List.of());

		var result = service.getSchedule(1L);

		assertEquals(1, result.slots().size());
		assertEquals("Projet Test", result.slots().get(0).projectTitle());
	}
}
