package org.xuan.dynamis.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.xuan.dynamis.domain.LocationProvider
import org.xuan.dynamis.domain.WeatherRepository
import org.xuan.dynamis.domain.model.GeoCoordinates
import org.xuan.dynamis.domain.model.WeatherForecast
import org.xuan.dynamis.ui.screen.home.HomeViewModel
import kotlinx.datetime.LocalDateTime
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InitKoinTest {
    @Before
    fun setup() {
        stopKoin()
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun teardown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun repeatedStartupReusesApplicationAndHttpClient() {
        val first = initKoin()
        val client = first.get<HttpClient>()
        assertSame(first, initKoin())
        assertSame(client, initKoin().get<HttpClient>())
    }

    @Test
    fun recreatingScreenDoesNotCloseApplicationDependencies() {
        val koin = initKoin()
        koin.loadModules(listOf(module {
            single<LocationProvider> {
                object : LocationProvider {
                    override suspend fun currentLocation() = GeoCoordinates(37.7, -122.4)
                    override suspend fun locality(coordinates: GeoCoordinates): String? = null
                }
            }
            single<WeatherRepository> {
                object : WeatherRepository {
                    override suspend fun getWeather(latitude: Double, longitude: Double) = WeatherForecast(
                        LocalDateTime(2026, 9, 30, 12, 0), "America/Los_Angeles", 18.0, "°C", 0, 22.0, 12.0,
                    )
                }
            }
        }))
        val client = koin.get<HttpClient>()
        val store = ViewModelStore()
        val provider = ViewModelProvider(store, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = koin.get<HomeViewModel>() as T
        })
        val retainedViewModel = provider[HomeViewModel::class.java]
        assertSame(retainedViewModel, provider[HomeViewModel::class.java])
        store.clear()
        assertSame(koin, initKoin())
        assertSame(client, initKoin().get<HttpClient>())
        assertTrue(client.coroutineContext[Job]!!.isActive)
    }
}
