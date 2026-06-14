package com.system_gestion_soutenance.api.notification.event;

import lombok.Getter;

@Getter
public class DefenseSessionUnfrozenEvent extends DomainEvent {
	private final Long sessionId;
	private final String sessionName;

	public DefenseSessionUnfrozenEvent(String actor, Long sessionId, String sessionName) {
		super(actor);
		this.sessionId = sessionId;
		this.sessionName = sessionName;
	}
}
