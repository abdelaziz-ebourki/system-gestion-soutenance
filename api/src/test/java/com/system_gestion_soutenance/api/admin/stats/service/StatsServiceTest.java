package com.system_gestion_soutenance.api.admin.stats.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.admin.room.repository.RoomRepository;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

	@Mock
	private StudentRepository studentRepository;
	@Mock
	private TeacherRepository teacherRepository;
	@Mock
	private DepartmentRepository departmentRepository;
	@Mock
	private RoomRepository roomRepository;
	@Mock
	private DefenseSessionRepository defenseSessionRepository;
	@InjectMocks
	private StatsService statsService;

	@Test
	void getStats_returnsAllCounts() {
		when(studentRepository.count()).thenReturn(100L);
		when(teacherRepository.count()).thenReturn(20L);
		when(departmentRepository.count()).thenReturn(5L);
		when(roomRepository.count()).thenReturn(15L);
		when(defenseSessionRepository.count()).thenReturn(3L);

		Map<String, Object> stats = statsService.getStats();

		assertEquals(100L, stats.get("totalStudents"));
		assertEquals(20L, stats.get("totalTeachers"));
		assertEquals(5L, stats.get("totalDepartments"));
		assertEquals(15L, stats.get("totalRooms"));
		assertEquals(3L, stats.get("totalDefenseSessions"));
	}
}
