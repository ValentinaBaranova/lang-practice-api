# Language Practice

A lightweight web application for creating and completing language practice exercises.

The project is focused on simple and fast teacher workflows:

* create an exercise set
* share a public link with students
* review student results

Students can complete exercises directly from a shared link without registration.

## Version 0.1 Scope

### Supported exercise type

* Fill-the-gap text exercises

### Teacher features

* Create exercise sets
* Edit exercise sets
* View exercise sets list
* Share public exercise links
* View student results

### Student features

* Open exercise by public link
* Enter name before starting
* Complete exercises
* Receive immediate feedback
* View final score
* Retry exercises unlimited times

## Planned features

* AI-assisted exercise generation using OpenAI
* Multiple exercise types
* Authentication
* Multi-language UI (EN / ES / RU)

## Tech Stack

### Backend

* Kotlin
* Spring Boot
* Maven
* PostgreSQL
* Flyway

### Frontend

* Next.js
* TypeScript
* Ant Design

## Architecture Notes

* PostgreSQL JSONB is used for flexible question storage
* UUIDs are used for entity identifiers
* The initial version is intentionally simple and optimized for MVP development
