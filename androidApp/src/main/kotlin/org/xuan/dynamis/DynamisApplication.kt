package org.xuan.dynamis

import android.app.Application
import org.xuan.dynamis.di.initKoin

class DynamisApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
