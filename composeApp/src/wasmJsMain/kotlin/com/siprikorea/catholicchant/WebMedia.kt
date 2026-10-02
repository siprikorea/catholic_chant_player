package com.siprikorea.catholicchant

import com.siprikorea.catholicchant.media.ChantPlayer
import com.siprikorea.catholicchant.media.PlatformMedia
import com.siprikorea.catholicchant.media.PlayerState
import com.siprikorea.catholicchant.media.SheetSource
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLAudioElement
import org.w3c.fetch.Response

/**
 * index.html 의 window.CHANT_MEDIA_URL 템플릿으로 미디어 URL 을 만든다.
 * `{path}` 는 URL 인코딩된 경로(Firebase Storage 용, sheet%2F002.jpg), `{rawPath}` 는 그대로의 경로.
 */
private fun mediaUrlTemplate(): String = js("window.CHANT_MEDIA_URL || 'media/{rawPath}'")

private fun encodeUriComponent(value: String): String = js("encodeURIComponent(value)")

private class MediaUrls(private val template: String) {
    fun of(path: String): String =
        template.replace("{path}", encodeUriComponent(path)).replace("{rawPath}", path)
}

fun webPlatformMedia(): PlatformMedia {
    val urls = MediaUrls(mediaUrlTemplate())
    return PlatformMedia(
        sheetSource = RemoteSheetSource(urls),
        createPlayer = { WebChantPlayer(urls) },
    )
}

private class RemoteSheetSource(private val urls: MediaUrls) : SheetSource {
    override suspend fun load(fileNo: String): ByteArray? = try {
        val response = window.fetch(urls.of("sheet/$fileNo.jpg")).await<Response>()
        if (!response.ok) null else Int8Array(response.arrayBuffer().await<ArrayBuffer>()).toByteArray()
    } catch (e: Throwable) {
        null
    }

    private fun Int8Array.toByteArray(): ByteArray = ByteArray(length) { this[it] }
}

private class WebChantPlayer(private val urls: MediaUrls) : ChantPlayer {
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val audio = (document.createElement("audio") as HTMLAudioElement).apply { preload = "auto" }

    init {
        listOf("timeupdate", "durationchange", "playing", "pause", "seeked").forEach { event ->
            audio.addEventListener(event) { sync() }
        }
        audio.addEventListener("waiting") { _state.update { it.copy(isLoading = true) } }
        audio.addEventListener("ended") {
            audio.currentTime = 0.0
            _state.update { it.copy(isPlaying = false, positionMs = 0) }
        }
        audio.addEventListener("error") {
            if (_state.value.songNo != null) {
                _state.update { it.copy(isPlaying = false, isLoading = false, error = "음원을 불러올 수 없습니다") }
            }
        }
    }

    private fun sync() {
        val duration = audio.duration
        _state.update {
            it.copy(
                isPlaying = !audio.paused,
                isLoading = audio.readyState < 3 && !audio.paused,
                positionMs = (audio.currentTime * 1000).toLong(),
                durationMs = if (duration.isFinite()) (duration * 1000).toLong() else 0,
            )
        }
    }

    override fun play(songNo: Int, fileNo: String) {
        _state.value = PlayerState(songNo = songNo, isLoading = true)
        audio.src = urls.of("mp3/$fileNo.mp3")
        audio.play()
    }

    override fun pause() = audio.pause()

    override fun resume() {
        audio.play()
    }

    override fun seekTo(positionMs: Long) {
        audio.currentTime = positionMs / 1000.0
    }

    override fun stop() {
        audio.pause()
        audio.removeAttribute("src")
        audio.load()
        _state.value = PlayerState()
    }

    override fun release() = stop()
}
