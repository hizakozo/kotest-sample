CREATE TABLE IF NOT EXISTS products (
    product_id UUID default gen_random_uuid() PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price INT NOT NULL
);