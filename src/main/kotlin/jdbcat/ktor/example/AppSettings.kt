package jdbcat.ktor.example

import com.typesafe.config.Config
import com.zaxxer.hikari.HikariConfig
import java.util.Properties

/**
 * Central repository of all application's settings.
 */
class AppSettings(private val config: Config) {

    // you can keep your application-specific data in main.conf
    // "application.conf" is already reserved by ktor and we don't want to keep our configs in there,
    // otherwise we are going to have more complicated code

    /**
     * Loads a HikariCP config specified in /resources/application.conf
     * or any other .conf files visible to HOCON.
     */
    val hikariMainDatabaseConfig by lazy {
        val dbConfig = config.getConfig("jdbcat-ktor.main-db.hikari")
        HikariConfig(dbConfig.toProperties())
    }

    val someOtherProperty by lazy {
        config.getConfig("some-other-config").getString("some-other-property")!!
    }
}

// Convert HOCON Config object into Properties (some libraries, e.g. HikariCP don't understand HOCON format).
private fun Config.toProperties() = Properties().also {
    for (e in this.entrySet()) {
        it.setProperty(e.key, this.getString(e.key))
    }
}
