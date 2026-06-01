package com.system_gestion_soutenance.api.coordinator.schedule.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.system_gestion_soutenance.api.admin.department.entity.Department;
import com.system_gestion_soutenance.api.admin.faculty.entity.Faculty;
import com.system_gestion_soutenance.api.admin.room.entity.Room;
import com.system_gestion_soutenance.api.admin.room.repository.RoomRepository;
import com.system_gestion_soutenance.api.coordinator.schedule.entity.SlotAssignment;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class SlotAssignmentRepositoryTest {

    @Autowired
    private SlotAssignmentRepository repository;

    @Autowired
    private TestEntityManager em;

    private Room savedRoom;

    @BeforeEach
    void setUp() {
        Faculty faculty = new Faculty();
        faculty.setName("Faculté des Sciences");
        faculty.setCode("FS");
        em.persist(faculty);

        Department department = new Department();
        department.setName("Informatique");
        department.setCode("INFO");
        department.setFaculty(faculty);
        em.persist(department);

        Room room = new Room();
        room.setName("Salle 101");
        room.setCapacity(30);
        room.setDepartment(department);
        savedRoom = em.persist(room);
    }

    @Test
    void findAllWithRoom_returnsSlotAssignmentsWithRoom() {
        SlotAssignment slot1 = new SlotAssignment();
        slot1.setTitle("Soutenance PFE");
        slot1.setDate("2026-06-15");
        slot1.setTime("10:00");
        slot1.setProjectId(1L);
        slot1.setRoom(savedRoom);
        em.persist(slot1);

        SlotAssignment slot2 = new SlotAssignment();
        slot2.setTitle("Soutenance MEMOIRE");
        slot2.setDate("2026-06-16");
        slot2.setTime("14:00");
        slot2.setProjectId(2L);
        slot2.setRoom(savedRoom);
        em.persist(slot2);

        em.flush();
        em.clear();

        List<SlotAssignment> result = repository.findAllWithRoom();

        assertEquals(2, result.size());
        assertNotNull(result.get(0).getRoom());
        assertEquals("Salle 101", result.get(0).getRoom().getName());
    }

    @Test
    void findByProjectId_returnsMatchingSlot() {
        SlotAssignment slot = new SlotAssignment();
        slot.setTitle("Soutenance");
        slot.setDate("2026-06-15");
        slot.setTime("10:00");
        slot.setProjectId(42L);
        slot.setRoom(savedRoom);
        em.persist(slot);

        em.flush();
        em.clear();

        List<SlotAssignment> result = repository.findByProjectId(42L);

        assertEquals(1, result.size());
        assertEquals(42L, result.get(0).getProjectId());
    }

    @Test
    void findByProjectId_noMatch_returnsEmpty() {
        List<SlotAssignment> result = repository.findByProjectId(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByProjectIdIn_returnsMatchingSlots() {
        SlotAssignment slot1 = new SlotAssignment();
        slot1.setTitle("S1");
        slot1.setDate("2026-06-15");
        slot1.setTime("10:00");
        slot1.setProjectId(1L);
        slot1.setRoom(savedRoom);
        em.persist(slot1);

        SlotAssignment slot2 = new SlotAssignment();
        slot2.setTitle("S2");
        slot2.setDate("2026-06-16");
        slot2.setTime("14:00");
        slot2.setProjectId(2L);
        slot2.setRoom(savedRoom);
        em.persist(slot2);

        em.flush();
        em.clear();

        List<SlotAssignment> result = repository.findByProjectIdIn(List.of(1L, 2L));

        assertEquals(2, result.size());
    }

    @Test
    void existsByProjectId_returnsTrue_whenExists() {
        SlotAssignment slot = new SlotAssignment();
        slot.setTitle("Soutenance");
        slot.setDate("2026-06-15");
        slot.setTime("10:00");
        slot.setProjectId(7L);
        slot.setRoom(savedRoom);
        em.persist(slot);
        em.flush();

        assertTrue(repository.existsByProjectId(7L));
    }

    @Test
    void existsByProjectId_returnsFalse_whenNotExists() {
        assertFalse(repository.existsByProjectId(999L));
    }
}
