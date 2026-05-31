package com.system_gestion_soutenance.api.user.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

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
