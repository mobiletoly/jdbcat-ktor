package jdbcat.ktor.example

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import jdbcat.core.tx
import jdbcat.ktor.example.db.dao.DepartmentDao
import jdbcat.ktor.example.db.dao.EmployeeDao
import kotlinx.coroutines.runBlocking
import org.koin.Logger.SLF4JLogger
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import org.spekframework.spek2.Spek
import org.spekframework.spek2.dsl.Root
import org.testcontainers.containers.PostgreSQLContainer
import java.util.Properties
import javax.sql.DataSource

/**
 * Derive your objects from this class if you want to initialize TestContainers database
 * and test REST interfaces.
 */
abstract class AppSpek(val appRoot: Root.() -> Unit) : Spek({

    beforeGroup {
        if (! postgresContainer.isRunning) {
            postgresContainer.start()
        }
    }

    appRoot()
}) {
    companion object {
        class AppPostgreSQLContainer : PostgreSQLContainer<AppPostgreSQLContainer>()

        val postgresContainer = AppPostgreSQLContainer()
        val jacksonMapper = jacksonObjectMapper()

        fun <R> withApp(test: suspend TestApplicationEngine.() -> R) = withTestApplication {
            runBlocking {
                initApp(application)
                test.invoke(this@withTestApplication)

                // Clean-up after each test
                val employeeDao = application.get<EmployeeDao>()
                val departmentDao = application.get<DepartmentDao>()
                val dataSource = application.get<DataSource>()
                dataSource.tx {
                    employeeDao.dropTableIfExists()
                    departmentDao.dropTableIfExists()
                }
                (dataSource as HikariDataSource).close()
            }
        }

        private fun initApp(application: Application) {
            val mainConfigProperties = Properties().apply {
                put("jdbcat-ktor.main-db.hikari.dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource")
                put("jdbcat-ktor.main-db.hikari.dataSource.url", postgresContainer.jdbcUrl)
                put("jdbcat-ktor.main-db.hikari.dataSource.user", postgresContainer.username)
                put("jdbcat-ktor.main-db.hikari.dataSource.password", postgresContainer.password)
                put("jdbcat-ktor.main-db.hikari.autoCommit", false)
                put("some-other-config.some-other-property", "some other test value")
            }
            val config = ConfigFactory.parseProperties(mainConfigProperties)

            application.install(Koin) {
                SLF4JLogger()
                modules(appModule)
                properties(mapOf("mainConfig" to config))
            }
            application.bootstrap()
        }
    }
}
