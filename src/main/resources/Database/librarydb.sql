DROP DATABASE IF EXISTS librarydb;
CREATE DATABASE IF NOT EXISTS librarydb;
USE librarydb;

CREATE TABLE IF NOT EXISTS categories (
    categoryID INT AUTO_INCREMENT PRIMARY KEY,
    categoryName VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS documents (
    documentID INT AUTO_INCREMENT PRIMARY KEY,
    documentName VARCHAR(255) NOT NULL,
    categoryID INT,
    authors LONGTEXT,
    quantity INT DEFAULT 1,
    ISBN VARCHAR(20) UNIQUE,
    description TEXT,  -- Description field added here
    FOREIGN KEY (categoryID) REFERENCES categories(categoryID) ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS readers (
    readerID INT AUTO_INCREMENT PRIMARY KEY,
    readerName VARCHAR(255) NOT NULL,
    fullName VARCHAR(255),
    email VARCHAR(100),
    phoneNumber VARCHAR(15),
    dateOfBirth DATE,
    avatar VARCHAR(255) DEFAULT 'readerAvatar.png',
    status VARCHAR(50) DEFAULT 'Active',
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    userID INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    hashedPassword VARCHAR(255) NOT NULL,
    userFullName VARCHAR(100) NOT NULL,
    role VARCHAR(100) DEFAULT 'Normal user',
    gmail VARCHAR(100),
    phoneNumber VARCHAR(15),
    dateOfBirth DATE,
    avatar VARCHAR(255) DEFAULT 'userAvatar.png'
);

CREATE TABLE IF NOT EXISTS documentOwner (
    documentID INT,
    ownerID INT,
    PRIMARY KEY (documentID, ownerID),
    FOREIGN KEY (documentID) REFERENCES documents(documentID) ON UPDATE CASCADE,
    FOREIGN KEY (ownerID) REFERENCES users(userID) ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS borrowings (
    borrowingID INT AUTO_INCREMENT PRIMARY KEY,
    readerID INT,
    documentID INT,
    borrowDate DATETIME,
    dueDate DATE,
    returnDate DATETIME,
    borrowingStatus ENUM('borrowing', 'returned', 'late') NOT NULL,
    FOREIGN KEY (documentID) REFERENCES documents(documentID) ON UPDATE CASCADE,
    FOREIGN KEY (readerID) REFERENCES readers(readerID) ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS reports (
    reportID INT AUTO_INCREMENT PRIMARY KEY,
    userID INT,
    reportType VARCHAR(10),
    title VARCHAR(255),
    content LONGTEXT,
    FOREIGN KEY (userID) REFERENCES users(userID) ON UPDATE CASCADE
);

INSERT INTO users (username, hashedPassword, userFullName, role, gmail, phoneNumber, dateOfBirth, avatar)
VALUES (
    'admin',
    'b894204754191dc4a528c9c98d85c5cdd630c1911afb69bccff40c92f5163973',
    'Administrator',
    'Admin',
    'nmploc@gmail.com',
    '1234567890',
    '2000-01-01',
    'adminAvatar.png'
);
--acc : admin --
--password : lib22022 --