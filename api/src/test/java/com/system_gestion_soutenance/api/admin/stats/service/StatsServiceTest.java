package com.system_gestion_soutenance.api.admin.stats.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import com.system_gestion_soutenance.api.admin.stats.dto.GlobalStatsResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

	@Mock
	private EntityManager entityManager;
	@Mock
	private Query query;
	@InjectMocks
	private StatsService statsService;

	@Test
	void getStats_returnsAllCounts() {
		when(entityManager.createNativeQuery(anyString())).thenReturn(query);
		when(query.getSingleResult()).thenReturn(new Object[]{100L, 20L, 5L, 15L, 3L});

		GlobalStatsResponse stats = statsService.getStats();

		assertEquals(100L, stats.totalStudents());
		assertEquals(20L, stats.totalTeachers());
		assertEquals(5L, stats.totalDepartments());
		assertEquals(15L, stats.totalRooms());
		assertEquals(3L, stats.totalDefenseSessions());
	}
}
