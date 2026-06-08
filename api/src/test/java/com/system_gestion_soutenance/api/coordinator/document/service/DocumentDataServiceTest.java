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
import com.system_gestion_soutenance.api.coordinator.defense.entity.Defense;
import com.system_gestion_soutenance.api.coordinator.defense.entity.JuryMember;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;

class DocumentDataServiceTest {

	private final DefenseRepository defenseRepository = mock(DefenseRepository.class);
	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final GroupRepository groupRepository = mock(GroupRepository.class);
	private final DefenseSessionRepository defenseSessionRepository = mock(DefenseSessionRepository.class);
	private final GeneralSettingsRepository generalSettingsRepository = mock(GeneralSettingsRepository.class);

	private final DocumentDataService service = new DocumentDataService(defenseRepository, projectRepository,
			groupRepository, defenseSessionRepository, generalSettingsRepository);

	private Project mockProject(Long id, String title, Teacher supervisor) {
		Project p = mock(Project.class);
		when(p.getId()).thenReturn(id);
		when(p.getTitle()).thenReturn(title);
		when(p.getSupervisor()).thenReturn(supervisor);
		when(p.getStudents()).thenReturn(List.of());
		return p;
	}

	private Defense mockDefense(Long id, Long projectId, Room room) {
		Defense d = mock(Defense.class);
		when(d.getId()).thenReturn(id);
		if (projectId != null) {
			Project p = new Project();
			p.setId(projectId);
			when(d.getProject()).thenReturn(p);
		} else {
			when(d.getProject()).thenReturn(null);
		}
		when(d.getDate()).thenReturn(LocalDate.of(2025, 6, 1));
		when(d.getTime()).thenReturn(LocalTime.of(9, 0));
		when(d.getRoom()).thenReturn(room);
		return d;
	}

	@Test
	void evaluationSheets_withValidSlot_returnsData() {
		Teacher supervisor = mock(Teacher.class);
		when(supervisor.getFirstName()).thenReturn("John");
		when(supervisor.getLastName()).thenReturn("Doe");

		Project project = mockProject(1L, "Projet Test", supervisor);
		Defense defense = mockDefense(10L, 1L, null);
		defense.getProject().setTitle("Projet Test");
		defense.getProject().setSupervisor(supervisor);

		when(defenseRepository.findById(10L)).thenReturn(Optional.of(defense));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));

		DefenseIdsRequest request = new DefenseIdsRequest(null, 1L);
		var result = service.evaluationSheets(request);

		assertEquals(1, result.size());
		assertEquals("Projet Test", result.get(0).projectTitle());
	}

	@Test
	void buildDefenseData_withNullSupervisor_returnsNullSupervisorName() {
		Defense defense = mockDefense(10L, 1L, null);
		Project project = mockProject(1L, "Projet", null);

		when(defenseRepository.findById(10L)).thenReturn(Optional.of(defense));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));

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
		Defense defense = mockDefense(10L, 1L, room);
		when(defense.getMembers()).thenReturn(List.of(new JuryMember(supervisor, "président")));

		when(defenseRepository.findById(10L)).thenReturn(Optional.of(defense));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());

		DefenseIdsRequest request = new DefenseIdsRequest(List.of(10L), null);
		var result = service.juryConvocations(request);

		assertEquals("Salle B", result.get(0).roomName());
	}

	@Test
	void getStudentNames_usesProjectFallback() {
		Defense defense = mockDefense(10L, 1L, null);
		Project project = mockProject(1L, "Projet", null);

		Student student = mock(Student.class);
		when(student.getFirstName()).thenReturn("Alice");
		when(student.getLastName()).thenReturn("Test");

		when(defenseRepository.findById(10L)).thenReturn(Optional.of(defense));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));
		when(project.getStudents()).thenReturn(List.of(student));

		DefenseIdsRequest request = new DefenseIdsRequest(null, 1L);
		var result = service.evaluationSheets(request);

		assertEquals(1, result.get(0).studentNames().size());
	}

	@Test
	void findDefenseSession_withNoGroupMatch_returnsNull() {
		Defense defense = mockDefense(10L, 1L, null);
		Project project = mockProject(1L, "Projet", null);

		Group group = mock(Group.class);
		when(group.getSessionId()).thenReturn(null);

		Teacher supervisor = mock(Teacher.class);
		when(supervisor.getFirstName()).thenReturn("John");
		when(supervisor.getLastName()).thenReturn("Doe");

		when(defense.getMembers()).thenReturn(List.of(new JuryMember(supervisor, "président")));
		when(defenseRepository.findById(10L)).thenReturn(Optional.of(defense));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of(group));

		DefenseIdsRequest request = new DefenseIdsRequest(List.of(10L), null);
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
		Defense defense = mockDefense(10L, 1L, null);

		com.system_gestion_soutenance.api.admin.config.juryrole.entity.TemplateRole role = new com.system_gestion_soutenance.api.admin.config.juryrole.entity.TemplateRole();
		role.setName("président");
		role.setCoefficient(2);

		JuryMember member = mock(JuryMember.class);
		when(member.getTeacher()).thenReturn(supervisor);
		when(member.getRoleName()).thenReturn("président");
		when(defense.getMembers()).thenReturn(List.of(member));

		when(defenseRepository.findById(10L)).thenReturn(Optional.of(defense));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));

		DefenseIdsRequest request = new DefenseIdsRequest(null, 1L);
		var result = service.evaluationSheets(request);

		assertEquals(1, result.size());
	}

	@Test
	void evaluationSheets_slotNotFound_throwsException() {
		DefenseIdsRequest request = new DefenseIdsRequest(List.of(99L), 1L);
		when(defenseRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.evaluationSheets(request));
	}

	@Test
	void evaluationSheets_skipsSlotWithoutProject() {
		Defense defense = mockDefense(10L, null, null);

		when(defenseRepository.findById(10L)).thenReturn(Optional.of(defense));

		DefenseIdsRequest request = new DefenseIdsRequest(List.of(10L), null);
		var result = service.evaluationSheets(request);

		assertTrue(result.isEmpty());
	}

	@Test
	void evaluationSheets_noDefensesForProject_throwsException() {
		when(defenseRepository.findByProject(any())).thenReturn(Optional.empty());

		DefenseIdsRequest request = new DefenseIdsRequest(null, 99L);

		assertThrows(EntityNotFoundException.class, () -> service.evaluationSheets(request));
	}

	@Test
	void attendanceList_withValidSession_returnsData() {
		DefenseSession ds = new DefenseSession();
		ds.setName("Session PFE");

		Project project = mockProject(1L, "Projet", null);
		Defense defense = mockDefense(10L, 1L, null);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense));
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
		Defense defense = mockDefense(10L, 1L, room);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense));
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

		Project project = mockProject(1L, "Projet", teacher);
		Defense defense = mockDefense(10L, 1L, null);
		when(defense.getMembers()).thenReturn(List.of(new JuryMember(teacher, "président")));

		when(defenseRepository.findById(10L)).thenReturn(Optional.of(defense));
		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());

		DefenseIdsRequest request = new DefenseIdsRequest(List.of(10L), null);
		var result = service.juryConvocations(request);

		assertEquals(1, result.size());
		assertEquals("Jane Smith", result.get(0).teacherName());
	}

	@Test
	void juryConvocations_withProjectNotFound_skipsSlot() {
		Defense defense = mockDefense(10L, 99L, null);

		when(defenseRepository.findById(10L)).thenReturn(Optional.of(defense));
		when(projectRepository.findById(99L)).thenReturn(Optional.empty());
		when(groupRepository.findByProjectId(99L)).thenReturn(List.of());

		DefenseIdsRequest request = new DefenseIdsRequest(List.of(10L), null);
		var result = service.juryConvocations(request);

		assertTrue(result.isEmpty());
	}

	@Test
	void juryConvocations_slotNotFound_throwsException() {
		DefenseIdsRequest request = new DefenseIdsRequest(List.of(99L), 1L);
		when(defenseRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.juryConvocations(request));
	}

	@Test
	void schedule_withValidSession_returnsData() {
		DefenseSession ds = new DefenseSession();
		ds.setName("Session PFE");

		Project project = mockProject(1L, "Projet", null);
		Defense defense = mockDefense(10L, 1L, null);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(ds));
		when(defenseRepository.findAllWithMembers()).thenReturn(List.of(defense));
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

		Project project = mockProject(1L, "Projet", null);

		Defense defense = mock(Defense.class);
		when(defense.getMembers()).thenReturn(List.of(new JuryMember(teacher, "examinateur")));
		when(defenseRepository.findByProject(project)).thenReturn(Optional.of(defense));

		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(generalSettingsRepository.findById(1L)).thenReturn(Optional.empty());
		when(groupRepository.findByProjectId(1L)).thenReturn(List.of());

		var result = service.procesVerbal(1L);

		assertEquals(1, result.juryMembers().size());
	}
}
