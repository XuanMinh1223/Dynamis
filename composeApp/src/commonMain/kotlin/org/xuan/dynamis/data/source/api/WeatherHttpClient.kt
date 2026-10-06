package org.xuan.dynamis.data.source.api

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.logging.Logger as KtorLogger

private val httpLog = Logger.withTag("Http")

fun HttpClientConfig<*>.configureWeatherClient() {
    expectSuccess = true
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 20_000
    }
    install(Logging) {
        // Request lines and status codes only; Kermit's minimum severity decides whether they print.
        level = LogLevel.INFO
        logger =
            object : KtorLogger {
                override fun log(message: String) = httpLog.d { message }
            }
    }
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}
