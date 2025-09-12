INSERT INTO letter(id, municipality_id, body, content_type, status, subject, party_id, deleted, created, updated,
                   support_text, support_information_url, support_information_email, support_information_phone)
VALUES ('43a32404-28ee-480f-a095-00d48109afab', '2281', 'body', 'text/plain', 'NEW', 'subject',
        'a51fc859-b867-4a02-b574-ec1040035b4a',
        0, '2023-10-01 12:00:00', '2023-10-01 12:00:00', 'support text', 'https://example.com/support',
        'support@email.com', '+46123456789'),
       ('9bb97fd2-4410-4a4b-9019-fdd98f01bd7c', '2281', 'body', 'text/plain', 'NEW', 'subject',
        '491ca409-94ee-4f21-b32b-9304be3c6077',
        0, '2023-10-01 12:00:00', '2023-10-01 12:00:00', 'support text', 'https://example.com/support',
        'support@email.com', '+46123456789');

INSERT INTO attachment(id, file_name, content, content_type, letter_id)
VALUES (1, 'attachment1.txt', 'dGVzdA==', 'text/plain', '43a32404-28ee-480f-a095-00d48109afab'),
       (2, 'attachment2.txt', 'dGVzdA==', 'text/plain', '43a32404-28ee-480f-a095-00d48109afab'),
       (3, 'attachment3.txt', 'dGVzdA==', 'text/plain', '9bb97fd2-4410-4a4b-9019-fdd98f01bd7c');
