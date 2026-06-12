package com.system_gestion_soutenance.api.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.user.dto.BulkCreateRequest;
import com.system_gestion_soutenance.api.user.dto.CreateUserRequest;
import com.system_gestion_soutenance.api.user.dto.UpdateUserRequest;
import com.system_gestion_soutenance.api.user.entity.*;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private UserAccountService accountService;
	@Mock
	private UserProfileService profileService;
	@Mock
	private UserConstraintService constraintService;

	@InjectMocks
	private UserService userService;

	private static User createUser(Long id, String email, Role role, String lastName, String firstName) {
		User u = new User();
		u.setId(id);
		u.setEmail(email);
		u.setPassword("");
		u.setRole(role);
		u.setLastName(lastName);
		u.setFirstName(firstName);
		u.setActive(true);
		return u;
	}

	@Test
	void listUsers_withRoleOnly_returnsFiltered() {
		User user = createUser(1L, "a@t.com", Role.STUDENT, "A", "B");
		Page<User> page = new PageImpl<>(List.of(user));
		when(userRepository.findByRole(eq(Role.STUDENT), any(PageRequest.class))).thenReturn(page);

		Page<User> result = userService.listUsers("student", 0, 10, null);

		assertEquals(1, result.getContent().size());
		assertEquals(user, result.getContent().get(0));
	}

	@Test
	void listUsers_withBlankRole_treatsAsNull() {
		when(userRepository.findAll(any(PageRequest.class))).thenReturn(Page.empty());

		Page<User> result = userService.listUsers(" ", 0, 10, null);

		assertEquals(0, result.getContent().size());
		verify(userRepository).findAll(PageRequest.of(0, 10));
	}

	@Test
	void createUser_callsAccountService() {
		CreateUserRequest request = new CreateUserRequest("Base", "User", "base@t.com", "ADMIN", null, null, null, null,
				null, null);
		User expectedUser = createUser(1L, "base@t.com", Role.ADMIN, "Base", "User");
		when(accountService.createUser(eq(request), eq(Role.ADMIN))).thenReturn(expectedUser);

		User result = userService.createUser(request);

		assertEquals("base@t.com", result.getEmail());
		verify(accountService).createUser(request, Role.ADMIN);
	}

	@Test
	void bulkCreate_callsAccountService() {
		var entry = new BulkCreateRequest.BulkUserEntry("Doe", "John", "j@t.com", null, null, null, null, null, null);
		var request = new BulkCreateRequest(List.of(entry), "teacher");
		User expectedUser = createUser(1L, "j@t.com", Role.TEACHER, "Doe", "John");
		when(accountService.bulkCreate(eq(request), eq(Role.TEACHER))).thenReturn(List.of(expectedUser));

		List<User> results = userService.bulkCreate(request);

		assertEquals(1, results.size());
		verify(accountService).bulkCreate(request, Role.TEACHER);
	}

	@Test
	void updateUser_coordinatesProfileUpdate() {
		User user = createUser(1L, "old@t.com", Role.STUDENT, "Old", "User");
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateUserRequest req = new UpdateUserRequest("New", "Name", null, null, null, null, null, null, null, null);
		User result = userService.updateUser(1L, req);

		assertNotNull(result);
		verify(profileService).updateBasicInfo(eq(user), eq(req));
		verify(userRepository).save(user);
	}

	@Test
	void updateUser_studentProfile_callsProfileService() {
		Student student = new Student();
		student.setId(1L);
		student.setRole(Role.STUDENT);
		when(userRepository.findById(1L)).thenReturn(Optional.of(student));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateUserRequest req = new UpdateUserRequest(null, null, null, null, null, null, 99L, null, null, null);
		User result = userService.updateUser(1L, req);

		assertNotNull(result);
		verify(profileService).updateStudentProfile(eq(student), eq(req));
	}

	@Test
	void updateUser_teacherProfile_callsProfileService() {
		Teacher teacher = new Teacher();
		teacher.setId(1L);
		teacher.setRole(Role.TEACHER);
		when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateUserRequest req = new UpdateUserRequest(null, null, null, null, null, null, null, null, 99L, null);
		User result = userService.updateUser(1L, req);

		assertNotNull(result);
		verify(profileService).updateTeacherProfile(eq(teacher), eq(req));
	}

	@Test
	void deleteUser_teacher_checksConstraints() {
		Teacher teacher = new Teacher();
		teacher.setId(1L);
		teacher.setRole(Role.TEACHER);
		when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));

		userService.deleteUser(1L);

		verify(constraintService).checkTeacherDeletionConstraints(1L);
		verify(userRepository).delete(teacher);
	}

	@Test
	void deleteUser_student_checksConstraints() {
		Student student = new Student();
		student.setId(1L);
		student.setRole(Role.STUDENT);
		when(userRepository.findById(1L)).thenReturn(Optional.of(student));

		userService.deleteUser(1L);

		verify(constraintService).checkStudentDeletionConstraints(1L);
		verify(userRepository).delete(student);
	}

	@Test
	void deleteUser_notFound_throws() {
		when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(99L));
	}

	@Test
	void listAllByRole_returnsUsers() {
		User user = createUser(1L, "a@t.com", Role.TEACHER, "A", "B");
		when(userRepository.findByRole(eq(Role.TEACHER), any(PageRequest.class)))
				.thenReturn(new PageImpl<>(List.of(user)));

		List<User> result = userService.listAllByRole("teacher");

		assertEquals(1, result.size());
	}

	@Test
	void invalidRole_throws() {
		assertThrows(InvalidBusinessStateException.class, () -> userService.listAllByRole("invalid"));
	}
}
