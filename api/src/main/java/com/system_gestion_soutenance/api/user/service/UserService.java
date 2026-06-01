package com.system_gestion_soutenance.api.user.service;

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
import com.system_gestion_soutenance.api.user.entity.Coordinator;
import com.system_gestion_soutenance.api.user.entity.Role;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.CoordinatorRepository;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Random;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final MajorRepository majorRepository;
	private final LevelRepository levelRepository;
	private final GradeRepository gradeRepository;
	private final DepartmentRepository departmentRepository;
	private final JuryMemberRepository juryMemberRepository;
	private final ProjectRepository projectRepository;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;
	private final String baseUrl;

	public UserService(UserRepository userRepository, StudentRepository studentRepository,
			TeacherRepository teacherRepository, CoordinatorRepository coordinatorRepository,
			MajorRepository majorRepository, LevelRepository levelRepository, GradeRepository gradeRepository,
			DepartmentRepository departmentRepository, JuryMemberRepository juryMemberRepository,
			ProjectRepository projectRepository, EmailService emailService, PasswordEncoder passwordEncoder,
			@Value("${app.ui.base-url}") String baseUrl) {
		this.userRepository = userRepository;
		this.majorRepository = majorRepository;
		this.levelRepository = levelRepository;
		this.gradeRepository = gradeRepository;
		this.departmentRepository = departmentRepository;
		this.juryMemberRepository = juryMemberRepository;
		this.projectRepository = projectRepository;
		this.emailService = emailService;
		this.passwordEncoder = passwordEncoder;
		this.baseUrl = baseUrl;
	}

	public PaginatedResponse<UserDto> listUsers(String role, int page, int limit, String search) {
		PageRequest pageable = PageRequest.of(page, limit);
		Page<User> userPage;

		Role roleEnum = (role != null && !role.isBlank()) ? parseRole(role) : null;

		if (search != null && !search.isBlank()) {
			userPage = userRepository.findByRoleAndSearch(roleEnum, search, pageable);
		} else if (roleEnum != null) {
			userPage = userRepository.findByRole(roleEnum, pageable);
		} else {
			userPage = userRepository.findAll(pageable);
		}

		List<UserDto> items = userPage.getContent().stream().map(UserDto::from).toList();

		return new PaginatedResponse<>(items, userPage.getTotalElements(), userPage.getTotalPages(), page, limit);
	}

	public List<UserDto> listAllByRole(String role) {
		Role roleEnum = parseRole(role);
		return userRepository.findByRole(roleEnum, PageRequest.of(0, 1000)).stream().map(UserDto::from).toList();
	}

	public UserDto createUser(CreateUserRequest request) {
		Role role = parseRole(request.role());

		if (userRepository.findByEmail(request.email()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un utilisateur avec cet email existe déjà");
		}

		User user = switch (role) {
			case STUDENT -> createStudent(request);
			case TEACHER -> createTeacher(request);
			case COORDINATOR -> createCoordinator(request);
			default -> createBaseUser(request, role);
		};

		user.setPassword(passwordEncoder.encode(generateTemporaryPassword()));
		user.setActive(false);
		user.setVerificationToken(UUID.randomUUID().toString());

		userRepository.save(user);
		sendVerificationEmail(user);

		return UserDto.from(user);
	}

	@Transactional
	public List<UserDto> bulkCreate(BulkCreateRequest request) {
		Role role = parseRole(request.role());
		List<UserDto> results = new ArrayList<>();

		for (BulkCreateRequest.BulkUserEntry entry : request.users()) {
			if (userRepository.findByEmail(entry.email()).isPresent()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Un utilisateur avec l'email " + entry.email() + " existe déjà");
			}

			User user = switch (role) {
				case STUDENT -> createBulkStudent(entry);
				case TEACHER -> createBulkTeacher(entry);
				case COORDINATOR -> createBulkCoordinator(entry);
				default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Rôle non supporté pour l'import en masse: " + role);
			};

			user.setPassword(passwordEncoder.encode(generateTemporaryPassword()));
			user.setActive(false);
			user.setVerificationToken(UUID.randomUUID().toString());

			userRepository.save(user);
			sendVerificationEmail(user);
			results.add(UserDto.from(user));
		}

		return results;
	}

	public UserDto updateUser(Long id, UpdateUserRequest request) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

		if (request.lastName() != null)
			user.setLastName(request.lastName());
		if (request.firstName() != null)
			user.setFirstName(request.firstName());
		if (request.email() != null) {
			if (userRepository.findByEmail(request.email()).isPresent() && !user.getEmail().equals(request.email())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un utilisateur avec cet email existe déjà");
			}
			user.setEmail(request.email());
		}
		if (request.role() != null) {
			user.setRole(parseRole(request.role()));
		}

		if (user instanceof Student student) {
			if (request.cne() != null)
				student.setCne(request.cne());
			if (request.majorId() != null) {
				Major major = majorRepository.findById(request.majorId())
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filière introuvable"));
				student.setMajor(major);
			}
			if (request.levelId() != null) {
				Level level = levelRepository.findById(request.levelId())
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Niveau introuvable"));
				student.setLevel(level);
			}
		}

		if (user instanceof Teacher teacher) {
			if (request.gradeId() != null) {
				Grade grade = gradeRepository.findById(request.gradeId())
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Grade introuvable"));
				teacher.setGrade(grade);
			}
			if (request.departmentId() != null) {
				Department dept = departmentRepository.findById(request.departmentId()).orElseThrow(
						() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Département introuvable"));
				teacher.setDepartment(dept);
			}
		}

		userRepository.save(user);
		return UserDto.from(user);
	}

	public void deleteUser(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

		if (user instanceof Teacher) {
			List<Department> headedDepts = departmentRepository.findByHead_Id(id);
			if (!headedDepts.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"Impossible de supprimer cet enseignant car il est responsable de département(s)");
			}
			if (!juryMemberRepository.findByTeacher_Id(id).isEmpty()) {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"Impossible de supprimer cet enseignant car il est membre d'un jury");
			}
			if (!projectRepository.findBySupervisorId(id).isEmpty()) {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"Impossible de supprimer cet enseignant car il encadre des projets");
			}
		}

		if (user instanceof Student && !projectRepository.findByStudentsId(id).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Impossible de supprimer cet étudiant car il est lié à des projets");
		}

		userRepository.delete(user);
	}

	private Student createStudent(CreateUserRequest request) {
		if (request.cne() == null || request.majorId() == null || request.levelId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Les champs cne, majorId et levelId sont requis pour un étudiant");
		}

		Major major = majorRepository.findById(request.majorId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filière introuvable"));
		Level level = levelRepository.findById(request.levelId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Niveau introuvable"));

		Student student = new Student();
		student.setEmail(request.email());
		student.setRole(Role.STUDENT);
		student.setLastName(request.lastName());
		student.setFirstName(request.firstName());
		student.setCne(request.cne());
		student.setMajor(major);
		student.setLevel(level);
		return student;
	}

	private Teacher createTeacher(CreateUserRequest request) {
		if (request.departmentId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Le champ departmentId est requis pour un enseignant");
		}

		Department dept = departmentRepository.findById(request.departmentId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Département introuvable"));

		Teacher teacher = new Teacher();
		teacher.setEmail(request.email());
		teacher.setRole(Role.TEACHER);
		teacher.setLastName(request.lastName());
		teacher.setFirstName(request.firstName());
		if (request.gradeId() != null) {
			Grade grade = gradeRepository.findById(request.gradeId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Grade introuvable"));
			teacher.setGrade(grade);
		}
		teacher.setDepartment(dept);
		return teacher;
	}

	private Coordinator createCoordinator(CreateUserRequest request) {
		Coordinator coordinator = new Coordinator();
		coordinator.setEmail(request.email());
		coordinator.setRole(Role.COORDINATOR);
		coordinator.setLastName(request.lastName());
		coordinator.setFirstName(request.firstName());
		return coordinator;
	}

	private User createBaseUser(CreateUserRequest request, Role role) {
		User user = new User();
		user.setEmail(request.email());
		user.setRole(role);
		user.setLastName(request.lastName());
		user.setFirstName(request.firstName());
		return user;
	}

	private Student createBulkStudent(BulkCreateRequest.BulkUserEntry entry) {
		if (entry.cne() == null || entry.majorName() == null || entry.levelName() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Les champs cne, majorName et levelName sont requis pour un étudiant");
		}

		Major major = majorRepository.findByName(entry.majorName()).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filière introuvable: " + entry.majorName()));
		Level level = levelRepository.findByName(entry.levelName()).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Niveau introuvable: " + entry.levelName()));

		Student student = new Student();
		student.setEmail(entry.email());
		student.setRole(Role.STUDENT);
		student.setLastName(entry.lastName());
		student.setFirstName(entry.firstName());
		student.setCne(entry.cne());
		student.setMajor(major);
		student.setLevel(level);
		return student;
	}

	private Teacher createBulkTeacher(BulkCreateRequest.BulkUserEntry entry) {
		if (entry.departmentName() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Le champ departmentName est requis pour un enseignant");
		}

		Department dept = departmentRepository.findByName(entry.departmentName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Département introuvable: " + entry.departmentName()));

		Teacher teacher = new Teacher();
		teacher.setEmail(entry.email());
		teacher.setRole(Role.TEACHER);
		teacher.setLastName(entry.lastName());
		teacher.setFirstName(entry.firstName());
		if (entry.gradeName() != null) {
			Grade grade = gradeRepository.findByName(entry.gradeName())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
							"Grade introuvable: " + entry.gradeName()));
			teacher.setGrade(grade);
		}
		teacher.setDepartment(dept);
		return teacher;
	}

	private Coordinator createBulkCoordinator(BulkCreateRequest.BulkUserEntry entry) {
		Coordinator coordinator = new Coordinator();
		coordinator.setEmail(entry.email());
		coordinator.setRole(Role.COORDINATOR);
		coordinator.setLastName(entry.lastName());
		coordinator.setFirstName(entry.firstName());
		return coordinator;
	}

	private Role parseRole(String role) {
		if (role == null || role.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le rôle est requis");
		}
		try {
			return Role.valueOf(role.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rôle invalide: " + role);
		}
	}

	private String generateTemporaryPassword() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(12);
		for (int i = 0; i < 12; i++) {
			sb.append(chars.charAt(rnd.nextInt(chars.length())));
		}
		return sb.toString();
	}

	private void sendVerificationEmail(User user) {
		String verificationLink = baseUrl + "/verify-account?token=" + user.getVerificationToken();
		emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationLink);
	}
}
