package com.system_gestion_soutenance.api.common.audit;

import com.system_gestion_soutenance.api.admin.audit.entity.AuditLog;
import com.system_gestion_soutenance.api.admin.audit.repository.AuditLogRepository;
import com.system_gestion_soutenance.api.common.service.SecurityService;
import java.time.LocalDateTime;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
@SuppressWarnings("PMD")

@Aspect
@Component
public class AuditAspect {

	private final AuditLogRepository auditLogRepository;
	private final TransactionTemplate transactionTemplate;
	private final SecurityService securityService;

	public AuditAspect(AuditLogRepository auditLogRepository, TransactionTemplate transactionTemplate,
			SecurityService securityService) {
		this.auditLogRepository = auditLogRepository;
		this.transactionTemplate = transactionTemplate;
		this.securityService = securityService;
	}

	@Around("@annotation(audited)")
	public Object audit(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
		Object result;
		String action = audited.action();
		String entity = audited.entity();
		try {
			result = joinPoint.proceed();
		} catch (Throwable t) {
			saveAuditLog(action, entity, joinPoint.getArgs(), t.getMessage());
			throw t;
		}

		String email = securityService.getOptionalCurrentUserEmail();
		if (email == null)
			return result;

		Long entityId = extractEntityId(joinPoint.getArgs(), result);
		saveAuditLog(action, entity, entityId, email, null);

		return result;
	}

	private void saveAuditLog(String action, String entity, Object[] args, String errorDetail) {
		Long entityId = extractEntityId(args, null);
		transactionTemplate.executeWithoutResult(statusTx -> {
			AuditLog auditLog = new AuditLog();
			auditLog.setAction(action);
			auditLog.setEntity(entity);
			auditLog.setEntityId(entityId);
			auditLog.setPerformedByEmail(null);
			auditLog.setDetails(action + " " + entity + (entityId != null ? " #" + entityId : "")
					+ (errorDetail != null ? " — " + errorDetail : ""));
			auditLog.setTimestamp(LocalDateTime.now());
			auditLogRepository.save(auditLog);
		});
	}

	private void saveAuditLog(String action, String entity, Long entityId, String email, String errorDetail) {
		transactionTemplate.executeWithoutResult(statusTx -> {
			AuditLog auditLog = new AuditLog();
			auditLog.setAction(action);
			auditLog.setEntity(entity);
			auditLog.setEntityId(entityId);
			auditLog.setPerformedByEmail(email);
			auditLog.setDetails(action + " " + entity + (entityId != null ? " #" + entityId : "")
					+ (errorDetail != null ? " — " + errorDetail : ""));
			auditLog.setTimestamp(LocalDateTime.now());
			auditLogRepository.save(auditLog);
		});
	}

	private Long extractEntityId(Object[] args, Object result) {
		if (result != null) {
			if (result instanceof Number n)
				return n.longValue();
			try {
				var idMethod = result.getClass().getMethod("getId");
				Object id = idMethod.invoke(result);
				if (id instanceof Number n)
					return n.longValue();
			} catch (Exception ignored) {
			}
			try {
				var idMethod = result.getClass().getMethod("id");
				Object id = idMethod.invoke(result);
				if (id instanceof Number n)
					return n.longValue();
			} catch (Exception ignored) {
			}
		}
		for (Object arg : args) {
			if (arg instanceof Number n)
				return n.longValue();
		}
		return null;
	}
}