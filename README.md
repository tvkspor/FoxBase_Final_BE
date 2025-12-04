<h1 align="left">
  <img src="https://res.cloudinary.com/ddlpbdgv5/image/upload/v1747475880/fox_uh0ewd.png?raw=true" alt="Foxbase Logo" width="40" style="vertical-align: middle; margin-right: 10px;" />
  Foxbase Backend
</h1>

Foxbase is a platform that allows users to read ebooks, publish their own writings, and interact with a community of readers and writers. This is the **backend** service that powers the application.

Built with a modern, scalable backend stack to support authentication, content management, ratings, payments, and more.

---

## ⚙️ Tech Stack

- **Spring Boot** - Java
- **REST API** architecture
- **MySQL** – Database
- **Google OAuth** – Sign in with Google
- **ZaloPay** *(Sandbox for testing)* – Payment integration
- **JWT** – Token-based authentication

## Build the run the project

- Run the main application:

```
.\gradlew bootRun
```

- Run the test profile:

```
.\gradlew bootRun --args='--spring.profiles.active=test'
```

- Run the integration tests:

```
newman run src/test/newman/collections/<test-name>.json -e src/test/newman/env/environment.json
```

- Run the tests with reporter:

```
newman run src/test/newman/collections/<test-name>.json \
  -e src/test/newman/env/environment.json \
  --reporters cli,html \
  --reporter-html-export src/test/newman/results/results.html
```