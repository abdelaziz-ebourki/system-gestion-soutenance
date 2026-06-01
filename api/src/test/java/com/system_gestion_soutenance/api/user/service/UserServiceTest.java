package com.system_gestion_soutenance.api.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.UserMapper;
import com.system_gestion_soutenance.api.user.dto.BulkCreateRequest;
import com.system_gestion_soutenance.api.user.dto.CreateUserRequest;
import com.system_gestion_soutenance.api.user.dto.UpdateUserRequest;
import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.entity.*;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private UserMapper userMapper;
	@Mock
	private UserAccountService accountService;
	@Mock
	private UserProfileService profileService;
	@Mock
	private UserConstraintService constraintService;

	@InjectMocks
	private UserService userService;

	@BeforeEach
	void setUp() {
		lenient().when(userMapper.toDto(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			if (user == null)
				return null;
			return new UserDto(user.getId(), user.getEmail(),
					user.getRole() == null ? null : user.getRole().name().toLowerCase(), user.getLastName(),
					user.getFirstName(), user.isActive(), null, null, null, null, null, null, null, null, null);
		});
	}

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

		PaginatedResponse<UserDto> result = userService.listUsers("student", 0, 10, null);

		assertEquals(1, result.items().size());
		assertEquals("student", result.items().get(0).role());
	}

	@Test
	void listUsers_withBlankRole_treatsAsNull() {
		when(userRepository.findAll(any(PageRequest.class))).thenReturn(Page.empty());

		PaginatedResponse<UserDto> result = userService.listUsers(" ", 0, 10, null);

		assertEquals(0, result.items().size());
		verify(userRepository).findAll(PageRequest.of(0, 10));
	}

	@Test
	void createUser_callsAccountService() {
		CreateUserRequest request = new CreateUserRequest("Base", "User", "base@t.com", "ADMIN", null, null, null, null,
				null);
		UserDto expectedDto = new UserDto(1L, "base@t.com", "admin", "Base", "User", true, null, null, null, null, null,
				null, null, null, null);
		when(accountService.createUser(eq(request), eq(Role.ADMIN))).thenReturn(expectedDto);

		UserDto result = userService.createUser(request);

		assertEquals("base@t.com", result.email());
		verify(accountService).createUser(request, Role.ADMIN);
	}

	@Test
	void bulkCreate_callsAccountService() {
		var entry = new BulkCreateRequest.BulkUserEntry("Doe", "John", "j@t.com", null, null, null, null, null);
		var request = new BulkCreateRequest(List.of(entry), "teacher");
		List<UserDto> expectedDtos = List.of(new UserDto(1L, "j@t.com", "teacher", "Doe", "John", true, null, null,
				null, null, null, null, null, null, null));
		when(accountService.bulkCreate(eq(request), eq(Role.TEACHER))).thenReturn(expectedDtos);

		List<UserDto> results = userService.bulkCreate(request);

		assertEquals(1, results.size());
		verify(accountService).bulkCreate(request, Role.TEACHER);
	}

	@Test
	void updateUser_coordinatesProfileUpdate() {
		User user = createUser(1L, "old@t.com", Role.STUDENT, "Old", "User");
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateUserRequest req = new UpdateUserRequest("New", "Name", null, null, null, null, null, null, null);
		userService.updateUser(1L, req);

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

		UpdateUserRequest req = new UpdateUserRequest(null, null, null, null, null, 99L, null, null, null);
		userService.updateUser(1L, req);

		verify(profileService).updateStudentProfile(eq(student), eq(req));
	}

	@Test
	void updateUser_teacherProfile_callsProfileService() {
		Teacher teacher = new Teacher();
		teacher.setId(1L);
		teacher.setRole(Role.TEACHER);
		when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateUserRequest req = new UpdateUserRequest(null, null, null, null, null, null, null, 99L, null);
		userService.updateUser(1L, req);

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

		assertThrows(ResponseStatusException.class, () -> userService.deleteUser(99L));
	}

	@Test
	void listAllByRole_returnsUsers() {
		User user = createUser(1L, "a@t.com", Role.TEACHER, "A", "B");
		when(userRepository.findByRole(eq(Role.TEACHER), any(PageRequest.class)))
				.thenReturn(new PageImpl<>(List.of(user)));

		List<UserDto> result = userService.listAllByRole("teacher");

		assertEquals(1, result.size());
	}

	@Test
	void invalidRole_throws() {
		assertThrows(ResponseStatusException.class, () -> userService.listAllByRole("invalid"));
	}
}
