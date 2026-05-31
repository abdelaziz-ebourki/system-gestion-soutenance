package com.system_gestion_soutenance.api.coordinator.schedule.entity;

import com.system_gestion_soutenance.api.admin.room.entity.Room;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "slot_assignment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotAssignment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String date;

	@Column(nullable = false)
	private String time;

	@Column(name = "project_id")
	private Long projectId;

	@ManyToOne
	@JoinColumn(name = "room_id")
	private Room room;
}
