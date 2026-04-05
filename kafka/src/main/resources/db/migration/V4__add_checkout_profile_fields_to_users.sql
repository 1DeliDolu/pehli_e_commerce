ALTER TABLE users
    ADD COLUMN first_name VARCHAR(80) NULL AFTER password,
    ADD COLUMN last_name VARCHAR(80) NULL AFTER first_name,
    ADD COLUMN phone VARCHAR(30) NULL AFTER last_name,
    ADD COLUMN company VARCHAR(120) NULL AFTER phone,
    ADD COLUMN street VARCHAR(120) NULL AFTER company,
    ADD COLUMN house_number VARCHAR(20) NULL AFTER street,
    ADD COLUMN address_line2 VARCHAR(120) NULL AFTER house_number,
    ADD COLUMN postal_code VARCHAR(5) NULL AFTER address_line2,
    ADD COLUMN city VARCHAR(80) NULL AFTER postal_code,
    ADD COLUMN country_code VARCHAR(2) NULL DEFAULT 'DE' AFTER city;
