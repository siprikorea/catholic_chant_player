package com.siprikorea.catholicchant

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.siprikorea.catholicchant.media.ChantPlayer
import com.siprikorea.catholicchant.media.PlatformMedia
import com.siprikorea.catholicchant.media.PlayerState
import com.siprikorea.catholicchant.media.SheetSource
import java.io.File
import java.io.FileNotFoundException
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

fun androidPlatformMedia(context: Context): PlatformMedia {
    val appContext = context.applicationContext
    return PlatformMedia(
        sheetSource = AssetSheetSource(appContext),
        createPlayer = { AndroidChantPlayer(appContext) },
    )
}

/** 디버그는 앱 assets, 릴리스는 install-time asset pack — 둘 다 AssetManager 로 접근한다. */
private class AssetSheetSource(private val context: Context) : SheetSource {
    override suspend fun load(fileNo: String): ByteArray? = withContext(Dispatchers.IO) {
        runCatching { context.assets.open("sheet/$fileNo.jpg").use { it.readBytes() } }.getOrNull()
    }
}

private class AndroidChantPlayer(private val context: Context) : ChantPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var player: MediaPlayer? = null
    private var ticker: Job? = null
    private var loadJob: Job? = null

    override fun play(songNo: Int, fileNo: String) {
        releasePlayer()
        _state.value = PlayerState(songNo = songNo, isLoading = true)
        loadJob = scope.launch {
            val mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
            }
            try {
                setDataSource(mediaPlayer, fileNo)
            } catch (e: Exception) {
                mediaPlayer.release()
                _state.value = PlayerState(songNo = songNo, error = "음원을 찾을 수 없습니다")
                return@launch
            }
            player = mediaPlayer
            mediaPlayer.setOnPreparedListener { mp ->
                mp.start()
                _state.update { it.copy(isLoading = false, isPlaying = true, durationMs = mp.duration.toLong()) }
                startTicker()
            }
            mediaPlayer.setOnCompletionListener { mp ->
                ticker?.cancel()
                mp.seekTo(0)
                _state.update { it.copy(isPlaying = false, positionMs = 0) }
            }
            mediaPlayer.setOnErrorListener { _, _, _ ->
                ticker?.cancel()
                _state.update { it.copy(isPlaying = false, isLoading = false, error = "재생할 수 없습니다") }
                true
            }
            mediaPlayer.prepareAsync()
        }
    }

    /**
     * 비압축 asset 은 파일 디스크립터로 바로 재생한다. asset pack 등에서 압축되어
     * openFd 가 실패하면 캐시 파일로 복사해 재생한다.
     */
    private suspend fun setDataSource(mediaPlayer: MediaPlayer, fileNo: String) {
        val assetPath = "mp3/$fileNo.mp3"
        try {
            context.assets.openFd(assetPath).use { afd ->
                mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            }
        } catch (e: FileNotFoundException) {
            val cached = withContext(Dispatchers.IO) {
                File(context.cacheDir, assetPath).also { file ->
                    if (!file.exists()) {
                        file.parentFile?.mkdirs()
                        context.assets.open(assetPath).use { input -> file.outputStream().use { input.copyTo(it) } }
                    }
                }
            }
            mediaPlayer.setDataSource(cached.path)
        }
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                player?.let { mp -> _state.update { it.copy(positionMs = mp.currentPosition.toLong()) } }
                delay(250)
            }
        }
    }

    override fun pause() {
        val mp = player ?: return
        if (mp.isPlaying) mp.pause()
        ticker?.cancel()
        _state.update { it.copy(isPlaying = false, positionMs = mp.currentPosition.toLong()) }
    }

    override fun resume() {
        val mp = player ?: return
        if (_state.value.isLoading) return
        mp.start()
        _state.update { it.copy(isPlaying = true, error = null) }
        startTicker()
    }

    override fun seekTo(positionMs: Long) {
        val mp = player ?: return
        mp.seekTo(positionMs.toInt())
        _state.update { it.copy(positionMs = positionMs) }
    }

    override fun stop() {
        releasePlayer()
        _state.value = PlayerState()
    }

    private fun releasePlayer() {
        loadJob?.cancel()
        ticker?.cancel()
        player?.release()
        player = null
    }

    override fun release() {
        releasePlayer()
        scope.cancel()
    }
}
