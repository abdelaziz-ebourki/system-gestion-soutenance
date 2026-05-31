package com.system_gestion_soutenance.api.common.audit;

import com.system_gestion_soutenance.api.admin.audit.entity.AuditLog;
import com.system_gestion_soutenance.api.admin.audit.repository.AuditLogRepository;
import java.time.LocalDateTime;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Aspect
@Component
public class AuditAspect {

	private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

	private final AuditLogRepository auditLogRepository;
	private final TransactionTemplate transactionTemplate;

	public AuditAspect(AuditLogRepository auditLogRepository, TransactionTemplate transactionTemplate) {
		this.auditLogRepository = auditLogRepository;
		this.transactionTemplate = transactionTemplate;
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

		String email = extractEmail();
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
			auditLog.setAdminEmail(null);
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
			auditLog.setAdminEmail(email);
			auditLog.setDetails(action + " " + entity + (entityId != null ? " #" + entityId : "")
					+ (errorDetail != null ? " — " + errorDetail : ""));
			auditLog.setTimestamp(LocalDateTime.now());
			auditLogRepository.save(auditLog);
		});
	}

	private String extractEmail() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated()) {
			Object principal = auth.getPrincipal();
			if (principal instanceof com.system_gestion_soutenance.api.user.entity.User user) {
				return user.getEmail();
			}
			if (principal instanceof String name && !"anonymousUser".equals(name)) {
				return name;
			}
		}
		return null;
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
