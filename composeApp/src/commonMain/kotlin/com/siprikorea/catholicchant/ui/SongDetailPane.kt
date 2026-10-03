package com.siprikorea.catholicchant.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ImageNotSupported
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material.icons.rounded.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.siprikorea.catholicchant.data.Song
import com.siprikorea.catholicchant.media.PlayerState
import com.siprikorea.catholicchant.media.SheetSource
import org.jetbrains.compose.resources.decodeToImageBitmap

@Composable
fun SongDetailPane(
    song: Song,
    sheetSource: SheetSource,
    playerState: PlayerState,
    onBack: (() -> Unit)?,
    onTogglePlay: () -> Unit,
    onPrev: (() -> Unit)?,
    onNext: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val isCurrent = playerState.songNo == song.no
    val isPlaying = isCurrent && playerState.isPlaying
    Column(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "목록으로") }
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { onPrev?.invoke() }, enabled = onPrev != null) {
                Icon(Icons.Rounded.ChevronLeft, contentDescription = "이전 곡")
            }
            IconButton(onClick = { onNext?.invoke() }, enabled = onNext != null) {
                Icon(Icons.Rounded.ChevronRight, contentDescription = "다음 곡")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "${song.no}번",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    song.displayTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                MetaRow(listOf(song.category, song.type, song.composer))
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = onTogglePlay,
                enabled = song.hasAudio,
                shape = RoundedCornerShape(16.dp),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                if (isCurrent && playerState.isLoading) {
                    CircularProgressIndicator(
                        Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = null)
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    when {
                        !song.hasAudio -> "음원 없음"
                        isPlaying -> "일시정지"
                        else -> "듣기"
                    },
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White, // 악보는 흰 종이 이미지라 다크 모드에서도 흰 바탕을 유지
            shadowElevation = 1.dp,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
        ) {
            SheetView(song, sheetSource)
        }
    }
}

private sealed interface SheetLoad {
    data object Loading : SheetLoad
    data object Missing : SheetLoad
    data class Ready(val bitmap: ImageBitmap) : SheetLoad
}

@Composable
private fun SheetView(song: Song, sheetSource: SheetSource) {
    val load by produceState<SheetLoad>(SheetLoad.Loading, song.no) {
        value = SheetLoad.Loading
        value = if (!song.hasSheet) {
            SheetLoad.Missing
        } else {
            sheetSource.load(song.fileNo)
                ?.let { bytes -> runCatching { bytes.decodeToImageBitmap() }.getOrNull() }
                ?.let { SheetLoad.Ready(it) }
                ?: SheetLoad.Missing
        }
    }
    Crossfade(load) { state ->
        when (state) {
            SheetLoad.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            SheetLoad.Missing -> Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Rounded.ImageNotSupported, contentDescription = null, tint = Color(0xFFB9A898), modifier = Modifier.size(48.dp))
                Text("악보가 없습니다", color = Color(0xFF6B5D57), modifier = Modifier.padding(top = 8.dp))
            }
            is SheetLoad.Ready -> ZoomableImage(state.bitmap, "${song.no}번 ${song.displayTitle} 악보")
        }
    }
}

/** 핀치/더블탭/마우스 휠 확대와 드래그 이동을 지원하는 이미지 */
@Composable
private fun ZoomableImage(bitmap: ImageBitmap, contentDescription: String) {
    var scale by remember(bitmap) { mutableFloatStateOf(1f) }
    var offset by remember(bitmap) { mutableStateOf(Offset.Zero) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }

    fun clamp(value: Offset, s: Float): Offset {
        val maxX = (s - 1f) * viewport.width / 2f
        val maxY = (s - 1f) * viewport.height / 2f
        return Offset(value.x.coerceIn(-maxX, maxX), value.y.coerceIn(-maxY, maxY))
    }

    /** focus 지점을 고정한 채 배율 변경 */
    fun zoomTo(newScale: Float, focus: Offset = Offset(viewport.width / 2f, viewport.height / 2f)) {
        val s = newScale.coerceIn(1f, 5f)
        val center = Offset(viewport.width / 2f, viewport.height / 2f)
        val contentPoint = (focus - center - offset) / scale
        offset = clamp(focus - center - contentPoint * s, s)
        scale = s
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        viewport = IntSize(constraints.maxWidth, constraints.maxHeight)
        Box(
            Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(bitmap) {
                    detectTapGestures(onDoubleTap = { tap -> if (scale > 1.01f) zoomTo(1f) else zoomTo(2.5f, tap) })
                }
                .pointerInput(bitmap) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        zoomTo(scale * zoom, centroid)
                        offset = clamp(offset + pan, scale)
                    }
                }
                .pointerInput(bitmap) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Scroll) {
                                val change = event.changes.first()
                                val factor = if (change.scrollDelta.y < 0) 1.15f else 1f / 1.15f
                                zoomTo(scale * factor, change.position)
                                change.consume()
                            }
                        }
                    }
                },
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
            )
        }
        Column(
            Modifier.align(Alignment.BottomEnd).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val fabColor = Color(0xFFFDF3F4)
            val iconColor = MaterialTheme.colorScheme.primary
            SmallFloatingActionButton(onClick = { zoomTo(scale * 1.4f) }, containerColor = fabColor, contentColor = iconColor) {
                Icon(Icons.Rounded.ZoomIn, contentDescription = "확대")
            }
            SmallFloatingActionButton(onClick = { zoomTo(scale / 1.4f) }, containerColor = fabColor, contentColor = iconColor) {
                Icon(Icons.Rounded.ZoomOut, contentDescription = "축소")
            }
            if (scale > 1.01f) {
                SmallFloatingActionButton(onClick = { zoomTo(1f) }, containerColor = fabColor, contentColor = iconColor) {
                    Icon(Icons.Rounded.ZoomOutMap, contentDescription = "원래 크기")
                }
            }
        }
    }
}

/** 넓은 화면에서 아무 곡도 선택하지 않았을 때 */
@Composable
fun EmptyDetail(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.LibraryBooks,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
            modifier = Modifier.size(72.dp),
        )
        Text(
            "성가를 선택하세요",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            "왼쪽 목록에서 곡을 고르면 악보가 여기에 표시됩니다",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
