# Language Practice API

Backend API for the Language Practice application — a platform for creating and completing interactive Spanish grammar exercises.

The API provides functionality for managing exercise sets, validating student answers, tracking attempts, and generating exercises with AI.

---

## 💡 Why I Built This

I built this project while learning Spanish in Argentina.

Most existing language practice websites either:
- don't support Argentine Spanish well,
- are uncomfortable for quick custom grammar practice,
- or rely on AI chat interfaces that are not ideal for repetitive tense exercises.

I wanted a faster way to generate and practice focused grammar exercises, especially for Spanish verb tenses used in everyday Argentine Spanish.

---

## 🌟 Features

- REST API for exercise creation and management
- Student exercise submission and scoring
- AI-powered exercise generation with OpenAI
- Support for multiple exercise formats
- Public and private exercise flows
- Validation and structured DTO-based API design
- Multilingual-ready architecture

---

## 🚀 Tech Stack

- Kotlin
- Spring Boot
- PostgreSQL
- Maven
- JPA / Hibernate
- OpenAI API
- Docker
- Testcontainers

---

## ⚙️ Getting Started

### 1. Installation

```bash
git clone <repository-url>
cd api
```

### 2. Configuration

Create an `.env` file or configure environment variables:

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/langpractice
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

OPENAI_API_KEY=your_api_key
```

### 3. Run Application

```bash
./mvnw spring-boot:run
```

API will start on:

```text
http://localhost:8080
```

---

## 🧪 Running Tests

```bash
./mvnw test
```

Integration tests use Testcontainers with PostgreSQL.

---

## 🏗 Architecture Notes

- Layered architecture (controller → service → repository)
- DTO-based API contracts
- UUID-based entity identifiers
- OpenAI integration isolated in dedicated service layer
- Validation using Jakarta Validation
- PostgreSQL persistence with Spring Data JPA
- Integration testing with Testcontainers

---

## 📂 Related Repositories

- [Web Frontend](https://github.com/ValentinaBaranova/lang-practice-web)
