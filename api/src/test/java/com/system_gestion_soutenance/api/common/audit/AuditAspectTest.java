package com.system_gestion_soutenance.api.common.audit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.system_gestion_soutenance.api.admin.audit.repository.AuditLogRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

	@Mock
	private AuditLogRepository auditLogRepository;
	@Mock
	private ProceedingJoinPoint joinPoint;

	private AuditAspect aspect;

	@BeforeEach
	void setUp() {
		aspect = new AuditAspect(auditLogRepository);
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
				&& "Test".equals(log.getEntity()) && "admin@test.com".equals(log.getAdminEmail())));
	}

	@Test
	void audit_noSecurityContext_skipsAudit() throws Throwable {
		when(joinPoint.proceed()).thenReturn("result");

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
}
