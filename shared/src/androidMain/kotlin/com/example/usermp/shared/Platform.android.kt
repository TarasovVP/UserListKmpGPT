package com.example.usermp.shared

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.usermp.db.AppDatabase
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

actual class PlatformConfiguration(val context: Context)

actual class DatabaseFactory actual constructor(
    private val platformConfiguration: PlatformConfiguration,
) {
    actual suspend fun create(): AppDatabase {
        val driver = AndroidSqliteDriver(
            AppDatabase.Schema.synchronous(),
            platformConfiguration.context,
            "users.db",
        )
        return AppDatabase(driver)
    }
}

actual fun createPlatformHttpEngine(): HttpClientEngineFactory<*> = OkHttp
