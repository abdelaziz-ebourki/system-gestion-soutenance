package com.system_gestion_soutenance.api.notification.event;

import lombok.Getter;

@Getter
public class StudentDocumentSubmittedEvent extends DomainEvent {
	private final Long documentId;
	private final String documentName;
	private final String studentName;

	public StudentDocumentSubmittedEvent(String actor, Long documentId, String documentName, String studentName) {
		super(actor);
		this.documentId = documentId;
		this.documentName = documentName;
		this.studentName = studentName;
	}
}
