package com.siprikorea.catholicchant.data

import kotlinx.serialization.Serializable

@Serializable
data class Song(
    val no: Int,
    val title: String,
    val category: String = "",
    val type: String = "",
    val composer: String = "",
    val firstLine: String = "",
    val hasSheet: Boolean = false,
    val hasAudio: Boolean = false,
) {
    /** 미디어 파일명에 쓰이는 0 패딩 번호 (예: 2 → "002") */
    val fileNo: String get() = no.toString().padStart(3, '0')

    val displayTitle: String get() = title.ifBlank { "${no}번 성가" }
}
