create table audit_logs
(
    id          bigserial primary key,
    occurred_at timestamp with time zone not null,
    actor       varchar(255)             not null,
    action      varchar(100)             not null,
    entity_type varchar(50),
    entity_id   bigint,
    result      varchar(20)              not null,
    description varchar(500)
);

create index idx_audit_logs_occurred_at on audit_logs(occurred_at);
create index idx_audit_logs_entity on audit_logs(entity_type, entity_id);
