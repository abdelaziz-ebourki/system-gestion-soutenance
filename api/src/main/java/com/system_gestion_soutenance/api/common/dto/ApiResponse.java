package com.system_gestion_soutenance.api.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
@SuppressWarnings("PMD")

@Schema(description = "Standard API response wrapper")
public record ApiResponse<T>(
		@Schema(description = "Whether the operation was successful", example = "true") boolean success,
		@Schema(description = "Human-readable message", example = "Operation successful") String message,
		@Schema(description = "Response payload (null on error)") T data,
		@Schema(description = "Response timestamp") LocalDateTime timestamp,
		@Schema(description = "List of validation errors (null on success)") List<String> errors) {

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, "Operation successful", data, LocalDateTime.now(), null);
	}

	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(true, message, data, LocalDateTime.now(), null);
	}

	public static <T> ApiResponse<T> error(String message, List<String> errors) {
		return new ApiResponse<>(false, message, null, LocalDateTime.now(), errors);
	}

	public static <T> ApiResponse<T> error(String message) {
		return error(message, null);
	}
}
