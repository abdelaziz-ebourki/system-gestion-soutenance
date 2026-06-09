package com.system_gestion_soutenance.api.coordinator.unavailability.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
@SuppressWarnings("PMD")

@Entity
@Table(name = "unavailability")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Unavailability {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "teacher_id", nullable = false)
	private Long teacherId;

	@Column(nullable = false)
	private String date;

	@ElementCollection
	@CollectionTable(name = "unavailability_slots", joinColumns = @JoinColumn(name = "unavailability_id"))
	@Column(name = "slot")
	private List<String> slots;
}