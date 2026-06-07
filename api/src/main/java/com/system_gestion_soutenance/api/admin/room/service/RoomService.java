package com.system_gestion_soutenance.api.admin.room.service;

import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.department.repository.DepartmentRepository;
import com.system_gestion_soutenance.api.admin.room.dto.BulkRoomRequest;
import com.system_gestion_soutenance.api.admin.room.dto.CreateRoomRequest;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.admin.room.repository.RoomRepository;
import com.system_gestion_soutenance.api.common.audit.Audited;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RoomService {

	private final RoomRepository roomRepository;
	private final DepartmentRepository departmentRepository;

	public RoomService(RoomRepository roomRepository, DepartmentRepository departmentRepository) {
		this.roomRepository = roomRepository;
		this.departmentRepository = departmentRepository;
	}

	public PaginatedResponse<Room> findAll(int page, int limit) {
		Page<Room> roomPage = roomRepository.findAll(PageRequest.of(page, limit));
		return new PaginatedResponse<>(roomPage.getContent(), roomPage.getTotalElements(), roomPage.getTotalPages(),
				page, limit);
	}

	@Audited(action = "CREATE", entity = "Room")
	@Transactional
	public Room create(CreateRoomRequest request) {
		Department dept = departmentRepository.findById(request.departmentId())
				.orElseThrow(() -> new InvalidBusinessStateException("Département introuvable"));

		Room room = new Room();
		room.setName(request.name());
		room.setCapacity(request.capacity());
		room.setDepartment(dept);
		return roomRepository.save(room);
	}

	@Audited(action = "BULK_CREATE", entity = "Room")
	@Transactional
	public List<Room> bulkCreate(BulkRoomRequest request) {
		List<Room> rooms = new ArrayList<>();

		for (BulkRoomRequest.RoomEntry entry : request.rooms()) {
			Department dept = departmentRepository.findById(entry.departmentId()).orElseThrow(
					() -> new InvalidBusinessStateException("Département introuvable: " + entry.departmentId()));

			Room room = new Room();
			room.setName(entry.name());
			room.setCapacity(entry.capacity());
			room.setDepartment(dept);
			rooms.add(room);
		}

		return roomRepository.saveAll(rooms);
	}

	@Audited(action = "UPDATE", entity = "Room")
	@Transactional
	public Room update(Long id, CreateRoomRequest request) {
		Room room = roomRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Salle non trouvée"));

		Department dept = departmentRepository.findById(request.departmentId())
				.orElseThrow(() -> new InvalidBusinessStateException("Département introuvable"));

		room.setName(request.name());
		room.setCapacity(request.capacity());
		room.setDepartment(dept);
		return roomRepository.save(room);
	}

	@Audited(action = "DELETE", entity = "Room")
	@Transactional
	public void delete(Long id) {
		if (!roomRepository.existsById(id)) {
			throw new EntityNotFoundException("Salle non trouvée");
		}
		roomRepository.deleteById(id);
	}
}
