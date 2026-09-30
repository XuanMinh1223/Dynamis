package di

import data.repo.DefaultWeatherRepository
import data.location.CompassLocationProvider
import data.source.api.KtorWeatherApi
import data.source.api.configureWeatherClient
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.MobileGeolocator
import data.source.api.WeatherApi
import domain.WeatherRepository
import domain.LocationProvider
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.dsl.module
import ui.screen.home.HomeViewModel
import io.ktor.client.HttpClient
import org.koin.dsl.onClose

fun appModule() : List<Module> = listOf(providesViewModel, providesHttpClient, providesApiService, providesRepository, providesGeoLocator)

val providesViewModel = module {
    viewModelOf(::HomeViewModel)
}

val providesHttpClient = module {
    single {
        HttpClient { configureWeatherClient() }
    } onClose { client -> client?.close() }
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
    single<LocationProvider> { CompassLocationProvider(get(), get()) }
}
