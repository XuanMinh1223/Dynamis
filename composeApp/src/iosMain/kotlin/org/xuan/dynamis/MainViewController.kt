package org.xuan.dynamis

import androidx.compose.ui.window.ComposeUIViewController
import org.xuan.dynamis.di.initKoin

fun MainViewController() = run {
    initKoin()
    ComposeUIViewController { App() }
}
