package com.system_gestion_soutenance.api.admin.config.level.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.level.dto.CreateLevelRequest;
import com.system_gestion_soutenance.api.admin.config.level.entity.Level;
import com.system_gestion_soutenance.api.admin.config.level.repository.LevelRepository;
import com.system_gestion_soutenance.api.user.repository.StudentRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class LevelConfigServiceTest {

	@Mock
	private LevelRepository levelRepository;
	@Mock
	private StudentRepository studentRepository;
	@InjectMocks
	private LevelConfigService levelConfigService;

	@Test
	void findAll_returnsAll() {
		when(levelRepository.findAll()).thenReturn(List.of(new Level()));
		assertEquals(1, levelConfigService.findAll().size());
	}

	@Test
	void create_success() {
		when(levelRepository.findByName("S6")).thenReturn(Optional.empty());
		when(levelRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Level result = levelConfigService.create(new CreateLevelRequest("S6"));
		assertEquals("S6", result.getName());
	}

	@Test
	void create_duplicate_throws() {
		when(levelRepository.findByName("S6")).thenReturn(Optional.of(new Level()));
		assertThrows(ResponseStatusException.class, () -> levelConfigService.create(new CreateLevelRequest("S6")));
	}

	@Test
	void update_success() {
		Level level = new Level();
		level.setId(1L);
		when(levelRepository.findById(1L)).thenReturn(Optional.of(level));
		when(levelRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Level result = levelConfigService.update(1L, new CreateLevelRequest("S7"));
		assertEquals("S7", result.getName());
	}

	@Test
	void delete_withStudents_throws() {
		Level level = new Level();
		level.setId(1L);
		when(levelRepository.findById(1L)).thenReturn(Optional.of(level));
		when(studentRepository.findByLevelId(1L))
				.thenReturn(List.of(new com.system_gestion_soutenance.api.user.entity.Student()));

		assertThrows(ResponseStatusException.class, () -> levelConfigService.delete(1L));
	}

	@Test
	void delete_success() {
		Level level = new Level();
		level.setId(1L);
		when(levelRepository.findById(1L)).thenReturn(Optional.of(level));
		when(studentRepository.findByLevelId(1L)).thenReturn(List.of());

		levelConfigService.delete(1L);
		verify(levelRepository).deleteById(1L);
	}
}
