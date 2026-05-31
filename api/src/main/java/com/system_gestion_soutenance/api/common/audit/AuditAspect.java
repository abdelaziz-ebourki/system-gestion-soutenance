package com.system_gestion_soutenance.api.common.audit;

import com.system_gestion_soutenance.api.admin.audit.entity.AuditLog;
import com.system_gestion_soutenance.api.admin.audit.repository.AuditLogRepository;
import java.time.LocalDateTime;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {

  private final AuditLogRepository auditLogRepository;

  public AuditAspect(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  @Around("@annotation(audited)")
  public Object audit(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
    Object result = joinPoint.proceed();

    String email = extractEmail();
    if (email == null) return result;

    Long entityId = extractEntityId(joinPoint.getArgs(), result);

    AuditLog log = new AuditLog();
    log.setAction(audited.action());
    log.setEntity(audited.entity());
    log.setEntityId(entityId);
    log.setAdminEmail(email);
    log.setDetails(audited.action() + " " + audited.entity() + " #" + entityId);
    log.setTimestamp(LocalDateTime.now());
    auditLogRepository.save(log);

    return result;
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
      if (result instanceof Number n) return n.longValue();
      try {
        var idMethod = result.getClass().getMethod("getId");
        Object id = idMethod.invoke(result);
        if (id instanceof Number n) return n.longValue();
      } catch (Exception ignored) {
      }
      try {
        var idMethod = result.getClass().getMethod("id");
        Object id = idMethod.invoke(result);
        if (id instanceof Number n) return n.longValue();
      } catch (Exception ignored) {
      }
    }
    for (Object arg : args) {
      if (arg instanceof Number n) return n.longValue();
    }
    return null;
  }
}
