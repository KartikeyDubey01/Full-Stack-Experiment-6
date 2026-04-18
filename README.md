# JPA & Hibernate with MySQL/PostgreSQL — Spring Boot Demo

A complete Spring Boot project demonstrating JPA and Hibernate integration with MySQL/PostgreSQL. Covers entity modelling, One-to-Many and Many-to-Many relationships, custom JPQL queries, Criteria API, sorting, pagination, and SQL analysis.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Database Setup](#database-setup)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Entity Relationships](#entity-relationships)
- [Query Types Demonstrated](#query-types-demonstrated)
- [Sample curl Commands](#sample-curl-commands)
- [Analyzing Generated SQL](#analyzing-generated-sql)
- [Common Errors & Fixes](#common-errors--fixes)

---

## Overview

This project demonstrates three core JPA/Hibernate concepts:

| Part | What it covers |
|------|----------------|
| **a** | Database connectivity via `application.properties`, `@Entity` classes, Spring Data `@Repository` |
| **b** | `@ManyToMany` (User ↔ Role with join table), `@OneToMany` / `@ManyToOne` (Category ↔ Product), fetching related data |
| **c** | JPQL `@Query`, derived queries, `Pageable` + `Sort`, Criteria API `Specification` for dynamic filtering, SQL log analysis |

---

## Tech Stack

| Technology | Version |
|------------|---------|
| Java | 17 |
| Spring Boot | 3.2.5 |
| Spring Data JPA | included in Boot |
| Hibernate | 6.x (via Boot) |
| MySQL | 8.0+ |
| Maven | 3.8+ |
| Lombok | latest |
| Jackson | included in Boot |

---

## Project Structure

```
jpa-demo/
├── src/
│   └── main/
│       ├── java/com/example/jpademo/
│       │   ├── JpaDemoApplication.java          ← entry point
│       │   ├── entity/
│       │   │   ├── User.java                    ← @ManyToMany → Role
│       │   │   ├── Role.java
│       │   │   ├── Category.java                ← @OneToMany → Product
│       │   │   └── Product.java                 ← @ManyToOne → Category
│       │   ├── repository/
│       │   │   ├── UserRepository.java          ← JPQL + pagination
│       │   │   ├── RoleRepository.java
│       │   │   ├── CategoryRepository.java
│       │   │   └── ProductRepository.java       ← JPQL + native + spec
│       │   ├── service/
│       │   │   ├── UserService.java
│       │   │   └── ProductService.java
│       │   ├── controller/
│       │   │   └── AppController.java           ← REST endpoints
│       │   └── specification/
│       │       └── ProductSpecifications.java   ← Criteria API
│       └── resources/
│           └── application.properties
├── pom.xml
└── README.md
```

---

## Prerequisites

Install the following before running the project:

| Tool | Version | Download |
|------|---------|----------|
| JDK | 17 or 21 | https://adoptium.net |
| Maven | 3.8+ | https://maven.apache.org |
| MySQL | 8.0+ | https://dev.mysql.com/downloads |
| IntelliJ IDEA | any | https://www.jetbrains.com/idea |

Verify your installations:

```bash
java -version
mvn -version
mysql --version
```

---

## Database Setup

Log into MySQL and create the database:

```bash
mysql -u root -p
```

```sql
CREATE DATABASE jpa_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SHOW DATABASES;
EXIT;
```

For PostgreSQL instead:

```bash
psql -U postgres
```

```sql
CREATE DATABASE jpa_demo;
\q
```

Hibernate will automatically create all tables (`users`, `roles`, `user_roles`, `categories`, `products`) on first run because `ddl-auto=update` is set.

---

## Configuration

Edit `src/main/resources/application.properties`:

### MySQL (default)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/jpa_demo?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

server.port=8080
```

### PostgreSQL (alternative)

Replace the datasource block with:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/jpa_demo
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

Also swap the Maven dependency in `pom.xml` from `mysql-connector-j` to `postgresql`.

---

## Running the Application

```bash
# Step 1 — enter the project directory
cd jpa-demo

# Step 2 — compile and package (skip tests for first run)
mvn clean install -DskipTests

# Step 3 — start the server
mvn spring-boot:run
```

Expected console output on successful startup:

```
Hibernate: create table roles (...)
Hibernate: create table users (...)
Hibernate: create table user_roles (...)
Hibernate: create table categories (...)
Hibernate: create table products (...)
Started JpaDemoApplication in 3.2 seconds (JVM running for 3.8)
```

The server runs at **http://localhost:8080**.

---

## API Endpoints

### User Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `/api/users` | Create a user with roles |
| `GET` | `/api/users` | Get all users |
| `GET` | `/api/users/by-role?role=ROLE_ADMIN` | Get users by role (JPQL) |
| `GET` | `/api/users/search?keyword=ali&page=0&size=5` | Paginated keyword search |

### Product Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `/api/products` | Create a product under a category |
| `GET` | `/api/products` | Get all products |
| `GET` | `/api/products/price-range?min=100&max=5000` | Filter by price range (JPQL) |
| `GET` | `/api/products/filter?minPrice=500&maxPrice=80000&category=Electronics&keyword=lap&page=0&size=5` | Dynamic filter with Criteria API |
| `GET` | `/api/products/paginated?page=0&size=5&sortBy=price` | Paginated + sorted list |
| `GET` | `/api/products/low-stock?threshold=20` | Low stock alert (native SQL) |

---

## Entity Relationships

### Many-to-Many — User ↔ Role

```
users ──────────── user_roles ──────────── roles
 id (PK)            user_id (FK)            id (PK)
 username           role_id (FK)            name
 email
```

Implemented with `@ManyToMany` + `@JoinTable` on the `User` entity. The join table `user_roles` is auto-created by Hibernate.

### One-to-Many — Category ↔ Product

```
categories ──────────────────── products
 id (PK)                          id (PK)
 name                             name
                                  price
                                  description
                                  stock
                                  category_id (FK)
```

Implemented with `@OneToMany` on `Category` and `@ManyToOne` on `Product`. `@JsonManagedReference` / `@JsonBackReference` prevent infinite JSON loops.

---

## Query Types Demonstrated

### 1. Derived Query (auto-generated SQL)

```java
List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
```

Spring Data generates the SQL automatically from the method name.

### 2. JPQL Named Query

```java
@Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
List<User> findUsersByRoleName(@Param("roleName") String roleName);
```

### 3. Paginated JPQL Query

```java
@Query("SELECT u FROM User u WHERE u.username LIKE %:keyword%")
Page<User> searchByUsername(@Param("keyword") String keyword, Pageable pageable);
```

### 4. Native SQL Query

```java
@Query(value = "SELECT * FROM products WHERE stock < :threshold", nativeQuery = true)
List<Product> findLowStockProducts(@Param("threshold") int threshold);
```

### 5. Criteria API (dynamic filters)

```java
Specification<Product> spec = ProductSpecifications.withFilters(minPrice, maxPrice, category, keyword);
return productRepository.findAll(spec, pageable);
```

Builds `WHERE` clauses at runtime based on which parameters are non-null — no hardcoded SQL strings needed.

---

## Sample curl Commands

```bash
# Create products
curl -s -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","price":75000,"description":"16GB RAM","stock":10,"category":"Electronics"}'

curl -s -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Phone","price":25000,"description":"5G ready","stock":50,"category":"Electronics"}'

curl -s -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Earbuds","price":3500,"description":"BT 5.3","stock":5,"category":"Electronics"}'

# Create users with roles
curl -s -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@test.com","roles":["ROLE_ADMIN","ROLE_USER"]}'

curl -s -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"bob","email":"bob@test.com","roles":["ROLE_USER"]}'

# Query users by role (JPQL)
curl -s "http://localhost:8080/api/users/by-role?role=ROLE_ADMIN"

# Filter products by price range (JPQL)
curl -s "http://localhost:8080/api/products/price-range?min=1000&max=50000"

# Dynamic filter using Criteria API
curl -s "http://localhost:8080/api/products/filter?minPrice=1000&maxPrice=80000&category=Electronics&page=0&size=5"

# Paginated and sorted
curl -s "http://localhost:8080/api/products/paginated?page=0&size=5&sortBy=price"

# Low stock (native SQL)
curl -s "http://localhost:8080/api/products/low-stock?threshold=20"
```

---

## Analyzing Generated SQL

With `show-sql=true` and `format_sql=true` active, Hibernate prints every SQL statement to the console.

**Example — Criteria API filter call:**

```sql
SELECT
    p.id, p.name, p.price, p.description, p.stock, p.category_id
FROM
    products p
INNER JOIN
    categories c ON p.category_id = c.id
WHERE
    p.price >= 1000.0
    AND p.price <= 80000.0
    AND c.name = 'Electronics'
ORDER BY
    p.price ASC
LIMIT 5 OFFSET 0;
```

**Example — Many-to-Many JPQL:**

```sql
SELECT DISTINCT
    u.id, u.username, u.email
FROM
    users u
INNER JOIN
    user_roles ur ON u.id = ur.user_id
INNER JOIN
    roles r ON ur.role_id = r.id
WHERE
    r.name = 'ROLE_ADMIN';
```

To see bind parameter values in the log, keep this line in `application.properties`:

```properties
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

---

## Common Errors & Fixes

| Error | Cause | Fix |
|-------|-------|-----|
| `Access denied for user 'root'` | Wrong DB password | Update `spring.datasource.password` in `application.properties` |
| `Unknown database 'jpa_demo'` | Database not created | Run `CREATE DATABASE jpa_demo;` in MySQL shell |
| `Communications link failure` | MySQL not running | Run `sudo service mysql start` (Linux) or start MySQL from System Preferences (Mac) |
| `Cannot find symbol: @Data` | Lombok annotation processing off | Enable annotation processing in IntelliJ: File → Settings → Build → Compiler → Annotation Processors |
| `Could not autowire RoleRepository` | Missing `@Repository` or interface not found | Make sure `RoleRepository.java` exists and is annotated with `@Repository` |
| `No serializer found for class` | Bidirectional JSON loop | Ensure `@JsonManagedReference` is on `Category.products` and `@JsonBackReference` is on `Product.category` |
| `Table 'jpa_demo.xxx' doesn't exist` | DDL auto not set | Set `spring.jpa.hibernate.ddl-auto=update` in `application.properties` |

---

## License

This project is created for educational purposes as part of a JPA & Hibernate experiment.

---

Author

Kartikey Dubey
UID: 23BCT10003
