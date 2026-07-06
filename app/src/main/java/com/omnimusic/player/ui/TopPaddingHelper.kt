package com.omnimusic.player.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * دالة تحسب الحشوة العلوية ديناميكياً
 * تتزامن مع اختفاء شريط البحث
 */
@Composable
fun rememberOmniTopPadding(): Dp {
    // قيمة الحشوة العلوية
    // يمكنك تعديل هذه القيمة حسب احتياجات التصميم
    return 16.dp
}
