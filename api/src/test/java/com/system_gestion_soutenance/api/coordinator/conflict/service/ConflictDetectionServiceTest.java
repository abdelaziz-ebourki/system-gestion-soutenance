package com.system_gestion_soutenance.api.coordinator.conflict.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.unavailability.entity.Unavailability;
import com.system_gestion_soutenance.api.coordinator.unavailability.repository.UnavailabilityRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleRequest;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.SlotAssignmentRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import org.junit.jupiter.api.Test;

class ConflictDetectionServiceTest {

	private final DefenseRepository defenseRepository = mock(DefenseRepository.class);
	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final UnavailabilityRepository unavailabilityRepository = mock(UnavailabilityRepository.class);
	private final DefenseSessionRepository defenseSessionRepository = mock(DefenseSessionRepository.class);
	private final GroupRepository groupRepository = mock(GroupRepository.class);

	private final ConflictDetectionService service = new ConflictDetectionService(defenseRepository, projectRepository,
			groupRepository, unavailabilityRepository, defenseSessionRepository);

	private ScheduleRequest singleSlot(String projectId, String roomId, String date, String time) {
		return new ScheduleRequest(1L, List
				.of(new SlotAssignmentRequest("Slot 1", date, time, Long.valueOf(projectId), Long.valueOf(roomId))));
	}

	@Test
	void validate_noExistingSchedule_returnsEmptyConflicts() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		var schedule = singleSlot("1", "1", "2025-06-01", "09:00");

		var result = service.validate(schedule, null);

		assertTrue(result.isEmpty());
	}

	@Test
	void checkProjectAlreadyScheduled_detectsDuplicate() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		var slots = List.of(new SlotAssignmentRequest("Slot 1", "2025-06-01", "09:00", 1L, 1L),
				new SlotAssignmentRequest("Slot 2", "2025-06-01", "10:00", 1L, 1L));

		var schedule = new ScheduleRequest(1L, slots);

		var result = service.validate(schedule, null);

		assertTrue(result.stream().anyMatch(c -> "project_already_scheduled".equals(c.type())));
	}

	@Test
	void checkSlotOccupied_detectsOverlap() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		var slots = List.of(new SlotAssignmentRequest("Slot 1", "2025-06-01", "09:00", 1L, 10L),
				new SlotAssignmentRequest("Slot 2", "2025-06-01", "09:00", 2L, 10L));

		var schedule = new ScheduleRequest(1L, slots);

		var result = service.validate(schedule, null);

		assertTrue(result.stream().anyMatch(c -> "slot_occupied".equals(c.type())));
	}

	@Test
	void checkDateOutOfBounds_detectsInvalidDate() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		DefenseSession ds = new DefenseSession();
		ds.setStartDate(LocalDate.of(2025, 6, 1));
		ds.setEndDate(LocalDate.of(2025, 6, 30));

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		var schedule = singleSlot("1", "1", "2025-07-01", "09:00");

		var result = service.validate(schedule, "1");

		assertTrue(result.stream().anyMatch(c -> "out_of_bounds".equals(c.type())));
	}

	@Test
	void checkTeacherDoubleBooked_noConflict_returnsEmpty() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(5L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);

		Defense defense = mock(Defense.class);
		when(defense.getMembers()).thenReturn(List.of(member));

		Project project1 = mock(Project.class);
		when(project1.getId()).thenReturn(1L);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
		when(defenseRepository.findByProject(project1)).thenReturn(Optional.of(defense));

		Project project2 = mock(Project.class);
		when(project2.getId()).thenReturn(2L);
		when(projectRepository.findById(2L)).thenReturn(Optional.of(project2));
		when(defenseRepository.findByProject(project2)).thenReturn(Optional.of(defense));

		var slots = List.of(new SlotAssignmentRequest("Slot 1", "2025-06-01", "09:00", 1L, 1L),
				new SlotAssignmentRequest("Slot 2", "2025-06-02", "09:00", 2L, 1L));

		var schedule = new ScheduleRequest(1L, slots);

		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "teacher_double_booked".equals(c.type())));
	}

	@Test
	void checkTeacherDoubleBooked_detectsConflict() {
		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(5L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);

		Project project1 = mock(Project.class);
		when(project1.getId()).thenReturn(1L);

		Defense defense1 = mock(Defense.class);
		when(defense1.getProject()).thenReturn(project1);
		when(defense1.getMembers()).thenReturn(List.of(member));
		when(defense1.getDate()).thenReturn(java.time.LocalDate.of(2025, 6, 1));
		when(defense1.getTime()).thenReturn(java.time.LocalTime.of(9, 0));

		Project project2 = mock(Project.class);
		when(project2.getId()).thenReturn(2L);

		Defense defense2 = mock(Defense.class);
		when(defense2.getProject()).thenReturn(project2);
		when(defense2.getMembers()).thenReturn(List.of(member));
		when(defense2.getDate()).thenReturn(java.time.LocalDate.of(2025, 6, 1));
		when(defense2.getTime()).thenReturn(java.time.LocalTime.of(9, 30));

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense1, defense2));

		var slots = List.of(new SlotAssignmentRequest("Slot 1", "2025-06-01", "09:00", 1L, 1L),
				new SlotAssignmentRequest("Slot 2", "2025-06-01", "09:30", 2L, 1L));

		var schedule = new ScheduleRequest(1L, slots);

		var result = service.validate(schedule, null);

		assertTrue(result.stream().anyMatch(c -> "teacher_double_booked".equals(c.type())));
	}

	@Test
	void checkTeacherDoubleBooked_sameDateDifferentTimes_noConflict() {
		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(5L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);

		Project project1 = mock(Project.class);
		when(project1.getId()).thenReturn(1L);

		Defense defense1 = mock(Defense.class);
		when(defense1.getProject()).thenReturn(project1);
		when(defense1.getMembers()).thenReturn(List.of(member));
		when(defense1.getDate()).thenReturn(java.time.LocalDate.of(2025, 6, 1));
		when(defense1.getTime()).thenReturn(java.time.LocalTime.of(9, 0));

		Project project2 = mock(Project.class);
		when(project2.getId()).thenReturn(2L);

		Defense defense2 = mock(Defense.class);
		when(defense2.getProject()).thenReturn(project2);
		when(defense2.getMembers()).thenReturn(List.of(member));
		when(defense2.getDate()).thenReturn(java.time.LocalDate.of(2025, 6, 1));
		when(defense2.getTime()).thenReturn(java.time.LocalTime.of(15, 0));

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense1, defense2));

		var slots = List.of(new SlotAssignmentRequest("Slot 1", "2025-06-01", "09:00", 1L, 1L),
				new SlotAssignmentRequest("Slot 2", "2025-06-01", "15:00", 2L, 1L));

		var schedule = new ScheduleRequest(1L, slots);

		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "teacher_double_booked".equals(c.type())));
	}

	@Test
	void checkSupervisorConflict_noSupervisor_skipsCheck() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		Project project = mock(Project.class);
		when(project.getSupervisor()).thenReturn(null);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

		var schedule = singleSlot("1", "1", "2025-06-01", "09:00");
		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "supervisor_conflict".equals(c.type())));
	}

	@Test
	void checkSupervisorConflict_noConflict_returnsEmpty() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		Teacher sup1 = mock(Teacher.class);
		when(sup1.getId()).thenReturn(5L);

		Teacher sup2 = mock(Teacher.class);
		when(sup2.getId()).thenReturn(6L);

		Project p1 = mock(Project.class);
		when(p1.getSupervisor()).thenReturn(sup1);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(p1));

		Project p2 = mock(Project.class);
		when(p2.getSupervisor()).thenReturn(sup2);
		when(projectRepository.findById(2L)).thenReturn(Optional.of(p2));

		var slots = List.of(new SlotAssignmentRequest("Slot 1", "2025-06-01", "09:00", 1L, 1L),
				new SlotAssignmentRequest("Slot 2", "2025-06-01", "10:00", 2L, 1L));

		var schedule = new ScheduleRequest(1L, slots);

		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "supervisor_conflict".equals(c.type())));
	}

	@Test
	void checkSupervisorConflict_detectsConflict() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		Teacher supervisor = mock(Teacher.class);
		when(supervisor.getId()).thenReturn(5L);

		Project project1 = mock(Project.class);
		when(project1.getSupervisor()).thenReturn(supervisor);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));

		Project project2 = mock(Project.class);
		when(project2.getSupervisor()).thenReturn(supervisor);
		when(projectRepository.findById(2L)).thenReturn(Optional.of(project2));

		var slots = List.of(new SlotAssignmentRequest("Slot 1", "2025-06-01", "09:00", 1L, 1L),
				new SlotAssignmentRequest("Slot 2", "2025-06-01", "09:30", 2L, 1L));

		var schedule = new ScheduleRequest(1L, slots);

		var result = service.validate(schedule, null);

		assertTrue(result.stream().anyMatch(c -> "supervisor_conflict".equals(c.type())));
	}

	@Test
	void checkSupervisorConflict_sameDateDifferentTimes_noConflict() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		Teacher supervisor = mock(Teacher.class);
		when(supervisor.getId()).thenReturn(5L);

		Project project1 = mock(Project.class);
		when(project1.getSupervisor()).thenReturn(supervisor);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));

		Project project2 = mock(Project.class);
		when(project2.getSupervisor()).thenReturn(supervisor);
		when(projectRepository.findById(2L)).thenReturn(Optional.of(project2));

		var slots = List.of(new SlotAssignmentRequest("Slot 1", "2025-06-01", "09:00", 1L, 1L),
				new SlotAssignmentRequest("Slot 2", "2025-06-01", "15:00", 2L, 1L));

		var schedule = new ScheduleRequest(1L, slots);

		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "supervisor_conflict".equals(c.type())));
	}

	@Test
	void checkTeacherUnavailable_teacherAvailable_noConflict() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(5L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);

		Defense defense = mock(Defense.class);
		when(defense.getMembers()).thenReturn(List.of(member));

		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));

		Unavailability ua = new Unavailability();
		ua.setId(1L);
		ua.setTeacherId(5L);
		ua.setDate(java.time.LocalDate.of(2025, 6, 1));
		ua.setSlots(List.of("10:00"));
		when(unavailabilityRepository.findAll()).thenReturn(List.of(ua));

		var schedule = singleSlot("1", "1", "2025-06-01", "09:00");
		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "teacher_unavailable".equals(c.type())));
	}

	@Test
	void checkTeacherUnavailable_detectsConflict() {
		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(5L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);

		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);

		Defense defense = mock(Defense.class);
		when(defense.getProject()).thenReturn(project);
		when(defense.getMembers()).thenReturn(List.of(member));
		when(defense.getDate()).thenReturn(java.time.LocalDate.of(2025, 6, 1));
		when(defense.getTime()).thenReturn(java.time.LocalTime.of(9, 0));

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense));

		Unavailability ua = new Unavailability();
		ua.setId(1L);
		ua.setTeacherId(5L);
		ua.setDate(java.time.LocalDate.of(2025, 6, 1));
		ua.setSlots(List.of("09:00"));
		when(unavailabilityRepository.findAll()).thenReturn(List.of(ua));

		var schedule = singleSlot("1", "1", "2025-06-01", "09:00");

		var result = service.validate(schedule, null);

		assertTrue(result.stream().anyMatch(c -> "teacher_unavailable".equals(c.type())));
	}

	@Test
	void checkProjectAlreadyScheduled_nullProjectId_skipsCheck() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		var schedule = new ScheduleRequest(1L,
				List.of(new SlotAssignmentRequest("Slot 1", "2025-06-01", "09:00", null, 1L)));
		var result = service.validate(schedule, null);
		assertTrue(result.stream().noneMatch(c -> "project_already_scheduled".equals(c.type())));
	}

	@Test
	void getJuryTeacherIds_nullTeacher_skipsMember() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(null);

		Defense defense = mock(Defense.class);
		when(defense.getMembers()).thenReturn(List.of(member));

		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));

		var result = service.validate(singleSlot("1", "1", "2025-06-01", "09:00"), null);

		assertTrue(result.stream().noneMatch(c -> "teacher_double_booked".equals(c.type())));
	}

	@Test
	void checkTeacherDoubleBooked_nullProjectId_skipsCheck() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		var schedule = new ScheduleRequest(1L,
				List.of(new SlotAssignmentRequest("Slot 1", "2025-06-01", "09:00", null, 1L)));
		var result = service.validate(schedule, null);
		assertTrue(result.stream().noneMatch(c -> "teacher_double_booked".equals(c.type())));
	}

	@Test
	void checkSupervisorConflict_projectNotFound_skipsCheck() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());
		when(projectRepository.findById(1L)).thenReturn(Optional.empty());

		var result = service.validate(singleSlot("1", "1", "2025-06-01", "09:00"), null);
		assertTrue(result.stream().noneMatch(c -> "supervisor_conflict".equals(c.type())));
	}

	@Test
	void checkTeacherUnavailable_nullFields_skipsCheck() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		var schedule = new ScheduleRequest(1L, List.of(new SlotAssignmentRequest("Slot 1", null, null, null, 1L)));
		var result = service.validate(schedule, null);
		assertTrue(result.stream().noneMatch(c -> "teacher_unavailable".equals(c.type())));
	}

	@Test
	void checkTeacherUnavailable_slotsFieldNull_skipsMatch() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(5L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);

		Defense defense = mock(Defense.class);
		when(defense.getMembers()).thenReturn(List.of(member));

		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));

		Unavailability ua = new Unavailability();
		ua.setId(1L);
		ua.setTeacherId(5L);
		ua.setDate(java.time.LocalDate.of(2025, 6, 1));
		ua.setSlots(null);
		when(unavailabilityRepository.findAll()).thenReturn(List.of(ua));

		var result = service.validate(singleSlot("1", "1", "2025-06-01", "09:00"), null);
		assertTrue(result.stream().noneMatch(c -> "teacher_unavailable".equals(c.type())));
	}

	@Test
	void validate_mergesWithExistingSlots() {
		Project existingProject = mock(Project.class);
		when(existingProject.getTitle()).thenReturn("Existing");
		when(existingProject.getId()).thenReturn(99L);

		Defense existing = mock(Defense.class);
		when(existing.getId()).thenReturn(99L);
		when(existing.getProject()).thenReturn(existingProject);
		when(existing.getDate()).thenReturn(LocalDate.of(2025, 6, 1));
		when(existing.getTime()).thenReturn(LocalTime.of(9, 0));
		when(existing.getProjectId()).thenReturn(99L);
		when(existing.getRoom()).thenReturn(null);

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(existing));

		var schedule = singleSlot("1", "1", "2025-06-02", "09:00");

		var result = service.validate(schedule, null);

		assertTrue(result.isEmpty());
	}

	@Test
	void checkDateOutOfBounds_withinBounds_noConflict() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		DefenseSession ds = new DefenseSession();
		ds.setStartDate(LocalDate.of(2025, 6, 1));
		ds.setEndDate(LocalDate.of(2025, 6, 30));
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		var schedule = singleSlot("1", "1", "2025-06-15", "09:00");
		var result = service.validate(schedule, "1");

		assertTrue(result.stream().noneMatch(c -> "out_of_bounds".equals(c.type())));
	}

	@Test
	void checkDateOutOfBounds_nullSessionId_skipsCheck() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		var schedule = singleSlot("1", "1", "2025-07-01", "09:00");

		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "out_of_bounds".equals(c.type())));
	}

	@Test
	void checkTeacherDoubleBooked_sharedTeachers_producesSingleConflict() {
		Teacher teacher1 = mock(Teacher.class);
		when(teacher1.getId()).thenReturn(5L);
		Teacher teacher2 = mock(Teacher.class);
		when(teacher2.getId()).thenReturn(6L);

		JuryMember member1 = mock(JuryMember.class);
		when(member1.getTeacher()).thenReturn(teacher1);
		JuryMember member2 = mock(JuryMember.class);
		when(member2.getTeacher()).thenReturn(teacher2);

		Project project1 = mock(Project.class);
		when(project1.getId()).thenReturn(1L);
		Defense defense1 = mock(Defense.class);
		when(defense1.getProject()).thenReturn(project1);
		when(defense1.getMembers()).thenReturn(List.of(member1, member2));
		when(defense1.getDate()).thenReturn(LocalDate.of(2025, 6, 1));
		when(defense1.getTime()).thenReturn(LocalTime.of(9, 0));

		Project project2 = mock(Project.class);
		when(project2.getId()).thenReturn(2L);
		Defense defense2 = mock(Defense.class);
		when(defense2.getProject()).thenReturn(project2);
		when(defense2.getMembers()).thenReturn(List.of(member1, member2));
		when(defense2.getDate()).thenReturn(LocalDate.of(2025, 6, 1));
		when(defense2.getTime()).thenReturn(LocalTime.of(9, 30));

		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense1, defense2));

		var slots = List.of(new SlotAssignmentRequest("Slot 1", "2025-06-01", "09:00", 1L, 1L),
				new SlotAssignmentRequest("Slot 2", "2025-06-01", "09:30", 2L, 1L));

		var schedule = new ScheduleRequest(1L, slots);
		var result = service.validate(schedule, null);

		long doubleBookedCount = result.stream().filter(c -> "teacher_double_booked".equals(c.type())).count();
		assertEquals(1, doubleBookedCount,
				"Should produce exactly one teacher_double_booked conflict for overlapping slots sharing multiple teachers");
	}

	@Test
	void checkSlotOccupied_overlappingTimes_detectsConflict() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		var slots = List.of(new SlotAssignmentRequest("Slot 1", "2025-06-01", "09:00", 1L, 10L),
				new SlotAssignmentRequest("Slot 2", "2025-06-01", "09:30", 2L, 10L));

		var schedule = new ScheduleRequest(1L, slots);
		var result = service.validate(schedule, null);

		assertTrue(result.stream().anyMatch(c -> "slot_occupied".equals(c.type())));
	}

	@Test
	void checkSlotOccupied_nonOverlappingTimes_noConflict() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		var slots = List.of(new SlotAssignmentRequest("Slot 1", "2025-06-01", "09:00", 1L, 10L),
				new SlotAssignmentRequest("Slot 2", "2025-06-01", "10:00", 2L, 10L));

		var schedule = new ScheduleRequest(1L, slots);
		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "slot_occupied".equals(c.type())));
	}

	@Test
	void checkStudentDoubleBooked_detectsConflict() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		Student student = mock(Student.class);
		when(student.getId()).thenReturn(100L);

		Project project1 = mock(Project.class);
		when(project1.getId()).thenReturn(1L);

		Project project2 = mock(Project.class);
		when(project2.getId()).thenReturn(2L);

		Group group1 = mock(Group.class);
		when(group1.getStudents()).thenReturn(List.of(student));
		Group group2 = mock(Group.class);
		when(group2.getStudents()).thenReturn(List.of(student));

		when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
		when(projectRepository.findById(2L)).thenReturn(Optional.of(project2));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of(group1));
		when(groupRepository.findByProjectId(2L)).thenReturn(List.of(group2));

		var slots = List.of(new SlotAssignmentRequest("Slot 1", "2025-06-01", "09:00", 1L, 1L),
				new SlotAssignmentRequest("Slot 2", "2025-06-01", "09:30", 2L, 1L));

		var schedule = new ScheduleRequest(1L, slots);
		var result = service.validate(schedule, null);

		assertTrue(result.stream().anyMatch(c -> "student_double_booked".equals(c.type())));
	}

	@Test
	void checkStudentDoubleBooked_differentTimes_noConflict() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());

		Student student = mock(Student.class);
		when(student.getId()).thenReturn(100L);

		Project project1 = mock(Project.class);
		when(project1.getId()).thenReturn(1L);

		Project project2 = mock(Project.class);
		when(project2.getId()).thenReturn(2L);

		Group group1 = mock(Group.class);
		when(group1.getStudents()).thenReturn(List.of(student));
		Group group2 = mock(Group.class);
		when(group2.getStudents()).thenReturn(List.of(student));

		when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));
		when(projectRepository.findById(2L)).thenReturn(Optional.of(project2));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of(group1));
		when(groupRepository.findByProjectId(2L)).thenReturn(List.of(group2));

		var slots = List.of(new SlotAssignmentRequest("Slot 1", "2025-06-01", "09:00", 1L, 1L),
				new SlotAssignmentRequest("Slot 2", "2025-06-01", "15:00", 2L, 1L));

		var schedule = new ScheduleRequest(1L, slots);
		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "student_double_booked".equals(c.type())));
	}

	@Test
	void checkStudentDoubleBooked_projectNotFound_skipsCheck() {
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of());
		when(projectRepository.findById(1L)).thenReturn(Optional.empty());

		var schedule = singleSlot("1", "1", "2025-06-01", "09:00");
		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "student_double_booked".equals(c.type())));
	}
}
