package com.siprikorea.catholicchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siprikorea.catholicchant.data.Song
import com.siprikorea.catholicchant.data.SongRepository
import com.siprikorea.catholicchant.media.ChantPlayer
import com.siprikorea.catholicchant.media.PlayerState
import com.siprikorea.catholicchant.search.SearchResult
import com.siprikorea.catholicchant.search.SongSearcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChantViewModel(private val player: ChantPlayer) : ViewModel() {
    private val songs = MutableStateFlow<List<Song>>(emptyList())
    private val searcher = songs.map { SongSearcher(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SongSearcher(emptyList()))

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _category = MutableStateFlow<String?>(null)
    val category: StateFlow<String?> = _category.asStateFlow()

    private val _selected = MutableStateFlow<Song?>(null)
    val selected: StateFlow<Song?> = _selected.asStateFlow()

    val isLoading: StateFlow<Boolean> = songs.map { it.isEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /** 전례 분류 목록 (곡 수가 많은 순) */
    val categories: StateFlow<List<String>> = songs.map { list ->
        list.map { it.category }.filter { it.isNotBlank() }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }.map { it.key }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** 입력할 때마다 즉시 다시 계산되는 검색 결과 */
    val results: StateFlow<List<SearchResult>> = combine(searcher, _query, _category) { searcher, query, category ->
        searcher.search(query).filter { category == null || it.song.category == category }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val playerState: StateFlow<PlayerState> = player.state

    val songCount: StateFlow<Int> = songs.map { it.size }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        viewModelScope.launch { songs.value = SongRepository.loadSongs() }
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onCategoryChange(value: String?) {
        _category.value = value
    }

    fun select(song: Song?) {
        _selected.value = song
    }

    fun songByNo(no: Int): Song? = songs.value.firstOrNull { it.no == no }

    /** 현재 선택곡 기준 이전/다음 번호의 곡 */
    fun adjacent(song: Song, step: Int): Song? {
        val list = songs.value
        val index = list.indexOfFirst { it.no == song.no }
        return list.getOrNull(index + step)
    }

    fun togglePlay(song: Song) {
        if (!song.hasAudio) return
        val state = player.state.value
        when {
            state.songNo != song.no -> player.play(song.no, song.fileNo)
            state.isPlaying -> player.pause()
            else -> player.resume()
        }
    }

    fun pause() = player.pause()
    fun resume() = player.resume()
    fun seekTo(positionMs: Long) = player.seekTo(positionMs)
    fun stop() = player.stop()

    override fun onCleared() {
        player.release()
    }
}
