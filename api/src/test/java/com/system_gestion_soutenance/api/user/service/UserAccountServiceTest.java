package com.system_gestion_soutenance.api.user.service;

import com.system_gestion_soutenance.api.admin.config.grade.entity.Grade;
import com.system_gestion_soutenance.api.admin.config.grade.repository.GradeRepository;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.level.repository.LevelRepository;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.repository.MajorRepository;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.common.mapper.UserMapper;
import com.system_gestion_soutenance.api.notification.service.EmailService;
import com.system_gestion_soutenance.api.user.dto.BulkCreateRequest;
import com.system_gestion_soutenance.api.user.dto.CreateUserRequest;
import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.entity.*;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private MajorRepository majorRepository;
	@Mock
	private LevelRepository levelRepository;
	@Mock
	private GradeRepository gradeRepository;
	@Mock
	private DepartmentRepository departmentRepository;
	@Mock
	private EmailService emailService;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private UserMapper userMapper;

	private UserAccountService userAccountService;
	private final String baseUrl = "http://localhost:8080";

	@BeforeEach
	void setUp() {
		userAccountService = new UserAccountService(userRepository, majorRepository, levelRepository, gradeRepository,
				departmentRepository, emailService, passwordEncoder, userMapper, baseUrl);
	}

	@Test
	void createUser_EmailExists_ThrowsException() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "test@test.com", null, null, null, null,
				null, null);
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userAccountService.createUser(request, Role.STUDENT));
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertEquals("Un utilisateur avec cet email existe déjà", ex.getReason());
	}

	@Test
	void createUser_Student_Success() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "student@test.com", null, "CNE123", 1L, 1L,
				null, null);
		Major major = new Major();
		Level level = new Level();
		UserDto dto = new UserDto(1L, "student@test.com", "STUDENT", "Last", "First", true, "CNE123", 1L, "Major", 1L,
				"Level", null, null, null, null);

		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(levelRepository.findById(1L)).thenReturn(Optional.of(level));
		when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
		doReturn(dto).when(userMapper).toDto(any(User.class));

		UserDto result = userAccountService.createUser(request, Role.STUDENT);

		assertNotNull(result);
		verify(userRepository).save(any(Student.class));
		verify(emailService).sendVerificationEmail(any(), any(), any());
	}

	@Test
	void createUser_Teacher_Success() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "teacher@test.com", null, null, null, null,
				1L, 1L);
		Department dept = new Department();
		Grade grade = new Grade();
		UserDto dto = new UserDto(1L, "teacher@test.com", "TEACHER", "Last", "First", true, null, null, null, null,
				null, 1L, "Grade", 1L, "Dept");

		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
		when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
		when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
		doReturn(dto).when(userMapper).toDto(any(User.class));

		UserDto result = userAccountService.createUser(request, Role.TEACHER);

		assertNotNull(result);
		verify(userRepository).save(any(Teacher.class));
	}

	@Test
	void createUser_Coordinator_Success() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "coord@test.com", null, null, null, null,
				null, null);
		UserDto dto = new UserDto(1L, "coord@test.com", "COORDINATOR", "Last", "First", true, null, null, null, null,
				null, null, null, null, null);

		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
		doReturn(dto).when(userMapper).toDto(any(User.class));

		UserDto result = userAccountService.createUser(request, Role.COORDINATOR);

		assertNotNull(result);
		verify(userRepository).save(any(Coordinator.class));
	}

	@Test
	void createUser_AdminRole_CreatesBaseUser() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "admin@test.com", null, null, null, null,
				null, null);
		UserDto dto = new UserDto(1L, "admin@test.com", "ADMIN", "Last", "First", true, null, null, null, null, null,
				null, null, null, null);

		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
		doReturn(dto).when(userMapper).toDto(any(User.class));

		UserDto result = userAccountService.createUser(request, Role.ADMIN);

		assertNotNull(result);
		verify(userRepository).save(any(User.class));
		verify(emailService).sendVerificationEmail(any(), any(), any());
	}

	@Test
	void createStudent_MajorNotFound_ThrowsException() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "student@test.com", null, "CNE123", 99L, 1L,
				null, null);
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(majorRepository.findById(99L)).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userAccountService.createUser(request, Role.STUDENT));
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertEquals("Filière introuvable", ex.getReason());
	}

	@Test
	void createStudent_LevelNotFound_ThrowsException() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "student@test.com", null, "CNE123", 1L, 99L,
				null, null);
		Major major = new Major();
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(levelRepository.findById(99L)).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userAccountService.createUser(request, Role.STUDENT));
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertEquals("Niveau introuvable", ex.getReason());
	}

	@Test
	void createTeacher_NullDepartment_ThrowsException() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "teacher@test.com", null, null, null, null,
				null, null);
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userAccountService.createUser(request, Role.TEACHER));
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertEquals("Le champ departmentId est requis pour un enseignant", ex.getReason());
	}

	@Test
	void createTeacher_DepartmentNotFound_ThrowsException() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "teacher@test.com", null, null, null, null,
				1L, 99L);
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userAccountService.createUser(request, Role.TEACHER));
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertEquals("Département introuvable", ex.getReason());
	}

	@Test
	void createTeacher_NullGrade_Success() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "teacher@test.com", null, null, null, null,
				null, 1L);
		Department dept = new Department();
		UserDto dto = new UserDto(1L, "teacher@test.com", "TEACHER", "Last", "First", true, null, null, null, null,
				null, null, null, 1L, "Dept");

		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
		when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
		doReturn(dto).when(userMapper).toDto(any(User.class));

		UserDto result = userAccountService.createUser(request, Role.TEACHER);

		assertNotNull(result);
		verify(userRepository).save(any(Teacher.class));
	}

	@Test
	void createTeacher_GradeNotFound_ThrowsException() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "teacher@test.com", null, null, null, null,
				99L, 1L);
		Department dept = new Department();
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
		when(gradeRepository.findById(99L)).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userAccountService.createUser(request, Role.TEACHER));
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertEquals("Grade introuvable", ex.getReason());
	}

	@Test
	void bulkCreate_Success() {
		BulkCreateRequest.BulkUserEntry entry = new BulkCreateRequest.BulkUserEntry("Last", "First", "bulk@test.com",
				"CNE", "Major", "Level", null, null);
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry), "STUDENT");
		Major major = new Major();
		Level level = new Level();
		UserDto dto = new UserDto(1L, "bulk@test.com", "STUDENT", "Last", "First", true, "CNE", 1L, "Major", 1L,
				"Level", null, null, null, null);

		when(userRepository.findByEmail(entry.email())).thenReturn(Optional.empty());
		when(majorRepository.findByName("Major")).thenReturn(Optional.of(major));
		when(levelRepository.findByName("Level")).thenReturn(Optional.of(level));
		when(passwordEncoder.encode(any())).thenReturn("encoded");
		doReturn(dto).when(userMapper).toDto(any(User.class));

		List<UserDto> results = userAccountService.bulkCreate(request, Role.STUDENT);

		assertEquals(1, results.size());
		verify(userRepository, times(1)).save(any(Student.class));
	}

	@Test
	void createUser_Student_MissingFields_ThrowsException() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "student@test.com", null, null, null, null,
				null, null);
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userAccountService.createUser(request, Role.STUDENT));
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertTrue(ex.getReason().contains("requis pour un étudiant"));
	}

	@Test
	void bulkCreate_UnsupportedRole_ThrowsException() {
		BulkCreateRequest.BulkUserEntry entry = new BulkCreateRequest.BulkUserEntry("Last", "First", "bulk@test.com",
				"CNE", "Major", "Level", null, null);
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry), "ADMIN");

		when(userRepository.findByEmail(entry.email())).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userAccountService.bulkCreate(request, Role.ADMIN));
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertTrue(ex.getReason().contains("Rôle non supporté"));
	}

	@Test
	void bulkCreate_DuplicateEmail_ThrowsException() {
		BulkCreateRequest.BulkUserEntry entry1 = new BulkCreateRequest.BulkUserEntry("Last", "First", "first@test.com",
				"CNE", "Major", "Level", null, null);
		BulkCreateRequest.BulkUserEntry entry2 = new BulkCreateRequest.BulkUserEntry("Last", "First", "second@test.com",
				"CNE2", "Major", "Level", null, null);
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry1, entry2), "STUDENT");
		Major major = new Major();
		Level level = new Level();

		when(userRepository.findByEmail("first@test.com")).thenReturn(Optional.empty());
		when(userRepository.findByEmail("second@test.com")).thenReturn(Optional.of(new User()));
		when(majorRepository.findByName("Major")).thenReturn(Optional.of(major));
		when(levelRepository.findByName("Level")).thenReturn(Optional.of(level));

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userAccountService.bulkCreate(request, Role.STUDENT));
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertTrue(ex.getReason().contains("existe déjà"));
	}

	@Test
	void bulkCreate_Teacher_Success() {
		BulkCreateRequest.BulkUserEntry entry = new BulkCreateRequest.BulkUserEntry("Last", "First", "teacher@test.com",
				null, null, null, null, "Dept");
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry), "TEACHER");
		Department dept = new Department();
		UserDto dto = new UserDto(1L, "teacher@test.com", "TEACHER", "Last", "First", true, null, null, null, null,
				null, null, null, null, null);

		when(userRepository.findByEmail(entry.email())).thenReturn(Optional.empty());
		when(departmentRepository.findByName("Dept")).thenReturn(Optional.of(dept));
		when(passwordEncoder.encode(any())).thenReturn("encoded");
		doReturn(dto).when(userMapper).toDto(any(User.class));

		List<UserDto> results = userAccountService.bulkCreate(request, Role.TEACHER);

		assertEquals(1, results.size());
		verify(userRepository).save(any(Teacher.class));
	}

	@Test
	void bulkCreate_Coordinator_Success() {
		BulkCreateRequest.BulkUserEntry entry = new BulkCreateRequest.BulkUserEntry("Last", "First", "coord@test.com",
				null, null, null, null, null);
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry), "COORDINATOR");
		UserDto dto = new UserDto(1L, "coord@test.com", "COORDINATOR", "Last", "First", true, null, null, null, null,
				null, null, null, null, null);

		when(userRepository.findByEmail(entry.email())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(any())).thenReturn("encoded");
		doReturn(dto).when(userMapper).toDto(any(User.class));

		List<UserDto> results = userAccountService.bulkCreate(request, Role.COORDINATOR);

		assertEquals(1, results.size());
		verify(userRepository).save(any(Coordinator.class));
	}

	@Test
	void bulkCreateStudent_NullCne_ThrowsException() {
		BulkCreateRequest.BulkUserEntry entry = new BulkCreateRequest.BulkUserEntry("Last", "First", "student@test.com",
				null, "Major", "Level", null, null);
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry), "STUDENT");

		when(userRepository.findByEmail(entry.email())).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userAccountService.bulkCreate(request, Role.STUDENT));
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertTrue(ex.getReason().contains("cne, majorName et levelName sont requis"));
	}

	@Test
	void bulkCreateStudent_MajorNotFound_ThrowsException() {
		BulkCreateRequest.BulkUserEntry entry = new BulkCreateRequest.BulkUserEntry("Last", "First", "student@test.com",
				"CNE", "NONEXISTENT", "Level", null, null);
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry), "STUDENT");

		when(userRepository.findByEmail(entry.email())).thenReturn(Optional.empty());
		when(majorRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userAccountService.bulkCreate(request, Role.STUDENT));
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertTrue(ex.getReason().contains("Filière introuvable"));
	}

	@Test
	void bulkCreateStudent_LevelNotFound_ThrowsException() {
		BulkCreateRequest.BulkUserEntry entry = new BulkCreateRequest.BulkUserEntry("Last", "First", "student@test.com",
				"CNE", "Major", "NONEXISTENT", null, null);
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry), "STUDENT");
		Major major = new Major();

		when(userRepository.findByEmail(entry.email())).thenReturn(Optional.empty());
		when(majorRepository.findByName("Major")).thenReturn(Optional.of(major));
		when(levelRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> userAccountService.bulkCreate(request, Role.STUDENT));
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
		assertTrue(ex.getReason().contains("Niveau introuvable"));
	}

	@Test
	void bulkCreate_MultipleEntries_Success() {
		BulkCreateRequest.BulkUserEntry entry1 = new BulkCreateRequest.BulkUserEntry("Last", "First", "first@test.com",
				"CNE1", "Major", "Level", null, null);
		BulkCreateRequest.BulkUserEntry entry2 = new BulkCreateRequest.BulkUserEntry("Last", "First", "second@test.com",
				"CNE2", "Major", "Level", null, null);
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry1, entry2), "STUDENT");
		Major major = new Major();
		Level level = new Level();
		UserDto dto = new UserDto(1L, "student@test.com", "STUDENT", "Last", "First", true, "CNE", 1L, "Major", 1L,
				"Level", null, null, null, null);

		when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
		when(majorRepository.findByName("Major")).thenReturn(Optional.of(major));
		when(levelRepository.findByName("Level")).thenReturn(Optional.of(level));
		when(passwordEncoder.encode(any())).thenReturn("encoded");
		doReturn(dto).when(userMapper).toDto(any(User.class));

		List<UserDto> results = userAccountService.bulkCreate(request, Role.STUDENT);

		assertEquals(2, results.size());
		verify(userRepository, times(2)).save(any(Student.class));
	}
}
