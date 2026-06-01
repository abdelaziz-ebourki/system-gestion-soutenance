package com.system_gestion_soutenance.api.user.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.system_gestion_soutenance.api.user.entity.Role;
import com.system_gestion_soutenance.api.user.entity.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void findByRoleAndSearch_withRoleAndNoSearch_returnsUsersWithRole() {
        createUser("admin@test.com", Role.ADMIN, "Admin", "User");
        createUser("coord@test.com", Role.COORDINATOR, "Coord", "User");
        createUser("teacher@test.com", Role.TEACHER, "Teacher", "User");
        em.flush();

        Page<User> result = repository.findByRoleAndSearch(Role.ADMIN, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(Role.ADMIN, result.getContent().get(0).getRole());
    }

    @Test
    void findByRoleAndSearch_withSearch_filtersByLastName() {
        createUser("alice@test.com", Role.TEACHER, "Smith", "Alice");
        createUser("bob@test.com", Role.TEACHER, "Jones", "Bob");
        createUser("charlie@test.com", Role.STUDENT, "Smith", "Charlie");
        em.flush();

        Page<User> result = repository.findByRoleAndSearch(null, "Smith", PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(u -> u.getLastName().equals("Smith")));
    }

    @Test
    void findByRoleAndSearch_withSearch_filtersByFirstName() {
        createUser("alice@test.com", Role.STUDENT, "Dupont", "Alice");
        createUser("bob@test.com", Role.STUDENT, "Martin", "Bob");
        em.flush();

        Page<User> result = repository.findByRoleAndSearch(Role.STUDENT, "Alice", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Alice", result.getContent().get(0).getFirstName());
    }

    @Test
    void findByRoleAndSearch_withSearch_filtersByEmail() {
        createUser("john.doe@test.com", Role.TEACHER, "Doe", "John");
        createUser("jane.doe@test.com", Role.TEACHER, "Doe", "Jane");
        createUser("other@test.com", Role.STUDENT, "Other", "User");
        em.flush();

        Page<User> result = repository.findByRoleAndSearch(Role.TEACHER, "doe", PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void findByRoleAndSearch_withNullRoleAndNullSearch_returnsAllUsersPaginated() {
        createUser("u1@test.com", Role.ADMIN, "A", "U1");
        createUser("u2@test.com", Role.COORDINATOR, "B", "U2");
        createUser("u3@test.com", Role.TEACHER, "C", "U3");
        em.flush();

        Page<User> result = repository.findByRoleAndSearch(null, null, PageRequest.of(0, 10));

        assertEquals(3, result.getTotalElements());
    }

    @Test
    void findByRoleAndSearch_caseInsensitiveSearch() {
        createUser("test@test.com", Role.ADMIN, "Dupont", "Jean");
        em.flush();

        Page<User> result = repository.findByRoleAndSearch(null, "dupont", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByRole_returnsAllUsersWithRole() {
        createUser("admin1@test.com", Role.ADMIN, "A", "1");
        createUser("admin2@test.com", Role.ADMIN, "A", "2");
        createUser("teacher@test.com", Role.TEACHER, "T", "1");
        em.flush();

        List<User> result = repository.findByRole(Role.ADMIN);

        assertEquals(2, result.size());
    }

    @Test
    void findByEmail_returnsUser() {
        createUser("unique@test.com", Role.ADMIN, "Unique", "User");
        em.flush();

        assertTrue(repository.findByEmail("unique@test.com").isPresent());
        assertFalse(repository.findByEmail("nonexistent@test.com").isPresent());
    }

    private void createUser(String email, Role role, String lastName, String firstName) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(role);
        user.setLastName(lastName);
        user.setFirstName(firstName);
        user.setActive(true);
        em.persist(user);
    }
}
