package com.system_gestion_soutenance.api.common.dto;

import java.time.LocalDateTime;
import java.util.List;
@SuppressWarnings("PMD")

public record ApiResponse<T>(boolean success, String message, T data, LocalDateTime timestamp, List<String> errors) {
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