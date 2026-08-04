alter table questionnaires
    alter column first_name drop not null,
    alter column last_name drop not null,
    alter column date_of_birth drop not null,
    alter column email drop not null,
    alter column mobile_phone drop not null,
    alter column address_line_1 drop not null,
    alter column suburb drop not null,
    alter column state drop not null,
    alter column postcode drop not null,
    alter column country drop not null;
