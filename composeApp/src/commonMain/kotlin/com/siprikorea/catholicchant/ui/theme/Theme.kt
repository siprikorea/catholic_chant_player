package com.siprikorea.catholicchant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
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

// 밝고 가벼운 톤: 부드러운 로즈 와인(주색) + 연한 금색(강조) + 거의 흰 배경
private val LightColors = lightColorScheme(
    primary = Color(0xFFB24A5E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFDEBEE),
    onPrimaryContainer = Color(0xFF6A2232),
    secondary = Color(0xFFA9822F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFCF3DE),
    onSecondaryContainer = Color(0xFF6B4E10),
    tertiary = Color(0xFF5B7FA6),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFDFCFA),
    onBackground = Color(0xFF2E2826),
    surface = Color(0xFFFDFCFA),
    onSurface = Color(0xFF2E2826),
    surfaceVariant = Color(0xFFF6F1EC),
    onSurfaceVariant = Color(0xFF857A75),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF8F5F1),
    surfaceContainerHigh = Color(0xFFF4F0EB),
    surfaceContainerHighest = Color(0xFFEEE9E3),
    outline = Color(0xFFCFC5BD),
    outlineVariant = Color(0xFFF0EAE3),
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
fun ChantTheme(content: @Composable () -> Unit) {
    // 악보가 흰 종이 이미지라 시스템 다크 모드와 관계없이 밝은 테마를 쓴다.
    MaterialTheme(
        colorScheme = LightColors,
        typography = chantTypography(),
        content = content,
    )
}
