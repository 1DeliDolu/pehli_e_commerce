CREATE TABLE orders (
    id                         BIGINT         NOT NULL AUTO_INCREMENT,
    order_number               VARCHAR(40)    NOT NULL UNIQUE,
    user_id                    BIGINT         NULL,
    session_id                 VARCHAR(100)   NOT NULL,
    customer_first_name        VARCHAR(80)    NOT NULL,
    customer_last_name         VARCHAR(80)    NOT NULL,
    customer_email             VARCHAR(100)   NOT NULL,
    customer_phone             VARCHAR(30)    NOT NULL,
    customer_company           VARCHAR(120)   NULL,
    street                     VARCHAR(120)   NOT NULL,
    house_number               VARCHAR(20)    NOT NULL,
    address_line2              VARCHAR(120)   NULL,
    postal_code                VARCHAR(5)     NOT NULL,
    city                       VARCHAR(80)    NOT NULL,
    country_code               VARCHAR(2)     NOT NULL,
    payment_method             VARCHAR(30)    NOT NULL,
    payment_status             VARCHAR(30)    NOT NULL,
    payment_transaction_id     VARCHAR(40)    NOT NULL,
    payment_provider_reference VARCHAR(40)    NOT NULL,
    payment_message            VARCHAR(255)   NULL,
    subtotal                   DECIMAL(10, 2) NOT NULL,
    status                     VARCHAR(20)    NOT NULL DEFAULT 'PLACED',
    cancel_reason              VARCHAR(255)   NULL,
    canceled_at                TIMESTAMP      NULL,
    created_at                 TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_items (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    order_id     BIGINT         NOT NULL,
    product_id   BIGINT         NULL,
    product_name VARCHAR(150)   NOT NULL,
    quantity     INT            NOT NULL,
    unit_price   DECIMAL(10, 2) NOT NULL,
    line_total   DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX idx_orders_user_created_at ON orders (user_id, created_at DESC);
CREATE INDEX idx_orders_customer_email_created_at ON orders (customer_email, created_at DESC);
CREATE INDEX idx_orders_status_created_at ON orders (status, created_at DESC);
