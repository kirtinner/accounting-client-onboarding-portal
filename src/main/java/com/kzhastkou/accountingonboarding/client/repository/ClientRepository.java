package com.kzhastkou.accountingonboarding.client.repository;

import com.kzhastkou.accountingonboarding.client.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsBySourceQuestionnaireId(Long questionnaireId);
    Optional<Client> findBySourceQuestionnaireId(Long questionnaireId);
}
