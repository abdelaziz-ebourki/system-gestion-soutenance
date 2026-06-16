package com.system_gestion_soutenance.api.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.ResourceConflictException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

class BaseCrudServiceTest {

	private final JpaRepository<String, Long> repository = mock(JpaRepository.class);
	private final TestCrudService service = new TestCrudService(repository);

	@Test
	void findAll_returnsAll() {
		when(repository.findAll()).thenReturn(List.of("a", "b"));

		List<String> result = service.findAll();

		assertEquals(2, result.size());
	}

	@Test
	void save_delegatesToRepository() {
		when(repository.save("entity")).thenReturn("saved");

		String result = service.save("entity");

		assertEquals("saved", result);
	}

	@Test
	void findByIdOrThrow_whenFound_returnsEntity() {
		when(repository.findById(1L)).thenReturn(Optional.of("entity"));

		String result = service.callFindByIdOrThrow(1L);

		assertEquals("entity", result);
	}

	@Test
	void findByIdOrThrow_whenNotFound_throwsException() {
		when(repository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.callFindByIdOrThrow(99L));
	}

	@Test
	void deleteWithCheck_whenNoConflict_deletes() {
		when(repository.findById(1L)).thenReturn(Optional.of("entity"));

		service.callDeleteWithCheck(1L, () -> false);

		verify(repository).deleteById(1L);
	}

	@Test
	void deleteWithCheck_whenConflict_throwsException() {
		when(repository.findById(1L)).thenReturn(Optional.of("entity"));

		assertThrows(ResourceConflictException.class, () -> service.callDeleteWithCheck(1L, () -> true));
		verify(repository, never()).deleteById(any());
	}

	@Test
	void deleteWithCheck_whenNullCheck_deletes() {
		when(repository.findById(1L)).thenReturn(Optional.of("entity"));

		service.callDeleteWithCheck(1L, null);

		verify(repository).deleteById(1L);
	}

	@Test
	void deleteWithCheck_whenNotFound_throwsException() {
		when(repository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> service.callDeleteWithCheck(99L, () -> false));
	}

	private static class TestCrudService extends BaseCrudService<String, Long, Void> {
		TestCrudService(JpaRepository<String, Long> repository) {
			super(repository);
		}

		String callFindByIdOrThrow(Long id) {
			return findByIdOrThrow(id, "TestEntity");
		}

		void callDeleteWithCheck(Long id, java.util.function.Supplier<Boolean> conflictCheck) {
			deleteWithCheck(id, "TestEntity", conflictCheck);
		}
	}
}
