package com.siprikorea.catholicchant.media

import kotlinx.coroutines.flow.StateFlow

/** 악보 이미지를 플랫폼별 저장소(Android assets / iOS 번들 / 웹 원격 URL)에서 읽는다. */
interface SheetSource {
    /** `sheet/NNN.jpg` 의 바이트. 없거나 실패하면 null. */
    suspend fun load(fileNo: String): ByteArray?
}

data class PlayerState(
    val songNo: Int? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val error: String? = null,
)

/** 성가 음원 플레이어. `mp3/NNN.mp3` 를 재생한다. */
interface ChantPlayer {
    val state: StateFlow<PlayerState>
    fun play(songNo: Int, fileNo: String)
    fun pause()
    fun resume()
    fun seekTo(positionMs: Long)
    fun stop()
    fun release()
}

/** 플랫폼 진입점이 주입하는 구현체 묶음 */
class PlatformMedia(
    val sheetSource: SheetSource,
    val createPlayer: () -> ChantPlayer,
)
