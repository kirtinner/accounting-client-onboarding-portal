package com.kzhastkou.accountingonboarding.questionnaire.repository;

import com.kzhastkou.accountingonboarding.questionnaire.entity.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {

    Optional<Questionnaire> findByInvitationId(Long invitationId);

    Optional<Questionnaire> findByInvitationToken(UUID token);

    boolean existsByInvitationId(Long invitationId);

    @Query("""
            select questionnaire
            from Questionnaire questionnaire
            join fetch questionnaire.invitation invitation
            where invitation.status in (
                com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus.SUBMITTED,
                com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus.APPROVED
            )
              and questionnaire.submittedAt is not null
            order by questionnaire.submittedAt desc
            """)
    List<Questionnaire> findSubmittedForAdminReview();

    @Query("""
            select questionnaire
            from Questionnaire questionnaire
            join fetch questionnaire.invitation
            where questionnaire.id = :id
            """)
    Optional<Questionnaire> findByIdWithInvitation(Long id);
}
