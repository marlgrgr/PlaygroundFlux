CREATE TABLE users (
    id BIGINT NOT NULL,
    fullname VARCHAR(255) NOT NULL,
    is_retired BOOLEAN NOT NULL,
    password VARCHAR(255) NOT NULL,
    password_change_required BOOLEAN NOT NULL,
    username VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE role (
    id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (role)
);

CREATE TABLE user_role (
    id BIGINT NOT NULL,
    role_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (id),
    FOREIGN KEY (role_id) REFERENCES role (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE SEQUENCE role_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE users_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE user_role_seq START WITH 1 INCREMENT BY 1;
