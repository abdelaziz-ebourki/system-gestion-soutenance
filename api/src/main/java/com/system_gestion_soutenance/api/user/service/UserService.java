package com.system_gestion_soutenance.api.user.service;

import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.user.dto.BulkCreateRequest;
import com.system_gestion_soutenance.api.user.dto.CreateUserRequest;
import com.system_gestion_soutenance.api.user.dto.UpdateUserRequest;
import com.system_gestion_soutenance.api.user.entity.Role;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final UserAccountService accountService;
	private final UserProfileService profileService;
	private final UserConstraintService constraintService;

	public UserService(UserRepository userRepository, UserAccountService accountService,
			UserProfileService profileService, UserConstraintService constraintService) {
		this.userRepository = userRepository;
		this.accountService = accountService;
		this.profileService = profileService;
		this.constraintService = constraintService;
	}

	public Page<User> listUsers(String role, int page, int limit, String search) {
		PageRequest pageable = PageRequest.of(page, limit);

		Role roleEnum = (role != null && !role.isBlank()) ? parseRole(role) : null;

		if (search != null && !search.isBlank()) {
			return userRepository.findByRoleAndSearch(roleEnum, search, pageable);
		} else if (roleEnum != null) {
			return userRepository.findByRole(roleEnum, pageable);
		} else {
			return userRepository.findAll(pageable);
		}
	}

	public List<User> listAllByRole(String role) {
		Role roleEnum = parseRole(role);
		return userRepository.findByRole(roleEnum, PageRequest.of(0, 1000)).getContent();
	}

	@Audited(action = "CREATE", entity = "User")
	public User createUser(CreateUserRequest request) {
		Role role = parseRole(request.role());
		return accountService.createUser(request, role);
	}

	@Audited(action = "BULK_CREATE", entity = "User")
	@Transactional
	public List<User> bulkCreate(BulkCreateRequest request) {
		Role role = parseRole(request.role());
		return accountService.bulkCreate(request, role);
	}

	@Audited(action = "UPDATE", entity = "User")
	@Transactional
	@CacheEvict(value = "users", key = "#id")
	public User updateUser(Long id, UpdateUserRequest request) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

		profileService.updateBasicInfo(user, request);

		if (user instanceof Student student) {
			profileService.updateStudentProfile(student, request);
		} else if (user instanceof Teacher teacher) {
			profileService.updateTeacherProfile(teacher, request);
		}

		if (request.role() != null) {
			user.setRole(parseRole(request.role()));
		}

		return userRepository.save(user);
	}

	@Audited(action = "DELETE", entity = "User")
	@Transactional
	@CacheEvict(value = "users", key = "#id")
	public void deleteUser(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

		if (user instanceof Teacher) {
			constraintService.checkTeacherDeletionConstraints(id);
		} else if (user instanceof Student) {
			constraintService.checkStudentDeletionConstraints(id);
		}

		userRepository.delete(user);
	}

	private Role parseRole(String role) {
		if (role == null || role.isBlank()) {
			throw new InvalidBusinessStateException("Le rôle est requis");
		}
		try {
			return Role.valueOf(role.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new InvalidBusinessStateException("Rôle invalide: " + role);
		}
	}
}
