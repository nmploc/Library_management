Here is the updated README file with the corrected directory for the **Database Diagram**:

---

[![Contributors](https://img.shields.io/badge/contributors-3-brightgreen.svg?style=for-the-badge)](#)  

# Library Management - Library Management App
<div align="center">  
  <img src="/src/main/resources/image/logo.png" alt="Logo" width="64" height="64">  
</div>

---  
<h2 style="font-size: 24px;">Contents</h2>

- <span style="font-size: 18px;">[Introduction](#introduction)</span>
- <span style="font-size: 18px;">[Features](#features)</span>
- <span style="font-size: 18px;">[Diagrams](#diagrams)</span>
- <span style="font-size: 18px;">[Built with](#built-with)</span>
- <span style="font-size: 18px;">[Installation](#installation)</span>
- <span style="font-size: 18px;">[License](#license)</span>
- <span style="font-size: 18px;">[Contact](#contact)</span>
---

## Introduction
Library Management is a library management application where librarians and administrators share the same user role, managing tasks such as book cataloging and reader (also called member) management. This project is the final assignment of Group 12 for the Object-Oriented Programming course, class 2425I_INT2204_19 at VNU-UET.

Our team:

| Name                 | Role        | Github link                              |
|----------------------|-------------|------------------------------------------|
| Dao Van Dat          | Team Leader | [DATTB09](https://github.com/DATTB09)    |
| Nguyen Manh Phuc Loc | Developer   | [nmploc](https://github.com/nmploc)      |
| Le Hoang Trung       | Developer   | [trungabc498](https://github.com/trungabc498) |

---  
## Features
- Login and search for book information (using Prefix Tree algorithm for smooth searching) are available for both user groups.

- **For Admin (Librarian)**:
    - Edit book details and add new books.
    - Retrieve book details using ISBN through the Google Books API to assist in adding books.
    - View borrowing records and mark books as returned when readers return them.
    - Access member information and delete members for serious violations.

---  
## Diagrams

- Class
<div align="center">  
  <img src="src/main/resources/image/Class Diagram no0.png" alt="Class">  
</div>

- Database:
<div align="center">  
  <img src="src/main/resources/image/Database Diagram.png" alt="Database">  
</div>

---  
## Built with
- **IntelliJ IDEA**: IDE for efficient development.
- **JDK 17**: Java Development Kit for building the core app.
- **OpenJFX 22**: Framework for building desktop applications with JavaFX.
- **XAMPP**: For running a local server environment for testing and database management.
- **Maven**: Build tool for managing dependencies.
- **ZXing**: QR code and barcode library.
- **CSS**: For styling the UI.
- **JavaFX**: Framework for building the graphical user interface.
- **Google Books API** and **Jackson**: For fetching book data from the Google Books API.

---  
## Installation
1. **Download and install XAMPP**:  
   Download from the [XAMPP official website](https://www.apachefriends.org/download.html).

2. **Download and install IntelliJ IDEA**:  
   Download from the [IntelliJ IDEA website](https://www.jetbrains.com/idea/download/?section=windows).

3. **Clone and open this repository in IntelliJ IDEA**:
   ```bash
   git clone https://github.com/nmploc/Library_management.git
   ```

4. **Open XAMPP and configure the database**:
    - Open XAMPP, start Apache and MySQL.
    - Go to `MySQL` > `Admin` to open the MySQL console.
    - In the MySQL console, copy the SQL commands from `librarydb.sql` located at `src/main/resources/Database/librarydb.sql` and execute them to import the database.

5. **Change the directory of XAMPP installation location**:
    - Navigate to the `startXampp` and `stopXampp` files in `databasehelper/src/main/java/library/DatabaseHelper.java`.
    - Update the paths to match your local XAMPP installation directory.

6. **Configure the project SDK to JDK 17**:
    - In IntelliJ IDEA, go to `File -> Project Structure -> Project Settings -> Project` and set the SDK to JDK 17.

7. **Open Maven tool window and run the following commands**:
    - In IntelliJ IDEA, open the Maven tool window and run:
      ```bash
      mvn clean
      mvn install
      ```

8. **Configure run/debug settings**:
    - Open `Run/Debug Configuration` window.
    - Click `New Application` and select JDK 17.
    - Choose `Modify Options` and enable `Add VM Options`.
    - In the VM options field, add:
      ```bash
      --module-path {path to sdk}/javafx-sdk-22.0.2/lib --add-modules javafx.controls,javafx.fxml,javafx.web
      ```

9. **Run the application**:
    - Save the configuration and run `AppStart` to start using the application.
 
## Contact
- **Email**: [quenten9212@gmail.com](mailto:quenten9212@gmail.com)
- **GitHub**: [DATTB09](https://github.com/DATTB09)

---

Let me know if you need any further adjustments!
