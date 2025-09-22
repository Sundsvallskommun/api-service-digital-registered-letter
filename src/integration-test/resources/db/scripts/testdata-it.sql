INSERT INTO organization(id, number, name)
VALUES ('a95aa330-7cad-4ce1-8bef-1e742fcac6e4', 44, 'Department 44'),
       ('fe85fe5e-7de1-4a48-bda7-8df8556b0265', 45, 'Department 45');

INSERT INTO `user`(id, username)
VALUES ('3bb3dc98-c674-448a-aa1c-bc4bdf3258bc', 'joe01doe'),
       ('36c85960-0b77-4228-a8b3-da86ca8078e7', 'sue02doe');

INSERT INTO letter(id, municipality_id, body, content_type, status, subject, party_id, deleted, created, updated,
                   support_text, support_information_url, support_information_email, support_information_phone,
                   organization_id, user_id)
VALUES ('43a32404-28ee-480f-a095-00d48109afab', '2281', 'body for municipality 2281 and Dept 44 issued by user joe01doe', 'text/plain', 'NEW', 'subject',
        'a51fc859-b867-4a02-b574-ec1040035b4a',
        0, '2023-10-01 12:00:00', '2023-10-01 12:00:00', 'support text', 'https://example.com/support',
        'support@email.com', '+46123456789', 'a95aa330-7cad-4ce1-8bef-1e742fcac6e4', '3bb3dc98-c674-448a-aa1c-bc4bdf3258bc'),
       
        ('f8853893-46a9-4249-a0e5-35d5595efd91', '2281', 'body for municipality 2281 and Dept 44 issued by user joe01doe', 'text/plain', 'SENT', 'subject',
        '3f4ac4dd-48fe-4303-b48c-93c929a31ee1',
        0, '2023-10-01 12:00:00', '2023-10-01 12:00:00', 'support text', 'https://example.com/support',
        'support@email.com', '+46123456789', 'a95aa330-7cad-4ce1-8bef-1e742fcac6e4', '3bb3dc98-c674-448a-aa1c-bc4bdf3258bc'),

        ('59eeec4c-81f3-4a96-918e-43a5e08a8ef0', '2281', 'body of deleted message for municipality 2281 and Dept 44 issued by user joe01doe', 'text/plain', 'SENT', 'subject',
        '3f4ac4dd-48fe-4303-b48c-93c929a31ee1',
        1, '2023-10-01 12:00:00', '2023-10-01 12:00:00', 'support text', 'https://example.com/support',
        'support@email.com', '+46123456789', 'a95aa330-7cad-4ce1-8bef-1e742fcac6e4', '3bb3dc98-c674-448a-aa1c-bc4bdf3258bc'),

        ('450970bb-118c-43a8-8813-6b67c2d33a3b', '2260', 'body of message for muncipality 2260 and Dept 45 issued by user joe01doe', 'text/plain', 'SENT', 'subject',
        '3f4ac4dd-48fe-4303-b48c-93c929a31ee1',
        0, '2023-10-01 12:00:00', '2023-10-01 12:00:00', 'support text', 'https://example.com/support',
        'support@email.com', '+46123456789', 'fe85fe5e-7de1-4a48-bda7-8df8556b0265', '3bb3dc98-c674-448a-aa1c-bc4bdf3258bc'),
        
        ('5c8634f2-078e-4ef0-888b-63cb1aaa0b11', '2281', 'body of message for muncipality 2281 and Dept 45 issued by user joe01doe', 'text/plain', 'SENT', 'subject',
        '3f4ac4dd-48fe-4303-b48c-93c929a31ee1',
        0, '2023-09-30 23:59:59.999', '2023-09-30 23:59:59.999', 'support text', 'https://example.com/support',
        'support@email.com', '+46123456789', 'fe85fe5e-7de1-4a48-bda7-8df8556b0265', '3bb3dc98-c674-448a-aa1c-bc4bdf3258bc'),
        
        ('bd3d2128-0d5a-457c-8629-0da4ffea85d9', '2281', 'body of message for muncipality 2281 and Dept 45 issued by user joe01doe', 'text/plain', 'SENT', 'subject',
        '3f4ac4dd-48fe-4303-b48c-93c929a31ee1',
        0, '2023-10-02 00:00:00.000', '2023-10-02 00:00:00.000', 'support text', 'https://example.com/support',
        'support@email.com', '+46123456789', 'fe85fe5e-7de1-4a48-bda7-8df8556b0265', '3bb3dc98-c674-448a-aa1c-bc4bdf3258bc'),

        ('4ace2698-7dea-4d0b-9c5d-eeac32c1b49a', '2281', 'body of message for muncipality 2281 and Dept 45 issued by user joe01doe', 'text/plain', 'SENT', 'subject',
        '3f4ac4dd-48fe-4303-b48c-93c929a31ee1',
        0, '2023-10-01 00:00:00', '2023-10-01 00:00:00', 'support text', 'https://example.com/support',
        'support@email.com', '+46123456789', 'fe85fe5e-7de1-4a48-bda7-8df8556b0265', '3bb3dc98-c674-448a-aa1c-bc4bdf3258bc'),
        
        ('9bb97fd2-4410-4a4b-9019-fdd98f01bd7c', '2281', 'body of message for muncipality 2281 and Dept 45 issued by user sue02doe', 'text/plain', 'NEW', 'subject',
        '491ca409-94ee-4f21-b32b-9304be3c6077',
        0, '2023-10-01 12:00:00', '2023-10-01 12:00:00', 'support text', 'https://example.com/support',
        'support@email.com', '+46123456789', 'fe85fe5e-7de1-4a48-bda7-8df8556b0265', '36c85960-0b77-4228-a8b3-da86ca8078e7');

INSERT INTO attachment(id, file_name, content, content_type, letter_id)
VALUES (1, 'attachment1.txt', 'dGVzdA==', 'text/plain', '43a32404-28ee-480f-a095-00d48109afab'),
       (2, 'attachment2.txt', 'dGVzdA==', 'text/plain', '43a32404-28ee-480f-a095-00d48109afab'),
       (3, 'attachment3.txt', 'dGVzdA==', 'text/plain', '9bb97fd2-4410-4a4b-9019-fdd98f01bd7c'),
       (4, 'attachment4.txt', 'dGVzdA==', 'text/plain', 'f8853893-46a9-4249-a0e5-35d5595efd91'),
       (5, 'attachment5.txt', 'dGVzdA==', 'text/plain', '450970bb-118c-43a8-8813-6b67c2d33a3b'),
       (6, 'attachment6.txt', 'dGVzdA==', 'text/plain', '59eeec4c-81f3-4a96-918e-43a5e08a8ef0'),
       (7, 'attachment7.txt', 'dGVzdA==', 'text/plain', '5c8634f2-078e-4ef0-888b-63cb1aaa0b11'),
       (8, 'attachment8.txt', 'dGVzdA==', 'text/plain', 'bd3d2128-0d5a-457c-8629-0da4ffea85d9'),
       (9, 'attachment9.txt', 'dGVzdA==', 'text/plain', '4ace2698-7dea-4d0b-9c5d-eeac32c1b49a');
