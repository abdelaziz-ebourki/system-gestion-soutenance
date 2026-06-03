package com.system_gestion_soutenance.api.student.stats.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.coordinator.schedule.repository.SlotAssignmentRepository;
import com.system_gestion_soutenance.api.student.document.entity.StudentDocument;
import com.system_gestion_soutenance.api.student.document.repository.StudentDocumentRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentStatsServiceTest {

	@Mock
	private StudentDocumentRepository documentRepository;
	@Mock
	private GroupRepository groupRepository;
	@Mock
	private SlotAssignmentRepository slotAssignmentRepository;

	@InjectMocks
	private StudentStatsService service;

	@Test
	void getStats_returnsStats() {
		StudentDocument doc1 = new StudentDocument();
		doc1.setStatus("submitted");
		StudentDocument doc2 = new StudentDocument();
		doc2.setStatus("missing");

		Project project = new Project();
		project.setId(10L);

		Group group = new Group();
		group.setProject(project);
		group.setStudents(List.of(student(1L), student(2L)));

		when(documentRepository.findByStudentId(1L)).thenReturn(List.of(doc1, doc2));
		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.of(group));
		when(slotAssignmentRepository.existsByProjectId(10L)).thenReturn(true);

		Map<String, Object> result = service.getStats(1L);

		assertEquals(2, result.get("documentCount"));
		assertEquals(1L, result.get("missingDocuments"));
		assertEquals(2, result.get("groupMembers"));
		assertEquals("scheduled", result.get("defenseStatus"));
	}

	@Test
	void getStats_withGroupButNoProject_returnsPending() {
		StudentDocument doc = new StudentDocument();
		doc.setStatus("submitted");

		Group group = new Group();
		group.setProject(null);
		group.setStudents(List.of(student(1L)));

		when(documentRepository.findByStudentId(1L)).thenReturn(List.of(doc));
		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.of(group));

		Map<String, Object> result = service.getStats(1L);

		assertEquals("pending", result.get("defenseStatus"));
		assertEquals(0L, result.get("missingDocuments"));
	}

	@Test
	void getStats_withGroupAndProjectButNoSchedule_returnsPending() {
		Project project = new Project();
		project.setId(10L);

		Group group = new Group();
		group.setProject(project);
		group.setStudents(List.of(student(1L)));

		when(documentRepository.findByStudentId(1L)).thenReturn(List.of());
		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.of(group));
		when(slotAssignmentRepository.existsByProjectId(10L)).thenReturn(false);

		Map<String, Object> result = service.getStats(1L);

		assertEquals("pending", result.get("defenseStatus"));
	}

	@Test
	void getStats_noGroup_returnsZeroMembers() {
		when(documentRepository.findByStudentId(1L)).thenReturn(List.of());
		when(groupRepository.findByStudentId(1L)).thenReturn(Optional.empty());

		Map<String, Object> result = service.getStats(1L);

		assertEquals(0, result.get("documentCount"));
		assertEquals(0L, result.get("missingDocuments"));
		assertEquals(0, result.get("groupMembers"));
		assertEquals("pending", result.get("defenseStatus"));
	}

	private static Student student(Long id) {
		Student s = new Student();
		s.setId(id);
		return s;
	}
}
