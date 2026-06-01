package com.system_gestion_soutenance.api.admin.config.major.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.major.dto.CreateMajorRequest;
import com.system_gestion_soutenance.api.admin.config.major.entity.Major;
import com.system_gestion_soutenance.api.admin.config.major.repository.MajorRepository;
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
class MajorConfigServiceTest {

	@Mock
	private MajorRepository majorRepository;
	@Mock
	private StudentRepository studentRepository;
	@InjectMocks
	private MajorConfigService majorConfigService;

	@Test
	void findAll_returnsAll() {
		when(majorRepository.findAll()).thenReturn(List.of(new Major()));
		assertEquals(1, majorConfigService.findAll().size());
	}

	@Test
	void create_success() {
		when(majorRepository.findByName("GL")).thenReturn(Optional.empty());
		when(majorRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		Major result = majorConfigService.create(new CreateMajorRequest("GL"));
		assertEquals("GL", result.getName());
	}

	@Test
	void create_duplicate_throws() {
		when(majorRepository.findByName("GL")).thenReturn(Optional.of(new Major()));
		assertThrows(ResponseStatusException.class, () -> majorConfigService.create(new CreateMajorRequest("GL")));
	}

	@Test
	void update_success() {
		Major major = new Major();
		major.setId(1L);
		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(majorRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		Major result = majorConfigService.update(1L, new CreateMajorRequest("IIR"));
		assertEquals("IIR", result.getName());
	}

	@Test
	void delete_withStudents_throws() {
		Major major = new Major();
		major.setId(1L);
		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(studentRepository.findByMajorId(1L))
				.thenReturn(List.of(new com.system_gestion_soutenance.api.user.entity.Student()));
		assertThrows(ResponseStatusException.class, () -> majorConfigService.delete(1L));
	}

	@Test
	void delete_success() {
		Major major = new Major();
		major.setId(1L);
		when(majorRepository.findById(1L)).thenReturn(Optional.of(major));
		when(studentRepository.findByMajorId(1L)).thenReturn(List.of());
		majorConfigService.delete(1L);
		verify(majorRepository).deleteById(1L);
	}
}
