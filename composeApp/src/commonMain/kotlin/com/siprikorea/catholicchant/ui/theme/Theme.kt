package com.siprikorea.catholicchant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.siprikorea.catholicchant.resources.Res
import com.siprikorea.catholicchant.resources.pretendard_bold
import com.siprikorea.catholicchant.resources.pretendard_medium
import com.siprikorea.catholicchant.resources.pretendard_regular
import com.siprikorea.catholicchant.resources.pretendard_semibold
import org.jetbrains.compose.resources.Font

// 전례색에서 따온 버건디(주색) + 금색(강조) + 아이보리(배경)
private val LightColors = lightColorScheme(
    primary = Color(0xFF7A1F2B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF7DCDD),
    onPrimaryContainer = Color(0xFF3F0710),
    secondary = Color(0xFF8C6A1F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF5E4BC),
    onSecondaryContainer = Color(0xFF2C1F00),
    tertiary = Color(0xFF3F5A73),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFBF7F0),
    onBackground = Color(0xFF221A18),
    surface = Color(0xFFFBF7F0),
    onSurface = Color(0xFF221A18),
    surfaceVariant = Color(0xFFF0E6DA),
    onSurfaceVariant = Color(0xFF6B5D57),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFCF7),
    surfaceContainer = Color(0xFFF6EFE5),
    surfaceContainerHigh = Color(0xFFF1E8DC),
    surfaceContainerHighest = Color(0xFFEBE1D3),
    outline = Color(0xFFB9A898),
    outlineVariant = Color(0xFFE6DACB),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF2B4BA),
    onPrimary = Color(0xFF55101C),
    primaryContainer = Color(0xFF6E1B27),
    onPrimaryContainer = Color(0xFFFFDADC),
    secondary = Color(0xFFE6C47A),
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = Color(0xFF594312),
    onSecondaryContainer = Color(0xFFFFE08F),
    tertiary = Color(0xFFA9C8E6),
    onTertiary = Color(0xFF0E3248),
    background = Color(0xFF17110F),
    onBackground = Color(0xFFEDE0DB),
    surface = Color(0xFF17110F),
    onSurface = Color(0xFFEDE0DB),
    surfaceVariant = Color(0xFF3A2F2B),
    onSurfaceVariant = Color(0xFFCDBFB8),
    surfaceContainerLowest = Color(0xFF120D0B),
    surfaceContainerLow = Color(0xFF201816),
    surfaceContainer = Color(0xFF251D1A),
    surfaceContainerHigh = Color(0xFF2F2623),
    surfaceContainerHighest = Color(0xFF3A302C),
    outline = Color(0xFF968880),
    outlineVariant = Color(0xFF4A3F3A),
)

@Composable
private fun pretendard() = FontFamily(
    Font(Res.font.pretendard_regular, FontWeight.Normal),
    Font(Res.font.pretendard_medium, FontWeight.Medium),
    Font(Res.font.pretendard_semibold, FontWeight.SemiBold),
    Font(Res.font.pretendard_bold, FontWeight.Bold),
)

@Composable
private fun chantTypography(): Typography {
    val family = pretendard()
    val base = Typography()
    fun TextStyle.withFamily() = copy(fontFamily = family)
    return Typography(
        displayLarge = base.displayLarge.withFamily(),
        displayMedium = base.displayMedium.withFamily(),
        displaySmall = base.displaySmall.withFamily(),
        headlineLarge = base.headlineLarge.withFamily(),
        headlineMedium = base.headlineMedium.withFamily(),
        headlineSmall = base.headlineSmall.withFamily(),
        titleLarge = base.titleLarge.withFamily(),
        titleMedium = base.titleMedium.withFamily(),
        titleSmall = base.titleSmall.withFamily(),
        bodyLarge = base.bodyLarge.withFamily(),
        bodyMedium = base.bodyMedium.withFamily(),
        bodySmall = base.bodySmall.withFamily(),
        labelLarge = base.labelLarge.withFamily(),
        labelMedium = base.labelMedium.withFamily(),
        labelSmall = base.labelSmall.withFamily(),
    )
}

@Composable
fun ChantTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = chantTypography(),
        content = content,
    )
}
