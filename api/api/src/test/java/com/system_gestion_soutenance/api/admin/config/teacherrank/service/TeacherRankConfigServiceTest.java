package com.system_gestion_soutenance.api.admin.config.teacherrank.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.teacherrank.dto.CreateTeacherRankRequest;
import com.system_gestion_soutenance.api.admin.config.teacherrank.entity.TeacherRank;
import com.system_gestion_soutenance.api.admin.config.teacherrank.repository.TeacherRankRepository;
import com.system_gestion_soutenance.api.user.entity.Teacher;
import com.system_gestion_soutenance.api.user.repository.TeacherRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import com.system_gestion_soutenance.api.common.exception.ResourceConflictException;

@ExtendWith(MockitoExtension.class)
class TeacherRankConfigServiceTest {

	@Mock
	private TeacherRankRepository teacherRankRepository;
	@Mock
	private TeacherRepository teacherRepository;

	@InjectMocks
	private TeacherRankConfigService teacherRankConfigService;

	@Test
	void findAll_returnsAll() {
		when(teacherRankRepository.findAll()).thenReturn(List.of(new TeacherRank()));
		assertEquals(1, teacherRankConfigService.findAll().size());
	}

	@Test
	void create_success() {
		when(teacherRankRepository.findByName("Prof")).thenReturn(Optional.empty());
		when(teacherRankRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		TeacherRank result = teacherRankConfigService.create(new CreateTeacherRankRequest("Prof"));

		assertEquals("Prof", result.getName());
	}

	@Test
	void create_duplicateName_throws() {
		when(teacherRankRepository.findByName("Prof")).thenReturn(Optional.of(new TeacherRank()));
		assertThrows(InvalidBusinessStateException.class,
				() -> teacherRankConfigService.create(new CreateTeacherRankRequest("Prof")));
	}

	@Test
	void update_success() {
		TeacherRank existing = new TeacherRank(1L, "Old");
		when(teacherRankRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(teacherRankRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		TeacherRank result = teacherRankConfigService.update(1L, new CreateTeacherRankRequest("New"));

		assertEquals("New", result.getName());
	}

	@Test
	void update_notFound_throws() {
		when(teacherRankRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(EntityNotFoundException.class,
				() -> teacherRankConfigService.update(99L, new CreateTeacherRankRequest("Name")));
	}

	@Test
	void delete_success() {
		TeacherRank teacherRank = new TeacherRank(1L, "Prof");
		when(teacherRankRepository.findById(1L)).thenReturn(Optional.of(teacherRank));
		when(teacherRepository.findByTeacherRankId(1L)).thenReturn(List.of());

		teacherRankConfigService.delete(1L);
		verify(teacherRankRepository).deleteById(1L);
	}

	@Test
	void delete_notFound_throws() {
		when(teacherRankRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(EntityNotFoundException.class, () -> teacherRankConfigService.delete(99L));
		verify(teacherRankRepository, never()).delete(any());
	}

	@Test
	void delete_withTeachers_throws() {
		TeacherRank teacherRank = new TeacherRank(1L, "Prof");
		when(teacherRankRepository.findById(1L)).thenReturn(Optional.of(teacherRank));
		when(teacherRepository.findByTeacherRankId(1L)).thenReturn(List.of(new Teacher()));

		assertThrows(ResourceConflictException.class, () -> teacherRankConfigService.delete(1L));
		verify(teacherRankRepository, never()).delete(any());
	}
}
