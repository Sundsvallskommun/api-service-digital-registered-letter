CREATE TABLE attachment
(
    id           VARCHAR(36)  NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    content_type VARCHAR(50)  NULL,
    content      LONGBLOB     NULL,
    letter_id    VARCHAR(36)  NOT NULL,
    PRIMARY KEY (id)
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
    created                   DATETIME     NULL,
    updated                   DATETIME     NULL,
    support_text              VARCHAR(255) NULL,
    support_information_url   VARCHAR(255) NULL,
    support_information_email VARCHAR(255) NULL,
    support_information_phone VARCHAR(255) NULL,
    PRIMARY KEY (id)
);

CREATE TABLE shedlock
(
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    locked_at  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    locked_by  VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);

ALTER TABLE attachment
    ADD CONSTRAINT fk_attachment_letter FOREIGN KEY (letter_id) REFERENCES letter (id);
