package com.system_gestion_soutenance.api.coordinator.conflict.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.admin.room.repository.RoomRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.coordinator.unavailability.entity.Unavailability;
import com.system_gestion_soutenance.api.coordinator.unavailability.repository.UnavailabilityRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;

class ConflictDetectionServiceTest {

	private final SlotAssignmentRepository slotAssignmentRepository = mock(SlotAssignmentRepository.class);
	private final RoomRepository roomRepository = mock(RoomRepository.class);
	private final GroupRepository groupRepository = mock(GroupRepository.class);
	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final JuryRepository juryRepository = mock(JuryRepository.class);
	private final UnavailabilityRepository unavailabilityRepository = mock(UnavailabilityRepository.class);
	private final DefenseSessionRepository defenseSessionRepository = mock(DefenseSessionRepository.class);

	private final ConflictDetectionService service = new ConflictDetectionService(slotAssignmentRepository,
			roomRepository, groupRepository, projectRepository, juryRepository, unavailabilityRepository,
			defenseSessionRepository);

	private Map<String, Map<String, Object>> singleSlot(String projectId, String roomId, String date, String time) {
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("projectId", projectId);
		data.put("roomId", roomId);
		data.put("date", date);
		data.put("time", time);
		return new LinkedHashMap<>(Map.of("1", data));
	}

	@Test
	void validate_noExistingSchedule_returnsEmptyConflicts() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		var schedule = singleSlot("1", "1", "2025-06-01", "09:00");

		var result = service.validate(schedule, null);

		assertTrue(result.isEmpty());
	}

	@Test
	void checkProjectAlreadyScheduled_detectsDuplicate() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Map<String, Object> data1 = new LinkedHashMap<>();
		data1.put("projectId", "1");
		data1.put("date", "2025-06-01");
		data1.put("time", "09:00");

		Map<String, Object> data2 = new LinkedHashMap<>();
		data2.put("projectId", "1");
		data2.put("date", "2025-06-01");
		data2.put("time", "10:00");

		Map<String, Map<String, Object>> schedule = new LinkedHashMap<>();
		schedule.put("1", data1);
		schedule.put("2", data2);

		var result = service.validate(schedule, null);

		assertTrue(result.stream().anyMatch(c -> "project_already_scheduled".equals(c.get("type"))));
	}

	@Test
	void checkSlotOccupied_detectsOverlap() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Map<String, Object> data1 = new LinkedHashMap<>();
		data1.put("projectId", "1");
		data1.put("roomId", "10");
		data1.put("date", "2025-06-01");
		data1.put("time", "09:00");

		Map<String, Object> data2 = new LinkedHashMap<>();
		data2.put("projectId", "2");
		data2.put("roomId", "10");
		data2.put("date", "2025-06-01");
		data2.put("time", "09:00");

		Map<String, Map<String, Object>> schedule = new LinkedHashMap<>();
		schedule.put("1", data1);
		schedule.put("2", data2);

		var result = service.validate(schedule, null);

		assertTrue(result.stream().anyMatch(c -> "slot_occupied".equals(c.get("type"))));
	}

	@Test
	void checkRoomCapacity_detectsOverflow() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Room room = mock(Room.class);
		when(room.getCapacity()).thenReturn(2);
		when(roomRepository.findById(10L)).thenReturn(Optional.of(room));

		Student s1 = mock(Student.class);
		Student s2 = mock(Student.class);
		Student s3 = mock(Student.class);

		Group group = mock(Group.class);
		when(group.getStudents()).thenReturn(List.of(s1, s2, s3));

		when(groupRepository.findByProjectId(1L)).thenReturn(List.of(group));

		var schedule = singleSlot("1", "10", "2025-06-01", "09:00");

		var result = service.validate(schedule, null);

		assertTrue(result.stream().anyMatch(c -> "room_capacity".equals(c.get("type"))));
	}

	@Test
	void checkRoomCapacity_withSufficientCapacity_noConflict() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Room room = mock(Room.class);
		when(room.getCapacity()).thenReturn(10);
		when(roomRepository.findById(10L)).thenReturn(Optional.of(room));

		Student s1 = mock(Student.class);
		Group group = mock(Group.class);
		when(group.getStudents()).thenReturn(List.of(s1));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of(group));

		var schedule = singleSlot("1", "10", "2025-06-01", "09:00");
		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "room_capacity".equals(c.get("type"))));
	}

	@Test
	void checkDateOutOfBounds_detectsInvalidDate() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		DefenseSession ds = new DefenseSession();
		ds.setStartDate(LocalDate.of(2025, 6, 1));
		ds.setEndDate(LocalDate.of(2025, 6, 30));

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		var schedule = singleSlot("1", "1", "2025-07-01", "09:00");

		var result = service.validate(schedule, "1");

		assertTrue(result.stream().anyMatch(c -> "out_of_bounds".equals(c.get("type"))));
	}

	@Test
	void checkTeacherDoubleBooked_noConflict_returnsEmpty() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(5L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);

		Jury jury = mock(Jury.class);
		when(jury.getMembers()).thenReturn(List.of(member));

		when(juryRepository.findByProjectId(1L)).thenReturn(List.of(jury));
		when(juryRepository.findByProjectId(2L)).thenReturn(List.of(jury));

		Map<String, Object> data1 = new LinkedHashMap<>();
		data1.put("projectId", "1");
		data1.put("date", "2025-06-01");
		data1.put("time", "09:00");

		Map<String, Object> data2 = new LinkedHashMap<>();
		data2.put("projectId", "2");
		data2.put("date", "2025-06-02");
		data2.put("time", "09:00");

		Map<String, Map<String, Object>> schedule = new LinkedHashMap<>();
		schedule.put("1", data1);
		schedule.put("2", data2);

		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "teacher_double_booked".equals(c.get("type"))));
	}

	@Test
	void checkTeacherDoubleBooked_detectsConflict() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(5L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);

		Jury jury = mock(Jury.class);
		when(jury.getMembers()).thenReturn(List.of(member));

		when(juryRepository.findByProjectId(1L)).thenReturn(List.of(jury));
		when(juryRepository.findByProjectId(2L)).thenReturn(List.of(jury));

		Map<String, Object> data1 = new LinkedHashMap<>();
		data1.put("projectId", "1");
		data1.put("date", "2025-06-01");
		data1.put("time", "09:00");

		Map<String, Object> data2 = new LinkedHashMap<>();
		data2.put("projectId", "2");
		data2.put("date", "2025-06-01");
		data2.put("time", "10:00");

		Map<String, Map<String, Object>> schedule = new LinkedHashMap<>();
		schedule.put("1", data1);
		schedule.put("2", data2);

		var result = service.validate(schedule, null);

		assertTrue(result.stream().anyMatch(c -> "teacher_double_booked".equals(c.get("type"))));
	}

	@Test
	void checkSupervisorConflict_noSupervisor_skipsCheck() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Project project = mock(Project.class);
		when(project.getSupervisor()).thenReturn(null);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

		var schedule = singleSlot("1", "1", "2025-06-01", "09:00");
		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "supervisor_conflict".equals(c.get("type"))));
	}

	@Test
	void checkSupervisorConflict_noConflict_returnsEmpty() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

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

		Map<String, Object> data1 = new LinkedHashMap<>();
		data1.put("projectId", "1");
		data1.put("date", "2025-06-01");
		data1.put("time", "09:00");

		Map<String, Object> data2 = new LinkedHashMap<>();
		data2.put("projectId", "2");
		data2.put("date", "2025-06-01");
		data2.put("time", "10:00");

		Map<String, Map<String, Object>> schedule = new LinkedHashMap<>();
		schedule.put("1", data1);
		schedule.put("2", data2);

		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "supervisor_conflict".equals(c.get("type"))));
	}

	@Test
	void checkSupervisorConflict_detectsConflict() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Teacher supervisor = mock(Teacher.class);
		when(supervisor.getId()).thenReturn(5L);

		Project project1 = mock(Project.class);
		when(project1.getSupervisor()).thenReturn(supervisor);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project1));

		Project project2 = mock(Project.class);
		when(project2.getSupervisor()).thenReturn(supervisor);
		when(projectRepository.findById(2L)).thenReturn(Optional.of(project2));

		Map<String, Object> data1 = new LinkedHashMap<>();
		data1.put("projectId", "1");
		data1.put("date", "2025-06-01");
		data1.put("time", "09:00");

		Map<String, Object> data2 = new LinkedHashMap<>();
		data2.put("projectId", "2");
		data2.put("date", "2025-06-01");
		data2.put("time", "10:00");

		Map<String, Map<String, Object>> schedule = new LinkedHashMap<>();
		schedule.put("1", data1);
		schedule.put("2", data2);

		var result = service.validate(schedule, null);

		assertTrue(result.stream().anyMatch(c -> "supervisor_conflict".equals(c.get("type"))));
	}

	@Test
	void checkBreakInterval_detectsViolation() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		DefenseSession ds = mock(DefenseSession.class);
		when(ds.getBreakDuration()).thenReturn(30);
		when(ds.getStartDate()).thenReturn(LocalDate.of(2025, 6, 1));
		when(ds.getEndDate()).thenReturn(LocalDate.of(2025, 6, 30));
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		Map<String, Object> data1 = new LinkedHashMap<>();
		data1.put("projectId", "1");
		data1.put("roomId", "10");
		data1.put("date", "2025-06-01");
		data1.put("time", "09:00");

		Map<String, Object> data2 = new LinkedHashMap<>();
		data2.put("projectId", "2");
		data2.put("roomId", "10");
		data2.put("date", "2025-06-01");
		data2.put("time", "09:15");

		Map<String, Map<String, Object>> schedule = new LinkedHashMap<>();
		schedule.put("1", data1);
		schedule.put("2", data2);

		var result = service.validate(schedule, "1");

		assertTrue(result.stream().anyMatch(c -> "break_violation".equals(c.get("type"))));
	}

	@Test
	void checkTeacherUnavailable_teacherAvailable_noConflict() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(5L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);

		Jury jury = mock(Jury.class);
		when(jury.getMembers()).thenReturn(List.of(member));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of(jury));

		Unavailability ua = new Unavailability(1L, 5L, "2025-06-01", List.of("10:00"));
		when(unavailabilityRepository.findAll()).thenReturn(List.of(ua));

		var schedule = singleSlot("1", "1", "2025-06-01", "09:00");
		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "teacher_unavailable".equals(c.get("type"))));
	}

	@Test
	void checkTeacherUnavailable_detectsConflict() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(5L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);

		Jury jury = mock(Jury.class);
		when(jury.getMembers()).thenReturn(List.of(member));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of(jury));

		Unavailability ua = new Unavailability(1L, 5L, "2025-06-01", List.of("09:00"));
		when(unavailabilityRepository.findAll()).thenReturn(List.of(ua));

		var schedule = singleSlot("1", "1", "2025-06-01", "09:00");

		var result = service.validate(schedule, null);

		assertTrue(result.stream().anyMatch(c -> "teacher_unavailable".equals(c.get("type"))));
	}

	@Test
	void checkProjectAlreadyScheduled_nullProjectId_skipsCheck() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("projectId", null);
		data.put("date", "2025-06-01");

		var schedule = new LinkedHashMap<>(Map.of("1", data));
		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "project_already_scheduled".equals(c.get("type"))));
	}

	@Test
	void checkRoomCapacity_nullProjectIdOrRoomId_skipsCheck() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("projectId", null);
		data.put("roomId", "1");
		data.put("date", "2025-06-01");
		data.put("time", "09:00");

		var result = service.validate(new LinkedHashMap<>(Map.of("1", data)), null);
		assertTrue(result.stream().noneMatch(c -> "room_capacity".equals(c.get("type"))));
	}

	@Test
	void checkRoomCapacity_roomNotFound_skipsCheck() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());
		when(roomRepository.findById(99L)).thenReturn(Optional.empty());

		var result = service.validate(singleSlot("1", "99", "2025-06-01", "09:00"), null);
		assertTrue(result.stream().noneMatch(c -> "room_capacity".equals(c.get("type"))));
	}

	@Test
	void getStudentCountForProject_fromGroupOrProjectFallback() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Room room = mock(Room.class);
		when(room.getCapacity()).thenReturn(0);
		when(roomRepository.findById(10L)).thenReturn(Optional.of(room));

		Group emptyGroup = mock(Group.class);
		when(emptyGroup.getStudents()).thenReturn(List.of());

		Group nullStudentsGroup = mock(Group.class);
		when(nullStudentsGroup.getStudents()).thenReturn(null);

		Student student = mock(Student.class);
		Project project = mock(Project.class);
		when(project.getStudents()).thenReturn(List.of(student));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

		when(groupRepository.findByProjectId(1L)).thenReturn(List.of(emptyGroup, nullStudentsGroup));

		var result = service.validate(singleSlot("1", "10", "2025-06-01", "09:00"), null);

		assertTrue(result.stream().anyMatch(c -> "room_capacity".equals(c.get("type"))));
	}

	@Test
	void getJuryTeacherIds_nullTeacher_skipsMember() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(null);

		Jury jury = mock(Jury.class);
		when(jury.getMembers()).thenReturn(List.of(member));

		when(juryRepository.findByProjectId(1L)).thenReturn(List.of(jury));

		var result = service.validate(singleSlot("1", "1", "2025-06-01", "09:00"), null);

		assertTrue(result.stream().noneMatch(c -> "teacher_double_booked".equals(c.get("type"))));
	}

	@Test
	void checkTeacherDoubleBooked_nullProjectId_skipsCheck() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("projectId", null);
		data.put("date", "2025-06-01");

		var result = service.validate(new LinkedHashMap<>(Map.of("1", data)), null);
		assertTrue(result.stream().noneMatch(c -> "teacher_double_booked".equals(c.get("type"))));
	}

	@Test
	void checkSupervisorConflict_projectNotFound_skipsCheck() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());
		when(projectRepository.findById(1L)).thenReturn(Optional.empty());

		var result = service.validate(singleSlot("1", "1", "2025-06-01", "09:00"), null);
		assertTrue(result.stream().noneMatch(c -> "supervisor_conflict".equals(c.get("type"))));
	}

	@Test
	void checkBreakInterval_invalidTimeFormat_skipsCheck() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Map<String, Object> data1 = new LinkedHashMap<>();
		data1.put("projectId", "1");
		data1.put("roomId", "10");
		data1.put("date", "2025-06-01");
		data1.put("time", "not-a-time");

		Map<String, Object> data2 = new LinkedHashMap<>();
		data2.put("projectId", "2");
		data2.put("roomId", "10");
		data2.put("date", "2025-06-01");
		data2.put("time", "09:00");

		Map<String, Map<String, Object>> schedule = new LinkedHashMap<>();
		schedule.put("1", data1);
		schedule.put("2", data2);

		var result = service.validate(schedule, null);
		assertTrue(result.stream().noneMatch(c -> "break_violation".equals(c.get("type"))));
	}

	@Test
	void checkTeacherUnavailable_nullFields_skipsCheck() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("projectId", null);
		data.put("date", null);
		data.put("time", null);

		var result = service.validate(new LinkedHashMap<>(Map.of("1", data)), null);
		assertTrue(result.stream().noneMatch(c -> "teacher_unavailable".equals(c.get("type"))));
	}

	@Test
	void checkTeacherUnavailable_slotsFieldNull_skipsMatch() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(5L);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);

		Jury jury = mock(Jury.class);
		when(jury.getMembers()).thenReturn(List.of(member));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of(jury));

		Unavailability ua = new Unavailability(1L, 5L, "2025-06-01", null);
		when(unavailabilityRepository.findAll()).thenReturn(List.of(ua));

		var result = service.validate(singleSlot("1", "1", "2025-06-01", "09:00"), null);
		assertTrue(result.stream().noneMatch(c -> "teacher_unavailable".equals(c.get("type"))));
	}

	@Test
	void validate_mergesWithExistingSlots() {
		SlotAssignment existing = mock(SlotAssignment.class);
		when(existing.getId()).thenReturn(99L);
		when(existing.getTitle()).thenReturn("Existing");
		when(existing.getDate()).thenReturn("2025-06-01");
		when(existing.getTime()).thenReturn("09:00");
		when(existing.getProjectId()).thenReturn(99L);
		when(existing.getRoom()).thenReturn(null);

		when(slotAssignmentRepository.findAll()).thenReturn(List.of(existing));

		var schedule = singleSlot("1", "1", "2025-06-02", "09:00");

		var result = service.validate(schedule, null);

		assertTrue(result.isEmpty());
	}

	@Test
	void checkDateOutOfBounds_withinBounds_noConflict() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		DefenseSession ds = new DefenseSession();
		ds.setStartDate(LocalDate.of(2025, 6, 1));
		ds.setEndDate(LocalDate.of(2025, 6, 30));
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		var schedule = singleSlot("1", "1", "2025-06-15", "09:00");
		var result = service.validate(schedule, "1");

		assertTrue(result.stream().noneMatch(c -> "out_of_bounds".equals(c.get("type"))));
	}

	@Test
	void checkDateOutOfBounds_nullSessionId_skipsCheck() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		var schedule = singleSlot("1", "1", "2025-07-01", "09:00");

		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "out_of_bounds".equals(c.get("type"))));
	}

	@Test
	void checkBreakInterval_sufficientGap_noViolation() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Map<String, Object> data1 = new LinkedHashMap<>();
		data1.put("projectId", "1");
		data1.put("roomId", "10");
		data1.put("date", "2025-06-01");
		data1.put("time", "09:00");

		Map<String, Object> data2 = new LinkedHashMap<>();
		data2.put("projectId", "2");
		data2.put("roomId", "10");
		data2.put("date", "2025-06-01");
		data2.put("time", "10:00");

		Map<String, Map<String, Object>> schedule = new LinkedHashMap<>();
		schedule.put("1", data1);
		schedule.put("2", data2);

		var result = service.validate(schedule, null);

		assertTrue(result.stream().noneMatch(c -> "break_violation".equals(c.get("type"))));
	}

	@Test
	void checkBreakInterval_nullSessionId_usesDefaultBreak() {
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		Map<String, Object> data1 = new LinkedHashMap<>();
		data1.put("projectId", "1");
		data1.put("roomId", "10");
		data1.put("date", "2025-06-01");
		data1.put("time", "09:00");

		Map<String, Object> data2 = new LinkedHashMap<>();
		data2.put("projectId", "2");
		data2.put("roomId", "10");
		data2.put("date", "2025-06-01");
		data2.put("time", "09:10");

		Map<String, Map<String, Object>> schedule = new LinkedHashMap<>();
		schedule.put("1", data1);
		schedule.put("2", data2);

		var result = service.validate(schedule, null);

		assertTrue(result.stream().anyMatch(c -> "break_violation".equals(c.get("type"))));
	}
}
