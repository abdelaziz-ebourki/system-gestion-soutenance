package com.system_gestion_soutenance.api.notification.event;

import lombok.Getter;

@Getter
public class StudentLeftGroupEvent extends DomainEvent {
	private final Long studentId;
	private final String studentName;
	private final Long groupId;

	public StudentLeftGroupEvent(String actor, Long studentId, String studentName, Long groupId) {
		super(actor);
		this.studentId = studentId;
		this.studentName = studentName;
		this.groupId = groupId;
	}
}
