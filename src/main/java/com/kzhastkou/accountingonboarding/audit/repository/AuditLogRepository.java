package com.kzhastkou.accountingonboarding.audit.repository;

import com.kzhastkou.accountingonboarding.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
