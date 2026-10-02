package com.siprikorea.catholicchant.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 검색어와 일치한 구간을 강조한 문자열 */
@Composable
fun highlighted(text: String, ranges: List<IntRange>): AnnotatedString {
    if (ranges.isEmpty()) return AnnotatedString(text)
    val style = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        background = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
    )
    return buildAnnotatedString {
        var cursor = 0
        for (range in ranges.sortedBy { it.first }) {
            val start = range.first.coerceIn(cursor, text.length)
            val end = (range.last + 1).coerceIn(start, text.length)
            append(text.substring(cursor, start))
            withStyle(style) { append(text.substring(start, end)) }
            cursor = end
        }
        append(text.substring(cursor))
    }
}

/** 성가 번호 배지. 재생 중이면 이퀄라이저 애니메이션을 보여준다. */
@Composable
fun NumberBadge(no: Int, size: Dp = 48.dp, active: Boolean = false, playing: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    val background = if (active) {
        Brush.linearGradient(listOf(colors.primary, colors.primary.copy(alpha = 0.78f)))
    } else {
        Brush.linearGradient(listOf(colors.primaryContainer, colors.secondaryContainer.copy(alpha = 0.7f)))
    }
    Box(
        modifier = Modifier.size(size).clip(RoundedCornerShape(size * 0.3f)).background(background),
        contentAlignment = Alignment.Center,
    ) {
        if (active && playing) {
            EqualizerBars(color = colors.onPrimary, modifier = Modifier.size(size * 0.45f))
        } else {
            Text(
                text = no.toString(),
                color = if (active) colors.onPrimary else colors.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * (if (no >= 100) 0.33f else 0.38f)).sp,
                letterSpacing = (-0.5).sp,
            )
        }
    }
}

@Composable
fun EqualizerBars(color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val heights = listOf(420, 300, 520).map { duration ->
        transition.animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(duration), RepeatMode.Reverse),
        )
    }
    Canvas(modifier) {
        val barWidth = size.width / 5
        heights.forEachIndexed { index, height ->
            val h = size.height * height.value
            drawRoundRect(
                color = color,
                topLeft = Offset(barWidth * (index * 2), size.height - h),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(barWidth / 2),
            )
        }
    }
}

/** 앱 로고: 버건디 바탕의 금색 십자가 */
@Composable
fun CrossLogo(size: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.3f))
            .background(Brush.linearGradient(listOf(Color(0xFF8E2433), Color(0xFF5E1520)))),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(size * 0.56f)) {
            val gold = Color(0xFFE6C47A)
            val w = size.toPx() * 0.56f
            val bar = w * 0.2f
            drawRoundRect(gold, Offset((w - bar) / 2, 0f), Size(bar, w), CornerRadius(bar / 3))
            drawRoundRect(gold, Offset(w * 0.12f, w * 0.26f), Size(w * 0.76f, bar), CornerRadius(bar / 3))
        }
    }
}

/** 작은 분류 라벨 (연중, 성모 …) */
@Composable
fun CategoryLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
fun MetaRow(items: List<String>, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        items.filter { it.isNotBlank() }.forEachIndexed { index, item ->
            if (index > 0) {
                Box(
                    Modifier.width(1.dp).height(12.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
            Text(item, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val seconds = (totalSeconds % 60).toString().padStart(2, '0')
    return "${totalSeconds / 60}:$seconds"
}
