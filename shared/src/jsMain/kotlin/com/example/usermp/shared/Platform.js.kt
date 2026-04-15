@file:Suppress("UnsafeCastFromDynamic")

package com.example.usermp.shared

import app.cash.sqldelight.async.coroutines.await
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import com.example.usermp.db.AppDatabase
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js
import org.w3c.dom.Worker

actual class PlatformConfiguration

actual class DatabaseFactory actual constructor(
    private val platformConfiguration: PlatformConfiguration,
) {
    actual suspend fun create(): AppDatabase {
        val driver = WebWorkerDriver(createSqlWorker())
        AppDatabase.Schema.create(driver).await()
        return AppDatabase(driver)
    }
}

actual fun createPlatformHttpEngine(): HttpClientEngineFactory<*> = Js

private fun createSqlWorker(): Worker {
    return js(
        "new Worker(new URL('@cashapp/sqldelight-sqljs-worker/sqljs.worker.js', import.meta.url))",
    ) as Worker
}
