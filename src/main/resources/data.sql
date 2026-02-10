-- dialect=H2

INSERT INTO USERS (
    USERNAME, PASSWORD, NICKNAME, EMAIL, ROLE, ENABLED, CREATED_AT, UPDATED_AT
) VALUES (
             'common_user', '1234', '공유저', 'common_user@example.com', 'USER', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
         );

INSERT INTO USERS (
    USERNAME, PASSWORD, NICKNAME, EMAIL, ROLE, ENABLED, CREATED_AT, UPDATED_AT
) VALUES (
             'common_admin', '1234', '공관리', 'common_admin@example.com', 'ADMIN', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
         );
