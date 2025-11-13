INSERT INTO organization(id, number, name)
VALUES ('a95aa330-7cad-4ce1-8bef-1e742fcac6e4', 44, 'Department 44'),
       ('fe85fe5e-7de1-4a48-bda7-8df8556b0265', 45, 'Department 45');

INSERT INTO `user`(id, username)
VALUES ('3bb3dc98-c674-448a-aa1c-bc4bdf3258bc', 'joe01doe'),
       ('36c85960-0b77-4228-a8b3-da86ca8078e7', 'sue02doe');

INSERT INTO signing_information (mrtd, signed, content_key, id, order_ref, internal_id, given_name, ip_address,
                                 name, ocsp_response, personal_number, signature, status, surname)
VALUES (0, '2023-10-15 10:20:00', '6b33fee5-6380-4cf5-9f53-fe992b57fa49', '0d458afc-526f-4d1d-aa0c-5a7228c37382', '4ca9c820-4ecb-4268-9cba-dfa938d17b9e',
        'f8853893-46a9-4249-a0e5-35d5595efd91', 'Karl', '127.0.0.1', 'Karl Banal', 'MIIHdgoBAKCCB28wggdrBg', '190001011234', 'PD94bWwgdmVyc2lvb', 'COMPLETED', 'Banal');

INSERT INTO letter(id, municipality_id, body, content_type, status, subject, party_id, deleted, created, updated,
                   support_text, support_information_url, support_information_email, support_information_phone,
                   organization_id, user_id, request_id, signing_information_id)
VALUES ('43a32404-28ee-480f-a095-00d48109afab', '2281', 'body for municipality 2281 and Dept 44 issued by user joe01doe', 'text/plain', 'NEW', 'subject',
        'a51fc859-b867-4a02-b574-ec1040035b4a', 0, '2023-10-01 12:00:00', '2023-10-01 12:00:00', 'support text', 'https://example.com/support',
        'support@email.com', '+46123456789', 'a95aa330-7cad-4ce1-8bef-1e742fcac6e4', '3bb3dc98-c674-448a-aa1c-bc4bdf3258bc', '997a5dfc-ec57-4d41-b4c3-b4990adff734',
        null),

        ('1a7b65d7-bafd-49be-9e97-6406b1bf5886', '2262', 'body for municipality 2262 and Dept 44 issued by user sue02doe', 'text/plain', 'PENDING', 'subject',
        '3f4ac4dd-48fe-4303-b48c-93c929a31ee1', 0, '2023-10-01 12:00:00', '2023-10-01 12:00:00', 'support text', 'https://example.com/support',
        'support@email.com', '+46123456789', 'a95aa330-7cad-4ce1-8bef-1e742fcac6e4', '36c85960-0b77-4228-a8b3-da86ca8078e7', '03ae04dc-ed22-4958-a1af-70e496e02fa8',
        null),

        ('f8853893-46a9-4249-a0e5-35d5595efd91', '2281', 'body for municipality 2281 and Dept 44 issued by user joe01doe', 'text/plain', 'SIGNED', 'subject',
        '3f4ac4dd-48fe-4303-b48c-93c929a31ee1', 0, '2023-10-01 12:00:00', '2023-10-01 12:00:00', 'support text', 'https://example.com/support',
        'support@email.com', '+46123456789', 'a95aa330-7cad-4ce1-8bef-1e742fcac6e4', '3bb3dc98-c674-448a-aa1c-bc4bdf3258bc', 'ae8af132-c4cf-4f78-b572-e2a3efd1961f',
        '0d458afc-526f-4d1d-aa0c-5a7228c37382'),

        ('59eeec4c-81f3-4a96-918e-43a5e08a8ef0', '2281', 'body of deleted message for municipality 2281 and Dept 44 issued by user joe01doe', 'text/plain', 'PENDING', 'subject',
        '3f4ac4dd-48fe-4303-b48c-93c929a31ee1', 1, '2023-10-01 12:00:00', '2023-10-01 12:00:00', 'support text', 'https://example.com/support',
        'support@email.com', '+46123456789', 'a95aa330-7cad-4ce1-8bef-1e742fcac6e4', '3bb3dc98-c674-448a-aa1c-bc4bdf3258bc', '8133c04e-e03f-4ec7-90d0-afe054007bbf',
        null),

        ('450970bb-118c-43a8-8813-6b67c2d33a3b', '2260', 'body of deleted message for muncipality 2260 and Dept 45 issued by user joe01doe', 'text/plain', 'PENDING', 'subject',
        '3f4ac4dd-48fe-4303-b48c-93c929a31ee1', 1, '2023-10-01 12:00:00', '2023-10-01 12:00:00', 'support text', 'https://example.com/support',
        'support@email.com', '+46123456789', 'fe85fe5e-7de1-4a48-bda7-8df8556b0265', '3bb3dc98-c674-448a-aa1c-bc4bdf3258bc', '16f95b8e-3e3f-4e5a-8106-b6cc5e6339d4',
        null),

        ('9bb97fd2-4410-4a4b-9019-fdd98f01bd7c', '2281', 'body of message for muncipality 2281 and Dept 45 issued by user sue02doe', 'text/plain', 'NEW', 'subject',
        '491ca409-94ee-4f21-b32b-9304be3c6077', 0, '2023-10-01 12:00:00', '2023-10-01 12:00:00', 'support text', 'https://example.com/support',
        'support@email.com', '+46123456789', 'fe85fe5e-7de1-4a48-bda7-8df8556b0265', '36c85960-0b77-4228-a8b3-da86ca8078e7', '3ef455c8-d358-42a6-bff4-3b7c89085495',
        null);

INSERT INTO attachment(id, file_name, content, content_type, letter_id)
VALUES ('f4666ea6-0324-490f-8e27-2b704e580a0a', 'attachment1.txt', 'dGVzdA==', 'text/plain', '43a32404-28ee-480f-a095-00d48109afab'),
       ('e5c653e4-285a-4e46-b7ad-f79fc6e95463', 'attachment2.txt', 'dGVzdA==', 'text/plain', '43a32404-28ee-480f-a095-00d48109afab'),
       ('c723e56a-21bd-4753-a7cf-1e428d7baeaa', 'attachment3.txt', 'dGVzdA==', 'text/plain', '9bb97fd2-4410-4a4b-9019-fdd98f01bd7c'),
       ('514f8c46-acca-45d6-b1ff-f11d10fbd9d4', 'attachment4.txt', 'dGVzdA==', 'text/plain', 'f8853893-46a9-4249-a0e5-35d5595efd91'),
       ('c4380df7-8971-424f-acd7-71307d24d9ad', 'attachment5.txt', 'dGVzdA==', 'text/plain', '450970bb-118c-43a8-8813-6b67c2d33a3b'),
       ('c0fa7fa4-4149-440a-84a1-50946a57cf01', 'attachment6.txt', 'dGVzdA==', 'text/plain', '59eeec4c-81f3-4a96-918e-43a5e08a8ef0');
