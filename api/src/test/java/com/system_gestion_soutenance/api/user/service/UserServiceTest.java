package com.system_gestion_soutenance.api.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.grade.entity.Grade;
import com.system_gestion_soutenance.api.admin.config.grade.repository.GradeRepository;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.level.repository.LevelRepository;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.repository.MajorRepository;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryMemberRepository;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.notification.service.EmailService;
import com.system_gestion_soutenance.api.user.dto.BulkCreateRequest;
import com.system_gestion_soutenance.api.user.dto.CreateUserRequest;
import com.system_gestion_soutenance.api.user.dto.UpdateUserRequest;
import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.entity.*;
import com.system_gestion_soutenance.api.user.repository.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private StudentRepository studentRepository;
	@Mock
	private TeacherRepository teacherRepository;
	@Mock
	private CoordinatorRepository coordinatorRepository;
	@Mock
	private MajorRepository majorRepository;
	@Mock
	private LevelRepository levelRepository;
	@Mock
	private GradeRepository gradeRepository;
	@Mock
	private DepartmentRepository departmentRepository;
	@Mock
	private JuryMemberRepository juryMemberRepository;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private EmailService emailService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService userService;

	private CreateUserRequest studentRequest;
	private CreateUserRequest teacherRequest;
	private CreateUserRequest coordinatorRequest;
	private Major major;
	private Level level;
	private Grade grade;
	private Department department;

	@BeforeEach
	void setUp() {
		major = new Major();
		major.setId(1L);
		major.setName("GL");

		level = new Level();
		level.setId(1L);
		level.setName("S6");

		grade = new Grade();
		grade.setId(1L);
		grade.setName("PES");

		department = new Department();
		department.setId(1L);
		department.setName("Informatique");

		studentRequest = new CreateUserRequest("Doe", "John", "john@test.com", "student", "CNE123", 1L, 1L, null, null);
		teacherRequest = new CreateUserRequest("Smith", "Jane", "jane@test.com", "teacher", null, null, null, 1L, 1L);
		coordinatorRequest = new CreateUserRequest("Coord", "Bob", "bob@test.com", "coordinator", null, null, null,
				null, null);
	}

	@Test
	void listUsers_withRoleOnly_returnsFiltered() {
		User user = new User(1L, "a@t.com", "", Role.STUDENT, "A", "B", true, null, null, null);
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
	void createUser_otherRole_createsBaseUser() {
		when(userRepository.findByEmail("base@t.com")).thenReturn(Optional.empty());
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		UserDto result = userService
				.createUser(new CreateUserRequest("Base", "User", "base@t.com", "ADMIN", null, null, null, null, null));

		assertEquals("base@t.com", result.email());
	}

	@Test
	void updateUser_roleChange_updatesRole() {
		User user = new User(1L, "old@t.com", "", Role.STUDENT, "Old", "User", true, null, null, null);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateUserRequest req = new UpdateUserRequest(null, null, null, "teacher", null, null, null, null, null);
		UserDto result = userService.updateUser(1L, req);

		assertEquals("teacher", result.role());
	}

	@Test
	void updateUser_studentMajorNotFound_throws() {
		Student student = new Student();
		student.setId(1L);
		student.setEmail("s@t.com");
		student.setRole(Role.STUDENT);
		when(userRepository.findById(1L)).thenReturn(Optional.of(student));
		when(majorRepository.findById(99L)).thenReturn(Optional.empty());

		UpdateUserRequest req = new UpdateUserRequest(null, null, null, null, null, 99L, null, null, null);
		assertThrows(ResponseStatusException.class, () -> userService.updateUser(1L, req));
	}

	@Test
	void updateUser_teacherGradeNotFound_throws() {
		Teacher teacher = new Teacher();
		teacher.setId(1L);
		teacher.setEmail("t@t.com");
		teacher.setRole(Role.TEACHER);
		when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
		when(gradeRepository.findById(99L)).thenReturn(Optional.empty());

		UpdateUserRequest req = new UpdateUserRequest(null, null, null, null, null, null, null, 99L, null);
		assertThrows(ResponseStatusException.class, () -> userService.updateUser(1L, req));
	}

	@Test
	void updateUser_teacherDepartmentNotFound_throws() {
		Teacher teacher = new Teacher();
		teacher.setId(1L);
		teacher.setEmail("t@t.com");
		teacher.setRole(Role.TEACHER);
		when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
		when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

		UpdateUserRequest req = new UpdateUserRequest(null, null, null, null, null, null, null, null, 99L);
		assertThrows(ResponseStatusException.class, () -> userService.updateUser(1L, req));
	}

	@Test
	void bulkCreate_teachers_success() {
		var entry = new BulkCreateRequest.BulkUserEntry("Doe", "John", "j@t.com", null, null, null, null,
				"Informatique");
		var request = new BulkCreateRequest(List.of(entry), "teacher");

		when(userRepository.findByEmail("j@t.com")).thenReturn(Optional.empty());
		when(departmentRepository.findByName("Informatique")).thenReturn(Optional.of(department));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		List<UserDto> results = userService.bulkCreate(request);

		assertEquals(1, results.size());
	}

	@Test
	void bulkCreate_unsupportedRole_throws() {
		var entry = new BulkCreateRequest.BulkUserEntry("Doe", "John", "j@t.com", null, null, null, null, null);
		var request = new BulkCreateRequest(List.of(entry), "ADMIN");

		when(userRepository.findByEmail("j@t.com")).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> userService.bulkCreate(request));
	}

	@Test
	void listUsers_withSearch_callsSearchMethod() {
		when(userRepository.findByRoleAndSearch(any(), anyString(), any())).thenReturn(Page.empty());

		PaginatedResponse<UserDto> result = userService.listUsers(null, 0, 10, "john");

		assertEquals(0, result.items().size());
		verify(userRepository).findByRoleAndSearch(null, "john", PageRequest.of(0, 10));
	}

	@Test
	void listUsers_withoutFilters_returnsAll() {
		when(userRepository.findAll(any(PageRequest.class))).thenReturn(Page.empty());

		PaginatedResponse<UserDto> result = userService.listUsers(null, 0, 10, null);

		assertEquals(0, result.items().size());
		verify(userRepository).findAll(PageRequest.of(0, 10));
	}

	@Test
	void listAllByRole_returnsUsers() {
		User user = new User(1L, "a@t.com", "", Role.TEACHER, "A", "B", true, null, null, null);
		when(userRepository.findByRole(Role.TEACHER)).thenReturn(List.of(user));

		List<UserDto> result = userService.listAllByRole("teacher");

		assertEquals(1, result.size());
	}

	@Test
	void createUser_student_success() {
		when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());
		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(levelRepository.findById(1L)).thenReturn(Optional.of(level));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		UserDto result = userService.createUser(studentRequest);

		assertEquals("john@test.com", result.email());
		assertEquals("student", result.role());
		verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
	}

	@Test
	void createUser_teacher_success() {
		when(userRepository.findByEmail("jane@test.com")).thenReturn(Optional.empty());
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
		when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		UserDto result = userService.createUser(teacherRequest);

		assertEquals("jane@test.com", result.email());
		assertEquals("teacher", result.role());
		verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
	}

	@Test
	void createUser_coordinator_success() {
		when(userRepository.findByEmail("bob@test.com")).thenReturn(Optional.empty());
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		UserDto result = userService.createUser(coordinatorRequest);

		assertEquals("bob@test.com", result.email());
		assertEquals("coordinator", result.role());
		verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
	}

	@Test
	void createUser_duplicateEmail_throws() {
		when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(new User()));

		assertThrows(ResponseStatusException.class, () -> userService.createUser(studentRequest));
		verify(userRepository, never()).save(any());
	}

	@Test
	void createUser_student_missingFields_throws() {
		CreateUserRequest bad = new CreateUserRequest("Doe", "John", "j@t.com", "student", null, null, null, null,
				null);

		assertThrows(ResponseStatusException.class, () -> userService.createUser(bad));
	}

	@Test
	void createUser_teacher_missingDepartment_throws() {
		CreateUserRequest bad = new CreateUserRequest("Smith", "J", "j@t.com", "teacher", null, null, null, null, null);

		assertThrows(ResponseStatusException.class, () -> userService.createUser(bad));
	}

	@Test
	void updateUser_sameEmail_noConflict() {
		User user = new User(1L, "same@t.com", "", Role.STUDENT, "Old", "User", true, null, null, null);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateUserRequest req = new UpdateUserRequest(null, null, "same@t.com", null, null, null, null, null, null);
		UserDto result = userService.updateUser(1L, req);

		assertEquals("same@t.com", result.email());
	}

	@Test
	void updateUser_studentUpdatesMajorAndLevel_success() {
		Student student = new Student();
		student.setId(1L);
		student.setEmail("s@t.com");
		student.setRole(Role.STUDENT);
		student.setLastName("Old");
		student.setFirstName("User");

		Major newMajor = new Major();
		newMajor.setId(2L);
		newMajor.setName("IIR");

		Level newLevel = new Level();
		newLevel.setId(2L);
		newLevel.setName("S5");

		when(userRepository.findById(1L)).thenReturn(Optional.of(student));
		when(majorRepository.findById(2L)).thenReturn(Optional.of(newMajor));
		when(levelRepository.findById(2L)).thenReturn(Optional.of(newLevel));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateUserRequest req = new UpdateUserRequest(null, null, null, null, null, 2L, 2L, null, null);
		UserDto result = userService.updateUser(1L, req);

		assertEquals("IIR", result.majorName());
		assertEquals("S5", result.levelName());
	}

	@Test
	void updateUser_teacherUpdatesGradeAndDepartment_success() {
		Teacher teacher = new Teacher();
		teacher.setId(1L);
		teacher.setEmail("t@t.com");
		teacher.setRole(Role.TEACHER);
		teacher.setLastName("Old");
		teacher.setFirstName("User");

		Grade newGrade = new Grade();
		newGrade.setId(2L);
		newGrade.setName("PA");

		Department newDept = new Department();
		newDept.setId(2L);
		newDept.setName("Maths");

		when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
		when(gradeRepository.findById(2L)).thenReturn(Optional.of(newGrade));
		when(departmentRepository.findById(2L)).thenReturn(Optional.of(newDept));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateUserRequest req = new UpdateUserRequest(null, null, null, null, null, null, null, 2L, 2L);
		UserDto result = userService.updateUser(1L, req);

		assertEquals("PA", result.gradeName());
		assertEquals("Maths", result.departmentName());
	}

	@Test
	void updateUser_updatesBasicFields() {
		User user = new User(1L, "old@t.com", "", Role.STUDENT, "Old", "User", true, null, null, null);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateUserRequest req = new UpdateUserRequest("New", "Name", null, null, null, null, null, null, null);
		UserDto result = userService.updateUser(1L, req);

		assertEquals("New", result.lastName());
		assertEquals("Name", result.firstName());
	}

	@Test
	void updateUser_duplicateEmail_throws() {
		User existing = new User(1L, "current@t.com", "", Role.STUDENT, "A", "B", true, null, null, null);
		User other = new User(2L, "new@t.com", "", Role.STUDENT, "C", "D", true, null, null, null);
		when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(userRepository.findByEmail("new@t.com")).thenReturn(Optional.of(other));

		UpdateUserRequest req = new UpdateUserRequest(null, null, "new@t.com", null, null, null, null, null, null);

		assertThrows(ResponseStatusException.class, () -> userService.updateUser(1L, req));
	}

	@Test
	void updateUser_userNotFound_throws() {
		when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResponseStatusException.class, () -> userService.updateUser(99L, mock(UpdateUserRequest.class)));
	}

	@Test
	void deleteUser_simpleUser_success() {
		User user = new User(1L, "a@t.com", "", Role.ADMIN, "A", "B", true, null, null, null);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		doNothing().when(userRepository).delete(user);

		userService.deleteUser(1L);

		verify(userRepository).delete(user);
	}

	@Test
	void deleteUser_teacher_noConstraints_success() {
		Teacher teacher = new Teacher();
		teacher.setId(1L);
		teacher.setRole(Role.TEACHER);
		when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
		when(departmentRepository.findByHead_Id(1L)).thenReturn(List.of());
		when(juryMemberRepository.findByTeacher_Id(1L)).thenReturn(List.of());
		when(projectRepository.findBySupervisorId(1L)).thenReturn(List.of());

		userService.deleteUser(1L);

		verify(userRepository).delete(teacher);
	}

	@Test
	void deleteUser_teacher_hasJuryConstraint_throws() {
		Teacher teacher = new Teacher();
		teacher.setId(1L);
		teacher.setRole(Role.TEACHER);
		when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
		when(departmentRepository.findByHead_Id(1L)).thenReturn(List.of());
		when(juryMemberRepository.findByTeacher_Id(1L))
				.thenReturn(List.of(new com.system_gestion_soutenance.api.coordinator.jury.entity.JuryMember()));

		assertThrows(ResponseStatusException.class, () -> userService.deleteUser(1L));
		verify(userRepository, never()).delete(any());
	}

	@Test
	void deleteUser_teacher_hasSupervisorConstraint_throws() {
		Teacher teacher = new Teacher();
		teacher.setId(1L);
		teacher.setRole(Role.TEACHER);
		when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
		when(departmentRepository.findByHead_Id(1L)).thenReturn(List.of());
		when(juryMemberRepository.findByTeacher_Id(1L)).thenReturn(List.of());
		when(projectRepository.findBySupervisorId(1L))
				.thenReturn(List.of(new com.system_gestion_soutenance.api.coordinator.project.entity.Project()));

		assertThrows(ResponseStatusException.class, () -> userService.deleteUser(1L));
		verify(userRepository, never()).delete(any());
	}

	@Test
	void deleteUser_studentNoProjects_success() {
		Student student = new Student();
		student.setId(1L);
		student.setRole(Role.STUDENT);
		when(userRepository.findById(1L)).thenReturn(Optional.of(student));
		when(projectRepository.findByStudentsId(1L)).thenReturn(List.of());
		doNothing().when(userRepository).delete(student);

		userService.deleteUser(1L);

		verify(userRepository).delete(student);
	}

	@Test
	void deleteUser_teacher_hasDepartmentHead_throws() {
		Teacher teacher = new Teacher();
		teacher.setId(1L);
		teacher.setRole(Role.TEACHER);
		when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
		when(departmentRepository.findByHead_Id(1L)).thenReturn(List.of(new Department()));

		assertThrows(ResponseStatusException.class, () -> userService.deleteUser(1L));
		verify(userRepository, never()).delete(any());
	}

	@Test
	void deleteUser_student_hasProject_throws() {
		Student student = new Student();
		student.setId(1L);
		student.setRole(Role.STUDENT);
		when(userRepository.findById(1L)).thenReturn(Optional.of(student));
		when(projectRepository.findByStudentsId(1L))
				.thenReturn(List.of(new com.system_gestion_soutenance.api.coordinator.project.entity.Project()));

		assertThrows(ResponseStatusException.class, () -> userService.deleteUser(1L));
		verify(userRepository, never()).delete(any());
	}

	@Test
	void bulkCreate_students_success() {
		var entry = new BulkCreateRequest.BulkUserEntry("Doe", "John", "j@t.com", "CNE123", "GL", "S6", null, null);
		var request = new BulkCreateRequest(List.of(entry), "student");

		when(userRepository.findByEmail("j@t.com")).thenReturn(Optional.empty());
		when(majorRepository.findByName("GL")).thenReturn(Optional.of(major));
		when(levelRepository.findByName("S6")).thenReturn(Optional.of(level));
		when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		List<UserDto> results = userService.bulkCreate(request);

		assertEquals(1, results.size());
		verify(emailService).sendVerificationEmail(anyString(), anyString(), anyString());
	}

	@Test
	void bulkCreate_duplicateEmail_throws() {
		var entry = new BulkCreateRequest.BulkUserEntry("Doe", "John", "j@t.com", "CNE123", null, null, null, null);
		var request = new BulkCreateRequest(List.of(entry), "coordinator");

		when(userRepository.findByEmail("j@t.com")).thenReturn(Optional.of(new User()));

		assertThrows(ResponseStatusException.class, () -> userService.bulkCreate(request));
	}

	@Test
	void invalidRole_throws() {
		assertThrows(ResponseStatusException.class, () -> userService.listAllByRole("invalid"));
	}
}
