create database library_db;
\c library_db;

CREATE TABLE member (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL
);

create table card (
    id SERIAL PRIMARY KEY,
    member_id INT NOT NULL UNIQUE REFERENCES member(id) ON DELETE CASCADE,
    number VARCHAR(255) NOT NULL UNIQUE,
    issued_on DATE NOT NULL,
    expires_on DATE NOT NULL
);

CREATE TABLE  author (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);
CREATE TABLE publisher (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE book (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    isbn VARCHAR(255) NOT NULL UNIQUE ,
    publisher_id INT NOT NULL REFERENCES publisher(id) ON DELETE CASCADE

);

CREATE TABLE book_author (
    book_id INT NOT NULL REFERENCES book(id) ON DELETE CASCADE,
    author_id INT NOT NULL REFERENCES author(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, author_id)
);

CREATE TABLE tag (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE book_tag (
    book_id INT NOT NULL REFERENCES book(id) ON DELETE CASCADE,
    tag_id INT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, tag_id)
);

CREATE TABLE book_copy (
    id SERIAL PRIMARY KEY,
    book_id INT NOT NULL REFERENCES book(id) ON DELETE CASCADE,
    barcode VARCHAR(255) NOT NULL UNIQUE,
    shelf_code VARCHAR(255) NOT NULL
);
CREATE TABLE borrowing (
    id SERIAL PRIMARY KEY,
    member_id INT NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    book_copy_id INT NOT NULL REFERENCES book_copy(id) ON DELETE CASCADE,
    borrowed_at TIMESTAMP NOT NULL,
    due_date TIMESTAMP NOT NULL,
    returned_at TIMESTAMP ,
    fine_eur NUMERIC(8, 2) NOT NULL DEFAULT 0,

    CHECK (fine_eur >= 0),
    CHECK (returned_at IS NULL OR returned_at >= borrowed_at),
    CHECK (due_date >= borrowed_at::DATE)
);