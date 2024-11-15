# T1toT2project
# Spring Boot Expense Management API

## Overview

This is a Spring Boot-based (v 3.3.3) web application designed for managing users, roles, profiles, expenses, and categories with **JWT-based authentication** and **role-based access control (RBAC)**. It provides secure CRUD operations for managing personal expenses and categories, with administrative users having additional privileges to manage roles, users, and profiles.

The application uses **Spring Security** to enforce security policies, including JWT-based stateless authentication and role-based authorization.

## Features

- **User Management**: CRUD operations for users, profiles, and roles.
- **Expense Management**: Track and manage user expenses and categorize them.
- **Category Management**: Categorize expenses (e.g., Food, Travel).
- **Role-based Access Control**: Admins can manage roles and users, while regular users can only manage their own data.
- **JWT Authentication**: Secure access via JWT tokens for stateless authentication.

## Key Modules

1. **Entities**:
    - Models representing the core data structures: `User`, `Role`, `Profile`, `Expense`, and `Category`.

2. **Controllers**:
    - Manage incoming HTTP requests and direct them to the appropriate business logic.

3. **Services**:
    - Business logic for user registration, expense management, role assignments, etc.

4. **Security**:
    - Handles JWT authentication, filtering, and authorization based on roles.

5. **Testing**:
    - Unit testing with JUnit 5 and Mockito for controllers, services, and repositories.

6. **No Front-End Included**:
    - The project is a backend API with RESTful endpoints. Front-end integration is possible with frameworks like React, Angular, or Vue.js.

7. **Postman Collection**:
    - A collection for testing all API endpoints is included.

---

## Installation

### Prerequisites

Ensure you have the following installed:

- JDK 17 or higher
- Maven (for building the project)
- PostgreSQL or any relational database (for production) or an in-memory database (for testing)
- Postman (for testing API endpoints)

### Steps to Set Up

1. **Clone the repository**:
    ```bash
    git clone https://github.com/BeringSea/T1toT2project
    cd expense-management-api
    ```

2. **Configure Database**:
    - Set up your PostgreSQL database or configure an in-memory database like H2 in `application.properties`.
    - Example for PostgreSQL:
      ```properties
      spring.datasource.url=jdbc:postgresql://localhost:5432/expense_db
      spring.datasource.username=your_db_user
      spring.datasource.password=your_db_password
      ```

3. **Install dependencies**:
    ```bash
    mvn clean install
    ```

4. **Run the application**:
    ```bash
    mvn spring-boot:run
    ```
   The application will start on `http://localhost:8080`.

---

## API Endpoints

### Authentication

- **POST /login**: Logs in a user and returns a JWT token.
- **POST /register**: Registers a new user with email, username, password, and profile.

### User Management (Admin)

- **GET /user**: Lists all users.
- **GET /user/{id}**: Get details of a specific user.
- **PUT /user/{id}**: Update a specific user.
- **DELETE /user/{id}**: Delete a specific user.

### Profile Management

- **GET /profiles/{userId}**: Get the profile of a user (admin can access any profile).
- **GET /profiles**: Get the logged-in user's profile.
- **PUT /profiles/{userId}**: Update a specific user's profile (admin only).

### Expense Management

- **POST /expenses**: Create a new expense for the logged-in user (admin can create for any user).
- **GET /expenses/{id}**: Retrieve a specific expense.
- **PUT /expenses/{id}**: Update a specific expense (admin can update for any user).
- **DELETE /expenses/{id}**: Delete a specific expense (admin can delete for any user).

### Category Management

- **POST /categories**: Create a new category (admin can create for any user).
- **GET /categories/{id}**: Retrieve a specific category.
- **PUT /categories/{id}**: Update a specific category (admin can update for any user).
- **DELETE /categories/{id}**: Delete a specific category (admin can delete for any user).

### Role Management (Admin)

- **POST /roles**: Create a new role (e.g., ROLE_USER, ROLE_ADMIN).
- **GET /roles**: List all available roles.
- **GET /roles/{id}**: Retrieve a specific role.
- **PUT /roles/{id}**: Update a role.
- **DELETE /roles/{id}**: Delete a role.

### Admin User Management

- **GET /user/{id}/profile**: Get the profile for a specific user (admin only).
- **DELETE /delete/expense/{id}**: Delete a specific expense (admin only).
- **DELETE /delete/category/{id}**: Delete a specific category (admin only).
- **DELETE /delete/user/{id}**: Delete a specific user (admin only).

---

## Security

### JWT Authentication

- After logging in with **POST /login**, a JWT token will be returned. Include this token in the `Authorization` header of subsequent requests in the format:
  ```bash
  Authorization: Bearer <your-jwt-token>
