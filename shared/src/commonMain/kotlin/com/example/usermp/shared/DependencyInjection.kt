package com.example.usermp.shared

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect class PlatformConfiguration

expect class DatabaseFactory(platformConfiguration: PlatformConfiguration) {
    suspend fun create(): com.example.usermp.db.AppDatabase
}

expect fun createPlatformHttpEngine(): HttpClientEngineFactory<*>

private var koinApplication: KoinApplication? = null

@PublishedApi
internal fun getSharedKoin(): Koin = requireNotNull(koinApplication) {
    "Koin has not been initialized yet."
}.koin

inline fun <reified T : Any> getSharedDependency(): T = getSharedKoin().get()

fun sharedModule(platformConfiguration: PlatformConfiguration): Module = module {
    single { platformConfiguration }
    single<CoroutineDispatcher> { Dispatchers.Default }
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }
    single { createHttpClient(get()) }
    single { DatabaseFactory(get()) }
    single { DatabaseProvider(get()) }
    single<UsersApi> { DummyJsonUsersApi(get()) }
    single<UserLocalDataSource> { SqlDelightUserLocalDataSource(get(), get()) }
    single<UsersRepository> { DefaultUsersRepository(get(), get()) }
    factory { UsersPresenter(get(), get()) }
}

fun initKoin(
    platformConfiguration: PlatformConfiguration,
    appDeclaration: KoinAppDeclaration? = null,
): Unit {
    if (koinApplication != null) return

    koinApplication = startKoin {
        appDeclaration?.invoke(this)
        modules(sharedModule(platformConfiguration))
    }
}

private fun createHttpClient(json: Json): HttpClient {
    return HttpClient(createPlatformHttpEngine()) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            level = LogLevel.NONE
        }
    }
}
