package com.siprikorea.catholicchant.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siprikorea.catholicchant.data.Song
import com.siprikorea.catholicchant.search.SearchResult

@Composable
fun SongListPane(
    query: String,
    onQueryChange: (String) -> Unit,
    categories: List<String>,
    category: String?,
    onCategoryChange: (String?) -> Unit,
    results: List<SearchResult>,
    songCount: Int,
    isLoading: Boolean,
    selectedNo: Int?,
    playingNo: Int?,
    isPlaying: Boolean,
    listState: LazyListState,
    autoFocus: Boolean,
    onSongClick: (Song) -> Unit,
    onPlayClick: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Header(songCount)
        SearchField(query, onQueryChange, autoFocus, Modifier.padding(horizontal = 16.dp))
        CategoryChips(categories, category, onCategoryChange)

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            results.isEmpty() -> EmptyResult(query)
            else -> {
                if (query.isNotBlank() || category != null) {
                    Text(
                        text = "${results.size}곡",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp, bottom = 4.dp),
                    )
                }
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(results, key = { it.song.no }) { result ->
                        SongRow(
                            result = result,
                            selected = result.song.no == selectedNo,
                            current = result.song.no == playingNo,
                            playing = isPlaying && result.song.no == playingNo,
                            onClick = { onSongClick(result.song) },
                            onPlayClick = { onPlayClick(result.song) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(songCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CrossLogo()
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                "가톨릭 성가",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                if (songCount > 0) "${songCount}곡 · 악보 보기와 음원 듣기" else "악보 보기와 음원 듣기",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, autoFocus: Boolean, modifier: Modifier) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    if (autoFocus) {
        LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }
    }
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.surfaceContainerLowest,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, colors.outlineVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp, end = 4.dp)) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = colors.primary)
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                modifier = Modifier.weight(1f).padding(vertical = 16.dp).focusRequester(focusRequester),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                "번호, 제목, 초성으로 검색",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                            )
                        }
                        inner()
                    }
                },
            )
            AnimatedVisibility(query.isNotEmpty(), enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = "검색어 지우기", tint = colors.onSurfaceVariant)
                }
            }
            if (query.isEmpty()) Spacer(Modifier.width(12.dp))
        }
    }
}

@Composable
private fun CategoryChips(categories: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { Chip("전체", selected == null) { onSelect(null) } }
        items(categories) { category ->
            Chip(category, selected == category) { onSelect(if (selected == category) null else category) }
        }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
        border = if (selected) null else FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}

@Composable
private fun SongRow(
    result: SearchResult,
    selected: Boolean,
    current: Boolean,
    playing: Boolean,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val song = result.song
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) colors.primaryContainer else colors.surfaceContainerLowest,
        border = BorderStroke(1.dp, if (selected) colors.primary.copy(alpha = 0.25f) else colors.outlineVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            NumberBadge(song.no, active = current, playing = playing)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = highlighted(song.displayTitle, result.titleRanges),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (song.category.isNotBlank()) {
                        CategoryLabel(song.category)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = highlighted(song.firstLine, result.firstLineRanges),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            if (song.hasAudio) {
                FilledTonalIconButton(
                    onClick = onPlayClick,
                    colors = if (current) {
                        IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                        )
                    } else {
                        IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = colors.primaryContainer,
                            contentColor = colors.primary,
                        )
                    },
                ) {
                    Icon(
                        if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (playing) "일시정지" else "재생",
                    )
                }
            } else {
                Icon(
                    Icons.Rounded.MusicOff,
                    contentDescription = "음원 없음",
                    tint = colors.outline.copy(alpha = 0.5f),
                    modifier = Modifier.padding(12.dp).size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptyResult(query: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            if (query.isBlank()) Icons.AutoMirrored.Rounded.QueueMusic else Icons.Rounded.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.padding(6.dp))
        Text(
            if (query.isBlank()) "곡이 없습니다" else "‘$query’에 맞는 성가가 없습니다",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            "번호(예: 2), 제목(예: 주 하느님), 초성(예: ㅈㅎㄴㄴ)으로 찾아보세요",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
