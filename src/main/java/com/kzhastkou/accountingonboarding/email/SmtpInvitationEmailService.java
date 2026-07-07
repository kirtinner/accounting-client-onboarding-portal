package com.kzhastkou.accountingonboarding.email;

import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class SmtpInvitationEmailService implements InvitationEmailService {

    static final String SUBJECT = "Complete your client onboarding";

    private static final DateTimeFormatter EXPIRY_FORMATTER = DateTimeFormatter
            .ofPattern("dd MMM yyyy")
            .withZone(ZoneId.systemDefault());

    private final JavaMailSender mailSender;
    private final String frontendBaseUrl;

    public SmtpInvitationEmailService(JavaMailSender mailSender,
                                      @Value("${frontend.base-url}") String frontendBaseUrl) {
        this.mailSender = mailSender;
        this.frontendBaseUrl = trimTrailingSlash(frontendBaseUrl);
    }

    @Override
    public void sendInvitation(OnboardingInvitation invitation) {
        mailSender.send(buildMessage(invitation));
    }

    SimpleMailMessage buildMessage(OnboardingInvitation invitation) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(invitation.getEmail());
        message.setSubject(SUBJECT);
        message.setText(buildBody(invitation));
        return message;
    }

    String buildBody(OnboardingInvitation invitation) {
        return """
                Hello %s,

                You have been invited to complete your client onboarding.

                Please use the link below:

                %s/onboarding/%s

                This invitation expires on %s.

                Regards,

                Unfair Advantage Accounting
                """.formatted(
                invitation.getPreferredName(),
                frontendBaseUrl,
                invitation.getToken(),
                EXPIRY_FORMATTER.format(invitation.getExpiresAt())
        );
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
