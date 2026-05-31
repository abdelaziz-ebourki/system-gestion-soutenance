package com.system_gestion_soutenance.api.teacher.unavailability.service;

import com.system_gestion_soutenance.api.coordinator.unavailability.entity.Unavailability;
import com.system_gestion_soutenance.api.coordinator.unavailability.repository.UnavailabilityRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TeacherUnavailabilityService {

	private final UnavailabilityRepository repository;

	public TeacherUnavailabilityService(UnavailabilityRepository repository) {
		this.repository = repository;
	}

	public Map<String, Object> getByTeacher(Long teacherId) {
		Map<String, List<String>> slotsByDate = new LinkedHashMap<>();
		for (Unavailability u : repository.findAll()) {
			if (u.getTeacherId().equals(teacherId)) {
				slotsByDate.put(u.getDate(), u.getSlots());
			}
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("slotsByDate", slotsByDate);
		return result;
	}

	public Map<String, Object> saveForTeacher(Long teacherId, Map<String, List<String>> slotsByDate) {
		List<Unavailability> existing = new ArrayList<>();
		for (Unavailability u : repository.findAll()) {
			if (u.getTeacherId().equals(teacherId)) {
				existing.add(u);
			}
		}
		repository.deleteAll(existing);

		for (Map.Entry<String, List<String>> entry : slotsByDate.entrySet()) {
			Unavailability u = new Unavailability();
			u.setTeacherId(teacherId);
			u.setDate(entry.getKey());
			u.setSlots(entry.getValue());
			repository.save(u);
		}

		return getByTeacher(teacherId);
	}
}
