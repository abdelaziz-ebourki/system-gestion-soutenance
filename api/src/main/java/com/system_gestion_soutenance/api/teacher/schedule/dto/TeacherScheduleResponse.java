package com.system_gestion_soutenance.api.teacher.schedule.dto;

import java.util.List;

public record TeacherScheduleResponse(List<SlotDetails> slots) {
}
