package com.siprikorea.catholicchant.data

import com.siprikorea.catholicchant.resources.Res
import kotlinx.serialization.json.Json

object SongRepository {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadSongs(): List<Song> {
        val bytes = Res.readBytes("files/songs.json")
        return json.decodeFromString<List<Song>>(bytes.decodeToString()).sortedBy { it.no }
    }
}
