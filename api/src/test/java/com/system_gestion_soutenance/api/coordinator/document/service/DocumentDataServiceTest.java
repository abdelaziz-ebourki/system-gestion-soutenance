package com.system_gestion_soutenance.api.coordinator.document.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.general.entity.GeneralSettings;
import com.system_gestion_soutenance.api.admin.config.general.repository.GeneralSettingsRepository;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.coordinator.document.dto.DefenseIdsRequest;
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
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;

class DocumentDataServiceTest {

	private final SlotAssignmentRepository slotAssignmentRepository = mock(SlotAssignmentRepository.class);
	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final JuryRepository juryRepository = mock(JuryRepository.class);
	private final GroupRepository groupRepository = mock(GroupRepository.class);
	private final DefenseSessionRepository defenseSessionRepository = mock(DefenseSessionRepository.class);
	private final GeneralSettingsRepository generalSettingsRepository = mock(GeneralSettingsRepository.class);

	private final DocumentDataService service = new DocumentDataService(slotAssignmentRepository, projectRepository,
			juryRepository, groupRepository, defenseSessionRepository, generalSettingsRepository);

	private Project mockProject(Long id, String title, Teacher supervisor) {
		Project p = mock(Project.class);
		when(p.getId()).thenReturn(id);
		when(p.getTitle()).thenReturn(title);
		when(p.getSupervisor()).thenReturn(supervisor);
		when(p.getStudents()).thenReturn(List.of());
		return p;
	}

	private SlotAssignment mockSlot(Long id, Long projectId, Room room, String date, String time) {
		SlotAssignment s = mock(SlotAssignment.class);
		when(s.getId()).thenReturn(id);
		when(s.getProjectId()).thenReturn(projectId);
		when(s.getRoom()).thenReturn(room);
		when(s.getDate()).thenReturn(date);
		when(s.getTime()).thenReturn(time);
		when(s.getTitle()).thenReturn("Slot " + id);
		return s;
	}

	@Test
	void evaluationSheets_withValidSlot_returnsData() {
		Teacher supervisor = mock(Teacher.class);
		when(supervisor.getFirstName()).thenReturn("John");
		when(supervisor.getLastName()).thenReturn("Doe");

		Project project = mockProject(1L, "Projet Test", supervisor);
		SlotAssignment slot = mockSlot(10L, 1L, null, "2025-06-01", "09:00");

		when(slotAssignmentRepository.findByProjectId(1L)).thenReturn(List.of(slot));
		when(slotAssignmentRepository.findById(10L)).thenReturn(Optional.of(slot));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of());
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());

		DefenseIdsRequest request = new DefenseIdsRequest(null, 1L);
		var result = service.evaluationSheets(request);

		assertEquals(1, result.size());
		assertEquals("Projet Test", result.get(0).projectTitle());
	}

	@Test
	void buildDefenseData_withNullSupervisor_returnsNullSupervisorName() {
		SlotAssignment slot = mockSlot(10L, 1L, null, "2025-06-01", "09:00");
		Project project = mockProject(1L, "Projet", null);

		when(slotAssignmentRepository.findByProjectId(1L)).thenReturn(List.of(slot));
		when(slotAssignmentRepository.findById(10L)).thenReturn(Optional.of(slot));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of());
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());

		DefenseIdsRequest request = new DefenseIdsRequest(null, 1L);
		var result = service.evaluationSheets(request);

		assertNull(result.get(0).supervisorName());
	}

	@Test
	void juryConvocations_withRoomNonEmpty_includesRoomName() {
		Room room = new Room();
		room.setName("Salle B");

		Teacher supervisor = mock(Teacher.class);
		when(supervisor.getFirstName()).thenReturn("John");
		when(supervisor.getLastName()).thenReturn("Doe");

		Project project = mockProject(1L, "Projet", supervisor);
		SlotAssignment slot = mockSlot(10L, 1L, room, "2025-06-01", "09:00");

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(supervisor);
		when(member.getRoleName()).thenReturn("président");

		Jury jury = mock(Jury.class);
		when(jury.getMembers()).thenReturn(List.of(member));

		when(slotAssignmentRepository.findById(10L)).thenReturn(Optional.of(slot));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of(jury));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());

		DefenseIdsRequest request = new DefenseIdsRequest(List.of(10L), null);
		var result = service.juryConvocations(request);

		assertEquals("Salle B", result.get(0).roomName());
	}

	@Test
	void getStudentNames_usesProjectFallback() {
		SlotAssignment slot = mockSlot(10L, 1L, null, "2025-06-01", "09:00");
		Project project = mockProject(1L, "Projet", null);

		Student student = mock(Student.class);
		when(student.getFirstName()).thenReturn("Alice");
		when(student.getLastName()).thenReturn("Test");

		when(slotAssignmentRepository.findByProjectId(1L)).thenReturn(List.of(slot));
		when(slotAssignmentRepository.findById(10L)).thenReturn(Optional.of(slot));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of());
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());
		when(project.getStudents()).thenReturn(List.of(student));

		DefenseIdsRequest request = new DefenseIdsRequest(null, 1L);
		var result = service.evaluationSheets(request);

		assertEquals(1, result.get(0).studentNames().size());
	}

	@Test
	void findDefenseSession_withNoGroupMatch_returnsNull() {
		SlotAssignment slot = mockSlot(10L, 1L, null, "2025-06-01", "09:00");
		Project project = mockProject(1L, "Projet", null);

		Group group = mock(Group.class);
		when(group.getSessionId()).thenReturn(null);

		when(slotAssignmentRepository.findByProjectId(1L)).thenReturn(List.of(slot));
		when(slotAssignmentRepository.findById(10L)).thenReturn(Optional.of(slot));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of());
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of(group));

		Teacher supervisor = mock(Teacher.class);
		when(supervisor.getFirstName()).thenReturn("John");
		when(supervisor.getLastName()).thenReturn("Doe");

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(supervisor);
		when(member.getRoleName()).thenReturn("président");

		Jury jury = mock(Jury.class);
		when(jury.getMembers()).thenReturn(List.of(member));

		when(juryRepository.findByProjectId(1L)).thenReturn(List.of(jury));

		DefenseIdsRequest request = new DefenseIdsRequest(null, 1L);
		var result = service.juryConvocations(request);

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	void evaluationSheets_withJuryTemplateAndCoefficients_includesCoefficients() {
		Teacher supervisor = mock(Teacher.class);
		when(supervisor.getFirstName()).thenReturn("John");
		when(supervisor.getLastName()).thenReturn("Doe");

		Project project = mockProject(1L, "Projet Test", supervisor);
		SlotAssignment slot = mockSlot(10L, 1L, null, "2025-06-01", "09:00");

		com.system_gestion_soutenance.api.admin.config.juryrole.entity.TemplateRole role = new com.system_gestion_soutenance.api.admin.config.juryrole.entity.TemplateRole();
		role.setName("président");
		role.setCoefficient(2);
		com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate template = new com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate();
		template.setRoles(List.of(role));

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(supervisor);
		when(member.getRoleName()).thenReturn("président");

		Jury jury = mock(Jury.class);
		when(jury.getMembers()).thenReturn(List.of(member));
		when(jury.getTemplate()).thenReturn(template);

		when(slotAssignmentRepository.findByProjectId(1L)).thenReturn(List.of(slot));
		when(slotAssignmentRepository.findById(10L)).thenReturn(Optional.of(slot));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of(jury));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());

		DefenseIdsRequest request = new DefenseIdsRequest(null, 1L);
		var result = service.evaluationSheets(request);

		assertEquals(1, result.size());
		Map<String, Integer> coeffs = result.get(0).evaluationCoefficients();
		assertEquals(2, coeffs.get("président"));
	}

	@Test
	void evaluationSheets_slotNotFound_throwsException() {
		DefenseIdsRequest request = new DefenseIdsRequest(List.of(99L), 1L);
		when(slotAssignmentRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.evaluationSheets(request));
	}

	@Test
	void evaluationSheets_skipsSlotWithoutProject() {
		SlotAssignment slot = mockSlot(10L, null, null, "2025-06-01", "09:00");

		when(slotAssignmentRepository.findByProjectId(1L)).thenReturn(List.of(slot));
		when(slotAssignmentRepository.findById(10L)).thenReturn(Optional.of(slot));

		DefenseIdsRequest request = new DefenseIdsRequest(null, 1L);
		var result = service.evaluationSheets(request);

		assertTrue(result.isEmpty());
	}

	@Test
	void evaluationSheets_noSlotsForProject_throwsException() {
		when(slotAssignmentRepository.findByProjectId(99L)).thenReturn(List.of());

		DefenseIdsRequest request = new DefenseIdsRequest(null, 99L);

		assertThrows(EntityNotFoundException.class, () -> service.evaluationSheets(request));
	}

	@Test
	void attendanceList_withValidSession_returnsData() {
		DefenseSession ds = new DefenseSession();
		ds.setName("Session PFE");

		Project project = mockProject(1L, "Projet", null);
		SlotAssignment slot = mockSlot(10L, 1L, null, "2025-06-01", "09:00");

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(slotAssignmentRepository.findAll()).thenReturn(List.of(slot));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());

		var result = service.attendanceList(1L);

		assertEquals("Session PFE", result.defenseSessionName());
	}

	@SuppressWarnings("unchecked")
	@Test
	void attendanceList_withRoom_includesRoomName() {
		DefenseSession ds = new DefenseSession();
		ds.setName("Session PFE");

		Room room = new Room();
		room.setName("Salle A");

		Project project = mockProject(1L, "Projet", null);
		SlotAssignment slot = mockSlot(10L, 1L, room, "2025-06-01", "09:00");

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(slotAssignmentRepository.findAll()).thenReturn(List.of(slot));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());

		var result = service.attendanceList(1L);

		assertEquals("Salle A", result.slots().get(0).roomName());
	}

	@Test
	void attendanceList_sessionNotFound_throwsException() {
		when(defenseSessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.attendanceList(99L));
	}

	@Test
	void juryConvocations_withValidData_returnsConvocations() {
		Teacher teacher = mock(Teacher.class);
		when(teacher.getFirstName()).thenReturn("Jane");
		when(teacher.getLastName()).thenReturn("Smith");

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);
		when(member.getRoleName()).thenReturn("président");

		Jury jury = mock(Jury.class);
		when(jury.getMembers()).thenReturn(List.of(member));

		SlotAssignment slot = mockSlot(10L, 1L, null, "2025-06-01", "09:00");
		Project project = mockProject(1L, "Projet", teacher);

		when(slotAssignmentRepository.findByProjectId(1L)).thenReturn(List.of(slot));
		when(slotAssignmentRepository.findById(10L)).thenReturn(Optional.of(slot));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of(jury));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());

		DefenseIdsRequest request = new DefenseIdsRequest(null, 1L);
		var result = service.juryConvocations(request);

		assertEquals(1, result.size());
		assertEquals("Jane Smith", result.get(0).teacherName());
	}

	@Test
	void juryConvocations_withProjectNotFound_skipsSlot() {
		SlotAssignment slot = mockSlot(10L, 99L, null, "2025-06-01", "09:00");

		when(slotAssignmentRepository.findByProjectId(1L)).thenReturn(List.of(slot));
		when(slotAssignmentRepository.findById(10L)).thenReturn(Optional.of(slot));
		when(projectRepository.findById(99L)).thenReturn(Optional.empty());
		when(groupRepository.findByProjectId(99L)).thenReturn(List.of());

		DefenseIdsRequest request = new DefenseIdsRequest(null, 1L);
		var result = service.juryConvocations(request);

		assertTrue(result.isEmpty());
	}

	@Test
	void juryConvocations_slotNotFound_throwsException() {
		DefenseIdsRequest request = new DefenseIdsRequest(List.of(99L), 1L);
		when(slotAssignmentRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.juryConvocations(request));
	}

	@Test
	void schedule_withValidSession_returnsData() {
		DefenseSession ds = new DefenseSession();
		ds.setName("Session PFE");

		Project project = mockProject(1L, "Projet", null);
		SlotAssignment slot = mockSlot(10L, 1L, null, "2025-06-01", "09:00");

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(slotAssignmentRepository.findAll()).thenReturn(List.of(slot));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());

		var result = service.schedule(1L);

		assertEquals("Session PFE", result.defenseSessionName());
		assertEquals(1, result.slots().size());
	}

	@Test
	void schedule_sessionNotFound_throwsException() {
		when(defenseSessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.schedule(99L));
	}

	@Test
	void procesVerbal_withValidProject_returnsData() {
		Teacher supervisor = mock(Teacher.class);
		when(supervisor.getFirstName()).thenReturn("John");
		when(supervisor.getLastName()).thenReturn("Doe");

		Project project = mockProject(1L, "Projet Test", supervisor);

		GeneralSettings settings = new GeneralSettings();
		settings.setInstitutionName("UnivH2C");

		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(generalSettingsRepository.findById(1L)).thenReturn(Optional.of(settings));
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of());
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());

		var result = service.procesVerbal(1L);

		assertNotNull(result.settings());
		assertEquals("Projet Test", result.grade().projectTitle());
		assertEquals("John Doe", result.supervisorName());
	}

	@Test
	void procesVerbal_projectNotFound_throwsException() {
		when(projectRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.procesVerbal(99L));
	}

	@Test
	void procesVerbal_withJuryMembers_includesThem() {
		Teacher teacher = mock(Teacher.class);
		when(teacher.getFirstName()).thenReturn("Jane");
		when(teacher.getLastName()).thenReturn("Smith");

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(teacher);
		when(member.getRoleName()).thenReturn("examinateur");

		Jury jury = mock(Jury.class);
		when(jury.getMembers()).thenReturn(List.of(member));

		Project project = mockProject(1L, "Projet", null);

		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(generalSettingsRepository.findById(1L)).thenReturn(Optional.empty());
		when(juryRepository.findByProjectId(1L)).thenReturn(List.of(jury));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());

		var result = service.procesVerbal(1L);

		assertEquals(1, result.juryMembers().size());
	}
}
