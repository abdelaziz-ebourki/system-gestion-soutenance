package com.system_gestion_soutenance.api.coordinator.stats.service;

import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import com.system_gestion_soutenance.api.coordinator.stats.dto.CoordinatorStatsResponse;
import org.springframework.stereotype.Service;
@SuppressWarnings("PMD")

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

	public CoordinatorStatsResponse getStats() {
		long totalProjects = projectRepository.count();
		long totalGroups = groupRepository.count();
		long totalJuries = juryRepository.count();
		long scheduledDefenses = defenseSessionRepository.count();

		return new CoordinatorStatsResponse(totalProjects, totalGroups, totalJuries, scheduledDefenses);
	}
}