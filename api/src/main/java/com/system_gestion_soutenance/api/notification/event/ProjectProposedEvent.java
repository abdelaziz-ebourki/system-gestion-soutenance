package com.system_gestion_soutenance.api.notification.event;

import lombok.Getter;

@Getter
public class ProjectProposedEvent extends DomainEvent {
	private final Long projectId;
	private final String projectTitle;
	private final String studentName;

	public ProjectProposedEvent(String actor, Long projectId, String projectTitle, String studentName) {
		super(actor);
		this.projectId = projectId;
		this.projectTitle = projectTitle;
		this.studentName = studentName;
	}
}
