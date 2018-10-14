# What do we build?

We are building a basic REST service to manage Departments and Employees entities with **ktor** framework.

# What tools/frameworks do we use?

You are landed to the right repository if you want to take a look at REST service template built on top of:

- **gradle** - no comments
- **ktor** for creating web application: https://github.com/ktorio/ktor
- **JDBCat** to access database: https://github.com/mobiletoly/jdbcat
  (if you want to replace it with ExposedSQL - we will have another example for that, but for now we want to
  stick with our own thin type-safe layer on top of JDBC, well it does support a proper transaction management
  in couroutines environment)
- **netty** for ktor to run on top of: https://netty.io/
- **HikariCP** for high-performance JDBC connection pool: https://github.com/brettwooldridge/HikariCP
- **Koin** for dependency injection: https://insert-koin.io/ 
- **PostgreSQL** for database: https://www.postgresql.org/
- **kotlin-logging** for logging: https://github.com/MicroUtils/kotlin-logging
- **HOCON** for application configuration: https://github.com/lightbend/config/
- **jackson** for JSON serialization/deserialization: https://github.com/FasterXML/jacksons

and for testing:
- **Testcontainers** for unit testing with a real database in Docker: https://github.com/testcontainers/testcontainers-java
- **junit5** + **spek** for writing tests: https://github.com/spekframework/spek/

and some other misc stuff:
- **ktlint** for Kotlin checkstyle
- **jacoco** for code coverage metrics

# Setup to run

