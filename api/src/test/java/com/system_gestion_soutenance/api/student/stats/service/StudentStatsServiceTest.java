package com.system_gestion_soutenance.api.student.stats.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.coordinator.group.entity.Group;
import com.system_gestion_soutenance.api.coordinator.group.repository.GroupRepository;
import com.system_gestion_soutenance.api.coordinator.defense.repository.DefenseRepository;
import com.system_gestion_soutenance.api.coordinator.project.entity.Project;
import com.system_gestion_soutenance.api.student.document.entity.StudentDocument;
import com.system_gestion_soutenance.api.student.document.repository.StudentDocumentRepository;
import com.system_gestion_soutenance.api.user.entity.Student;
import java.util.List;
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
	private DefenseRepository defenseRepository;

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
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		when(defenseRepository.existsByProject_Id(10L)).thenReturn(true);

		com.system_gestion_soutenance.api.student.stats.dto.StudentStatsResponse result = service.getStats(1L);

		assertEquals(2, result.documentCount());
		assertEquals(1L, result.missingDocuments());
		assertEquals(2, result.groupMembers());
		assertEquals("scheduled", result.defenseStatus());
	}

	@Test
	void getStats_withGroupButNoProject_returnsPending() {
		StudentDocument doc = new StudentDocument();
		doc.setStatus("submitted");

		Group group = new Group();
		group.setProject(null);
		group.setStudents(List.of(student(1L)));

		when(documentRepository.findByStudentId(1L)).thenReturn(List.of(doc));
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));

		com.system_gestion_soutenance.api.student.stats.dto.StudentStatsResponse result = service.getStats(1L);

		assertEquals("pending", result.defenseStatus());
		assertEquals(0L, result.missingDocuments());
	}

	@Test
	void getStats_withGroupAndProjectButNoSchedule_returnsPending() {
		Project project = new Project();
		project.setId(10L);

		Group group = new Group();
		group.setProject(project);
		group.setStudents(List.of(student(1L)));

		when(documentRepository.findByStudentId(1L)).thenReturn(List.of());
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.of(group));
		when(defenseRepository.existsByProject_Id(10L)).thenReturn(false);

		com.system_gestion_soutenance.api.student.stats.dto.StudentStatsResponse result = service.getStats(1L);

		assertEquals("pending", result.defenseStatus());
	}

	@Test
	void getStats_noGroup_returnsZeroMembers() {
		when(documentRepository.findByStudentId(1L)).thenReturn(List.of());
		when(groupRepository.findFirstByStudentsIdOrderByIdAsc(1L)).thenReturn(Optional.empty());

		com.system_gestion_soutenance.api.student.stats.dto.StudentStatsResponse result = service.getStats(1L);

		assertEquals(0, result.documentCount());
		assertEquals(0L, result.missingDocuments());
		assertEquals(0, result.groupMembers());
		assertEquals("pending", result.defenseStatus());
	}

	private static Student student(Long id) {
		Student s = new Student();
		s.setId(id);
		return s;
	}
}
