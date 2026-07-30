update onboarding_invitations
set expires_at = null
where status = 'DRAFT';
