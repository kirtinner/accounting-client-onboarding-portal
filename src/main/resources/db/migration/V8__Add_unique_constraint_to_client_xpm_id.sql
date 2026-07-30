alter table clients
    add constraint uk_clients_xpm_client_id
        unique (xpm_client_id);