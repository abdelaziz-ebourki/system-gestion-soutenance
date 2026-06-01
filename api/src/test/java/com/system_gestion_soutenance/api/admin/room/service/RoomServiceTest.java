package com.system_gestion_soutenance.api.admin.room.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.admin.room.dto.BulkRoomRequest;
import com.system_gestion_soutenance.api.admin.room.dto.CreateRoomRequest;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.admin.room.repository.RoomRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

	@Mock
	private RoomRepository roomRepository;
	@Mock
	private DepartmentRepository departmentRepository;
	@InjectMocks
	private RoomService roomService;

	@Test
	void findAll_returnsPaginated() {
		Page<Room> page = new PageImpl<>(List.of(new Room()));
		when(roomRepository.findAll((Pageable) any())).thenReturn(page);
		assertEquals(1, roomService.findAll(0, 10).items().size());
	}

	@Test
	void create_success() {
		Department dept = new Department();
		dept.setId(1L);
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
		when(roomRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Room result = roomService.create(new CreateRoomRequest("Salle 1", 30, 1L));
		assertEquals("Salle 1", result.getName());
		assertEquals(30, result.getCapacity());
	}

	@Test
	void create_departmentNotFound_throws() {
		when(departmentRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> roomService.create(new CreateRoomRequest("X", 10, 99L)));
	}

	@Test
	void bulkCreate_success() {
		Department dept = new Department();
		dept.setId(1L);
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
		when(roomRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

		var entry = new BulkRoomRequest.RoomEntry("S1", 20, 1L);
		List<Room> results = roomService.bulkCreate(new BulkRoomRequest(List.of(entry)));

		assertEquals(1, results.size());
	}

	@Test
	void update_success() {
		Room existing = new Room();
		existing.setId(1L);
		Department dept = new Department();
		dept.setId(1L);
		when(roomRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
		when(roomRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Room result = roomService.update(1L, new CreateRoomRequest("Updated", 25, 1L));
		assertEquals("Updated", result.getName());
	}

	@Test
	void delete_success() {
		when(roomRepository.existsById(1L)).thenReturn(true);
		roomService.delete(1L);
		verify(roomRepository).deleteById(1L);
	}

	@Test
	void delete_notFound_throws() {
		when(roomRepository.existsById(99L)).thenReturn(false);
		assertThrows(ResponseStatusException.class, () -> roomService.delete(99L));
	}
}
