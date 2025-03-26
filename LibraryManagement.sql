create database LibraryManagement;
-- drop database LibraryManagement;
use LibraryManagement;
CREATE TABLE employee (
    user_id VARCHAR(50) PRIMARY KEY,
    password VARCHAR(50) NOT NULL
);
CREATE TABLE books (
    book_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    author VARCHAR(100) NOT NULL,
    Genre VARCHAR(50),
    available INT DEFAULT 0,
    borrowed INT DEFAULT 0
);
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT,
    user_id VARCHAR(50),
    action VARCHAR(10), -- 'Borrow' or 'Return'
    transaction_date DATETIME,
    FOREIGN KEY (book_id) REFERENCES books(book_id),
    FOREIGN KEY (user_id) REFERENCES employee(user_id)
);

INSERT INTO employee (user_id, password) 
VALUES ('admin', 'admin123'),('shasha','sha123'),('kora','kora123');

INSERT INTO books (book_id, name, author, Genre, available, borrowed) 
VALUES 
(101, 'The Great Gatsby', 'F. Scott Fitzgerald', 'Classic', 5, 0),
(102, 'To Kill a Mockingbird', 'Harper Lee', 'Fiction', 3, 0),
(103, '1984', 'George Orwell', 'Dystopian', 7, 0),
(104, 'Pride and Prejudice', 'Jane Austen', 'Romance', 4, 0),
(105, 'The Hobbit', 'J.R.R. Tolkien', 'Fantasy', 6, 0)