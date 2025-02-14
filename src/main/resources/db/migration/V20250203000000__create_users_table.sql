-- Create users table
CREATE TABLE users
(
    id       UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Create user roles table for role-based access
CREATE TABLE user_roles
(
    user_id UUID REFERENCES users (id) ON DELETE CASCADE,
    role    VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- Insert default admin user (password should be hashed in real scenarios)
-- admin123: $2a$10$TlQBI3zTvxxTdd6ZPw2LIeeTzG0OmnVlcTwhtBz5Sx4swqAPutvIi
-- user123: $2a$10$AtTGB0PU4Z2o7egEvLreserPVWoVJz/sntDjxdQOnrNdZJPv8Y9i6
INSERT INTO users (id, username, password)
VALUES (random_uuid(), 'admin', '$2a$10$TlQBI3zTvxxTdd6ZPw2LIeeTzG0OmnVlcTwhtBz5Sx4swqAPutvIi'),
       (random_uuid(), 'user', '$2a$10$AtTGB0PU4Z2o7egEvLreserPVWoVJz/sntDjxdQOnrNdZJPv8Y9i6');

-- Assign roles to users
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE username = 'admin';
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_USER' FROM users WHERE username = 'user';
