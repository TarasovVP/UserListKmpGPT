package com.example.usermp.shared

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.usermp.db.AppDatabase
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.java.Java
import java.io.File
import java.util.Properties

actual class PlatformConfiguration

actual class DatabaseFactory actual constructor(
    private val platformConfiguration: PlatformConfiguration,
) {
    actual suspend fun create(): AppDatabase {
        val databaseDirectory = File(System.getProperty("user.home"), ".user-list-kmp")
        databaseDirectory.mkdirs()

        val databaseFile = File(databaseDirectory, "users.db")
        val driver = JdbcSqliteDriver(
            url = "jdbc:sqlite:${databaseFile.absolutePath}",
            properties = Properties(),
            schema = AppDatabase.Schema.synchronous(),
        )
        return AppDatabase(driver)
    }
}

actual fun createPlatformHttpEngine(): HttpClientEngineFactory<*> = Java
