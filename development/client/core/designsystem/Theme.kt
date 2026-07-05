package com.surfschool.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object SurfSchoolColors {
    val Primary = Color(0xFF0055FF)
    val Background = Color(0xFFFFFFFF)
    val TextMain = Color(0xFF1C1C1E)
    val TextSecondary = Color(0xFF8E8E93)
    val Error = Color(0xFFFF3B30)
    val Surface = Color(0xFFF2F2F7)
}

object SurfSchoolTypography {
    val H1 = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SurfSchoolColors.TextMain)
    val H2 = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SurfSchoolColors.TextMain)
    val Body = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, color = SurfSchoolColors.TextMain)
    val Caption = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = SurfSchoolColors.TextSecondary)
}

object SurfSchoolPaddings {
    val Small = 8.dp
    val Medium = 16.dp
    val Large = 24.dp
}
