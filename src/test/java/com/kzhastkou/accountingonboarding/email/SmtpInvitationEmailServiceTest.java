package com.kzhastkou.accountingonboarding.email;

import com.kzhastkou.accountingonboarding.invitation.entity.ClientType;
import com.kzhastkou.accountingonboarding.invitation.entity.InvitationStatus;
import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SmtpInvitationEmailServiceTest {

    @Test
    void sendInvitationBuildsExpectedSubjectAndInvitationLink() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        SmtpInvitationEmailService service = new SmtpInvitationEmailService(mailSender, "http://localhost:5173/");
        UUID token = UUID.randomUUID();
        OnboardingInvitation invitation = new OnboardingInvitation(
                token,
                "Alex Smith",
                "alex@example.com",
                ClientType.INDIVIDUAL,
                InvitationStatus.SENT,
                Instant.parse("2026-07-07T00:00:00Z"),
                Instant.parse("2026-07-17T00:00:00Z"),
                null
        );

        service.sendInvitation(invitation);

        var messageCaptor = forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage message = messageCaptor.getValue();

        assertEquals("Complete your client onboarding", message.getSubject());
        assertEquals("alex@example.com", message.getTo()[0]);
        assertNotNull(message.getText());
        assertTrue(message.getText().contains("Hello Alex Smith,"));
        assertTrue(message.getText().contains("http://localhost:5173/onboarding/" + token));
    }
}
