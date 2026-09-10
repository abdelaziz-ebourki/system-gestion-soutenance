package com.system_gestion_soutenance.api.coordinator.group.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.group.dto.CreateGroupRequest;
import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.entity.GroupStatus;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.coordinator.group.document.GroupDocumentService;

class GroupServiceTest {

	private final GroupRepository groupRepository = mock(GroupRepository.class);
	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final StudentRepository studentRepository = mock(StudentRepository.class);
	private final DefenseSessionRepository defenseSessionRepository = mock(DefenseSessionRepository.class);
	private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
	private final SecurityService securityService = mock(SecurityService.class);
	private final GroupDocumentService groupDocumentService = mock(GroupDocumentService.class);

	private final GroupService service = new GroupService(groupRepository, projectRepository, studentRepository,
			defenseSessionRepository, eventPublisher, securityService, groupDocumentService);

	@Test
	void findAll_returnsAllGroups() {
		Group group = mock(Group.class);
		when(group.getId()).thenReturn(1L);
		when(group.getGroupName()).thenReturn("Groupe A");
		when(group.getProject()).thenReturn(null);
		when(group.getStudents()).thenReturn(List.of());
		when(group.getDefenseSession()).thenReturn(null);
		when(groupRepository.findAllWithDetails()).thenReturn(List.of(group));

		var result = service.findAll();

		assertEquals(1, result.size());
		assertEquals("Groupe A", result.get(0).getGroupName());
	}

	@Test
	void create_withValidRequest_returnsGroup() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(10L);
		when(project.getTitle()).thenReturn("Projet Test");

		Student student = mock(Student.class);
		when(student.getId()).thenReturn(1L);
		when(student.getFirstName()).thenReturn("Jane");
		when(student.getLastName()).thenReturn("Smith");

		Group savedGroup = mock(Group.class);
		when(savedGroup.getId()).thenReturn(1L);
		when(savedGroup.getGroupName()).thenReturn("Groupe A");
		when(savedGroup.getProject()).thenReturn(project);
		when(savedGroup.getStudents()).thenReturn(List.of(student));

		DefenseSession ds = mock(DefenseSession.class);
		when(ds.getId()).thenReturn(100L);
		when(savedGroup.getDefenseSession()).thenReturn(ds);
		when(savedGroup.getLeaderId()).thenReturn(1L);

		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
		when(studentRepository.findAllById(List.of(1L))).thenReturn(List.of(student));
		when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);

		CreateGroupRequest request = new CreateGroupRequest("Groupe A", 10L, List.of(1L), 100L, 1L);
		var result = service.create(request);

		assertEquals("Groupe A", result.getGroupName());
		assertEquals(Long.valueOf(10L), result.getProject().getId());
		assertEquals(1L, result.getLeaderId());
	}

	@Test
	void create_projectNotFound_throwsException() {
		when(projectRepository.findById(99L)).thenReturn(Optional.empty());

		CreateGroupRequest request = new CreateGroupRequest("Groupe", 99L, List.of(), null, null);

		assertThrows(InvalidBusinessStateException.class, () -> service.create(request));
	}

	@Test
	void create_withNullStudentIds_createsWithoutStudents() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(1L);
		when(project.getTitle()).thenReturn("Title");

		Group savedGroup = mock(Group.class);
		when(savedGroup.getId()).thenReturn(1L);
		when(savedGroup.getGroupName()).thenReturn("Groupe");
		when(savedGroup.getProject()).thenReturn(project);
		when(savedGroup.getStudents()).thenReturn(List.of());
		when(savedGroup.getDefenseSession()).thenReturn(null);

		when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
		when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);

		CreateGroupRequest request = new CreateGroupRequest("Groupe", 1L, null, null, null);
		var result = service.create(request);

		assertEquals("Groupe", result.getGroupName());
	}

	@Test
	void delete_existingGroup_deletes() {
		when(groupRepository.existsById(1L)).thenReturn(true);

		service.delete(1L);

		verify(groupRepository).deleteById(1L);
	}

	@Test
	void delete_groupNotFound_throwsException() {
		when(groupRepository.existsById(99L)).thenReturn(false);

		assertThrows(EntityNotFoundException.class, () -> service.delete(99L));
	}

	@Test
	void create_leaderNotInStudentIds_throws() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(10L);

		Student s1 = mock(Student.class);
		when(s1.getId()).thenReturn(1L);

		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
		when(studentRepository.findAllById(List.of(1L))).thenReturn(List.of(s1));

		CreateGroupRequest request = new CreateGroupRequest("Groupe A", 10L, List.of(1L), null, 99L);

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> service.create(request));
		assertEquals("Le leader doit être membre du groupe", ex.getMessage());
	}

	@Test
	void create_exceedsMaxGroupSize_throws() {
		Project project = mock(Project.class);
		when(project.getId()).thenReturn(10L);

		Student s1 = mock(Student.class);
		when(s1.getId()).thenReturn(1L);
		Student s2 = mock(Student.class);
		when(s2.getId()).thenReturn(2L);
		Student s3 = mock(Student.class);
		when(s3.getId()).thenReturn(3L);

		DefenseSession session = mock(DefenseSession.class);
		when(session.getMaxGroupSize()).thenReturn(2);

		when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
		when(studentRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of(s1, s2, s3));
		when(defenseSessionRepository.findById(100L)).thenReturn(Optional.of(session));

		CreateGroupRequest request = new CreateGroupRequest("Groupe A", 10L, List.of(1L, 2L, 3L), 100L, null);

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> service.create(request));
		assertEquals("Le groupe a atteint sa taille maximale", ex.getMessage());
	}

	@Test
	void removeMember_groupNotFound_throws() {
		when(groupRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.removeMember(99L, 1L));
	}

	@Test
	void removeMember_studentNotInGroup_throws() {
		Student bob = student(2L);

		Group group = new Group();
		group.setId(10L);
		group.setStudents(new java.util.ArrayList<>(List.of(bob)));
		group.setLeaderId(2L);

		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(studentRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.removeMember(10L, 1L));
	}

	@Test
	void removeMember_lastMember_deletesGroup() {
		Student alice = student(1L);
		when(studentRepository.findById(1L)).thenReturn(Optional.of(alice));
		when(securityService.getCurrentUserEmail()).thenReturn("coord@test.com");

		Group group = new Group();
		group.setId(10L);
		group.setStudents(new java.util.ArrayList<>(List.of(alice)));
		group.setLeaderId(1L);
		group.setProject(null);

		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));

		service.removeMember(10L, 1L);

		verify(groupRepository).deleteById(10L);
		verify(groupRepository, never()).save(any());
		verify(eventPublisher)
				.publishEvent(any(com.system_gestion_soutenance.api.notification.event.DomainEvent.class));
	}

	@Test
	void removeMember_lastMemberWithProject_throws() {
		Student alice = student(1L);
		when(studentRepository.findById(1L)).thenReturn(Optional.of(alice));

		Project project = mock(Project.class);
		when(project.getId()).thenReturn(10L);

		Group group = new Group();
		group.setId(10L);
		group.setStudents(new java.util.ArrayList<>(List.of(alice)));
		group.setLeaderId(1L);
		group.setProject(project);

		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> service.removeMember(10L, 1L));
		assertEquals("Impossible de supprimer un groupe ayant un projet assigné", ex.getMessage());
		verify(groupRepository, never()).deleteById(any());
		verify(groupRepository, never()).save(any());
	}

	@Test
	void removeMember_leaderRemoved_reassignsLeadership() {
		Student alice = student(1L);
		Student bob = student(2L);
		when(studentRepository.findById(1L)).thenReturn(Optional.of(alice));
		when(securityService.getCurrentUserEmail()).thenReturn("coord@test.com");

		Group group = new Group();
		group.setId(10L);
		group.setStudents(new java.util.ArrayList<>(List.of(alice, bob)));
		group.setLeaderId(1L);

		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));

		service.removeMember(10L, 1L);

		verify(groupRepository).save(argThat(g -> g.getLeaderId().equals(2L)));
		verify(eventPublisher)
				.publishEvent(any(com.system_gestion_soutenance.api.notification.event.DomainEvent.class));
	}

	@Test
	void removeMember_memberRemoved_leaderUnchanged() {
		Student alice = student(1L);
		Student bob = student(2L);
		when(studentRepository.findById(2L)).thenReturn(Optional.of(bob));
		when(securityService.getCurrentUserEmail()).thenReturn("coord@test.com");

		Group group = new Group();
		group.setId(10L);
		group.setStudents(new java.util.ArrayList<>(List.of(alice, bob)));
		group.setLeaderId(1L);

		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));

		service.removeMember(10L, 2L);

		verify(groupRepository).save(argThat(g -> g.getLeaderId().equals(1L)));
		verify(eventPublisher)
				.publishEvent(any(com.system_gestion_soutenance.api.notification.event.DomainEvent.class));
	}

	@Test
	void approveGroup_pending_setsActive() {
		Group group = new Group();
		group.setId(1L);
		group.setStatus(GroupStatus.PENDING);

		when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
		when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Group result = service.approveGroup(1L);

		assertEquals(GroupStatus.ACTIVE, result.getStatus());
	}

	@Test
	void approveGroup_notPending_throws() {
		Group group = new Group();
		group.setId(1L);
		group.setStatus(GroupStatus.ACTIVE);

		when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

		assertThrows(InvalidBusinessStateException.class, () -> service.approveGroup(1L));
	}

	@Test
	void approveGroup_notFound_throws() {
		when(groupRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.approveGroup(99L));
	}

	@Test
	void rejectGroup_pending_deletesGroup() {
		Group group = new Group();
		group.setId(1L);
		group.setStatus(GroupStatus.PENDING);
		group.setStudents(new java.util.ArrayList<>(List.of(student(1L))));

		when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

		service.rejectGroup(1L);

		verify(groupRepository).delete(group);
	}

	@Test
	void rejectGroup_notPending_throws() {
		Group group = new Group();
		group.setId(1L);
		group.setStatus(GroupStatus.ACTIVE);

		when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

		assertThrows(InvalidBusinessStateException.class, () -> service.rejectGroup(1L));
	}

	@Test
	void extendGroupFormation_extendsDate() {
		DefenseSession session = new DefenseSession();
		session.setId(1L);
		session.setGroupFormationEndDate(LocalDate.of(2026, 6, 30));

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(defenseSessionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		DefenseSession result = service.extendGroupFormation(1L, 5);

		assertEquals(LocalDate.of(2026, 7, 5), result.getGroupFormationEndDate());
	}

	@Test
	void extendGroupFormation_nullEndDate_throws() {
		DefenseSession session = new DefenseSession();
		session.setId(1L);
		session.setGroupFormationEndDate(null);

		when(defenseSessionRepository.findById(1L)).thenReturn(Optional.of(session));

		assertThrows(InvalidBusinessStateException.class, () -> service.extendGroupFormation(1L, 5));
	}

	@Test
	void assignStudentToGroup_success() {
		Student student = student(1L);
		DefenseSession session = new DefenseSession();
		session.setId(5L);
		session.setMaxGroupSize(4);

		Group group = new Group();
		group.setId(10L);
		group.setDefenseSession(session);
		group.setStudents(new java.util.ArrayList<>(List.of(student(2L))));

		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(defenseSessionRepository.findById(5L)).thenReturn(Optional.of(session));
		when(groupRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Group result = service.assignStudentToGroup(1L, 10L);

		assertEquals(2, result.getStudents().size());
	}

	@Test
	void assignStudentToGroup_studentAlreadyInGroup_throws() {
		when(studentRepository.findById(1L)).thenReturn(Optional.of(student(1L)));
		when(groupRepository.findById(10L)).thenReturn(Optional.of(new Group()));
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(new Group()));

		assertThrows(InvalidBusinessStateException.class, () -> service.assignStudentToGroup(1L, 10L));
	}

	@Test
	void assignStudentToGroup_groupFull_throws() {
		Student student = student(1L);
		DefenseSession session = new DefenseSession();
		session.setId(5L);
		session.setMaxGroupSize(1);

		Group group = new Group();
		group.setId(10L);
		group.setDefenseSession(session);
		group.setStudents(new java.util.ArrayList<>(List.of(student(2L))));

		when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
		when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());
		when(defenseSessionRepository.findById(5L)).thenReturn(Optional.of(session));

		assertThrows(InvalidBusinessStateException.class, () -> service.assignStudentToGroup(1L, 10L));
	}

	@Test
	void getUngroupedStudents_excludesActiveGroupMembers() {
		Student s1 = student(1L);
		Student s2 = student(2L);
		Student s3 = student(3L);

		Group g = new Group();
		g.setId(10L);
		g.setStatus(GroupStatus.ACTIVE);
		g.setStudents(List.of(s1, s2));

		when(groupRepository.findByDefenseSessionId(5L)).thenReturn(List.of(g));
		when(studentRepository.findAll()).thenReturn(List.of(s1, s2, s3));

		List<Student> ungrouped = service.getUngroupedStudents(5L);

		assertEquals(1, ungrouped.size());
		assertEquals(3L, ungrouped.get(0).getId());
	}

	private static Student student(Long id) {
		Student s = new Student();
		s.setId(id);
		return s;
	}
}
