package com.kzhastkou.accountingonboarding.invitation.repository;

import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OnboardingInvitationRepository extends JpaRepository<OnboardingInvitation, Long> {

    Optional<OnboardingInvitation> findByToken(UUID token);

    boolean existsByToken(UUID token);

    List<OnboardingInvitation> findAllByOrderByCreatedAtDesc();
}
