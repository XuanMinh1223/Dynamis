package org.xuan.dynamis

import android.app.Application
import android.content.pm.ApplicationInfo
import org.xuan.dynamis.di.initKoin
import org.xuan.dynamis.logging.configureLogging

class DynamisApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        configureLogging(isDebug = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0)
        initKoin()
    }
}
