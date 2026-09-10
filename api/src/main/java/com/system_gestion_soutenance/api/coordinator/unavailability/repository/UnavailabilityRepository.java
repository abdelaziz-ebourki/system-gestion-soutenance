package com.system_gestion_soutenance.api.coordinator.unavailability.repository;

import com.system_gestion_soutenance.api.coordinator.unavailability.entity.Unavailability;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnavailabilityRepository extends JpaRepository<Unavailability, Long> {
}
