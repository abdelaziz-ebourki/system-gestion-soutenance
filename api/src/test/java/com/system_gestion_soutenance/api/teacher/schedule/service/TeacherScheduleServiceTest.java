package com.system_gestion_soutenance.api.teacher.schedule.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.entity.Jury;
import com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.user.entity.Teacher;
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

	private static SlotAssignment slot(Long projectId, String date, String time) {
		SlotAssignment s = new SlotAssignment();
		s.setProjectId(projectId);
		s.setDate(date);
		s.setTime(time);
		return s;
	}

	private final SlotAssignmentRepository slotAssignmentRepository = mock(SlotAssignmentRepository.class);
	private final JuryRepository juryRepository = mock(JuryRepository.class);
	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final GroupRepository groupRepository = mock(GroupRepository.class);

	private final TeacherScheduleService service = new TeacherScheduleService(slotAssignmentRepository, juryRepository,
			projectRepository, groupRepository);

	@Test
	void getSchedule_noData_returnsEmpty() {
		when(juryRepository.findAll()).thenReturn(List.of());
		when(projectRepository.findAll()).thenReturn(List.of());
		when(groupRepository.findAll()).thenReturn(List.of());
		when(slotAssignmentRepository.findAll()).thenReturn(List.of());

		assertTrue(service.getSchedule(1L).slots().isEmpty());
	}

	@Test
	void getSchedule_teacherIsSupervisor_returnsSchedule() {
		Teacher supervisor = teacher(1L);
		Project project = project(10L, "Supervised Project", supervisor);

		when(juryRepository.findAll()).thenReturn(List.of());
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(groupRepository.findAll()).thenReturn(List.of());
		when(slotAssignmentRepository.findAll()).thenReturn(List.of(slot(10L, "2026-06-01", "09:00")));
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

		when(juryRepository.findAll()).thenReturn(List.of());
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(groupRepository.findAll()).thenReturn(List.of(group));
		when(slotAssignmentRepository.findAll()).thenReturn(List.of(slot(10L, "2026-06-01", "09:00")));
		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

		var result = service.getSchedule(1L);

		assertEquals(1, result.slots().size());
		assertTrue(result.slots().get(0).studentNames().isEmpty());
	}

	@Test
	void getSchedule_withNullRoom_returnsEmptyRoomName() {
		Teacher supervisor = teacher(1L);
		Project project = project(10L, "Projet", supervisor);

		SlotAssignment s = slot(10L, "2026-06-01", "09:00");
		s.setRoom(null);

		when(juryRepository.findAll()).thenReturn(List.of());
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(groupRepository.findAll()).thenReturn(List.of());
		when(slotAssignmentRepository.findAll()).thenReturn(List.of(s));
		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

		var result = service.getSchedule(1L);

		assertEquals("", result.slots().get(0).roomName());
	}

	@Test
	void getSchedule_withNullSlotProjectId_skipsSlot() {
		Teacher supervisor = teacher(1L);
		Project project = project(10L, "Projet", supervisor);

		SlotAssignment nullPidSlot = slot(null, "2026-06-01", "09:00");

		when(juryRepository.findAll()).thenReturn(List.of());
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(groupRepository.findAll()).thenReturn(List.of());
		when(slotAssignmentRepository.findAll()).thenReturn(List.of(nullPidSlot));

		var result = service.getSchedule(1L);

		assertTrue(result.slots().isEmpty());
	}

	@Test
	void getSchedule_withProjectNotFound_skipsSlot() {
		Teacher supervisor = teacher(1L);
		Project project = project(10L, "Projet", supervisor);

		when(juryRepository.findAll()).thenReturn(List.of());
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(groupRepository.findAll()).thenReturn(List.of());
		when(slotAssignmentRepository.findAll()).thenReturn(List.of(slot(99L, "2026-06-01", "09:00")));
		when(projectRepository.findById(99L)).thenReturn(Optional.empty());

		var result = service.getSchedule(1L);

		assertTrue(result.slots().isEmpty());
	}

	@Test
	void getSchedule_withNullProjectStudents_returnsEmptyNames() {
		Teacher supervisor = teacher(1L);
		Project project = project(10L, "Projet", supervisor);
		project.setStudents(null);

		when(juryRepository.findAll()).thenReturn(List.of());
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(groupRepository.findAll()).thenReturn(List.of());
		when(slotAssignmentRepository.findAll()).thenReturn(List.of(slot(10L, "2026-06-01", "09:00")));
		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

		var result = service.getSchedule(1L);

		assertEquals(1, result.slots().size());
		assertTrue(result.slots().get(0).studentNames().isEmpty());
	}

	@Test
	void getSchedule_withRoom_returnsRoomName() {
		Teacher supervisor = teacher(1L);
		Project project = project(10L, "Projet", supervisor);

		SlotAssignment s = slot(10L, "2026-06-01", "09:00");
		Room room = new Room();
		room.setName("Salle C");
		s.setRoom(room);

		when(juryRepository.findAll()).thenReturn(List.of());
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(groupRepository.findAll()).thenReturn(List.of());
		when(slotAssignmentRepository.findAll()).thenReturn(List.of(s));
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

		Jury jury = mock(Jury.class);
		when(jury.getProject()).thenReturn(project);
		when(jury.getMembers()).thenReturn(List.of(member));

		SlotAssignment slotJury = mock(SlotAssignment.class);
		when(slotJury.getProjectId()).thenReturn(10L);

		SlotAssignment slotSup = mock(SlotAssignment.class);
		when(slotSup.getProjectId()).thenReturn(20L);

		when(juryRepository.findAll()).thenReturn(List.of(jury));
		when(projectRepository.findAll()).thenReturn(List.of(project, supervisedProject));
		when(groupRepository.findAll()).thenReturn(List.of());
		when(slotAssignmentRepository.findAll()).thenReturn(List.of(slotJury, slotSup));
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

		Jury jury = mock(Jury.class);
		when(jury.getProject()).thenReturn(project);
		when(jury.getMembers()).thenReturn(List.of(member));

		SlotAssignment slot = mock(SlotAssignment.class);
		when(slot.getProjectId()).thenReturn(10L);

		when(juryRepository.findAll()).thenReturn(List.of(jury));
		when(projectRepository.findAll()).thenReturn(List.of(project));
		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
		when(groupRepository.findAll()).thenReturn(List.of());
		when(slotAssignmentRepository.findAll()).thenReturn(List.of(slot));

		var result = service.getSchedule(1L);

		assertEquals(1, result.slots().size());
		assertEquals("Projet Test", result.slots().get(0).projectTitle());
	}
}
