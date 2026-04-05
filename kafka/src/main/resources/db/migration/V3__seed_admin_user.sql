INSERT INTO users (username, email, password, role, enabled)
SELECT 'admin',
       'admin@local.dev',
       '$2a$10$5pw4D4BK.N2tzRskk/L6DunqsfMJg4CuVpPNwEpOctYnbSflWrVxu',
       'ROLE_ADMIN',
       TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE username = 'admin'
       OR email = 'admin@local.dev'
);
