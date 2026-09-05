# Sunrise Dental Clinic — Appointment & Patient Management System

A distributed, menu-driven Java application built for the WRIT1 assignment
(Online Vehicle... — actually: dental clinic appointment/patient management
brief). See `docs/` and the accompanying report for the full UML design
(Task A), test plan (Task C) and version-control workflow (Task D).

## Architecture

Two separate Java programs communicate over HTTP/JSON:

```
ConsoleClientApp  --HTTP/JSON-->  ApiServerApp  --JDBC-->  MySQL
(menu-driven UI)                  (REST web service)       (sunrise_dental)
```

This satisfies the brief's "distributed application with web services"
requirement: the client and server are independent processes (they could
run on different machines), and multiple receptionist workstations could
share one server + database.

**Why no Spring Boot?** The server is built entirely on
`com.sun.net.httpserver.HttpServer` (part of the standard JDK) rather than
a framework, so the whole project compiles and runs with **zero external
dependencies** beyond the MySQL JDBC driver. This was a deliberate
engineering trade-off made while developing in an offline sandbox with no
access to Maven Central — see "Known limitation" below. The service/DAO
layers underneath are framework-agnostic, so swapping in Spring Boot later
would only mean rewriting the thin handler classes in `server/`.

## Design patterns used

| Pattern | Where | Why |
|---|---|---|
| **Singleton** | `patterns/DatabaseConnection` | One shared, configured connection point to MySQL |
| **DAO** | `dao/I*DAO` + `dao/*DAOImpl` | Decouples business logic from persistence; enables the in-memory fakes used in testing |
| **Factory** | `patterns/UserFactory` | Centralises correct construction of Receptionist/Administrator + password hashing |
| **Builder** | `patterns/AppointmentBuilder` | Avoids a telescoping constructor for Appointment's several required fields |
| **Observer** | `patterns/NotificationCenter` + `AppointmentEventListener` | Decouples "an appointment happened" from "send an email/SMS about it" |
| **Facade** | `service/AppointmentService`, `service/BillService` | One simple entry point over validation + multiple DAOs |

Full rationale for each is in the project report (Task B section).

## Running it

Requires: Java 17+, Maven, MySQL Server 8.

```bash
# 1. Create the database and load schema + seed data
mysql -u root -p -e "CREATE DATABASE sunrise_dental"
mysql -u root -p sunrise_dental < src/main/resources/schema.sql
mysql -u root -p sunrise_dental < src/main/resources/data.sql

# 2. Start the API server (terminal 1)
mvn compile exec:java -Dexec.mainClass=com.sunrisedental.server.ApiServerApp

# 3. Start the console client (terminal 2)
mvn compile exec:java -Dexec.mainClass=com.sunrisedental.client.ConsoleClientApp
```

Default accounts (see `data.sql`):

| Role | Username | Password |
|---|---|---|
| Administrator | `admin` | `Admin@123` |
| Receptionist | `reception1` | `Reception@123` |

Change these after first login in any real deployment.

### Running the tests

```bash
mvn test
```

See `docs/test-plan.md` for the full test plan, test data and TDD rationale.

## Known limitation (please read before marking Task B)

This project was developed inside a cloud sandbox with **no general
internet access** (no Maven Central, no apt, no npm) — only the tools
already installed locally were available. That environment could not
install a MySQL server or download the MySQL JDBC driver, so:

- The web-service layer was deliberately built on the JDK's own
  `HttpServer` instead of Spring Boot, so it could be compiled *and run*
  in that sandbox with zero downloads (see `docs/dev-notes.md` for the
  verification log — an end-to-end HTTP smoke test and the full 48-test
  JUnit suite were both executed successfully there, against in-memory
  fake databases).
- The DAO layer, `pom.xml` and `schema.sql` all target real MySQL and are
  believed correct, but were **not** run against a live MySQL server
  before this first commit. The first task on a machine with internet
  access (i.e. yours) is to run the steps above and confirm end-to-end
  behaviour against real MySQL, then commit any fixes that surfaces.

## Project layout

```
src/main/java/com/sunrisedental/
  domain/    entity classes matching the Task A class diagram
  dao/       DAO pattern: interfaces + MySQL implementations
  patterns/  Singleton, Factory, Builder, Observer implementations
  service/   Facade-pattern business logic + validation
  server/    REST web-service layer (JDK HttpServer)
  client/    menu-driven console client
  util/      validation, JSON, password hashing, shared DTOs
src/main/resources/
  schema.sql, data.sql
src/test/java/com/sunrisedental/
  fakes/     in-memory test doubles for the DAO interfaces
  ...        JUnit 5 test suite
docs/
  test-plan.md, dev-notes.md
```

## MySQL Verification Update (05 Sep 2026)

This has since been verified for real: MySQL 8 was installed locally, the schema and seed data were loaded via MySQL Workbench, and both ApiServerApp and ConsoleClientApp were run successfully end-to-end against it (a real appointment registered, a bill generated, a revenue report produced). See the project report, Section 9 ("Evidence the System Runs"), for the unedited screenshot.
