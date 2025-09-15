package com.lhr.flighttracker.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.lhr.flighttracker.R

val notoSansTCFamily = FontFamily(
    Font(R.font.noto_sans_tc_thin, FontWeight.Thin),
    Font(R.font.noto_sans_tc_extra_light, FontWeight.ExtraLight),
    Font(R.font.noto_sans_tc_light, FontWeight.Light),
    Font(R.font.noto_sans_tc_regular, FontWeight.Normal),
    Font(R.font.noto_sans_tc_medium, FontWeight.Medium),
    Font(R.font.noto_sans_tc_semi_bold, FontWeight.SemiBold),
    Font(R.font.noto_sans_tc_bold, FontWeight.Bold),
    Font(R.font.noto_sans_tc_extra_bold, FontWeight.ExtraBold),
    Font(R.font.noto_sans_tc_black, FontWeight.Black)
)

// Default Material 3 typography values
val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = notoSansTCFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = notoSansTCFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = notoSansTCFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = notoSansTCFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = notoSansTCFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = notoSansTCFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = notoSansTCFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = notoSansTCFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = notoSansTCFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = notoSansTCFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = notoSansTCFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = notoSansTCFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = notoSansTCFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = notoSansTCFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = notoSansTCFamily),
)