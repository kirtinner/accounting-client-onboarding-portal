package com.kzhastkou.accountingonboarding.invitation.repository;

import com.kzhastkou.accountingonboarding.invitation.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    Optional<Invitation> findByToken(String token);
}
