package com.system_gestion_soutenance.api.user.service;

import com.system_gestion_soutenance.api.admin.config.teacherrank.entity.TeacherRank;
import com.system_gestion_soutenance.api.admin.config.teacherrank.repository.TeacherRankRepository;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.level.repository.LevelRepository;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.repository.MajorRepository;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.notification.service.EmailService;
import com.system_gestion_soutenance.api.user.dto.BulkCreateRequest;
import com.system_gestion_soutenance.api.user.dto.CreateUserRequest;
import com.system_gestion_soutenance.api.user.entity.*;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;

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
	private TeacherRankRepository teacherRankRepository;
	@Mock
	private DepartmentRepository departmentRepository;
	@Mock
	private EmailService emailService;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private UserCacheService userCacheService;

	private UserAccountService userAccountService;
	private final String baseUrl = "http://localhost:8080";

	@BeforeEach
	void setUp() {
		userAccountService = new UserAccountService(userRepository, majorRepository, levelRepository,
				teacherRankRepository, departmentRepository, emailService, passwordEncoder, userCacheService, baseUrl);
	}

	@Test
	void createUser_EmailExists_ThrowsException() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "test@test.com", null, null, null, null,
				null, null, null);
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> userAccountService.createUser(request, Role.STUDENT));
		assertEquals("Un utilisateur avec cet email existe déjà", ex.getMessage());
	}

	@Test
	void createUser_Student_Success() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "student@test.com", null, "CNE123", null, 1L,
				1L, null, null);
		Major major = new Major();
		Level level = new Level();

		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(levelRepository.findById(1L)).thenReturn(Optional.of(level));
		when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

		User result = userAccountService.createUser(request, Role.STUDENT);

		assertNotNull(result);
		verify(userRepository).save(any(Student.class));
		verify(emailService).sendVerificationEmail(any(), any(), any());
	}

	@Test
	void createUser_Teacher_Success() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "teacher@test.com", null, null, null, null,
				null, 1L, 1L);
		Department dept = new Department();
		TeacherRank teacherRank = new TeacherRank();

		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
		when(teacherRankRepository.findById(1L)).thenReturn(Optional.of(teacherRank));
		when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

		User result = userAccountService.createUser(request, Role.TEACHER);

		assertNotNull(result);
		verify(userRepository).save(any(Teacher.class));
	}

	@Test
	void createUser_Coordinator_Success() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "coord@test.com", null, null, null, null,
				null, null, null);

		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

		User result = userAccountService.createUser(request, Role.COORDINATOR);

		assertNotNull(result);
		verify(userRepository).save(any(Coordinator.class));
	}

	@Test
	void createUser_AdminRole_CreatesBaseUser() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "admin@test.com", null, null, null, null,
				null, null, null);

		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

		User result = userAccountService.createUser(request, Role.ADMIN);

		assertNotNull(result);
		verify(userRepository).save(any(User.class));
		verify(emailService).sendVerificationEmail(any(), any(), any());
	}

	@Test
	void createStudent_MajorNotFound_ThrowsException() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "student@test.com", null, "CNE123", null,
				99L, 1L, null, null);
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(majorRepository.findById(99L)).thenReturn(Optional.empty());

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> userAccountService.createUser(request, Role.STUDENT));
		assertEquals("Filière introuvable", ex.getMessage());
	}

	@Test
	void createStudent_LevelNotFound_ThrowsException() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "student@test.com", null, "CNE123", null, 1L,
				99L, null, null);
		Major major = new Major();
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(levelRepository.findById(99L)).thenReturn(Optional.empty());

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> userAccountService.createUser(request, Role.STUDENT));
		assertEquals("Niveau introuvable", ex.getMessage());
	}

	@Test
	void createTeacher_NullDepartment_ThrowsException() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "teacher@test.com", null, null, null, null,
				null, null, null);
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> userAccountService.createUser(request, Role.TEACHER));
		assertEquals("Le champ departmentId est requis pour un enseignant", ex.getMessage());
	}

	@Test
	void createTeacher_DepartmentNotFound_ThrowsException() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "teacher@test.com", null, null, null, null,
				null, 1L, 99L);
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> userAccountService.createUser(request, Role.TEACHER));
		assertEquals("Département introuvable", ex.getMessage());
	}

	@Test
	void createTeacher_NullGrade_Success() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "teacher@test.com", null, null, null, null,
				null, null, 1L);
		Department dept = new Department();

		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
		when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

		User result = userAccountService.createUser(request, Role.TEACHER);

		assertNotNull(result);
		verify(userRepository).save(any(Teacher.class));
	}

	@Test
	void createTeacher_GradeNotFound_ThrowsException() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "teacher@test.com", null, null, null, null,
				null, 99L, 1L);
		Department dept = new Department();
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
		when(teacherRankRepository.findById(99L)).thenReturn(Optional.empty());

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> userAccountService.createUser(request, Role.TEACHER));
		assertEquals("Rank introuvable", ex.getMessage());
	}

	@Test
	void bulkCreate_Success() {
		BulkCreateRequest.BulkUserEntry entry = new BulkCreateRequest.BulkUserEntry("Last", "First", "bulk@test.com",
				"CNE", null, "Major", "Level", null, null);
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry), "STUDENT");
		Major major = new Major();
		Level level = new Level();

		when(userRepository.findByEmail(entry.email())).thenReturn(Optional.empty());
		when(majorRepository.findByName("Major")).thenReturn(Optional.of(major));
		when(levelRepository.findByName("Level")).thenReturn(Optional.of(level));
		when(passwordEncoder.encode(any())).thenReturn("encoded");

		List<User> results = userAccountService.bulkCreate(request, Role.STUDENT);

		assertEquals(1, results.size());
		verify(userRepository, times(1)).save(any(Student.class));
	}

	@Test
	void createUser_Student_MissingFields_ThrowsException() {
		CreateUserRequest request = new CreateUserRequest("Last", "First", "student@test.com", null, null, null, null,
				null, null, null);
		when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> userAccountService.createUser(request, Role.STUDENT));
		assertTrue(ex.getMessage().contains("requis pour un étudiant"));
	}

	@Test
	void bulkCreate_UnsupportedRole_ThrowsException() {
		BulkCreateRequest.BulkUserEntry entry = new BulkCreateRequest.BulkUserEntry("Last", "First", "bulk@test.com",
				"CNE", null, "Major", "Level", null, null);
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry), "ADMIN");

		when(userRepository.findByEmail(entry.email())).thenReturn(Optional.empty());

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> userAccountService.bulkCreate(request, Role.ADMIN));
		assertTrue(ex.getMessage().contains("Rôle non supporté"));
	}

	@Test
	void bulkCreate_DuplicateEmail_ThrowsException() {
		BulkCreateRequest.BulkUserEntry entry1 = new BulkCreateRequest.BulkUserEntry("Last", "First", "first@test.com",
				"CNE", null, "Major", "Level", null, null);
		BulkCreateRequest.BulkUserEntry entry2 = new BulkCreateRequest.BulkUserEntry("Last", "First", "second@test.com",
				"CNE2", null, "Major", "Level", null, null);
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry1, entry2), "STUDENT");
		Major major = new Major();
		Level level = new Level();

		when(userRepository.findByEmail("first@test.com")).thenReturn(Optional.empty());
		when(userRepository.findByEmail("second@test.com")).thenReturn(Optional.of(new User()));
		when(majorRepository.findByName("Major")).thenReturn(Optional.of(major));
		when(levelRepository.findByName("Level")).thenReturn(Optional.of(level));

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> userAccountService.bulkCreate(request, Role.STUDENT));
		assertTrue(ex.getMessage().contains("existe déjà"));
	}

	@Test
	void bulkCreate_Teacher_Success() {
		BulkCreateRequest.BulkUserEntry entry = new BulkCreateRequest.BulkUserEntry("Last", "First", "teacher@test.com",
				null, null, null, null, null, "Dept");
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry), "TEACHER");
		Department dept = new Department();

		when(userRepository.findByEmail(entry.email())).thenReturn(Optional.empty());
		when(departmentRepository.findByName("Dept")).thenReturn(Optional.of(dept));
		when(passwordEncoder.encode(any())).thenReturn("encoded");

		List<User> results = userAccountService.bulkCreate(request, Role.TEACHER);

		assertEquals(1, results.size());
		verify(userRepository).save(any(Teacher.class));
	}

	@Test
	void bulkCreate_Coordinator_Success() {
		BulkCreateRequest.BulkUserEntry entry = new BulkCreateRequest.BulkUserEntry("Last", "First", "coord@test.com",
				null, null, null, null, null, null);
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry), "COORDINATOR");

		when(userRepository.findByEmail(entry.email())).thenReturn(Optional.empty());
		when(passwordEncoder.encode(any())).thenReturn("encoded");

		List<User> results = userAccountService.bulkCreate(request, Role.COORDINATOR);

		assertEquals(1, results.size());
		verify(userRepository).save(any(Coordinator.class));
	}

	@Test
	void bulkCreateStudent_NullCne_ThrowsException() {
		BulkCreateRequest.BulkUserEntry entry = new BulkCreateRequest.BulkUserEntry("Last", "First", "student@test.com",
				null, null, "Major", "Level", null, null);
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry), "STUDENT");

		when(userRepository.findByEmail(entry.email())).thenReturn(Optional.empty());

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> userAccountService.bulkCreate(request, Role.STUDENT));
		assertTrue(ex.getMessage().contains("cne, majorName et levelName sont requis"));
	}

	@Test
	void bulkCreateStudent_MajorNotFound_ThrowsException() {
		BulkCreateRequest.BulkUserEntry entry = new BulkCreateRequest.BulkUserEntry("Last", "First", "student@test.com",
				"CNE", null, "NONEXISTENT", "Level", null, null);
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry), "STUDENT");

		when(userRepository.findByEmail(entry.email())).thenReturn(Optional.empty());
		when(majorRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> userAccountService.bulkCreate(request, Role.STUDENT));
		assertTrue(ex.getMessage().contains("Filière introuvable"));
	}

	@Test
	void bulkCreateStudent_LevelNotFound_ThrowsException() {
		BulkCreateRequest.BulkUserEntry entry = new BulkCreateRequest.BulkUserEntry("Last", "First", "student@test.com",
				"CNE", null, "Major", "NONEXISTENT", null, null);
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry), "STUDENT");
		Major major = new Major();

		when(userRepository.findByEmail(entry.email())).thenReturn(Optional.empty());
		when(majorRepository.findByName("Major")).thenReturn(Optional.of(major));
		when(levelRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

		InvalidBusinessStateException ex = assertThrows(InvalidBusinessStateException.class,
				() -> userAccountService.bulkCreate(request, Role.STUDENT));
		assertTrue(ex.getMessage().contains("Niveau introuvable"));
	}

	@Test
	void bulkCreate_MultipleEntries_Success() {
		BulkCreateRequest.BulkUserEntry entry1 = new BulkCreateRequest.BulkUserEntry("Last", "First", "first@test.com",
				"CNE1", null, "Major", "Level", null, null);
		BulkCreateRequest.BulkUserEntry entry2 = new BulkCreateRequest.BulkUserEntry("Last", "First", "second@test.com",
				"CNE2", null, "Major", "Level", null, null);
		BulkCreateRequest request = new BulkCreateRequest(List.of(entry1, entry2), "STUDENT");
		Major major = new Major();
		Level level = new Level();

		when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
		when(majorRepository.findByName("Major")).thenReturn(Optional.of(major));
		when(levelRepository.findByName("Level")).thenReturn(Optional.of(level));
		when(passwordEncoder.encode(any())).thenReturn("encoded");

		List<User> results = userAccountService.bulkCreate(request, Role.STUDENT);

		assertEquals(2, results.size());
		verify(userRepository, times(2)).save(any(Student.class));
	}
}
