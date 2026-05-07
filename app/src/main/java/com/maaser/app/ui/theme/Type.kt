package com.maaser.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.maaser.app.R

val RubikFamily = FontFamily(
    Font(R.font.rubik_regular, FontWeight.Normal),
    Font(R.font.rubik_medium, FontWeight.Medium),
    Font(R.font.rubik_bold, FontWeight.Bold)
)

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = RubikFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    titleLarge   = TextStyle(fontFamily = RubikFamily, fontWeight = FontWeight.Medium, fontSize = 20.sp),
    bodyLarge    = TextStyle(fontFamily = RubikFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    labelSmall   = TextStyle(fontFamily = RubikFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp)
)
