package com.system_gestion_soutenance.api.admin.config.email.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "email_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailConfig {

	@Id
	private Long id = 1L;

	private String host;

	private int port;

	private String username;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	@Column(name = "sender_name")
	private String senderName;

	@Column(name = "sender_email")
	private String senderEmail;

	private String encryption;
}
