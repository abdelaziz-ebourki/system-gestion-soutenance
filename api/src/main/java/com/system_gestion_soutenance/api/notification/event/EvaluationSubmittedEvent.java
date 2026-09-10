package com.system_gestion_soutenance.api.notification.event;

import lombok.Getter;

@Getter
public class EvaluationSubmittedEvent extends DomainEvent {
	private final Long evaluationId;
	private final String projectTitle;
	private final double score;

	public EvaluationSubmittedEvent(String actor, Long evaluationId, String projectTitle, double score) {
		super(actor);
		this.evaluationId = evaluationId;
		this.projectTitle = projectTitle;
		this.score = score;
	}
}
