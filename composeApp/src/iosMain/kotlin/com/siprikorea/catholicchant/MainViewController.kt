package com.siprikorea.catholicchant

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused")
fun MainViewController(): UIViewController {
    val media = iosPlatformMedia()
    return ComposeUIViewController { App(media) }
}
