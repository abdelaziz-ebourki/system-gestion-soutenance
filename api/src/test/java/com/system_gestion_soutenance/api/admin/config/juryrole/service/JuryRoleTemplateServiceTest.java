package com.system_gestion_soutenance.api.admin.config.juryrole.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.juryrole.dto.CreateJuryRoleTemplateRequest;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.JuryRoleTemplate;
import com.system_gestion_soutenance.api.admin.config.juryrole.entity.TemplateRole;
import com.system_gestion_soutenance.api.admin.config.juryrole.repository.JuryRoleTemplateRepository;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseSession;
import com.system_gestion_soutenance.api.admin.defensesession.entity.DefenseType;
import com.system_gestion_soutenance.api.admin.defensesession.repository.DefenseSessionRepository;
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
class JuryRoleTemplateServiceTest {

	@Mock
	private JuryRoleTemplateRepository juryRoleTemplateRepository;
	@Mock
	private DefenseSessionRepository defenseSessionRepository;

	@InjectMocks
	private JuryRoleTemplateService service;

	@Test
	void findAll_returnsAll() {
		when(juryRoleTemplateRepository.findAll()).thenReturn(List.of(new JuryRoleTemplate()));
		assertEquals(1, service.findAll().size());
	}

	@Test
	void create_success() {
		when(juryRoleTemplateRepository.findByName("Template PFE")).thenReturn(Optional.empty());
		when(juryRoleTemplateRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		CreateJuryRoleTemplateRequest req = new CreateJuryRoleTemplateRequest("Template PFE", "PFE",
				List.of(new CreateJuryRoleTemplateRequest.RoleEntry("Président", 1, 2)));

		JuryRoleTemplate result = service.create(req);

		assertEquals("Template PFE", result.getName());
		assertEquals(DefenseType.PFE, result.getDefenseType());
		assertEquals(1, result.getRoles().size());
		assertEquals("Président", result.getRoles().get(0).getName());
	}

	@Test
	void create_duplicateName_throws() {
		when(juryRoleTemplateRepository.findByName("Template PFE")).thenReturn(Optional.of(new JuryRoleTemplate()));
		CreateJuryRoleTemplateRequest req = new CreateJuryRoleTemplateRequest("Template PFE", "PFE",
				List.of(new CreateJuryRoleTemplateRequest.RoleEntry("Président", 1, 2)));

		assertThrows(InvalidBusinessStateException.class, () -> service.create(req));
	}

	@Test
	void create_duplicateRoleNames_throws() {
		when(juryRoleTemplateRepository.findByName("Template PFE")).thenReturn(Optional.empty());
		CreateJuryRoleTemplateRequest req = new CreateJuryRoleTemplateRequest("Template PFE", "PFE",
				List.of(new CreateJuryRoleTemplateRequest.RoleEntry("Président", 1, 2),
						new CreateJuryRoleTemplateRequest.RoleEntry("Président", 1, 2)));

		assertThrows(InvalidBusinessStateException.class, () -> service.create(req));
	}

	@Test
	void update_success() {
		JuryRoleTemplate existing = new JuryRoleTemplate(1L, "Old", DefenseType.PFE, new java.util.ArrayList<>());
		existing.getRoles().add(new TemplateRole("Président", 1, 2));

		when(juryRoleTemplateRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(juryRoleTemplateRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		CreateJuryRoleTemplateRequest req = new CreateJuryRoleTemplateRequest("New", "MEMOIRE",
				List.of(new CreateJuryRoleTemplateRequest.RoleEntry("Rapporteur", 1, 1),
						new CreateJuryRoleTemplateRequest.RoleEntry("Examinateur", 2, 1)));

		JuryRoleTemplate result = service.update(1L, req);

		assertEquals("New", result.getName());
		assertEquals(DefenseType.MEMOIRE, result.getDefenseType());
		assertEquals(2, result.getRoles().size());
	}

	@Test
	void update_notFound_throws() {
		when(juryRoleTemplateRepository.findById(99L)).thenReturn(Optional.empty());
		CreateJuryRoleTemplateRequest req = new CreateJuryRoleTemplateRequest("X", "PFE",
				List.of(new CreateJuryRoleTemplateRequest.RoleEntry("Rôle", 1, 1)));

		assertThrows(EntityNotFoundException.class, () -> service.update(99L, req));
	}

	@Test
	void delete_success() {
		JuryRoleTemplate template = new JuryRoleTemplate(1L, "T", DefenseType.PFE, new java.util.ArrayList<>());
		when(juryRoleTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
		when(defenseSessionRepository.findByJuryRoleTemplate_Id(1L)).thenReturn(List.of());

		service.delete(1L);

		verify(juryRoleTemplateRepository).delete(template);
	}

	@Test
	void delete_notFound_throws() {
		when(juryRoleTemplateRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(EntityNotFoundException.class, () -> service.delete(99L));
		verify(juryRoleTemplateRepository, never()).delete(any());
	}

	@Test
	void delete_withDefenseSessions_throws() {
		JuryRoleTemplate template = new JuryRoleTemplate(1L, "T", DefenseType.PFE, new java.util.ArrayList<>());
		when(juryRoleTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
		when(defenseSessionRepository.findByJuryRoleTemplate_Id(1L)).thenReturn(List.of(new DefenseSession()));

		assertThrows(ResourceConflictException.class, () -> service.delete(1L));
		verify(juryRoleTemplateRepository, never()).delete(any());
	}
}
