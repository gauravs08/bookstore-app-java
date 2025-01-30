-- Add a new column 'bookstore_id' to the 'books' table
ALTER TABLE books
    ADD COLUMN bookstore_id BIGINT;

-- Set a default bookstore_id for all existing books to 100
UPDATE books
SET bookstore_id = 1001;

-- Add foreign key constraint to link books to bookstores
ALTER TABLE books
    ADD CONSTRAINT fk_books_bookstore
        FOREIGN KEY (bookstore_id)
            REFERENCES bookstore (id)
            ON DELETE CASCADE;


