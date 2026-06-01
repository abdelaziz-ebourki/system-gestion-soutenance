package com.system_gestion_soutenance.api.coordinator.schedule.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.settings.defense.entity.DefenseSettings;
import com.system_gestion_soutenance.api.admin.config.settings.defense.repository.DefenseSettingsRepository;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSessionStatus;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.admin.room.repository.RoomRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.notification.repository.NotificationRepository;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ScheduleServiceTest {

	private final SlotAssignmentRepository slotAssignmentRepository = mock(SlotAssignmentRepository.class);
	private final RoomRepository roomRepository = mock(RoomRepository.class);
	private final DefenseSessionRepository defenseSessionRepository = mock(DefenseSessionRepository.class);
	private final DefenseSettingsRepository defenseSettingsRepository = mock(DefenseSettingsRepository.class);
	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final JuryRepository juryRepository = mock(JuryRepository.class);
	private final GroupRepository groupRepository = mock(GroupRepository.class);
	private final NotificationRepository notificationRepository = mock(NotificationRepository.class);

	private final ScheduleService service = new ScheduleService(slotAssignmentRepository, roomRepository,
			defenseSessionRepository, defenseSettingsRepository, projectRepository, juryRepository, groupRepository,
			notificationRepository);

	@Test
	void getSchedule_withSlots_returnsSchedule() {
		Room room = mock(Room.class);
		when(room.getId()).thenReturn(10L);

		SlotAssignment slot = mock(SlotAssignment.class);
		when(slot.getId()).thenReturn(1L);
		when(slot.getTitle()).thenReturn("Slot 1");
		when(slot.getDate()).thenReturn("2025-06-01");
		when(slot.getTime()).thenReturn("09:00");
		when(slot.getProjectId()).thenReturn(5L);
		when(slot.getRoom()).thenReturn(room);

		when(slotAssignmentRepository.findAllWithRoom()).thenReturn(List.of(slot));

		var result = service.getSchedule();

		assertEquals(1, result.size());
		assertEquals("Slot 1", result.get("1").get("title"));
	}

	@Test
	void getSchedule_noSlots_returnsEmpty() {
		when(slotAssignmentRepository.findAllWithRoom()).thenReturn(List.of());

		assertTrue(service.getSchedule().isEmpty());
	}

	@Test
	void saveSchedule_savesAndReturns() {
		Room room = mock(Room.class);
		when(room.getId()).thenReturn(10L);
		when(roomRepository.findById(10L)).thenReturn(Optional.of(room));

		SlotAssignment savedSlot = mock(SlotAssignment.class);
		when(savedSlot.getId()).thenReturn(1L);
		when(savedSlot.getTitle()).thenReturn("Slot 1");
		when(savedSlot.getDate()).thenReturn("2025-06-01");
		when(savedSlot.getTime()).thenReturn("09:00");
		when(savedSlot.getProjectId()).thenReturn(5L);
		when(savedSlot.getRoom()).thenReturn(room);

		when(slotAssignmentRepository.save(any(SlotAssignment.class))).thenReturn(savedSlot);
		when(slotAssignmentRepository.findAllWithRoom()).thenReturn(List.of(savedSlot));

		Map<String, Object> slotData = new LinkedHashMap<>();
		slotData.put("title", "Slot 1");
		slotData.put("date", "2025-06-01");
		slotData.put("time", "09:00");
		slotData.put("projectId", 5);
		slotData.put("roomId", 10);

		Map<String, Map<String, Object>> schedule = Map.of("1", slotData);

		var result = service.saveSchedule(schedule);

		assertEquals(1, result.size());
		verify(slotAssignmentRepository).deleteAll();
	}

	@Test
	void saveSchedule_withProjectIdWithoutRoomId() {
		SlotAssignment savedSlot = mock(SlotAssignment.class);
		when(savedSlot.getId()).thenReturn(1L);
		when(savedSlot.getTitle()).thenReturn("Slot 1");
		when(savedSlot.getDate()).thenReturn("2025-06-01");
		when(savedSlot.getTime()).thenReturn("09:00");
		when(savedSlot.getProjectId()).thenReturn(5L);
		when(savedSlot.getRoom()).thenReturn(null);

		when(slotAssignmentRepository.save(any(SlotAssignment.class))).thenReturn(savedSlot);
		when(slotAssignmentRepository.findAllWithRoom()).thenReturn(List.of(savedSlot));

		Map<String, Object> slotData = new LinkedHashMap<>();
		slotData.put("title", "Slot 1");
		slotData.put("date", "2025-06-01");
		slotData.put("time", "09:00");
		slotData.put("projectId", 5);

		Map<String, Map<String, Object>> schedule = Map.of("1", slotData);
		var result = service.saveSchedule(schedule);

		assertEquals(1, result.size());
	}

	@Test
	void saveSchedule_withNullRoomIdField_skipsRoomLookup() {
		SlotAssignment savedSlot = mock(SlotAssignment.class);
		when(savedSlot.getId()).thenReturn(1L);
		when(savedSlot.getTitle()).thenReturn("Slot 1");
		when(savedSlot.getDate()).thenReturn("2025-06-01");
		when(savedSlot.getTime()).thenReturn("09:00");
		when(savedSlot.getProjectId()).thenReturn(null);
		when(savedSlot.getRoom()).thenReturn(null);

		when(slotAssignmentRepository.save(any(SlotAssignment.class))).thenReturn(savedSlot);
		when(slotAssignmentRepository.findAllWithRoom()).thenReturn(List.of(savedSlot));

		Map<String, Object> slotData = new LinkedHashMap<>();
		slotData.put("title", "Slot 1");
		slotData.put("date", "2025-06-01");
		slotData.put("time", "09:00");
		slotData.put("projectId", 5);
		slotData.put("roomId", null);

		Map<String, Map<String, Object>> schedule = Map.of("1", slotData);
		var result = service.saveSchedule(schedule);

		assertEquals(1, result.size());
	}

	@Test
	void getStudentCountForProject_withNullRoom_skipsCapacityCheck() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Projet");
		when(project.getStatus()).thenReturn("approved");
		when(project.getStudents())
				.thenReturn(List.of(mock(com.system_gestion_soutenance.api.user.entity.Student.class)));

		Group group = mock(Group.class);
		when(group.getStudents())
				.thenReturn(List.of(mock(com.system_gestion_soutenance.api.user.entity.Student.class)));

		DefenseSession ds = new DefenseSession();
		ds.setDefenseDuration(20);
		ds.setBreakDuration(10);
		ds.setStartDate(LocalDate.of(2025, 6, 1));
		ds.setEndDate(LocalDate.of(2025, 6, 1));

		DefenseSettings settings = new DefenseSettings();
		settings.setStartTime("09:00");
		settings.setEndTime("10:00");

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseSettingsRepository.findById(1L)).thenReturn(Optional.of(settings));
		when(roomRepository.findAll()).thenReturn(List.of());
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of(mock(Jury.class)));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of(group));

		assertThrows(ResponseStatusException.class, () -> service.autoGenerate(1L));
	}

	@Test
	void getStudentCountForProject_withEmptyGroupAndProjectStudents_usesProject() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Projet");
		when(project.getStatus()).thenReturn("approved");
		when(project.getStudents())
				.thenReturn(List.of(mock(com.system_gestion_soutenance.api.user.entity.Student.class)));

		Group emptyGroup = mock(Group.class);
		when(emptyGroup.getStudents()).thenReturn(List.of());

		Room room = mock(Room.class);
		when(room.getCapacity()).thenReturn(1);

		DefenseSession ds = new DefenseSession();
		ds.setDefenseDuration(20);
		ds.setBreakDuration(10);
		ds.setStartDate(LocalDate.of(2025, 6, 1));
		ds.setEndDate(LocalDate.of(2025, 6, 1));

		DefenseSettings settings = new DefenseSettings();
		settings.setStartTime("09:00");
		settings.setEndTime("10:00");

		Jury jury = mock(Jury.class);
		when(jury.getProject()).thenReturn(project);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseSettingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(settings));
		when(roomRepository.findAll()).thenReturn(List.of(room));
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(juryRepository.findAll()).thenReturn(List.of(jury));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of(emptyGroup));

		var result = service.autoGenerate(1L);

		assertFalse(result.isEmpty());
	}

	@Test
	void toLong_withString_parsesCorrectly() {
		SlotAssignment saved = mock(SlotAssignment.class);
		when(saved.getId()).thenReturn(1L);
		when(saved.getTitle()).thenReturn("S");
		when(saved.getDate()).thenReturn("2025-06-01");
		when(saved.getTime()).thenReturn("09:00");
		when(saved.getProjectId()).thenReturn(null);
		when(saved.getRoom()).thenReturn(null);
		when(slotAssignmentRepository.save(any(SlotAssignment.class))).thenReturn(saved);
		when(slotAssignmentRepository.findAll()).thenReturn(List.of(saved));

		Map<String, Object> slotData = new LinkedHashMap<>();
		slotData.put("title", "S");
		slotData.put("date", "2025-06-01");
		slotData.put("time", "09:00");
		slotData.put("projectId", "5");

		Map<String, Map<String, Object>> schedule = Map.of("1", slotData);
		service.saveSchedule(schedule);

		verify(slotAssignmentRepository).save(argThat(s -> s.getProjectId() == 5L));
	}

	@Test
	void saveSchedule_roomNotFound_throwsException() {
		when(roomRepository.findById(99L)).thenReturn(Optional.empty());

		Map<String, Object> slotData = new LinkedHashMap<>();
		slotData.put("title", "Slot");
		slotData.put("date", "2025-06-01");
		slotData.put("time", "09:00");
		slotData.put("roomId", 99);

		Map<String, Map<String, Object>> schedule = Map.of("1", slotData);

		assertThrows(ResponseStatusException.class, () -> service.saveSchedule(schedule));
	}

	@Test
	void autoGenerate_withValidData_generatesSchedule() {
		DefenseSession ds = new DefenseSession();
		ds.setStartDate(LocalDate.of(2025, 6, 1));
		ds.setEndDate(LocalDate.of(2025, 6, 1));
		ds.setDefenseDuration(30);
		ds.setBreakDuration(15);

		DefenseSettings settings = new DefenseSettings();
		settings.setStartTime("09:00");
		settings.setEndTime("10:00");

		Room room = new Room();
		room.setId(1L);
		room.setName("Salle A");
		room.setCapacity(10);

		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Projet Test");
		when(project.getStatus()).thenReturn("approved");

		Group group = mock(Group.class);
		when(group.getStudents())
				.thenReturn(List.of(mock(com.system_gestion_soutenance.api.user.entity.Student.class)));

		Jury jury = mock(Jury.class);
		when(jury.getProject()).thenReturn(project);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseSettingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(settings));
		when(roomRepository.findAll()).thenReturn(List.of(room));
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(juryRepository.findAll()).thenReturn(List.of(jury));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of(group));

		var result = service.autoGenerate(1L);

		assertFalse(result.isEmpty());
		assertEquals("Projet Test", result.values().iterator().next().get("title"));
	}

	@Test
	void autoGenerate_sessionNotFound_throwsException() {
		when(defenseSessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> service.autoGenerate(99L));
	}

	@Test
	void autoGenerate_settingsNotFound_throwsException() {
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(new DefenseSession()));
		when(defenseSettingsRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> service.autoGenerate(1L));
	}

	@Test
	void autoGenerate_noApprovedProjects_throwsException() {
		var ds = new DefenseSession();
		ds.setDefenseDuration(20);
		ds.setBreakDuration(10);
		var settings = new DefenseSettings();
		settings.setStartTime("08:00");
		settings.setEndTime("18:00");
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseSettingsRepository.findById(1L)).thenReturn(Optional.of(settings));
		when(roomRepository.findAll()).thenReturn(List.of(new Room()));
		when(projectRepository.findAll()).thenReturn(List.of());

		assertThrows(ResponseStatusException.class, () -> service.autoGenerate(1L));
	}

	@Test
	void saveSchedule_withoutOptionalFields_savesCorrectly() {
		SlotAssignment savedSlot = mock(SlotAssignment.class);
		when(savedSlot.getId()).thenReturn(1L);
		when(savedSlot.getTitle()).thenReturn("Slot 1");
		when(savedSlot.getDate()).thenReturn("2025-06-01");
		when(savedSlot.getTime()).thenReturn("09:00");
		when(savedSlot.getProjectId()).thenReturn(null);
		when(savedSlot.getRoom()).thenReturn(null);

		when(slotAssignmentRepository.save(any(SlotAssignment.class))).thenReturn(savedSlot);
		when(slotAssignmentRepository.findAllWithRoom()).thenReturn(List.of(savedSlot));

		Map<String, Object> slotData = new LinkedHashMap<>();
		slotData.put("title", "Slot 1");
		slotData.put("date", "2025-06-01");
		slotData.put("time", "09:00");

		Map<String, Map<String, Object>> schedule = Map.of("1", slotData);

		var result = service.saveSchedule(schedule);

		assertEquals(1, result.size());
		verify(slotAssignmentRepository).deleteAll();
	}

	@Test
	void autoGenerate_noRooms_throwsException() {
		DefenseSession ds = new DefenseSession();
		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseSettingsRepository.findById(1L)).thenReturn(Optional.of(new DefenseSettings()));
		when(roomRepository.findAll()).thenReturn(List.of());

		assertThrows(ResponseStatusException.class, () -> service.autoGenerate(1L));
	}

	@Test
	void publish_activeSession_updatesStatusAndCreatesNotification() {
		DefenseSession ds = new DefenseSession();
		ds.setName("Session PFE");
		ds.setStatus(DefenseSessionStatus.ACTIVE);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		service.publish(1L);

		assertEquals(DefenseSessionStatus.SCHEDULED, ds.getStatus());
		verify(defenseSessionRepository).save(ds);
		verify(notificationRepository).save(any());
	}

	@Test
	void publish_nonActiveSession_doesNotUpdateStatus() {
		DefenseSession ds = new DefenseSession();
		ds.setName("Session");
		ds.setStatus(DefenseSessionStatus.DRAFT);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));

		service.publish(1L);

		assertEquals(DefenseSessionStatus.DRAFT, ds.getStatus());
		verify(defenseSessionRepository, never()).save(ds);
		verify(notificationRepository).save(any());
	}

	@Test
	void publish_sessionNotFound_throwsException() {
		when(defenseSessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> service.publish(99L));
	}

	@Test
	void cancelDefense_existingSlot_deletesAndNotifies() {
		SlotAssignment slot = mock(SlotAssignment.class);
		when(slot.getId()).thenReturn(1L);
		when(slot.getTitle()).thenReturn("Slot 1");
		when(slot.getDate()).thenReturn("2025-06-01");
		when(slot.getTime()).thenReturn("09:00");

		when(slotAssignmentRepository.findById(1L)).thenReturn(Optional.of(slot));

		service.cancelDefense(1L);

		verify(slotAssignmentRepository).delete(slot);
		verify(notificationRepository).save(any());
	}

	@Test
	void cancelDefense_slotNotFound_throwsException() {
		when(slotAssignmentRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> service.cancelDefense(99L));
	}
}
