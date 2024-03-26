CREATE TABLE bookstore (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           name VARCHAR(255) NOT NULL,
                           address VARCHAR(255) NOT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- populate the new column based on existing data
INSERT INTO bookstore (id, name, address,created_at) VALUES (1001,'Odin Book Oy', 'Helsinki', current_timestamp);
INSERT INTO bookstore (id, name, address,created_at) VALUES (1002,'Helmet Book Oy', 'Kallio', current_timestamp);
