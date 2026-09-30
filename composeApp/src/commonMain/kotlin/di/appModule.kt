package di

import data.repo.DefaultWeatherRepository
import data.source.api.KtorWeatherApi
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.MobileGeolocator
import data.source.api.WeatherApi
import domain.WeatherRepository
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.dsl.module
import ui.screen.home.HomeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun appModule() : List<Module> = listOf(providesViewModel, providesHttpClient, providesApiService, providesRepository, providesGeoLocator)

val providesViewModel = module {
    viewModelOf(::HomeViewModel)
}

val providesHttpClient = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }
}

val providesApiService = module {
    single<WeatherApi> { KtorWeatherApi(get()) }
}

val providesRepository = module {
    single<WeatherRepository> { DefaultWeatherRepository(get()) }
}

val providesGeoLocator = module {
    single<Geolocator> { MobileGeolocator() }
    single<Geocoder> { Geocoder() }
}
