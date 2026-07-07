package com.kzhastkou.accountingonboarding.questionnaire.repository;

import com.kzhastkou.accountingonboarding.questionnaire.entity.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {

    Optional<Questionnaire> findByInvitationId(Long invitationId);

    Optional<Questionnaire> findByInvitationToken(UUID token);

    boolean existsByInvitationId(Long invitationId);
}
