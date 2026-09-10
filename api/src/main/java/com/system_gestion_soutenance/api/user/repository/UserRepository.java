package com.system_gestion_soutenance.api.user.repository;

import com.system_gestion_soutenance.api.user.entity.Role;
import com.system_gestion_soutenance.api.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	Optional<User> findByVerificationToken(String verificationToken);

	Optional<User> findByResetToken(String resetToken);

	Page<User> findByRole(Role role, Pageable pageable);

	List<User> findByRole(Role role);

	@Query("""
			SELECT u FROM User u
			WHERE (:role IS NULL OR u.role = :role)
			AND (:search IS NULL
			     OR LOWER(u.lastName)  LIKE LOWER(CONCAT('%', :search, '%'))
			     OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
			     OR LOWER(u.email)     LIKE LOWER(CONCAT('%', :search, '%')))
			""")
	Page<User> findByRoleAndSearch(@Param("role") Role role, @Param("search") String search, Pageable pageable);
}
