package com.system_gestion_soutenance.api.user.controller;

import com.system_gestion_soutenance.api.common.dto.ApiResponse;
import com.system_gestion_soutenance.api.common.mapper.UserMapper;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import com.system_gestion_soutenance.api.user.dto.ChangePasswordRequest;
import com.system_gestion_soutenance.api.user.dto.UpdateProfileRequest;
import com.system_gestion_soutenance.api.user.dto.UserDto;
import com.system_gestion_soutenance.api.user.entity.User;
import com.system_gestion_soutenance.api.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@SuppressWarnings("PMD")

@RestController
@RequestMapping("/api/me")
@Tag(name = "Profile", description = "Self-service profile management")
public class ProfileController {

	private final SecurityService securityService;
	private final UserProfileService userProfileService;
	private final UserMapper userMapper;

	public ProfileController(SecurityService securityService, UserProfileService userProfileService,
			UserMapper userMapper) {
		this.securityService = securityService;
		this.userProfileService = userProfileService;
		this.userMapper = userMapper;
	}

	@GetMapping
	@Operation(summary = "Get current user profile")
	public ResponseEntity<ApiResponse<UserDto>> getProfile() {
		User user = securityService.getCurrentUser();
		return ResponseEntity.ok(ApiResponse.success("Profil récupéré avec succès", userMapper.toDto(user)));
	}

	@PatchMapping
	@Operation(summary = "Update current user profile", description = "Update the authenticated user's first name and/or last name.")
	public ResponseEntity<ApiResponse<UserDto>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
		User user = securityService.getCurrentUser();
		userProfileService.updateOwnProfile(user, request);
		return ResponseEntity.ok(ApiResponse.success("Profil mis à jour avec succès", userMapper.toDto(user)));
	}

	@PutMapping("/password")
	@Operation(summary = "Change password", description = "Change the authenticated user's password. Requires current password.")
	public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
		User user = securityService.getCurrentUser();
		userProfileService.changePassword(user, request);
		return ResponseEntity.ok(ApiResponse.success("Mot de passe modifié avec succès", null));
	}
}
