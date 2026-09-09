package com.system_gestion_soutenance.api.common.audit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.audit.repository.AuditLogRepository;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuditAspectTest {

	@Mock
	private AuditLogRepository auditLogRepository;
	@Mock
	private SecurityService securityService;
	@Mock
	private ProceedingJoinPoint joinPoint;

	private AuditAspect aspect;

	@BeforeEach
	void setUp() {
		TransactionTemplate tt = new TransactionTemplate() {
			@Override
			public <T> T execute(TransactionCallback<T> action) {
				try {
					action.doInTransaction(null);
				} catch (TransactionException e) {
					throw new RuntimeException(e);
				}
				return null;
			}
		};
		aspect = new AuditAspect(auditLogRepository, tt, securityService);
		lenient().when(securityService.getOptionalCurrentUserEmail()).thenReturn("admin@test.com");
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void audit_savesLog() throws Throwable {
		when(joinPoint.proceed()).thenReturn(42L);
		when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

		Authentication auth = mock(Authentication.class);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getPrincipal()).thenReturn("admin@test.com");
		SecurityContextHolder.getContext().setAuthentication(auth);

		Audited audited = new Audited() {
			@Override
			public String action() {
				return "CREATE";
			}

			@Override
			public String entity() {
				return "Test";
			}

			@Override
			public Class<? extends java.lang.annotation.Annotation> annotationType() {
				return Audited.class;
			}
		};

		Object result = aspect.audit(joinPoint, audited);

		assertEquals(42L, result);
		verify(auditLogRepository).save(argThat(log -> "CREATE".equals(log.getAction())
				&& "Test".equals(log.getEntity()) && "admin@test.com".equals(log.getPerformedByEmail())));
	}

	@Test
	void audit_noSecurityContext_skipsAudit() throws Throwable {
		when(joinPoint.proceed()).thenReturn("result");
		when(securityService.getOptionalCurrentUserEmail()).thenReturn(null);

		SecurityContextHolder.clearContext();

		Audited audited = new Audited() {
			@Override
			public String action() {
				return "DELETE";
			}

			@Override
			public String entity() {
				return "Test";
			}

			@Override
			public Class<? extends java.lang.annotation.Annotation> annotationType() {
				return Audited.class;
			}
		};

		Object result = aspect.audit(joinPoint, audited);

		assertEquals("result", result);
		verify(auditLogRepository, never()).save(any());
	}

	@Test
	void audit_extractsEntityIdFromResultWithGetId() throws Throwable {
		Object entity = new Object() {
			public Long getId() {
				return 99L;
			}
		};
		when(joinPoint.proceed()).thenReturn(entity);
		when(joinPoint.getArgs()).thenReturn(new Object[]{});

		Authentication auth = mock(Authentication.class);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getPrincipal()).thenReturn("admin@test.com");
		SecurityContextHolder.getContext().setAuthentication(auth);

		Audited audited = new Audited() {
			@Override
			public String action() {
				return "UPDATE";
			}

			@Override
			public String entity() {
				return "Entity";
			}

			@Override
			public Class<? extends java.lang.annotation.Annotation> annotationType() {
				return Audited.class;
			}
		};

		aspect.audit(joinPoint, audited);

		verify(auditLogRepository).save(argThat(log -> 99L == log.getEntityId()));
	}

	@Test
	void audit_extractsEmailFromUserPrincipal() throws Throwable {
		when(joinPoint.proceed()).thenReturn(42L);
		when(joinPoint.getArgs()).thenReturn(new Object[]{1L});
		when(securityService.getOptionalCurrentUserEmail()).thenReturn("user@test.com");

		com.system_gestion_soutenance.api.user.entity.User user = new com.system_gestion_soutenance.api.user.entity.User();
		user.setEmail("user@test.com");
		Authentication auth = mock(Authentication.class);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getPrincipal()).thenReturn(user);
		SecurityContextHolder.getContext().setAuthentication(auth);

		Audited audited = new Audited() {
			@Override
			public String action() {
				return "CREATE";
			}

			@Override
			public String entity() {
				return "Test";
			}

			@Override
			public Class<? extends java.lang.annotation.Annotation> annotationType() {
				return Audited.class;
			}
		};

		aspect.audit(joinPoint, audited);

		verify(auditLogRepository).save(argThat(log -> "user@test.com".equals(log.getPerformedByEmail())));
	}

	@Test
	void audit_anonymousUser_skipsAudit() throws Throwable {
		when(joinPoint.proceed()).thenReturn("result");
		when(securityService.getOptionalCurrentUserEmail()).thenReturn(null);

		Authentication auth = mock(Authentication.class);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getPrincipal()).thenReturn("anonymousUser");
		SecurityContextHolder.getContext().setAuthentication(auth);

		Audited audited = new Audited() {
			@Override
			public String action() {
				return "DELETE";
			}

			@Override
			public String entity() {
				return "Test";
			}

			@Override
			public Class<? extends java.lang.annotation.Annotation> annotationType() {
				return Audited.class;
			}
		};

		aspect.audit(joinPoint, audited);

		verify(auditLogRepository, never()).save(any());
	}

	@Test
	void audit_notAuthenticated_skipsAudit() throws Throwable {
		when(joinPoint.proceed()).thenReturn("result");
		when(securityService.getOptionalCurrentUserEmail()).thenReturn(null);

		Authentication auth = mock(Authentication.class);
		when(auth.isAuthenticated()).thenReturn(false);
		SecurityContextHolder.getContext().setAuthentication(auth);

		Audited audited = new Audited() {
			@Override
			public String action() {
				return "UPDATE";
			}

			@Override
			public String entity() {
				return "X";
			}

			@Override
			public Class<? extends java.lang.annotation.Annotation> annotationType() {
				return Audited.class;
			}
		};

		aspect.audit(joinPoint, audited);

		verify(auditLogRepository, never()).save(any());
	}

	@Test
	void audit_extractsEntityIdFromResultWithIdMethod() throws Throwable {
		Object entity = new Object() {
			public Long id() {
				return 77L;
			}
		};
		when(joinPoint.proceed()).thenReturn(entity);
		when(joinPoint.getArgs()).thenReturn(new Object[]{});

		Authentication auth = mock(Authentication.class);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getPrincipal()).thenReturn("admin@test.com");
		SecurityContextHolder.getContext().setAuthentication(auth);

		Audited audited = new Audited() {
			@Override
			public String action() {
				return "CREATE";
			}

			@Override
			public String entity() {
				return "E";
			}

			@Override
			public Class<? extends java.lang.annotation.Annotation> annotationType() {
				return Audited.class;
			}
		};

		aspect.audit(joinPoint, audited);

		verify(auditLogRepository).save(argThat(log -> 77L == log.getEntityId()));
	}

	@Test
	void audit_nullResult_usesArgsForEntityId() throws Throwable {
		when(joinPoint.proceed()).thenReturn(null);
		when(joinPoint.getArgs()).thenReturn(new Object[]{3L});

		Authentication auth = mock(Authentication.class);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getPrincipal()).thenReturn("admin@test.com");
		SecurityContextHolder.getContext().setAuthentication(auth);

		Audited audited = new Audited() {
			@Override
			public String action() {
				return "DELETE";
			}

			@Override
			public String entity() {
				return "X";
			}

			@Override
			public Class<? extends java.lang.annotation.Annotation> annotationType() {
				return Audited.class;
			}
		};

		aspect.audit(joinPoint, audited);

		verify(auditLogRepository).save(argThat(log -> 3L == log.getEntityId()));
	}

	@Test
	void audit_extractsEntityIdWhenGetIdReturnsNonNumber() throws Throwable {
		Object entity = new Object() {
			public Object getId() {
				return "not-a-number";
			}
		};
		when(joinPoint.proceed()).thenReturn(entity);
		when(joinPoint.getArgs()).thenReturn(new Object[]{5L});

		Authentication auth = mock(Authentication.class);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getPrincipal()).thenReturn("admin@test.com");
		SecurityContextHolder.getContext().setAuthentication(auth);

		Audited audited = mock(Audited.class);
		when(audited.action()).thenReturn("UPDATE");
		when(audited.entity()).thenReturn("E");

		aspect.audit(joinPoint, audited);

		verify(auditLogRepository).save(argThat(log -> 5L == log.getEntityId()));
	}

	@Test
	void audit_extractsEntityIdWhenGetIdFallsBackToNumberArg() throws Throwable {
		Object entity = new Object() {
			@SuppressWarnings("unused")
			public String id() {
				return "not-a-number";
			}
		};
		when(joinPoint.proceed()).thenReturn(entity);
		when(joinPoint.getArgs()).thenReturn(new Object[]{42L});

		Authentication auth = mock(Authentication.class);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getPrincipal()).thenReturn("admin@test.com");
		SecurityContextHolder.getContext().setAuthentication(auth);

		Audited audited = mock(Audited.class);
		when(audited.action()).thenReturn("DELETE");
		when(audited.entity()).thenReturn("E");

		aspect.audit(joinPoint, audited);

		verify(auditLogRepository).save(argThat(log -> 42L == log.getEntityId()));
	}

	@Test
	void audit_extractsEntityIdFromFirstArgWhenResultIsNumber() throws Throwable {
		when(joinPoint.proceed()).thenReturn(1L);
		when(joinPoint.getArgs()).thenReturn(new Object[]{5L});

		Authentication auth = mock(Authentication.class);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getPrincipal()).thenReturn("admin@test.com");
		SecurityContextHolder.getContext().setAuthentication(auth);

		Audited audited = new Audited() {
			@Override
			public String action() {
				return "DELETE";
			}

			@Override
			public String entity() {
				return "X";
			}

			@Override
			public Class<? extends java.lang.annotation.Annotation> annotationType() {
				return Audited.class;
			}
		};

		aspect.audit(joinPoint, audited);

		verify(auditLogRepository).save(argThat(log -> 1L == log.getEntityId()));
	}

	@Test
	void audit_proceedThrows_savesErrorLogAndRethrows() throws Throwable {
		when(joinPoint.proceed()).thenThrow(new RuntimeException("error detail"));
		when(joinPoint.getArgs()).thenReturn(new Object[]{42L});

		Audited audited = mock(Audited.class);
		when(audited.action()).thenReturn("CREATE");
		when(audited.entity()).thenReturn("Test");

		assertThrows(RuntimeException.class, () -> aspect.audit(joinPoint, audited));

		verify(auditLogRepository).save(argThat(log -> "CREATE".equals(log.getAction())
				&& "Test".equals(log.getEntity()) && log.getPerformedByEmail() == null
				&& log.getDetails().contains("error detail") && log.getDetails().contains("#42")));
	}

	@Test
	void audit_entityIdNull_whenNoIdInResultOrArgs() throws Throwable {
		when(joinPoint.proceed()).thenReturn(new Object());
		when(joinPoint.getArgs()).thenReturn(new Object[]{"string", new Object()});

		Authentication auth = mock(Authentication.class);
		when(auth.isAuthenticated()).thenReturn(true);
		when(auth.getPrincipal()).thenReturn("admin@test.com");
		SecurityContextHolder.getContext().setAuthentication(auth);

		Audited audited = mock(Audited.class);
		when(audited.action()).thenReturn("UPDATE");
		when(audited.entity()).thenReturn("E");

		aspect.audit(joinPoint, audited);

		verify(auditLogRepository).save(argThat(log -> "admin@test.com".equals(log.getPerformedByEmail())
				&& log.getEntityId() == null && "UPDATE E".equals(log.getDetails())));
	}
}
