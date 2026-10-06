package org.xuan.dynamis

import androidx.compose.ui.window.ComposeUIViewController
import org.xuan.dynamis.di.initKoin
import org.xuan.dynamis.logging.configureLogging
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalNativeApi::class)
@Suppress("ktlint:standard:function-naming") // Called by name from Swift.
fun MainViewController() =
    run {
        configureLogging(isDebug = Platform.isDebugBinary)
        initKoin()
        ComposeUIViewController { App() }
    }
