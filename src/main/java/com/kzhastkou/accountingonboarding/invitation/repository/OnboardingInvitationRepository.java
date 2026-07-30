package com.kzhastkou.accountingonboarding.invitation.repository;

import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OnboardingInvitationRepository extends JpaRepository<OnboardingInvitation, Long> {

    Optional<OnboardingInvitation> findByToken(UUID token);

    boolean existsByToken(UUID token);

    List<OnboardingInvitation> findAllByOrderByCreatedAtDesc();

    List<OnboardingInvitation> findAllByStatusInOrderByCreatedAtDesc(Collection<InvitationStatus> statuses);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
       update OnboardingInvitation invitation
       set invitation.status = :expiredStatus
       where invitation.status = :sentStatus
         and invitation.expiresAt <= :now
       """)
    int expireInvitations(
            @Param("sentStatus") InvitationStatus sentStatus,
            @Param("expiredStatus") InvitationStatus expiredStatus,
            @Param("now") Instant now
    );
}
