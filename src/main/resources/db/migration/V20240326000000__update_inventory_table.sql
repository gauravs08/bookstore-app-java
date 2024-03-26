-- Add the new column
ALTER TABLE inventory
    ADD COLUMN bookstore_id BIGINT NOT NULL default 1001 ;


-- Add foreign key constraint
ALTER TABLE inventory
    ADD CONSTRAINT fk_inventory_bookstore
        FOREIGN KEY (bookstore_id) REFERENCES bookstore(id);

-- Drop the existing primary key constraint
ALTER TABLE inventory DROP PRIMARY KEY;

-- Add the new primary key constraint with two fields (id and bookstore_id)
ALTER TABLE inventory ADD CONSTRAINT inventory_pkey PRIMARY KEY (id, bookstore_id);





UPDATE inventory SET bookstore_id = 1001 where ID='3fa85f64-5717-4562-b3fc-2c963f66afa9';
UPDATE inventory SET bookstore_id = 1002 where ID='3fa85f64-5717-4562-b3fc-2c963f66afa1';
UPDATE inventory SET bookstore_id = 1002 where ID='3fa85f64-5717-4562-b3fc-2c963f66afa2';
UPDATE inventory SET bookstore_id = 1002 where ID='3fa85f64-5717-4562-b3fc-2c963f66afa3';
UPDATE inventory SET bookstore_id = 1002 where ID='3fa85f64-5717-4562-b3fc-2c963f66afa4';

insert into inventory(id, bookstore_id,COPIES) values('3fa85f64-5717-4562-b3fc-2c963f66afa9', 1002, 33);