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
       add column signing_id varchar(36) after request_id;
        
   alter table if exists letter 
       add constraint uk_signing_id unique (signing_id);
      
    alter table if exists letter 
       add constraint fk_signing_info_letter 
       foreign key (signing_id) 
       references signing_information (id);
