package com.system_gestion_soutenance.api.notification.repository;

import com.system_gestion_soutenance.api.notification.entity.AppNotification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<AppNotification, Long> {
	List<AppNotification> findAllByOrderByTimestampDesc();

	Page<AppNotification> findAllByOrderByTimestampDesc(Pageable pageable);

	Page<AppNotification> findByUserIdOrUserIdIsNullOrderByTimestampDesc(Long userId, Pageable pageable);
}
