package com.omnimusic.player.ui.components

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.omnimusic.player.data.model.Track

@Composable
fun HomeSongCard(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    
    // الألوان الافتراضية للكارت قبل سحب ألوان الغلاف
    var backgroundColor by remember(track.id) { mutableStateOf(Color(0xFF1C1C1E)) }
    var titleColor by remember(track.id) { mutableStateOf(Color.White) }
    var artistColor by remember(track.id) { mutableStateOf(Color.White.copy(alpha = 0.65f)) }

    // إعداد طلب الصورة لتعطيل Hardware Bitmaps لكي تتمكن مكتبة Palette من قراءة البيانات
    val imageRequest = remember(track.albumArtUri) {
        ImageRequest.Builder(context)
            .data(track.albumArtUri)
            .allowHardware(false) // هامة جداً لنجاح سحب الألوان
            .crossfade(true)
            .build()
    }

    Row(
        modifier = modifier
            .size(width = 245.dp, height = 115.dp) // نفس حجم وأبعاد الصورة المرجعية بالظبط
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        
        // الجانب الأيسر: مساحة النصوص الملونة ديناميكياً
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Text(
                text = track.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = titleColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Text(
                text = track.artistDisplay,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = artistColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        // الجانب الأيمن: صورة الغلاف المربعة تماماً وعليها زر التشغيل
        Box(
            modifier = Modifier
                .size(115.dp)
                .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
        ) {
            if (track.albumArtUri != null) {
                AsyncImage(
                    model = imageRequest, // استخدام الـ Request المخصص هنا
                    contentDescription = track.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onState = { state ->
                        if (state is AsyncImagePainter.State.Success) {
                            val bitmap = (state.result.drawable as? BitmapDrawable)?.bitmap
                            bitmap?.let { bmp ->
                                Palette.from(bmp).generate { palette ->
                                    palette?.let { pal ->
                                        // سحب اللون الأساسي المسيطر على الصورة
                                        val extractedBg = pal.getDominantColor(
                                            pal.getMutedColor(
                                                pal.getVibrantColor(Color(0xFF1C1C1E).toArgb())
                                            )
                                        )
                                        val resolvedBg = Color(extractedBg)
                                        backgroundColor = resolvedBg

                                        // حساب التباين لضبط ألوان النصوص بناءً على سطوع الخلفية المستخرجة
                                        if (resolvedBg.luminance() > 0.45f) {
                                            // إذا كانت الخلفية فاتحة (مثل غلاف Olivia Rodrigo السماوي أو غلاف telepatía الوردي)
                                            titleColor = Color(pal.getDarkVibrantColor(Color(0xFF121212).toArgb()))
                                            artistColor = Color(pal.getDarkMutedColor(Color(0xFF333333).toArgb()))
                                        } else {
                                            // إذا كانت الخلفية غامقة (مثل كارت Cataclysm المظلم)
                                            titleColor = Color(pal.getLightVibrantColor(Color.White.toArgb()))
                                            artistColor = Color(pal.getLightMutedColor(Color(0xFFCCCCCC).toArgb()))
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            // زر التشغيل الدائري الشفاف في منتصف الغلاف تماماً
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(34.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.35f))
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(22.dp)
                )
            }
        }
    }
}
