package com.system_gestion_soutenance.api.admin.room.dto;

@SuppressWarnings("PMD")

public record UpdateRoomRequest(String name, Integer capacity, Long departmentId) {
}
