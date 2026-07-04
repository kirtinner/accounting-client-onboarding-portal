package com.kzhastkou.accountingonboarding.invitation.repository;

import com.kzhastkou.accountingonboarding.invitation.entity.Invitation;
import com.kzhastkou.accountingonboarding.invitation.entity.QuestionnaireSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionnaireSubmissionRepository extends JpaRepository<QuestionnaireSubmission, Long> {

    boolean existsByInvitation(Invitation invitation);
}
