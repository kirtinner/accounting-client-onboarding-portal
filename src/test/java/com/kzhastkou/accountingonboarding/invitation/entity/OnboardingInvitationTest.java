package com.kzhastkou.accountingonboarding.invitation.entity;

import com.kzhastkou.accountingonboarding.common.exception.BadRequestException;
import com.kzhastkou.accountingonboarding.common.model.ClientType;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OnboardingInvitationTest {

    @Test
    void shouldExpireWhenExpirationTimeEqualsNow() {
        // Arrange
        OnboardingInvitation invitation = createDraftInvitation("test@example.com");
        Instant now = Instant.now();
        invitation.markSent(now.minus(Duration.ofDays(1)), Duration.ofDays(1));

        // Act
        invitation.markExpired(now);

        // Assert
        assertEquals(InvitationStatus.EXPIRED, invitation.getStatus());
    }

    @Test
    void shouldNotExpireWhenExpirationTimeIsInFuture() {
        // Arrange
        Instant now = Instant.now();
        OnboardingInvitation invitation = createDraftInvitation("test@example.com");
        invitation.markSent(now, Duration.ofDays(1));


        // Act + Assert
        assertThrows(
                BadRequestException.class,
                () -> invitation.markExpired(now)
        );
        assertEquals(InvitationStatus.SENT, invitation.getStatus());
    }

    @Test
    void shouldNotExpireWhenInvitationStatusIsNotSent() {
        // Arrange
        OnboardingInvitation invitation = createDraftInvitation("test@example.com");

        // Act + Assert
        assertThrows(
                BadRequestException.class,
                () -> invitation.markExpired(Instant.now())
        );
        assertEquals(InvitationStatus.DRAFT, invitation.getStatus());
    }

    @Test
    void shouldNotExpireWhenExpirationTimestampIsNull() {
        // Arrange
        OnboardingInvitation invitation = createDraftInvitation("test@example.com");

        // Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> invitation.markExpired(null)
        );
        assertEquals(InvitationStatus.DRAFT, invitation.getStatus());
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