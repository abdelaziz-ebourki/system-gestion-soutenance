package com.system_gestion_soutenance.api.notification.event;

import lombok.Getter;

@Getter
public class DefenseSessionStatusChangedEvent extends DomainEvent {
	private final Long sessionId;
	private final String sessionName;
	private final String newStatus;

	public DefenseSessionStatusChangedEvent(String actor, Long sessionId, String sessionName, String newStatus) {
		super(actor);
		this.sessionId = sessionId;
		this.sessionName = sessionName;
		this.newStatus = newStatus;
	}
}
