## Student Placement Tracker
### 📌 Project Overview

Student Placement Tracker is a web-based application developed using Spring Boot that helps manage and track student placement information.
The system allows administrators or placement coordinators to store, retrieve, and manage details of students and companies involved in campus placements.

The application connects to a MySQL database and provides backend APIs to manage placement-related data efficiently.

This project is useful for:

Tracking student placement status

Managing company information

Viewing placement records

Simplifying placement data management

### 🚀 Features

Store student placement details

Manage company information

Connect to MySQL database

Static frontend resources (CSS, logos)

### 🛠️ Tech Stack
Backend

Java – Version 17

Spring Boot – Version 3.2.5

Spring Boot Web Starter

Maven – Dependency Management

Database

MySQL

MySQL Connector/J – Version 9.6.0

Frontend (Static Resources)

HTML

CSS

Static images/logos

### 📂 Project Structure
<img width="530" height="706" alt="Screenshot 2026-03-18 235048" src="https://github.com/user-attachments/assets/3c715378-38a3-4683-b618-0ac36097d6cb" />


### 🔄 Project Workflow

The application follows a simple Spring Boot architecture flow:

#### 1️⃣ Application Start

The project starts from:

PlacementTrackerApplication.java

Spring Boot initializes the application and starts the embedded server.

#### 2️⃣ Database Connection

DBConnection.java handles the connection between the application and MySQL database.

Responsibilities:

Establish database connection

Manage SQL queries

#### 3️⃣ Controller Layer

PlacementController.java

Handles incoming HTTP requests such as:

Adding student details

Fetching placement data

Managing company information

Controller communicates with the database layer to perform operations.

#### 4️⃣ Model Layer

The model classes represent database entities.

Student.java

Stores student details like name, branch, placement status.

Company.java

Stores company information.

#### 5️⃣ Static Resources

Located in:

src/main/resources/static

Contains:

CSS styles

Company logos

Static UI resources

⚙️ Prerequisites

#### Before running the project, install the following:

Java JDK 17

Maven 3.8+

MySQL Server

IDE (IntelliJ / Eclipse / VS Code)

🗄️ Database Setup

Install MySQL

Create a database

CREATE DATABASE placement_tracker;

Update database credentials in:

src/main/resources/application.properties

##### Example configuration:

spring.datasource.url=jdbc:mysql://localhost:3306/placement_tracker
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
#### ▶️ How to Run the Project
Step 1

Clone the repository or download the project.

git clone <repository-link>
Step 2

Navigate to the project directory.

cd Student_Placement_Tracker
Step 3

Build the project using Maven.

mvn clean install
Step 4

Run the Spring Boot application.

mvn spring-boot:run

OR run:

PlacementTrackerApplication.java

from your IDE.

Step 5

Open the application in your browser:

http://localhost:8080
### 📸 Application Screenshots

You can add screenshots here.

Example:

## Dashboard (add student, add company)

<img width="1871" height="783" alt="Screenshot 2026-03-18 225915" src="https://github.com/user-attachments/assets/b93550c3-a083-4460-bb31-11baa06a77c3" />

## Student List
<img width="1811" height="731" alt="Screenshot 2026-03-18 225929" src="https://github.com/user-attachments/assets/71e75c0b-fdda-4788-9a60-70d35b85877c" />


## Company List
<img width="1872" height="664" alt="Screenshot 2026-03-18 225938" src="https://github.com/user-attachments/assets/1b75211c-689e-4459-abaf-ec4cf6bce9c1" />


## Placement status according to company

<img width="1436" height="822" alt="Screenshot 2026-03-18 230125" src="https://github.com/user-attachments/assets/19a45a46-9a8e-49ba-972b-dd34c0b506a3" />


<img width="1460" height="864" alt="Screenshot 2026-03-18 230547" src="https://github.com/user-attachments/assets/91a17096-7fc3-4900-9ad3-f7c396b690e4" />

## Place a Student
<img width="1863" height="621" alt="Screenshot 2026-03-18 230045" src="https://github.com/user-attachments/assets/94cd18b9-bc65-46f2-830a-860d173109d8" />


📈 Future Improvements

Add authentication and login system

Build a complete frontend (React / Angular)

Add placement analytics dashboard

Export placement reports

Admin panel for placement officers
