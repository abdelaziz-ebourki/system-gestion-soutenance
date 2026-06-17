package com.system_gestion_soutenance.api.coordinator.stats.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.jury.repository.JuryRepository;
import com.system_gestion_soutenance.api.coordinator.project.repository.ProjectRepository;
import org.junit.jupiter.api.Test;

class CoordinatorStatsServiceTest {

	private final ProjectRepository projectRepository = mock(ProjectRepository.class);
	private final GroupRepository groupRepository = mock(GroupRepository.class);
	private final JuryRepository juryRepository = mock(JuryRepository.class);
	private final DefenseSessionRepository defenseSessionRepository = mock(DefenseSessionRepository.class);

	private final CoordinatorStatsService service = new CoordinatorStatsService(projectRepository, groupRepository,
			juryRepository, defenseSessionRepository);

	@Test
	void getStats_returnsCounts() {
		when(projectRepository.count()).thenReturn(10L);
		when(groupRepository.count()).thenReturn(5L);
		when(juryRepository.count()).thenReturn(8L);
		when(defenseSessionRepository.count()).thenReturn(3L);

		var result = service.getStats();

		assertEquals(10L, result.totalProjects());
		assertEquals(5L, result.totalGroups());
		assertEquals(8L, result.totalDefenses());
		assertEquals(3L, result.scheduledDefenses());
	}

	@Test
	void getStats_allZero_returnsZeroCounts() {
		when(projectRepository.count()).thenReturn(0L);
		when(groupRepository.count()).thenReturn(0L);
		when(juryRepository.count()).thenReturn(0L);
		when(defenseSessionRepository.count()).thenReturn(0L);

		var result = service.getStats();

		assertEquals(0L, result.totalProjects());
		assertEquals(0L, result.totalGroups());
		assertEquals(0L, result.totalDefenses());
		assertEquals(0L, result.scheduledDefenses());
	}
}
