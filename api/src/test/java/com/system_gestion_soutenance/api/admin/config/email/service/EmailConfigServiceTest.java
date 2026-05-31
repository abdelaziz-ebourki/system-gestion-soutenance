package com.system_gestion_soutenance.api.admin.config.email.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.config.email.dto.UpdateEmailConfigRequest;
import com.system_gestion_soutenance.api.admin.config.email.entity.EmailConfig;
import com.system_gestion_soutenance.api.admin.config.email.repository.EmailConfigRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class EmailConfigServiceTest {

	@Mock
	private EmailConfigRepository repository;
	@InjectMocks
	private EmailConfigService service;

	@Test
	void get_existing_returnsConfig() {
		EmailConfig config = new EmailConfig();
		config.setHost("smtp.test.com");
		when(repository.findById(1L)).thenReturn(Optional.of(config));
		assertEquals("smtp.test.com", service.get().getHost());
	}

	@Test
	void get_missing_throws() {
		when(repository.findById(1L)).thenReturn(Optional.empty());
		assertThrows(ResponseStatusException.class, () -> service.get());
	}

	@Test
	void update_existing_updatesFields() {
		EmailConfig existing = new EmailConfig();
		existing.setId(1L);
		when(repository.findById(1L)).thenReturn(Optional.of(existing));
		when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateEmailConfigRequest req = new UpdateEmailConfigRequest("new.host.com", 587, "user", "new-pass", "Sender",
				"s@s.com", "tls");
		EmailConfig result = service.update(req);

		assertEquals("new.host.com", result.getHost());
	}

	@Test
	void update_passwordNull_doesNotOverride() {
		EmailConfig existing = new EmailConfig();
		existing.setId(1L);
		existing.setPassword("old-pass");
		when(repository.findById(1L)).thenReturn(Optional.of(existing));
		when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

		UpdateEmailConfigRequest req = new UpdateEmailConfigRequest("h", 25, "u", null, "S", "e@e.com", "none");
		EmailConfig result = service.update(req);

		assertEquals("old-pass", result.getPassword());
	}
}
