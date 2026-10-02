package com.siprikorea.catholicchant

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val media = webPlatformMedia()
    ComposeViewport(document.body!!) { App(media) }
    document.getElementById("loading")?.remove()
}
