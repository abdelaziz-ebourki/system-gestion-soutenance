package com.system_gestion_soutenance.api.coordinator.stats.service;

import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CoordinatorStatsService {

	private final ProjectRepository projectRepository;
	private final GroupRepository groupRepository;
	private final JuryRepository juryRepository;
	private final DefenseSessionRepository defenseSessionRepository;

	public CoordinatorStatsService(ProjectRepository projectRepository, GroupRepository groupRepository,
			JuryRepository juryRepository, DefenseSessionRepository defenseSessionRepository) {
		this.projectRepository = projectRepository;
		this.groupRepository = groupRepository;
		this.juryRepository = juryRepository;
		this.defenseSessionRepository = defenseSessionRepository;
	}

	public Map<String, Object> getStats() {
		Map<String, Object> stats = new HashMap<>();
		stats.put("totalProjects", projectRepository.count());
		stats.put("totalGroups", groupRepository.count());
		stats.put("totalJuries", juryRepository.count());
		stats.put("scheduledDefenses", defenseSessionRepository.count());
		return stats;
	}
}
