-- Drop database if it exists and create a new one
DROP DATABASE IF EXISTS librarydb;

CREATE DATABASE IF NOT EXISTS librarydb;

USE librarydb;

-- Table to store categories
CREATE TABLE IF NOT EXISTS categories (
    categoryID INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    categoryName VARCHAR(255) NOT NULL
);

-- Table to store documents (books)
CREATE TABLE IF NOT EXISTS documents (
    documentID INT AUTO_INCREMENT PRIMARY KEY,
    documentName VARCHAR(255) NOT NULL,
    categoryID INT,
    authors LONGTEXT,
    quantity INT DEFAULT 1,;
    FOREIGN KEY (categoryID) REFERENCES categories(categoryID) ON UPDATE CASCADE
);

-- Table to store users (for admin and regular users)
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

-- Table to store document owners (association between documents and users)
CREATE TABLE IF NOT EXISTS documentOwner (
    documentID INT,
    ownerID INT,
    PRIMARY KEY (documentID, ownerID),
    FOREIGN KEY (documentID) REFERENCES documents(documentID) ON UPDATE CASCADE,
    FOREIGN KEY (ownerID) REFERENCES users(userID) ON UPDATE CASCADE
);

--Create the updated borrowings table
CREATE TABLE IF NOT EXISTS borrowings (
    borrowingID INT AUTO_INCREMENT PRIMARY KEY,
    readerID INT,
    documentID INT,
    borrowDate DATETIME,
    dueDate DATE,
    returnDate DATETIME,
    borrowingStatus VARCHAR(20),
    FOREIGN KEY (documentID) REFERENCES documents(documentID) ON UPDATE CASCADE,
    FOREIGN KEY (readerID) REFERENCES readers(readerID) ON UPDATE CASCADE
);

-- Table to store reports (for user complaints or issues)
CREATE TABLE IF NOT EXISTS reports (
    reportID INT AUTO_INCREMENT PRIMARY KEY,
    userID INT,
    reportType VARCHAR(10),
    title VARCHAR(255),
    content LONGTEXT,
    FOREIGN KEY (userID) REFERENCES users(userID) ON UPDATE CASCADE
);

-- Table to store readers (for managing reader profiles)
CREATE TABLE IF NOT EXISTS readers (
    readerID INT AUTO_INCREMENT PRIMARY KEY,           -- ID người đọc
    readerName VARCHAR(255) NOT NULL UNIQUE,              -- Tên người dùng
    fullName VARCHAR(255),                     -- Tên đầy đủ người đọc
    email VARCHAR(100),                                 -- Email của người đọc
    phoneNumber VARCHAR(15),                            -- Số điện thoại của người đọc
    dateOfBirth DATE,                                   -- Ngày sinh người đọc
    avatar VARCHAR(255) DEFAULT 'readerAvatar.png',     -- Avatar của người đọc (mặc định là ảnh placeholder)
status VARCHAR(50) DEFAULT 'Active',                -- Trạng thái tài khoản (ví dụ: Active, Inactive)
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,      -- Thời gian tạo tài khoản
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- Thời gian cập nhật tài khoản
);

