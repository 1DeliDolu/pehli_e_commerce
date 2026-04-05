CREATE TABLE categories (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)  NOT NULL UNIQUE,
    description VARCHAR(500)  NULL,
    PRIMARY KEY (id)
);

CREATE TABLE products (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    category_id BIGINT         NOT NULL,
    name        VARCHAR(150)   NOT NULL,
    description VARCHAR(1000)  NULL,
    price       DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories(id)
);

INSERT INTO categories (name, description) VALUES
    ('Electronics', 'Technology and digital products'),
    ('Books', 'Software, business and personal development books'),
    ('Office', 'Daily office supplies');

INSERT INTO products (category_id, name, description, price) VALUES
    (1, 'Mechanical Keyboard', 'Mechanical keyboard for software developers', 129.90),
    (1, '27 Inch Monitor', 'IPS monitor for office and development', 289.00),
    (2, 'Kafka in Action', 'Kafka fundamentals and real-world examples', 44.90),
    (3, 'Notebook Set', '3-piece notebook set for meetings and planning', 12.50);
