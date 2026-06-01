package com.system_gestion_soutenance.api.user.service;

import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.mapper.UserMapper;
import com.system_gestion_soutenance.api.user.dto.BulkCreateRequest;
import com.system_gestion_soutenance.api.user.dto.CreateUserRequest;
import com.system_gestion_soutenance.api.user.dto.UpdateUserRequest;
import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.entity.Role;
import com.system_gestion_soutenance.api.user.entity.Student;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final UserAccountService accountService;
	private final UserProfileService profileService;
	private final UserConstraintService constraintService;

	public UserService(UserRepository userRepository, UserMapper userMapper, UserAccountService accountService,
			UserProfileService profileService, UserConstraintService constraintService) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.accountService = accountService;
		this.profileService = profileService;
		this.constraintService = constraintService;
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

		List<UserDto> items = userPage.getContent().stream().map(userMapper::toDto).toList();

		return new PaginatedResponse<>(items, userPage.getTotalElements(), userPage.getTotalPages(), page, limit);
	}

	public List<UserDto> listAllByRole(String role) {
		Role roleEnum = parseRole(role);
		return userRepository.findByRole(roleEnum, PageRequest.of(0, 1000)).stream().map(userMapper::toDto).toList();
	}

	public UserDto createUser(CreateUserRequest request) {
		Role role = parseRole(request.role());
		return accountService.createUser(request, role);
	}

	@Transactional
	public List<UserDto> bulkCreate(BulkCreateRequest request) {
		Role role = parseRole(request.role());
		return accountService.bulkCreate(request, role);
	}

	@Transactional
	@CacheEvict(value = "users", key = "#id")
	public UserDto updateUser(Long id, UpdateUserRequest request) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
						org.springframework.http.HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

		profileService.updateBasicInfo(user, request);

		if (user instanceof Student student) {
			profileService.updateStudentProfile(student, request);
		} else if (user instanceof Teacher teacher) {
			profileService.updateTeacherProfile(teacher, request);
		}

		if (request.role() != null) {
			user.setRole(parseRole(request.role()));
		}

		userRepository.save(user);
		return userMapper.toDto(user);
	}

	@Transactional
	@CacheEvict(value = "users", key = "#id")
	public void deleteUser(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
						org.springframework.http.HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

		if (user instanceof Teacher) {
			constraintService.checkTeacherDeletionConstraints(id);
		} else if (user instanceof Student) {
			constraintService.checkStudentDeletionConstraints(id);
		}

		userRepository.delete(user);
	}

	private Role parseRole(String role) {
		if (role == null || role.isBlank()) {
			throw new org.springframework.web.server.ResponseStatusException(
					org.springframework.http.HttpStatus.BAD_REQUEST, "Le rôle est requis");
		}
		try {
			return Role.valueOf(role.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new org.springframework.web.server.ResponseStatusException(
					org.springframework.http.HttpStatus.BAD_REQUEST, "Rôle invalide: " + role);
		}
	}
}
