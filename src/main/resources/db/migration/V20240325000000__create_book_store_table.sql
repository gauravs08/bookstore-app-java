CREATE TABLE bookstore (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           name VARCHAR(255) NOT NULL,
                           address VARCHAR(255) NOT NULL,
    -- Add other columns as needed
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE inventory
    ADD bookstore_id BIGINT;

-- Add foreign key constraint
ALTER TABLE inventory ADD CONSTRAINT fk_inventory_bookstore
FOREIGN KEY (bookstore_id) REFERENCES bookstore(id);

-- populate the new column based on existing data
INSERT INTO bookstore (id, name, address,created_at) VALUES (1001,'Odin Book Oy', 'Helsinki', current_timestamp);
INSERT INTO bookstore (id, name, address,created_at) VALUES (1002,'Helmet Book Oy', 'Kallio', current_timestamp);

UPDATE inventory SET bookstore_id = 1001 where ID='3fa85f64-5717-4562-b3fc-2c963f66afa9';
UPDATE inventory SET bookstore_id = 1002 where ID='3fa85f64-5717-4562-b3fc-2c963f66afa1';
UPDATE inventory SET bookstore_id = 1002 where ID='3fa85f64-5717-4562-b3fc-2c963f66afa2';
UPDATE inventory SET bookstore_id = 1002 where ID='3fa85f64-5717-4562-b3fc-2c963f66afa3';
UPDATE inventory SET bookstore_id = 1002 where ID='3fa85f64-5717-4562-b3fc-2c963f66afa4';