package com.system_gestion_soutenance.api.user.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String email;
	private String password;

	@Enumerated(EnumType.STRING)
	private Role role;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "is_active")
	private boolean isActive;

	@Column(name = "verification_token")
	private String verificationToken;

	@Column(name = "reset_token")
	private String resetToken;

	@Column(name = "reset_token_expires")
	private Instant resetTokenExpires;
}
