package jdbcat.ktor.example

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import java.util.Properties

/**
 * Central repository of all application's settings.
 */
class AppGlobals {

    private val hoconConfig = ConfigFactory.load()

    /**
     * Loads a HikariCP config specified in /resources/application.conf
     * or any other .conf files visible to HOCON.
     */
    val hikariMainDatabaseConfig: HikariConfig by lazy {
        val props = hoconConfig.getConfig("hikari-jdbcat.ktor.example.main-db").toProperties()
        HikariConfig(props)
    }
}

// Convert HOCON Config object into Properties (some libraries, e.g. HikariCP don't understand HOCON format).
private fun Config.toProperties() = Properties().also {
    for (e in this.entrySet()) {
        it.setProperty(e.key, this.getString(e.key))
    }
}
