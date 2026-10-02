package com.siprikorea.catholicchant

import androidx.compose.runtime.Composable

/** 시스템 뒤로가기 처리 (Android 만 해당, 그 외 플랫폼은 화면 내 뒤로 버튼 사용) */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
