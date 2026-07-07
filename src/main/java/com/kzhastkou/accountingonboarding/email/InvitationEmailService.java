package com.kzhastkou.accountingonboarding.email;

import com.kzhastkou.accountingonboarding.invitation.entity.OnboardingInvitation;

public interface InvitationEmailService {

    void sendInvitation(OnboardingInvitation invitation);
}
