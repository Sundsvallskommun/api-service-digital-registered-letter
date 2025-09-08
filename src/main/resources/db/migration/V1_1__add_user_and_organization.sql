    alter table letter
       add column user_id varchar(36) after party_id,
       add column organization_id varchar(36) after user_id;
 
    create table organization (
        number integer not null,
        id varchar(36) not null,
        name varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table user (
        id varchar(36) not null,
        username varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create index idx_number 
       on organization (number);

    alter table if exists organization 
       add constraint uk_number unique (number);

    create index idx_username 
       on user (username);

    alter table if exists user 
       add constraint uk_username unique (username);

    alter table if exists letter 
       add constraint fk_organization_letter 
       foreign key (organization_id) 
       references organization (id);

    alter table if exists letter 
       add constraint fk_user_letter 
       foreign key (user_id) 
       references user (id);
       