package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.notification.entity.AppNotification;
import com.system_gestion_soutenance.api.notification.dto.AppNotificationDto;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface AppNotificationMapper {
	AppNotificationDto toDto(AppNotification notification);
}
