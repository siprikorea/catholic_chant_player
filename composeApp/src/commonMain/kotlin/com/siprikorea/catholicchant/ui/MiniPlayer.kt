package com.siprikorea.catholicchant.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.siprikorea.catholicchant.data.Song
import com.siprikorea.catholicchant.media.PlayerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniPlayer(
    song: Song,
    state: PlayerState,
    onOpen: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    var dragPosition by remember(song.no) { mutableStateOf<Float?>(null) }
    val duration = state.durationMs.coerceAtLeast(1).toFloat()
    val position = dragPosition ?: state.positionMs.toFloat().coerceIn(0f, duration)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.surfaceContainerLowest,
        shadowElevation = 6.dp,
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Column(Modifier.padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.weight(1f).clickable(onClick = onOpen),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NumberBadge(song.no, size = 40.dp, active = true, playing = state.isPlaying)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            song.displayTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = colors.onSurface,
                        )
                        Text(
                            state.error ?: "${song.no}번 · ${formatTime(position.toLong())} / ${formatTime(state.durationMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.error != null) colors.error else colors.onSurfaceVariant,
                        )
                    }
                }
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.padding(10.dp).size(28.dp), strokeWidth = 3.dp)
                } else {
                    FilledIconButton(onClick = onTogglePlay, enabled = state.error == null) {
                        Icon(
                            if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (state.isPlaying) "일시정지" else "재생",
                        )
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.Close, contentDescription = "닫기", tint = colors.onSurfaceVariant)
                }
            }
            Slider(
                value = position,
                valueRange = 0f..duration,
                enabled = state.durationMs > 0,
                onValueChange = { dragPosition = it },
                onValueChangeFinished = {
                    dragPosition?.let { onSeek(it.toLong()) }
                    dragPosition = null
                },
                colors = SliderDefaults.colors(
                    thumbColor = colors.primary,
                    activeTrackColor = colors.primary,
                    inactiveTrackColor = colors.outlineVariant,
                ),
                thumb = {
                    Surface(shape = RoundedCornerShape(50), color = colors.primary, modifier = Modifier.size(14.dp)) {}
                },
                track = { sliderState ->
                    SliderDefaults.Track(
                        sliderState = sliderState,
                        modifier = Modifier.height(4.dp),
                        thumbTrackGapSize = 0.dp,
                        drawStopIndicator = null,
                        colors = SliderDefaults.colors(
                            activeTrackColor = colors.primary,
                            inactiveTrackColor = colors.outlineVariant,
                        ),
                    )
                },
                modifier = Modifier.padding(horizontal = 4.dp).height(24.dp),
            )
        }
    }
}
