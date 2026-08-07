package com.kzhastkou.accountingonboarding.audit.repository;

import com.kzhastkou.accountingonboarding.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByOccurredAtDesc();
}
