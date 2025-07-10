-- Migração para criar tabela products diretamente em inglês

CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
); 