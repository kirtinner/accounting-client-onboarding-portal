create table practices (
    id bigserial primary key,
    name varchar(255) not null,
    created_at timestamp not null
);

create table invitations (
    id bigserial primary key,
    token varchar(100) not null unique,
    client_email varchar(255) not null,
    status varchar(50) not null,
    expires_at timestamp not null,
    created_at timestamp not null,
    practice_id bigint references practices(id)
);

create table questionnaire_submissions (
    id bigserial primary key,
    invitation_id bigint not null unique references invitations(id),
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    phone varchar(50),
    email varchar(255) not null,
    residential_address text,
    notes text,
    submitted_at timestamp not null
);
