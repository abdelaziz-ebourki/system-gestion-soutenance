package com.system_gestion_soutenance.api.common.service;

import com.system_gestion_soutenance.api.common.dto.NamedRequest;
import com.system_gestion_soutenance.api.common.dto.PaginatedResponse;
import com.system_gestion_soutenance.api.common.exception.InvalidBusinessStateException;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("PMD")

public abstract class AbstractNamedConfigService<E, C extends NamedRequest, U extends NamedRequest>
		extends
			BaseCrudService<E, Long, C> {

	protected AbstractNamedConfigService(JpaRepository<E, Long> repository) {
		super(repository);
	}

	protected abstract E newEntity();

	protected abstract void setName(E entity, String name);

	protected abstract Optional<E> findByName(String name);

	protected abstract String duplicateMessage();

	protected abstract String entityLabel();

	protected abstract Supplier<Boolean> usageCheck(Long id);

	protected PaginatedResponse<E> findAllPaged(int page, int limit) {
		Page<E> resultPage = repository.findAll(PageRequest.of(page, limit));
		return new PaginatedResponse<>(resultPage.getContent(), resultPage.getTotalElements(),
				resultPage.getTotalPages(), page, limit);
	}

	@Transactional
	protected E createNamed(C request) {
		if (findByName(request.name()).isPresent()) {
			throw new InvalidBusinessStateException(duplicateMessage());
		}
		E entity = newEntity();
		setName(entity, request.name());
		return save(entity);
	}

	@Transactional
	protected E updateNamed(Long id, C request) {
		E entity = findByIdOrThrow(id, entityLabel());
		setName(entity, request.name());
		return save(entity);
	}

	@Transactional
	protected E updatePartialNamed(Long id, U request) {
		E entity = findByIdOrThrow(id, entityLabel());
		if (request.name() != null) {
			setName(entity, request.name());
		}
		return save(entity);
	}

	@Transactional
	protected void deleteNamed(Long id) {
		deleteWithCheck(id, entityLabel(), usageCheck(id));
	}
}
