package com.system_gestion_soutenance.api.coordinator.stats.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;

class CoordinatorStatsServiceTest {

	private final EntityManager entityManager = mock(EntityManager.class);
	private final Query query = mock(Query.class);

	private final CoordinatorStatsService service = new CoordinatorStatsService(entityManager);

	@Test
	void getStats_returnsCounts() {
		when(entityManager.createNativeQuery(anyString())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(new Object[]{10L, 5L, 8L, 3L});

		var result = service.getStats();

		assertEquals(10L, result.totalProjects());
		assertEquals(5L, result.totalGroups());
		assertEquals(8L, result.totalJuries());
		assertEquals(3L, result.scheduledDefenses());
	}

	@Test
	void getStats_allZero_returnsZeroCounts() {
		when(entityManager.createNativeQuery(anyString())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(new Object[]{0L, 0L, 0L, 0L});

		var result = service.getStats();

		assertEquals(0L, result.totalProjects());
		assertEquals(0L, result.totalGroups());
	}
}
