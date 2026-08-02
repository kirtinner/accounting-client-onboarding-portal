package com.kzhastkou.accountingonboarding.invitation.repository;

import com.kzhastkou.accountingonboarding.common.model.ClientType;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OnboardingInvitationRepositoryTest {

    @Autowired
    private OnboardingInvitationRepository repository;

    @Test
    void shouldFindOnlySentInvitationsPastExpirationDate() {
        Instant now = Instant.now();

        // SENT, expiresAt in past — should be expired
        OnboardingInvitation shouldExpire = createDraftInvitation("expire@test.com");
        shouldExpire.markSent(now.minus(20, ChronoUnit.DAYS), Duration.ofDays(10)); // expiresAt = 10 days ago

        // SENT, expiresAt in future — should not be expired
        OnboardingInvitation notYetExpired = createDraftInvitation("notyet@test.com");
        notYetExpired.markSent(now, Duration.ofDays(10)); // expiresAt = 10 days from now

        // DRAFT - should not be expired because of the wrong status
        OnboardingInvitation wrongStatus = createDraftInvitation("wrongstatus@test.com");

        repository.save(shouldExpire);
        repository.save(notYetExpired);
        repository.save(wrongStatus);

        // Act
        List<OnboardingInvitation> expiredInvitationList = repository.findAllByStatusAndExpiresAtLessThanEqual(
                InvitationStatus.SENT,
                now
        );
        assertEquals(1, expiredInvitationList.size());
        assertEquals(shouldExpire.getId(), expiredInvitationList.get(0).getId());
    }

    private OnboardingInvitation createDraftInvitation(String email) {
        return new OnboardingInvitation(
                UUID.randomUUID(),
                "Test Name",
                email,
                ClientType.INDIVIDUAL,
                Instant.now(),
                "test-user"
        );
    }
}