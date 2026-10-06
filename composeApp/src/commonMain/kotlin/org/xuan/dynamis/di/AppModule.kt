package org.xuan.dynamis.di

import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.MobileGeolocator
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.xuan.dynamis.data.location.CompassLocationProvider
import org.xuan.dynamis.data.repo.DefaultRadarRepository
import org.xuan.dynamis.data.repo.DefaultWeatherRepository
import org.xuan.dynamis.data.source.api.KtorRadarApi
import org.xuan.dynamis.data.source.api.KtorWeatherApi
import org.xuan.dynamis.data.source.api.RadarApi
import org.xuan.dynamis.data.source.api.WeatherApi
import org.xuan.dynamis.data.source.api.configureWeatherClient
import org.xuan.dynamis.domain.LocationProvider
import org.xuan.dynamis.domain.RadarRepository
import org.xuan.dynamis.domain.WeatherRepository
import org.xuan.dynamis.ui.screen.home.HomeViewModel
import org.xuan.dynamis.ui.screen.radar.RadarViewModel

val appModule =
    module {
        single { HttpClient { configureWeatherClient() } } onClose { it?.close() }
        single<WeatherApi> { KtorWeatherApi(get()) }
        single<RadarApi> { KtorRadarApi(get()) }
        single<RadarRepository> { DefaultRadarRepository(get()) }
        single<WeatherRepository> { DefaultWeatherRepository(get()) }
        single<Geolocator> { MobileGeolocator() } onClose { it?.stopTracking() }
        single<Geocoder> { Geocoder() }
        single<LocationProvider> { CompassLocationProvider(get(), get()) }
        viewModelOf(::HomeViewModel)
        viewModelOf(::RadarViewModel)
    }
