create table onboarding_invitations (
    id bigserial primary key,
    token uuid not null unique,
    preferred_name varchar(255) not null,
    email varchar(255) not null,
    client_type varchar(50) not null,
    status varchar(50) not null,
    created_at timestamp with time zone not null,
    expires_at timestamp with time zone not null,
    sent_at timestamp with time zone,
    submitted_at timestamp with time zone,
    approved_at timestamp with time zone,
    xpm_sent_at timestamp with time zone,
    cancelled_at timestamp with time zone,
    created_by varchar(255)
);
