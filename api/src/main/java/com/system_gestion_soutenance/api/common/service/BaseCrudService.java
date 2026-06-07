package com.system_gestion_soutenance.api.common.service;

import java.util.List;
import java.util.function.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import com.system_gestion_soutenance.api.common.exception.EntityNotFoundException;
import com.system_gestion_soutenance.api.common.exception.ResourceConflictException;

public abstract class BaseCrudService<T, ID, R> {

	protected final JpaRepository<T, ID> repository;

	protected BaseCrudService(JpaRepository<T, ID> repository) {
		this.repository = repository;
	}

	public List<T> findAll() {
		return repository.findAll();
	}

	protected T findByIdOrThrow(ID id, String entityName) {
		return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(entityName + " non trouvé"));
	}

	public T save(T entity) {
		return repository.save(entity);
	}

	protected void deleteWithCheck(ID id, String entityName, Supplier<Boolean> conflictCheck) {
		findByIdOrThrow(id, entityName);
		if (conflictCheck != null && conflictCheck.get()) {
			throw new ResourceConflictException(entityName + " cannot be deleted because it is currently in use");
		}
		repository.deleteById(id);
	}
}
