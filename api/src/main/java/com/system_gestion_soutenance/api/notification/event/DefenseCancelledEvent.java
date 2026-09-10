package com.system_gestion_soutenance.api.notification.event;

import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class DefenseCancelledEvent extends DomainEvent {
	private final Long defenseId;
	private final LocalDate date;
	private final LocalTime time;

	public DefenseCancelledEvent(String actor, Long defenseId, LocalDate date, LocalTime time) {
		super(actor);
		this.defenseId = defenseId;
		this.date = date;
		this.time = time;
	}
}
