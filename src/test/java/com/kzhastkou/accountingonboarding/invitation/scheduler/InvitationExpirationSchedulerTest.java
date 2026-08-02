package com.kzhastkou.accountingonboarding.invitation.scheduler;

import com.kzhastkou.accountingonboarding.invitation.service.InvitationExpirationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InvitationExpirationSchedulerTest {

    @Mock
    private InvitationExpirationService expirationService;

    private InvitationExpirationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new InvitationExpirationScheduler(expirationService);
    }

    @Test
    void expireInvitationsDailyShouldExpireInvitations() {
        // Act
        scheduler.expireInvitationsDaily();

        // Assert
        verify(expirationService).expireInvitations();
    }

    @Test
    void expireInvitationsOnStartupShouldExpireInvitations() {
        // Act
        scheduler.expireInvitationsOnStartup();

        // Assert
        verify(expirationService).expireInvitations();
    }
}
