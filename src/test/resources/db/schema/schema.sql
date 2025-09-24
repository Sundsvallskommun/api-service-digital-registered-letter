    create table attachment (
        id varchar(36) not null,
        letter_id varchar(36) not null,
        content_type varchar(50),
        file_name varchar(255) not null,
        content longblob,
        primary key (id)
    ) engine=InnoDB;

    create table letter (
        deleted bit not null,
        municipality_id varchar(4),
        created datetime(6),
        updated datetime(6),
        id varchar(36) not null,
        organization_id varchar(36),
        party_id varchar(36),
        request_id varchar(36),
        signing_id varchar(36),
        user_id varchar(36),
        status varchar(40),
        content_type varchar(50),
        body LONGTEXT,
        subject varchar(255),
        support_information_email varchar(255),
        support_information_phone varchar(255),
        support_information_url varchar(255),
        support_text varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table organization (
        number integer not null,
        id varchar(36) not null,
        name varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table signing_information (
        mrtd bit,
        signed datetime(6),
        content_key varchar(36),
        id varchar(36) not null,
        internal_id varchar(36),
        order_ref varchar(36),
        given_name varchar(255),
        ip_address varchar(255),
        name varchar(255),
        ocsp_response longtext,
        personal_number varchar(255),
        signature longtext,
        status varchar(255),
        surname varchar(255),
        primary key (id)
    ) engine=InnoDB;
    
    create table user (
        id varchar(36) not null,
        username varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    alter table if exists letter 
       add constraint uk_signing_id unique (signing_id);
       
    create index idx_number 
       on organization (number);

    alter table if exists organization 
       add constraint uk_number unique (number);

    create index idx_username 
       on user (username);

    alter table if exists user 
       add constraint uk_username unique (username);

    alter table if exists attachment 
       add constraint fk_attachment_letter 
       foreign key (letter_id) 
       references letter (id);

    alter table if exists letter 
       add constraint fk_organization_letter 
       foreign key (organization_id) 
       references organization (id);

    alter table if exists letter 
       add constraint fk_signing_info_letter 
       foreign key (signing_id) 
       references signing_information (id);
       
    alter table if exists letter 
       add constraint fk_user_letter 
       foreign key (user_id) 
       references user (id);
