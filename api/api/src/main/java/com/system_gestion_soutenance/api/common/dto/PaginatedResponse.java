package com.system_gestion_soutenance.api.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
@SuppressWarnings("PMD")

@Schema(description = "Paginated list of items")
public record PaginatedResponse<T>(@Schema(description = "List of items for the current page") List<T> items,
		@Schema(description = "Total number of items across all pages", example = "42") long total,
		@Schema(description = "Total number of pages", example = "5") int pageCount,
		@Schema(description = "Current page number (0-based)", example = "0") int currentPage,
		@Schema(description = "Number of items per page", example = "10") int size) {
}
