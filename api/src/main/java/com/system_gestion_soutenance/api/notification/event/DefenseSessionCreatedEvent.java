package com.system_gestion_soutenance.api.notification.event;

import lombok.Getter;

@Getter
public class DefenseSessionCreatedEvent extends DomainEvent {
	private final Long sessionId;
	private final String sessionName;

	public DefenseSessionCreatedEvent(String actor, Long sessionId, String sessionName) {
		super(actor);
		this.sessionId = sessionId;
		this.sessionName = sessionName;
	}
}
