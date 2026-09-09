package com.system_gestion_soutenance.api.user.service;

import com.system_gestion_soutenance.api.admin.config.teacherrank.entity.TeacherRank;
import com.system_gestion_soutenance.api.admin.config.teacherrank.repository.TeacherRankRepository;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.level.repository.LevelRepository;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.repository.MajorRepository;
import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.user.dto.ChangePasswordRequest;
import com.system_gestion_soutenance.api.user.dto.UpdateProfileRequest;
import com.system_gestion_soutenance.api.user.dto.UpdateUserRequest;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.util.PasswordValidator;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@SuppressWarnings("PMD")

@Service
@Transactional
public class UserProfileService {

	private final MajorRepository majorRepository;
	private final LevelRepository levelRepository;
	private final TeacherRankRepository teacherRankRepository;
	private final DepartmentRepository departmentRepository;
	private final PasswordEncoder passwordEncoder;
	private final PasswordValidator passwordValidator;
	private final UserRepository userRepository;

	public UserProfileService(MajorRepository majorRepository, LevelRepository levelRepository,
			TeacherRankRepository teacherRankRepository, DepartmentRepository departmentRepository,
			PasswordEncoder passwordEncoder, PasswordValidator passwordValidator, UserRepository userRepository) {
		this.majorRepository = majorRepository;
		this.levelRepository = levelRepository;
		this.teacherRankRepository = teacherRankRepository;
		this.departmentRepository = departmentRepository;
		this.passwordEncoder = passwordEncoder;
		this.passwordValidator = passwordValidator;
		this.userRepository = userRepository;
	}

	public void updateBasicInfo(User user, UpdateUserRequest request) {
		if (request.lastName() != null)
			user.setLastName(request.lastName());
		if (request.firstName() != null)
			user.setFirstName(request.firstName());
	}

	public void updateStudentProfile(Student student, UpdateUserRequest request) {
		if (request.cne() != null)
			student.setCne(request.cne());
		if (request.codeApogee() != null)
			student.setCodeApogee(request.codeApogee());
		if (request.majorId() != null) {
			Major major = majorRepository.findById(request.majorId())
					.orElseThrow(() -> new InvalidBusinessStateException("Filière introuvable"));
			student.setMajor(major);
		}
		if (request.levelId() != null) {
			Level level = levelRepository.findById(request.levelId())
					.orElseThrow(() -> new InvalidBusinessStateException("Niveau introuvable"));
			student.setLevel(level);
		}
	}

	public void updateTeacherProfile(Teacher teacher, UpdateUserRequest request) {
		if (request.teacherRankId() != null) {
			TeacherRank teacherRank = teacherRankRepository.findById(request.teacherRankId())
					.orElseThrow(() -> new InvalidBusinessStateException("Rank introuvable"));
			teacher.setTeacherRank(teacherRank);
		}
		if (request.departmentId() != null) {
			Department dept = departmentRepository.findById(request.departmentId())
					.orElseThrow(() -> new InvalidBusinessStateException("Département introuvable"));
			teacher.setDepartment(dept);
		}
	}

	public void updateOwnProfile(User user, UpdateProfileRequest request) {
		if (request.lastName() != null) {
			user.setLastName(request.lastName());
		}
		if (request.firstName() != null) {
			user.setFirstName(request.firstName());
		}
		userRepository.save(user);
	}

	public void changePassword(User user, ChangePasswordRequest request) {
		if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
			throw new InvalidBusinessStateException("Le mot de passe actuel est incorrect");
		}
		try {
			passwordValidator.validate(request.newPassword());
		} catch (IllegalArgumentException e) {
			throw new InvalidBusinessStateException(e.getMessage());
		}
		user.setPassword(passwordEncoder.encode(request.newPassword()));
		userRepository.save(user);
	}
}