package com.system_gestion_soutenance.api.notification.event;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class DomainEvent {
	private final LocalDateTime timestamp = LocalDateTime.now();
	private final String actor;

	protected DomainEvent(String actor) {
		this.actor = actor;
	}
}
