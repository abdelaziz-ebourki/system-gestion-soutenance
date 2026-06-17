package com.system_gestion_soutenance.api.coordinator.stats.service;

import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.stats.dto.CoordinatorStatsResponse;
import org.springframework.stereotype.Service;
@SuppressWarnings("PMD")

@Service
public class CoordinatorStatsService {

	private final ProjectRepository projectRepository;
	private final GroupRepository groupRepository;
	private final DefenseRepository defenseRepository;
	private final DefenseSessionRepository defenseSessionRepository;

	public CoordinatorStatsService(ProjectRepository projectRepository, GroupRepository groupRepository,
			DefenseRepository defenseRepository, DefenseSessionRepository defenseSessionRepository) {
		this.projectRepository = projectRepository;
		this.groupRepository = groupRepository;
		this.defenseRepository = defenseRepository;
		this.defenseSessionRepository = defenseSessionRepository;
	}

	public CoordinatorStatsResponse getStats() {
		long totalProjects = projectRepository.count();
		long totalGroups = groupRepository.count();
		long totalDefenses = defenseRepository.count();
		long scheduledDefenses = defenseSessionRepository.count();

		return new CoordinatorStatsResponse(totalProjects, totalGroups, totalDefenses, scheduledDefenses);
	}
}
