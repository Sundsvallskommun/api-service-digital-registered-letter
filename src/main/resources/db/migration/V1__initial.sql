CREATE TABLE attachment
(
    id           VARCHAR(36)  NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    content_type VARCHAR(50)  NULL,
    content      LONGBLOB     NULL,
    letter_id    VARCHAR(36)  NOT NULL,
    CONSTRAINT pk_attachment PRIMARY KEY (id)
);

CREATE TABLE letter
(
    id                        VARCHAR(36)  NOT NULL,
    municipality_id           VARCHAR(4)   NULL,
    body                      LONGTEXT     NULL,
    content_type              VARCHAR(50)  NULL,
    status                    VARCHAR(40)  NULL,
    subject                   VARCHAR(255) NULL,
    party_id                  VARCHAR(36)  NULL,
    deleted                   BIT(1)       NOT NULL,
    created                   datetime     NULL,
    updated                   datetime     NULL,
    support_text              VARCHAR(255) NULL,
    support_information_url   VARCHAR(255) NULL,
    support_information_email VARCHAR(255) NULL,
    support_information_phone VARCHAR(255) NULL,
    CONSTRAINT pk_letter PRIMARY KEY (id)
);

ALTER TABLE attachment
    ADD CONSTRAINT FK_ATTACHMENT_ON_LETTER FOREIGN KEY (letter_id) REFERENCES letter (id);
