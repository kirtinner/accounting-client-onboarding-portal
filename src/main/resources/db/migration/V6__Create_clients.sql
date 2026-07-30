create table clients
(
    id                      bigserial primary key,
    client_type             varchar(50)              not null,
    source_questionnaire_id bigint                   not null unique,

    first_name              varchar(255)             not null,
    middle_name             varchar(255),
    last_name               varchar(255)             not null,
    date_of_birth           date                     not null,

    email                   varchar(255)             not null,
    mobile_phone            varchar(50)              not null,

    address_line_1          varchar(255)             not null,
    address_line_2          varchar(255),
    suburb                  varchar(255)             not null,
    state                   varchar(100)             not null,
    postcode                varchar(20)              not null,
    country                 varchar(100)             not null,

    xpm_client_id           varchar(255),

    created_at              timestamp with time zone not null,
    updated_at              timestamp with time zone not null,

    constraint fk_clients_questionnaire
        foreign key (source_questionnaire_id)
            references questionnaires (id)
);