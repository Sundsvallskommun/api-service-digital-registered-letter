create table if not exists tenant
(
    id              varchar(36)  not null,
    org_number      varchar(12)  not null,
    tenant_key      varchar(255) not null,
    municipality_id varchar(4)   not null,
    created         datetime(6),
    modified        datetime(6),
    primary key (id),
    index idx_tenant_municipality_id (municipality_id),
    constraint uk_tenant_org_municipality unique (org_number, municipality_id)
) engine = InnoDB;


alter table if exists letter
    add column if not exists tenant_id varchar(36);

alter table if exists letter
    add constraint fk_letter_tenant foreign key if not exists (tenant_id) references tenant (id);
