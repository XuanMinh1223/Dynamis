package org.xuan.dynamis.di

import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

/** Initialize once per application process, independently of UI composition. */
fun initKoin(): Koin =
    KoinPlatform.getKoinOrNull() ?: startKoin {
        modules(appModule)
    }.koin
