create table tenant (
    id varchar(36) not null,
    org_number varchar(255) not null,
    tenant_key varchar(255) not null,
    municipality_id varchar(255) not null,
    primary key (id),
    index idx_tenant_municipality_id (municipality_id),
    constraint uk_tenant_org_municipality unique (org_number, municipality_id)
) engine=InnoDB;
