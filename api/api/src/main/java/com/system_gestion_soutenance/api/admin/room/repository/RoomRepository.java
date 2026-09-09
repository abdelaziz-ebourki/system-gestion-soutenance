package com.system_gestion_soutenance.api.admin.room.repository;

import com.system_gestion_soutenance.api.admin.room.entity.Room;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
@SuppressWarnings("PMD")

public interface RoomRepository extends JpaRepository<Room, Long> {
	List<Room> findByDepartment_Id(Long departmentId);
}