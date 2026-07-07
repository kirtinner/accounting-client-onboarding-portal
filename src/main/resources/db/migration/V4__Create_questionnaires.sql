create table questionnaires (
    id bigserial primary key,
    invitation_id bigint not null unique references onboarding_invitations(id),
    first_name varchar(255) not null,
    middle_name varchar(255),
    last_name varchar(255) not null,
    date_of_birth date not null,
    email varchar(255) not null,
    mobile_phone varchar(50) not null,
    address_line_1 varchar(255) not null,
    address_line_2 varchar(255),
    suburb varchar(255) not null,
    state varchar(100) not null,
    postcode varchar(20) not null,
    country varchar(100) not null default 'Australia',
    client_confirmed boolean not null default false,
    submitted_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index idx_questionnaires_invitation_id on questionnaires(invitation_id);
