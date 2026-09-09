package com.system_gestion_soutenance.api.student.group.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.common.mapper.StudentGroupMapper;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.entity.GroupStatus;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import org.springframework.context.ApplicationEventPublisher;
import com.system_gestion_soutenance.api.common.service.SecurityService;

@ExtendWith(MockitoExtension.class)
class StudentGroupServiceTest {

	@Mock
	private GroupRepository groupRepository;
	@Mock
	private StudentRepository studentRepository;
	@Mock
	private DefenseSessionRepository defenseSessionRepository;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private StudentGroupMapper studentGroupMapper;
	@Mock
	private ApplicationEventPublisher eventPublisher;
	@Mock
	private SecurityService securityService;

	@InjectMocks
	private StudentGroupService service;

	@Test
	void getWorkspace_withNullActiveSession_usesDefaults() {
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(groupRepository.findAllWithDetails()).thenReturn(List.of());
		when(defenseSessionRepository.findAll()).thenReturn(List.of());

		com.system_gestion_soutenance.api.student.group.dto.StudentGroupWorkspaceResponse result = service
				.getWorkspace(1L);

		assertEquals("", result.groupCreationStartDate());
		assertEquals("", result.groupCreationEndDate());
		assertFalse(result.isGroupCreationOpen());
	}

	@Test
	void getWorkspace_withActiveSession_returnsDates() {
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(groupRepository.findAllWithDetails()).thenReturn(List.of());

		DefenseSession activeSession = new DefenseSession();
		activeSession.setGroupFormationStartDate(LocalDate.now().minusDays(1));
		activeSession.setGroupFormationEndDate(LocalDate.now().plusDays(1));
		when(defenseSessionRepository.findAll()).thenReturn(List.of(activeSession));

		com.system_gestion_soutenance.api.student.group.dto.StudentGroupWorkspaceResponse result = service
				.getWorkspace(1L);

		assertEquals(LocalDate.now().minusDays(1).toString(), result.groupCreationStartDate());
		assertEquals(LocalDate.now().plusDays(1).toString(), result.groupCreationEndDate());
	}

	@Test
	void getWorkspace_noGroup_returnsNullCurrent() {
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(groupRepository.findAllWithDetails()).thenReturn(List.of());
		when(defenseSessionRepository.findAll()).thenReturn(List.of());

		var result = service.getWorkspace(1L);

		assertNull(result.currentGroup());
		assertEquals(0, result.availableGroups().size());
	}

	@Test
	void getWorkspace_inGroup_excludesCurrentFromAvailable() {
		Student student = student(1L, "Alice", "Test");
		Group group = new Group();
		group.setId(10L);
		group.setGroupName("Groupe Test");
		group.setStudents(List.of(student));

		Group other = new Group();
		other.setId(20L);
		other.setGroupName("Autre Groupe");
		other.setStudents(List.of(student(2L, "Bob", "Test")));

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		when(groupRepository.findAllWithDetails()).thenReturn(List.of(group, other));
		when(defenseSessionRepository.findAll()).thenReturn(List.of());
		when(studentGroupMapper.toDetails(group, 1L))
				.thenReturn(new com.system_gestion_soutenance.api.student.group.dto.GroupDetailsResponse(10L,
						"Groupe Test", null, null, List.of()));

		var result = service.getWorkspace(1L);

		assertNotNull(result.currentGroup());
		assertEquals(1, result.availableGroups().size());
		assertEquals(20L, result.availableGroups().get(0).id());
	}

	@Test
	void getWorkspace_groupWithNullProject_returnsNullProjectTitle() {
		Student student = student(1L, "Alice", "Test");
		Group group = new Group();
		group.setId(10L);
		group.setGroupName("Groupe Test");
		group.setStudents(List.of(student));
		group.setProject(null);

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		when(groupRepository.findAllWithDetails()).thenReturn(List.of(group));
		when(defenseSessionRepository.findAll()).thenReturn(List.of());
		when(studentGroupMapper.toDetails(group, 1L)).thenReturn(
				new com.system_gestion_soutenance.api.student.group.dto.GroupDetailsResponse(10L, "Groupe Test", null,
						null, List.of(new com.system_gestion_soutenance.api.student.group.dto.GroupMemberResponse(1L,
								"Alice Test", null, "member"))));

		com.system_gestion_soutenance.api.student.group.dto.StudentGroupWorkspaceResponse result = service
				.getWorkspace(1L);

		com.system_gestion_soutenance.api.student.group.dto.GroupDetailsResponse current = result.currentGroup();
		assertNull(current.projectTitle());
		assertNull(current.supervisorName());
	}

	@Test
	void createGroup_success() {
		Student student = student(1L, "Alice", "Test");
		DefenseSession session = new DefenseSession();
		session.setId(5L);
		session.setGroupFormationStartDate(LocalDate.now().minusDays(1));
		session.setGroupFormationEndDate(LocalDate.now().plusDays(1));
		session.setMaxGroupSize(4);

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(defenseSessionRepository.findById(5L)).thenReturn(Optional.of(session));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Group result = service.createGroup(1L, "Groupe Alpha", 5L);

		assertEquals("Groupe Alpha", result.getGroupName());
		assertEquals(1L, result.getLeaderId());
		assertEquals(5L, result.getDefenseSession().getId());
		assertEquals(GroupStatus.PENDING, result.getStatus());
	}

	@Test
	void createGroup_alreadyInGroup_throws() {
		Group group = new Group();
		group.setStudents(List.of(student(1L, "A", "B")));
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));

		assertThrows(InvalidBusinessStateException.class, () -> service.createGroup(1L, "Groupe", 5L));
	}

	@Test
	void createGroup_sessionNotFound_throws() {
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(defenseSessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.createGroup(1L, "Groupe", 99L));
	}

	@Test
	void createGroup_formationWindowClosed_throws() {
		DefenseSession session = new DefenseSession();
		session.setId(5L);
		session.setGroupFormationStartDate(LocalDate.now().minusDays(10));
		session.setGroupFormationEndDate(LocalDate.now().minusDays(1));

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(defenseSessionRepository.findById(5L)).thenReturn(Optional.of(session));

		assertThrows(InvalidBusinessStateException.class, () -> service.createGroup(1L, "Groupe", 5L));
	}

	@Test
	void createGroup_studentNotFound_throws() {
		DefenseSession session = new DefenseSession();
		session.setId(5L);
		session.setGroupFormationStartDate(LocalDate.now().minusDays(1));
		session.setGroupFormationEndDate(LocalDate.now().plusDays(1));

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(defenseSessionRepository.findById(5L)).thenReturn(Optional.of(session));
		when(studentRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(InvalidBusinessStateException.class, () -> service.createGroup(1L, "Groupe", 5L));
	}

	@Test
	void joinGroup_success() {
		Student student = student(1L, "Alice", "Test");
		DefenseSession session = new DefenseSession();
		session.setId(5L);
		session.setGroupFormationStartDate(LocalDate.now().minusDays(1));
		session.setGroupFormationEndDate(LocalDate.now().plusDays(1));

		Group group = new Group();
		group.setId(10L);
		group.setDefenseSession(session);
		group.setGroupName("Groupe Test");
		group.setStudents(new ArrayList<>(List.of(student(2L, "Bob", "Test"))));

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Group result = service.joinGroup(10L, 1L);

		assertEquals(2, result.getStudents().size());
	}

	@Test
	void joinGroup_formationWindowClosed_throws() {
		DefenseSession session = new DefenseSession();
		session.setId(5L);
		session.setGroupFormationStartDate(LocalDate.now().minusDays(10));
		session.setGroupFormationEndDate(LocalDate.now().minusDays(1));

		Group group = new Group();
		group.setId(10L);
		group.setDefenseSession(session);
		group.setStudents(new ArrayList<>(List.of(student(2L, "Bob", "Test"))));

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));

		assertThrows(InvalidBusinessStateException.class, () -> service.joinGroup(10L, 1L));
	}

	@Test
	void joinGroup_groupNotFound_throws() {
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(groupRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.joinGroup(99L, 1L));
	}

	@Test
	void joinGroup_alreadyInGroup_throws() {
		Group group = new Group();
		group.setStudents(List.of(student(1L, "A", "B")));
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));

		assertThrows(InvalidBusinessStateException.class, () -> service.joinGroup(10L, 1L));
	}

	@Test
	void joinGroup_studentNotFound_throws() {
		Group group = new Group();
		group.setId(10L);
		group.setStudents(new ArrayList<>());

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(studentRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(InvalidBusinessStateException.class, () -> service.joinGroup(10L, 1L));
	}

	@Test
	void joinGroup_withNullStudentList_initializesList() {
		Student student = student(1L, "Alice", "Test");
		Group group = new Group();
		group.setId(10L);
		group.setGroupName("Groupe Test");
		group.setStudents(null);

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Group result = service.joinGroup(10L, 1L);

		assertEquals(1, result.getStudents().size());
	}

	@Test
	void joinGroup_alreadyInSpecificGroup_throws() {
		Student student = student(1L, "Alice", "Test");
		Group group = new Group();
		group.setId(10L);
		group.setStudents(new ArrayList<>(List.of(student)));

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));

		assertThrows(InvalidBusinessStateException.class, () -> service.joinGroup(10L, 1L));
	}

	@Test
	void joinGroup_atMaxSize_throws() {
		Student student = student(1L, "Alice", "Test");
		DefenseSession session = new DefenseSession();
		session.setId(5L);
		session.setGroupFormationStartDate(LocalDate.now().minusDays(1));
		session.setGroupFormationEndDate(LocalDate.now().plusDays(1));
		session.setMaxGroupSize(2);

		Group group = new Group();
		group.setId(10L);
		group.setDefenseSession(session);
		group.setStudents(new ArrayList<>(List.of(student(2L, "Bob", "T"), student(3L, "Carol", "T"))));

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> service.joinGroup(10L, 1L));
		assertEquals("Le groupe a atteint sa taille maximale", ex.getMessage());
	}

	@Test
	void joinGroup_belowMaxSize_succeeds() {
		Student student = student(1L, "Alice", "Test");
		DefenseSession session = new DefenseSession();
		session.setId(5L);
		session.setGroupFormationStartDate(LocalDate.now().minusDays(1));
		session.setGroupFormationEndDate(LocalDate.now().plusDays(1));
		session.setMaxGroupSize(3);

		Group group = new Group();
		group.setId(10L);
		group.setDefenseSession(session);
		group.setStudents(new ArrayList<>(List.of(student(2L, "Bob", "T"))));

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Group result = service.joinGroup(10L, 1L);

		assertEquals(2, result.getStudents().size());
	}

	@Test
	void joinGroup_zeroMaxSize_allowsJoin() {
		Student student = student(1L, "Alice", "Test");
		DefenseSession session = new DefenseSession();
		session.setId(5L);
		session.setGroupFormationStartDate(LocalDate.now().minusDays(1));
		session.setGroupFormationEndDate(LocalDate.now().plusDays(1));
		session.setMaxGroupSize(0);

		Group group = new Group();
		group.setId(10L);
		group.setDefenseSession(session);
		group.setStudents(new ArrayList<>(List.of(student(2L, "Bob", "T"))));

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Group result = service.joinGroup(10L, 1L);

		assertEquals(2, result.getStudents().size());
	}

	@Test
	void leaveGroup_notInGroup_throws() {
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> service.leaveGroup(1L));
		assertEquals("Vous n'êtes membre d'aucun groupe", ex.getMessage());
	}

	@Test
	void leaveGroup_groupHasProject_throws() {
		Student student = student(1L, "Alice", "Test");
		Group group = new Group();
		group.setId(10L);
		group.setStudents(new ArrayList<>(List.of(student)));
		group.setLeaderId(1L);
		group.setProject(mock(Project.class));

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> service.leaveGroup(1L));
		assertEquals("Impossible de quitter un groupe ayant un projet assigné", ex.getMessage());
	}

	@Test
	void leaveGroup_lastMember_deletesGroup() {
		Student student = student(1L, "Alice", "Test");
		Group group = new Group();
		group.setId(10L);
		group.setStudents(new ArrayList<>(List.of(student)));
		group.setLeaderId(1L);

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(securityService.getCurrentUserEmail()).thenReturn("test@test.com");

		service.leaveGroup(1L);

		verify(groupRepository).deleteById(10L);
		verify(groupRepository, never()).save(any());
	}

	@Test
	void leaveGroup_leaderLeaves_reassignsLeadership() {
		Student alice = student(1L, "Alice", "Test");
		Student bob = student(2L, "Bob", "Test");
		Group group = new Group();
		group.setId(10L);
		group.setStudents(new ArrayList<>(List.of(alice, bob)));
		group.setLeaderId(1L);

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(studentRepository.findById(1L)).thenReturn(Optional.of(alice));
		when(securityService.getCurrentUserEmail()).thenReturn("test@test.com");

		service.leaveGroup(1L);

		verify(groupRepository).save(argThat(g -> g.getLeaderId().equals(2L)));
	}

	@Test
	void leaveGroup_memberLeaves_leaderUnchanged() {
		Student alice = student(1L, "Alice", "Test");
		Student bob = student(2L, "Bob", "Test");
		Group group = new Group();
		group.setId(10L);
		group.setStudents(new ArrayList<>(List.of(alice, bob)));
		group.setLeaderId(1L);

		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(2L)).thenReturn(Optional.of(group));
		when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(studentRepository.findById(2L)).thenReturn(Optional.of(bob));
		when(securityService.getCurrentUserEmail()).thenReturn("test@test.com");

		service.leaveGroup(2L);

		verify(groupRepository).save(argThat(g -> g.getLeaderId().equals(1L)));
	}

	private static Student student(Long id, String firstName, String lastName) {
		Student s = new Student();
		s.setId(id);
		s.setFirstName(firstName);
		s.setLastName(lastName);
		return s;
	}
}
