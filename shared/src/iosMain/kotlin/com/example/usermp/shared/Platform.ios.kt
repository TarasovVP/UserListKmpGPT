package com.example.usermp.shared

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.usermp.db.AppDatabase
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual class PlatformConfiguration

actual class DatabaseFactory actual constructor(
    private val platformConfiguration: PlatformConfiguration,
) {
    actual suspend fun create(): AppDatabase {
        val driver = NativeSqliteDriver(
            schema = AppDatabase.Schema.synchronous(),
            name = "users.db",
        )
        return AppDatabase(driver)
    }
}

actual fun createPlatformHttpEngine(): HttpClientEngineFactory<*> = Darwin
