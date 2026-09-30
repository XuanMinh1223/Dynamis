package org.xuan.dynamis

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import org.xuan.dynamis.di.appModule
import org.koin.compose.KoinApplication
import org.xuan.dynamis.ui.DynamisApp
import org.xuan.dynamis.ui.theme.DynamisTheme

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        DynamisTheme {
            Surface(color = MaterialTheme.colorScheme.background) {
                DynamisApp()
            }
        }
    }
}
