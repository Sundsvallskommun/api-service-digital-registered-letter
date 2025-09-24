    create table signing_information (
        mrtd bit,
        signed datetime(6),
        content_key varchar(36),
        id varchar(36) not null,
        order_ref varchar(36),
        internal_id varchar(36),
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

    alter table if exists letter
       drop foreign key fk_organization_letter;

    alter table if exists letter
       drop foreign key fk_user_letter;
       
    alter table if exists letter 
       add constraint fk_letter_organization 
       foreign key (organization_id) 
       references organization (id);

    alter table if exists letter 
       add constraint fk_letter_user 
       foreign key (user_id) 
       references user (id);

    alter table if exists letter 
       add column signing_information_id varchar(36) after request_id;
        
    alter table if exists letter 
       add constraint uk_signing_information_id unique (signing_information_id);
      
    alter table if exists letter 
       add constraint fk_letter_signing_information 
       foreign key (signing_information_id) 
       references signing_information (id);
