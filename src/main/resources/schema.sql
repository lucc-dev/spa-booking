CREATE TABLE IF NOT EXISTS customers(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(100) UNIQUE,
    phone_number    VARCHAR(20) UNIQUE,
    gender          VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS service_items (
    id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(255) NOT NULL UNIQUE,
    description         VARCHAR(255),
    duration_minutes    INT NOT NULL,
    price               INT NOT NULL
);