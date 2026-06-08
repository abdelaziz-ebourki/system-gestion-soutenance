package com.system_gestion_soutenance.api.coordinator.defense.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.settings.defense.entity.DefenseSettings;
import com.system_gestion_soutenance.api.admin.config.settings.defense.repository.DefenseSettingsRepository;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.admin.room.repository.RoomRepository;
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.entity.ProjectStatus;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.dto.ScheduleRequest;
import com.system_gestion_soutenance.api.coordinator.jury.dto.CreateJuryRequest;
import com.system_gestion_soutenance.api.coordinator.jury.dto.UpdateJuryRequest;
import com.system_gestion_soutenance.api.notification.repository.NotificationRepository;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;

class DefenseServiceTest {

	private final DefenseRepository defenseRepository = mock(DefenseRepository.class);
	private final RoomRepository roomRepository = mock(RoomRepository.class);
	private final DefenseSessionRepository defenseSessionRepository = mock(DefenseSessionRepository.class);
	private final DefenseSettingsRepository defenseSettingsRepository = mock(DefenseSettingsRepository.class);
	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final GroupRepository groupRepository = mock(GroupRepository.class);
	private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
	private final TeacherRepository teacherRepository = mock(TeacherRepository.class);

	private final DefenseService service = new DefenseService(defenseRepository, roomRepository,
			defenseSessionRepository, defenseSettingsRepository, projectRepository, groupRepository,
			notificationRepository, teacherRepository);

	@Test
	void getSchedule_returnsAllDefenses() {
		Defense defense = mock(Defense.class);
		when(defenseRepository.findAll()).thenReturn(List.of(defense));

		var result = service.getSchedule();

		assertEquals(1, result.size());
		verify(defenseRepository).findAll();
	}

	@Test
	void saveSchedule_savesDefenses() {
		Room room = mock(Room.class);
		when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
		Project project = mock(Project.class);
		when(projectRepository.findById(5L)).thenReturn(Optional.of(project));

		when(defenseRepository.save(any(Defense.class))).thenAnswer(i -> i.getArguments()[0]);
		when(defenseRepository.findAll()).thenReturn(List.of());

		ScheduleRequest request = new ScheduleRequest(1L,
				List.of(new com.system_gestion_soutenance.api.coordinator.schedule.dto.SlotAssignmentRequest("Slot 1",
						"2025-06-01", "09:00", 5L, 10L)));

		var result = service.saveSchedule(request);

		verify(defenseRepository).deleteAll();
		verify(defenseRepository, atLeastOnce()).save(any(Defense.class));
	}

	@Test
	void createJury_withValidRequest_returnsDefense() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

		Defense defense = mock(Defense.class);
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));

		Teacher teacher = mock(Teacher.class);
		when(teacher.getId()).thenReturn(5L);
		when(teacherRepository.findById(5L)).thenReturn(Optional.of(teacher));

		when(defenseRepository.save(any(Defense.class))).thenReturn(defense);

		CreateJuryRequest.MemberEntry member = new CreateJuryRequest.MemberEntry(5L, "président");
		CreateJuryRequest request = new CreateJuryRequest(1L, null, List.of(member));

		var result = service.createJury(request);

		assertNotNull(result);
		verify(defenseRepository).save(defense);
	}

	@Test
	void createJury_projectNotFound_throwsException() {
		when(projectRepository.findById(99L)).thenReturn(Optional.empty());

		CreateJuryRequest request = new CreateJuryRequest(99L, null, List.of());

		assertThrows(InvalidBusinessStateException.class, () -> service.createJury(request));
	}

	@Test
	void createJury_duplicateTeacher_throwsException() {
		Project project = mock(Project.class);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(mock(Defense.class)));

		CreateJuryRequest.MemberEntry m1 = new CreateJuryRequest.MemberEntry(5L, "président");
		CreateJuryRequest.MemberEntry m2 = new CreateJuryRequest.MemberEntry(5L, "examinateur");
		CreateJuryRequest request = new CreateJuryRequest(1L, null, List.of(m1, m2));

		assertThrows(InvalidBusinessStateException.class, () -> service.createJury(request));
	}

	@Test
	void updateJury_withValidRequest_updatesDefense() {
		Defense defense = mock(Defense.class);
		when(defenseRepository.findById(1L)).thenReturn(Optional.of(defense));

		Project project = mock(Project.class);
		when(projectRepository.findById(2L)).thenReturn(Optional.of(project));

		when(defenseRepository.save(any(Defense.class))).thenReturn(defense);

		UpdateJuryRequest request = new UpdateJuryRequest(2L, null, null);
		var result = service.updateJury(1L, request);

		assertNotNull(result);
		verify(defense).setProject(project);
		verify(defenseRepository).save(defense);
	}

	@Test
	void cancelDefense_existingDefense_deletesAndNotifies() {
		Defense defense = mock(Defense.class);
		when(defense.getDate()).thenReturn(LocalDate.of(2025, 6, 1));
		when(defense.getTime()).thenReturn(java.time.LocalTime.of(9, 0));
		when(defenseRepository.findById(1L)).thenReturn(Optional.of(defense));

		service.cancelDefense(1L);

		verify(defenseRepository).delete(defense);
		verify(notificationRepository).save(any());
	}

	@Test
	void cancelDefense_notFound_throwsException() {
		when(defenseRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.cancelDefense(99L));
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
		settings.setEndTime("12:00");

		Room room = new Room();
		room.setId(1L);
		room.setCapacity(10);

		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Projet Test");
		when(project.getStatus()).thenReturn(ProjectStatus.APPROVED);

		Defense defense = mock(Defense.class);
		when(defense.getProject()).thenReturn(project);
		when(defense.getMembers()).thenReturn(List.of(mock(JuryMember.class)));

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseSettingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(settings));
		when(roomRepository.findAll()).thenReturn(List.of(room));
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(defenseRepository.findAll()).thenReturn(List.of(defense));
		when(groupRepository.findAll()).thenReturn(List.of());

		var result = service.autoGenerate(1L);

		assertFalse(result.isEmpty());
		assertEquals("Projet Test", result.get(0).title());
	}

	@Test
	void autoGenerate_sessionNotFound_throwsException() {
		when(defenseSessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.autoGenerate(99L));
	}
}
