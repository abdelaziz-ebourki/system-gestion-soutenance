package com.system_gestion_soutenance.api.notification.event;

import lombok.Getter;

@Getter
public class ProjectStatusChangedEvent extends DomainEvent {
	private final Long projectId;
	private final String projectTitle;
	private final String oldStatus;
	private final String newStatus;

	public ProjectStatusChangedEvent(String actor, Long projectId, String projectTitle, String oldStatus,
			String newStatus) {
		super(actor);
		this.projectId = projectId;
		this.projectTitle = projectTitle;
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
	}
}
