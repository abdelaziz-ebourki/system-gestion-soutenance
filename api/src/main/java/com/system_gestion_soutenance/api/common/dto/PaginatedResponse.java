package com.system_gestion_soutenance.api.common.dto;

import java.util.List;

public record PaginatedResponse<T>(List<T> items, long total, int pageCount) {
}
