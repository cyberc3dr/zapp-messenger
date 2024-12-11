package ru.sliva.zapp.data.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import org.jetbrains.annotations.ApiStatus.Experimental
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// накидал чего-то
@Experimental
class DefaultDatabaseManager(val driver: String, val url: String, val user: String, val pass: String) {

    private val logger: Logger = LoggerFactory.getLogger(DefaultDatabaseManager::class.java.name)


    // todo переписать этот говнокод (молюсь)
    suspend fun connect() : Database {
        delay(50L)

        logger.info("new database connection")

        db = Database.connect(url, driver, user, pass)

        return db
    }

    suspend fun <T> query(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    companion object {
        lateinit var db: Database
    }

}