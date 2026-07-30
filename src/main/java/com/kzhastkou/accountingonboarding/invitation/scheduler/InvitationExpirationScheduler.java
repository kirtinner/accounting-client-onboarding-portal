package com.kzhastkou.accountingonboarding.invitation.scheduler;

import com.kzhastkou.accountingonboarding.invitation.service.InvitationExpirationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InvitationExpirationScheduler {
    private static final Logger log =
            LoggerFactory.getLogger(InvitationExpirationScheduler.class);

    private final InvitationExpirationService expirationService;

    public InvitationExpirationScheduler(InvitationExpirationService expirationService) {
        this.expirationService = expirationService;
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Australia/Melbourne")
    public void expireInvitationsDaily() {
        expireInvitations();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void expireInvitationsOnStartup() {
        expireInvitations();
    }

    private void expireInvitations() {
        int expiredCount = expirationService.expireInvitations();

        if (expiredCount > 0) {
            log.info(
                    "Invitation expiration completed. {} invitation(s) expired.",
                    expiredCount
            );
        } else {
            log.debug("Invitation expiration completed. No invitations expired.");
        }
    }
}
