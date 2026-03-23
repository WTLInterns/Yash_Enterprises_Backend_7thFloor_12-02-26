# Attendance Backend

A production-ready HR Attendance backend in Java 17 using Spring Boot, Spring Security (session/login, no JWT), MySQL, Flyway, local file upload, OpenAPI, and layered architecture.

## Features
- MySQL + Spring Data JPA/Hibernate + Flyway
- Session/cookie-based authentication (Spring Security)
- BCrypt password hashing
- Employees, Attendance (CRUD), Dashboard, etc.
- Layered: DTOs + MapStruct, services, controllers, clean architecture
- File upload (local disk; e.g. for profile images, document uploads)
- Swagger UI and OpenAPI docs
- Unit and integration tests

## How to Run

1. **MySQL Setup**
    - Ensure MySQL is running on your machine.
    - Create a database named `attendance_db`:
      ```sql
      CREATE DATABASE attendance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
      ```
    - Update the `spring.datasource.username` and `spring.datasource.password` in `src/main/resources/application.properties`.
2. **Build and Start App**
    ```bash
    mvn clean package
    java -jar target/attendance-backend-1.0.0.jar
    ```
   App is on http://localhost:8080
3. **Swagger API Docs**
   Visit [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
4. **Uploading Files**
   Files are stored in the `uploads/` folder in your project root.
5. **Flyway Migration**
   - On first start, Flyway will auto-create tables and indexes, including initial admin user if seeded.

---

## Main Endpoints
- `POST /api/auth/login` (email, password — creates session cookie)
- `POST /api/auth/register` (admin creates new user)
- `POST /api/auth/logout`
- Standard `/api/employees`, `/api/attendance` endpoints

## Folder Structure
```
|- src/main/java/com/company/attendance/
|    |- AttendanceApplication.java
|    |- config/
|    |- controller/
|    |- dto/
|    |- entity/
|    |- mapper/
|    |- repository/
|    |- service/
|    |- util/
|- src/main/resources/
|    |- application.properties
|    |- db/migration/
|- uploads/
```

This backend is ready to run. Extend as needed for teams, clients, forms, leaves, tasks, etc!

