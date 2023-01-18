For more up-to-date example please take a look at:
https://github.com/mobiletoly/ktor-hexagonal-multimodule


# What do we build?

We are building a basic REST service to manage Departments and Employees entities with **ktor** framework.

# What tools/frameworks do we use?

You are landed to the right repository if you want to take a look at REST service template built on top of:

- **gradle** - no comments
- **kotlin 1.3** - no comments
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
- **junit** + **spek2** for writing tests: https://github.com/spekframework/spek/

and some other misc stuff:
- **ktlint** for Kotlin checkstyle
- **jacoco** for code coverage metrics

# Setup

## Prerequisites

#### Docker

Docker is not required to run this example, but makes setup a little easier. We recommend installing it.

#### PostgreSQL

If you have a local PostgreSQL running on your computer, all you have to do is to edit `/resources/main.conf`
file to specify proper PostgreSQL configuration, such as host, database name, user name and password.
If you don't have PostgreSQL installed, you can do it now by installing from:
https://www.postgresql.org/download/
and set it up with user name and password that can later be used to configure 

If you have docker installed and running, then you should be able to install PostgreSQL that would
match default settings in `/resources/main.conf`

```bash
$ docker run --name localpostgres -d -p 5432:5432 -e POSTGRES_PASSWORD=postgresspass postgres:alpine
```

this will create and run a local instance of PostgreSQL database with user name "postgres" and
password "postgresspass".

Or use

```bash
$ docker start localpostgres
```

if localpostgres container is already created. 

## Run application

There are few different ways how you can start jdbcat-ktor app. Easiest way is to use gradle
and run service from command line. Another approach is to run directly from IntelliJ IDEA.

Feel free to use curl to access jdbcat-ktor REST endpoints. We have also included Postman collection file
`./postman/jdbcat-ktor.postman_collection.json`.

#### Gradle

Go to application's directory and run:

```bash
$ ./gradlew build run
```

This command will start jdbcat-ktor service on port 8080.
And will run unit tests as well.

#### IntelliJ IDEA

If you have checked out a code from our git repository - you will have IntelliJ project already available
to you with "API Server" task. Just select and run it. If you are building your own project or
imported jdbcat-ktor as Gradle project with your own IntelliJ project - then you need to select
"Edit / Run Configuration", add new "Application" and setup fields:

| Field                   | Value                                       |
| ----------------------- | ------------------------------------------- |
| Main class              | io.ktor.server.netty.DevelopmentEngine      |
| Program arguments       | -config=src/main/resources/application.conf |
| Use classpath or module | jdbcat-ktor_main                            |

You can enable auto-reload mode, we have a separate config for it (obviously don't use it in production). Change
"Program arguments" to "-config=src/main/resources/application-dev.conf"
Then you can run
```bash
$ ./gradlew -t -x test -x shadowJar -x shadowDistZip -x distZip -x distTar -x ktlintTestCheck -x ktlintMainCheck -x junitPlatformTest build
```
to enable continuous compilation (so every time you press Save in editor - code will be recompiled and redeployed
with need to manually restart a server). Read more about this functionality here https://ktor.io/servers/autoreload.htm

#### Run tests

```bash
$ ./gradlew test
```

Most of our tests are actually integration tests that test application functionality by hitting
REST endpoints. The command above will also generate a code coverage report that you can find
in `./build/jacoco/test/html`

You can also run unit tests from IntelliJ IDEA as well. If you use our IntelliJ project then
"Spek tests" is already available for you. If you have imported our project instead - then you need
to install "Spek Framework" plugin and after that go to **Edit Configuration**, choose add new Configuration
and select **Spek 2 - JVM**. You can leave all fields empty but select `jdbcat-ktor_test` in
"Use classpath or module" field. After that you can run it (if you need a code coverage make sure to perform
**Run / Run 'Spek tests' with Coverage**).
