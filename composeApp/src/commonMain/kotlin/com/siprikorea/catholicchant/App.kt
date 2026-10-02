package com.siprikorea.catholicchant

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siprikorea.catholicchant.data.Song
import com.siprikorea.catholicchant.media.PlatformMedia
import com.siprikorea.catholicchant.ui.EmptyDetail
import com.siprikorea.catholicchant.ui.MiniPlayer
import com.siprikorea.catholicchant.ui.SongDetailPane
import com.siprikorea.catholicchant.ui.SongListPane
import com.siprikorea.catholicchant.ui.theme.ChantTheme

@Composable
fun App(media: PlatformMedia) {
    val viewModel = viewModel { ChantViewModel(media.createPlayer()) }
    ChantTheme {
        ChantScreen(viewModel, media)
    }
}

@Composable
private fun ChantScreen(viewModel: ChantViewModel, media: PlatformMedia) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val songCount by viewModel.songCount.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current

    val playingSong = playerState.songNo?.let { viewModel.songByNo(it) }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            val wide = maxWidth >= 840.dp

            @Composable
            fun ListPane(modifier: Modifier) = SongListPane(
                query = query,
                onQueryChange = viewModel::onQueryChange,
                categories = categories,
                category = category,
                onCategoryChange = viewModel::onCategoryChange,
                results = results,
                songCount = songCount,
                isLoading = isLoading,
                selectedNo = if (wide) selected?.no else null,
                playingNo = playerState.songNo,
                isPlaying = playerState.isPlaying,
                listState = listState,
                autoFocus = wide,
                onSongClick = { song ->
                    if (!wide) keyboard?.hide()
                    viewModel.select(song)
                },
                onPlayClick = viewModel::togglePlay,
                modifier = modifier,
            )

            @Composable
            fun DetailPane(song: Song, modifier: Modifier) = SongDetailPane(
                song = song,
                sheetSource = media.sheetSource,
                playerState = playerState,
                onBack = if (wide) null else ({ viewModel.select(null) }),
                onTogglePlay = { viewModel.togglePlay(song) },
                onPrev = viewModel.adjacent(song, -1)?.let { prev -> { viewModel.select(prev) } },
                onNext = viewModel.adjacent(song, 1)?.let { next -> { viewModel.select(next) } },
                modifier = modifier,
            )

            @Composable
            fun PlayerBar() = AnimatedVisibility(
                visible = playingSong != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                playingSong?.let { song ->
                    MiniPlayer(
                        song = song,
                        state = playerState,
                        onOpen = { viewModel.select(song) },
                        onTogglePlay = { viewModel.togglePlay(song) },
                        onSeek = viewModel::seekTo,
                        onClose = viewModel::stop,
                    )
                }
            }

            if (wide) {
                // 넓은 화면: 재생바는 오른쪽 악보 영역 아래
                Row(Modifier.fillMaxSize()) {
                    ListPane(Modifier.width(440.dp).fillMaxHeight())
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        AnimatedContent(
                            targetState = selected,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            contentKey = { it?.no },
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        ) { song ->
                            if (song == null) EmptyDetail() else DetailPane(song, Modifier.fillMaxSize())
                        }
                        PlayerBar()
                    }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = selected,
                        transitionSpec = {
                            val forward = targetState != null && initialState == null
                            val backward = targetState == null
                            when {
                                forward -> slideInHorizontally { it / 3 } + fadeIn() togetherWith fadeOut()
                                backward -> fadeIn() togetherWith slideOutHorizontally { it / 3 } + fadeOut()
                                else -> fadeIn() togetherWith fadeOut()
                            }
                        },
                        contentKey = { it?.no },
                        modifier = Modifier.weight(1f),
                    ) { song ->
                        if (song == null) ListPane(Modifier.fillMaxSize()) else DetailPane(song, Modifier.fillMaxSize())
                    }
                    PlayerBar()
                }
                PlatformBackHandler(enabled = selected != null) { viewModel.select(null) }
            }
        }
    }
}
