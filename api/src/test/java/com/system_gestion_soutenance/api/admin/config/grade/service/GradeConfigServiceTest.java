package com.system_gestion_soutenance.api.admin.config.grade.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.grade.dto.CreateGradeRequest;
import com.system_gestion_soutenance.api.admin.config.grade.entity.Grade;
import com.system_gestion_soutenance.api.admin.config.grade.repository.GradeRepository;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GradeConfigServiceTest {

	@Mock
	private GradeRepository gradeRepository;
	@Mock
	private TeacherRepository teacherRepository;

	@InjectMocks
	private GradeConfigService gradeConfigService;

	@Test
	void findAll_returnsAll() {
		when(gradeRepository.findAll()).thenReturn(List.of(new Grade()));
		assertEquals(1, gradeConfigService.findAll().size());
	}

	@Test
	void create_success() {
		when(gradeRepository.findByName("Prof")).thenReturn(Optional.empty());
		when(gradeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Grade result = gradeConfigService.create(new CreateGradeRequest("Prof"));

		assertEquals("Prof", result.getName());
	}

	@Test
	void create_duplicateName_throws() {
		when(gradeRepository.findByName("Prof")).thenReturn(Optional.of(new Grade()));
		assertThrows(ResponseStatusException.class, () -> gradeConfigService.create(new CreateGradeRequest("Prof")));
	}

	@Test
	void update_success() {
		Grade existing = new Grade(1L, "Old");
		when(gradeRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(gradeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Grade result = gradeConfigService.update(1L, new CreateGradeRequest("New"));

		assertEquals("New", result.getName());
	}

	@Test
	void update_notFound_throws() {
		when(gradeRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(ResponseStatusException.class,
				() -> gradeConfigService.update(99L, new CreateGradeRequest("Name")));
	}

	@Test
	void delete_success() {
		Grade grade = new Grade(1L, "Prof");
		when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
		when(teacherRepository.findByGradeId(1L)).thenReturn(List.of());

		gradeConfigService.delete(1L);
		verify(gradeRepository).deleteById(1L);
	}

	@Test
	void delete_notFound_throws() {
		when(gradeRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> gradeConfigService.delete(99L));
		verify(gradeRepository, never()).delete(any());
	}

	@Test
	void delete_withTeachers_throws() {
		Grade grade = new Grade(1L, "Prof");
		when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
		when(teacherRepository.findByGradeId(1L)).thenReturn(List.of(new Teacher()));

		assertThrows(ResponseStatusException.class, () -> gradeConfigService.delete(1L));
		verify(gradeRepository, never()).delete(any());
	}
}
