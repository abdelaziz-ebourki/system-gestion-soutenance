package com.system_gestion_soutenance.api.teacher.unavailability.service;

import com.system_gestion_soutenance.api.coordinator.unavailability.entity.Unavailability;
import com.system_gestion_soutenance.api.coordinator.unavailability.repository.UnavailabilityRepository;
import com.system_gestion_soutenance.api.teacher.unavailability.dto.TeacherUnavailabilityRequest;
import com.system_gestion_soutenance.api.teacher.unavailability.dto.TeacherUnavailabilityResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherUnavailabilityService {

	private final UnavailabilityRepository repository;

	public TeacherUnavailabilityService(UnavailabilityRepository repository) {
		this.repository = repository;
	}

	public TeacherUnavailabilityResponse getByTeacher(Long teacherId) {
		Map<String, List<String>> slotsByDate = new LinkedHashMap<>();
		for (Unavailability u : repository.findAll()) {
			if (u.getTeacherId().equals(teacherId)) {
				slotsByDate.put(u.getDate(), u.getSlots());
			}
		}
		return new TeacherUnavailabilityResponse(slotsByDate);
	}

	@Transactional
	public TeacherUnavailabilityResponse saveForTeacher(Long teacherId, TeacherUnavailabilityRequest request) {
		List<Unavailability> existing = new ArrayList<>();
		for (Unavailability u : repository.findAll()) {
			if (u.getTeacherId().equals(teacherId)) {
				existing.add(u);
			}
		}
		repository.deleteAll(existing);

		for (var slotRequest : request.slots()) {
			Unavailability u = new Unavailability();
			u.setTeacherId(teacherId);
			u.setDate(slotRequest.date());
			u.setSlots(slotRequest.slots());
			repository.save(u);
		}

		return getByTeacher(teacherId);
	}
}
