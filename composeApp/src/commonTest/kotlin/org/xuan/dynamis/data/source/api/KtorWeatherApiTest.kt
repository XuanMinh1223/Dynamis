package org.xuan.dynamis.data.source.api

import org.xuan.dynamis.data.repo.DefaultWeatherRepository
import org.xuan.dynamis.data.repo.InvalidForecastException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KtorWeatherApiTest {
    @Test
    fun sendsCoordinatesAndOnlyRequestedForecastFields() = runTest {
        val engine = MockEngine { request ->
            assertEquals("https", request.url.protocol.name)
            assertEquals("api.open-meteo.com", request.url.host)
            assertEquals("/v1/forecast", request.url.encodedPath)
            assertEquals("37.7", request.url.parameters["latitude"])
            assertEquals("-122.4", request.url.parameters["longitude"])
            assertEquals("auto", request.url.parameters["timezone"])
            assertEquals("celsius", request.url.parameters["temperature_unit"])
            assertEquals(ApiConstants.ParameterValues.CURRENT_VALUES, request.url.parameters["current"])
            assertEquals(ApiConstants.ParameterValues.HOURLY_VALUES, request.url.parameters["hourly"])
            assertEquals(ApiConstants.ParameterValues.DAILY_VALUES, request.url.parameters["daily"])
            respond(validForecast, headers = jsonHeaders)
        }
        val client = HttpClient(engine) { configureWeatherClient() }
        try {
            val weather = DefaultWeatherRepository(KtorWeatherApi(client)).getWeather(37.7, -122.4)
            assertEquals(18.4, weather.temperature)
            assertEquals(22.0, weather.todayHigh)
        } finally {
            client.close()
        }
    }

    @Test
    fun rejectsHttpErrorInsteadOfDeserializingAnEmptyForecast() = runTest {
        val client = HttpClient(MockEngine {
            respond("""{"error":true,"reason":"Invalid request"}""", HttpStatusCode.BadRequest, jsonHeaders)
        }) { configureWeatherClient() }
        try {
            assertFailsWith<ClientRequestException> { KtorWeatherApi(client).getWeather(37.7, -122.4) }
        } finally {
            client.close()
        }
    }

    @Test
    fun rejectsIncompleteSuccessPayloadAtRepositoryBoundary() = runTest {
        val client = HttpClient(MockEngine { respond("{}", headers = jsonHeaders) }) {
            configureWeatherClient()
        }
        try {
            assertFailsWith<InvalidForecastException> {
                DefaultWeatherRepository(KtorWeatherApi(client)).getWeather(37.7, -122.4)
            }
        } finally {
            client.close()
        }
    }

    @Test
    fun callerCancellationCancelsTheHttpRequest() = runTest {
        val started = CompletableDeferred<Unit>()
        val cancelled = CompletableDeferred<Unit>()
        val client = HttpClient(MockEngine {
            started.complete(Unit)
            try {
                awaitCancellation()
            } finally {
                cancelled.complete(Unit)
            }
        }) { configureWeatherClient() }
        try {
            val request = launch { KtorWeatherApi(client).getWeather(37.7, -122.4) }
            started.await()
            request.cancel(CancellationException("Leaving screen"))
            request.join()
            cancelled.await()
            assertTrue(request.isCancelled)
        } finally {
            client.close()
        }
    }

    private companion object {
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
        val validForecast = """
            {
              "timezone": "America/Los_Angeles",
              "current_units": {"temperature_2m": "°C"},
              "current": {"time": "2026-09-30T23:45", "temperature_2m": 18.4, "weather_code": 3},
              "daily": {"time": ["2026-09-30"], "temperature_2m_max": [22.0], "temperature_2m_min": [12.0]},
              "ignored_future_field": true
            }
        """.trimIndent()
    }
}
