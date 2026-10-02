package com.siprikorea.catholicchant

import com.siprikorea.catholicchant.media.ChantPlayer
import com.siprikorea.catholicchant.media.PlatformMedia
import com.siprikorea.catholicchant.media.PlayerState
import com.siprikorea.catholicchant.media.SheetSource
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy

fun iosPlatformMedia(): PlatformMedia = PlatformMedia(
    sheetSource = BundleSheetSource,
    createPlayer = { IosChantPlayer() },
)

/** 앱 번들에 복사된 media/sheet, media/mp3 폴더 */
private object BundleSheetSource : SheetSource {
    override suspend fun load(fileNo: String): ByteArray? = withContext(Dispatchers.Default) {
        val path = NSBundle.mainBundle.pathForResource(fileNo, "jpg", "sheet") ?: return@withContext null
        NSData.dataWithContentsOfFile(path)?.toByteArray()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val result = ByteArray(size)
    if (size > 0) result.usePinned { memcpy(it.addressOf(0), bytes, length) }
    return result
}

@OptIn(ExperimentalForeignApi::class)
private class IosChantPlayer : ChantPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var player: AVAudioPlayer? = null
    private var ticker: Job? = null

    override fun play(songNo: Int, fileNo: String) {
        releasePlayer()
        val url = NSBundle.mainBundle.URLForResource(fileNo, "mp3", "mp3")
        val audio = url?.let { AVAudioPlayer(contentsOfURL = it, error = null) }
        if (audio == null) {
            _state.value = PlayerState(songNo = songNo, error = "음원을 찾을 수 없습니다")
            return
        }
        // 무음 모드에서도 재생되도록 playback 카테고리 사용
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, null)
        AVAudioSession.sharedInstance().setActive(true, null)
        audio.prepareToPlay()
        audio.play()
        player = audio
        _state.value = PlayerState(
            songNo = songNo,
            isPlaying = true,
            durationMs = (audio.duration * 1000).toLong(),
        )
        startTicker()
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val audio = player ?: break
                if (!audio.playing) {
                    // 끝까지 재생됨
                    audio.currentTime = 0.0
                    _state.update { it.copy(isPlaying = false, positionMs = 0) }
                    break
                }
                _state.update { it.copy(positionMs = (audio.currentTime * 1000).toLong()) }
                delay(250)
            }
        }
    }

    override fun pause() {
        val audio = player ?: return
        ticker?.cancel()
        audio.pause()
        _state.update { it.copy(isPlaying = false, positionMs = (audio.currentTime * 1000).toLong()) }
    }

    override fun resume() {
        val audio = player ?: return
        audio.play()
        _state.update { it.copy(isPlaying = true) }
        startTicker()
    }

    override fun seekTo(positionMs: Long) {
        val audio = player ?: return
        audio.currentTime = positionMs / 1000.0
        _state.update { it.copy(positionMs = positionMs) }
    }

    override fun stop() {
        releasePlayer()
        _state.value = PlayerState()
    }

    private fun releasePlayer() {
        ticker?.cancel()
        player?.stop()
        player = null
    }

    override fun release() {
        releasePlayer()
        scope.cancel()
    }
}
