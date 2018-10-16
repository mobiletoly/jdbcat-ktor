package jdbcat.ktor.example

import com.typesafe.config.ConfigFactory
import io.ktor.application.Application
import mu.KotlinLogging
import org.koin.ktor.ext.installKoin
import org.koin.log.Logger.SLF4JLogger

// All REST calls must specify version, e.g. http://localhost/api/v1/healthcheck
const val serviceApiVersionV1 = "api/v1"

private val logger = KotlinLogging.logger { }

/**
 * Application's ENTRY POINT.
 * This method is called by Ktor and this entry point is configured in /resources/application.conf
 */
@Suppress("unused")
fun Application.main() {

    val mainConfig = ConfigFactory.load("main.conf")

    // Add Koin DI support for Ktor
    installKoin(
        listOf(appModule),
        extraProperties = mapOf("mainConfig" to mainConfig),
        logger = SLF4JLogger()
    )

    // Initial bootstrap
    bootstrap()
}
