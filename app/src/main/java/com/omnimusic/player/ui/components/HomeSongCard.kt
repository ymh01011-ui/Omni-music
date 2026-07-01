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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.omnimusic.player.data.model.Track

@Composable
fun HomeSongCard(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // الألوان الافتراضية للكارت قبل سحب ألوان الغلاف (تتماشى مع التصميم الداكن)
    var backgroundColor by remember(track.id) { mutableStateOf(Color(0xFF151515)) }
    var titleColor by remember(track.id) { mutableStateOf(Color.White) }
    var artistColor by remember(track.id) { mutableStateOf(Color.White.copy(alpha = 0.65f)) }

    Row(
        modifier = modifier
            .size(width = 245.dp, height = 115.dp) // ضبط الأبعاد والارتفاع بدقة ليكون متناسقاً تماماً مع كروت الـ Albums بالأسفل
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        
        // الجانب الأيسر: يحتوي على النصوص ويأخذ المساحة المتبقية ديناميكياً
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Text(
                text = track.title,
                maxLines = 2, // يتيح نزول الاسم لسطر ثانٍ عند الحاجة مثل كارت Cataclysm
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

        // الجانب الأيمن: صورة الغلاف المربعة تماماً بشكلها الطبيعي وعليها زر التحكم
        Box(
            modifier = Modifier
                .size(115.dp) // نفس ارتفاع الكارت ليكون مربعاً كاملاً ملتصقاً بالحافة اليمنى
                .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
        ) {
            if (track.albumArtUri != null) {
                AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = track.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onState = { state ->
                        if (state is AsyncImagePainter.State.Success) {
                            val bitmap = (state.result.drawable as? BitmapDrawable)?.bitmap
                            bitmap?.let { bmp ->
                                Palette.from(bmp).generate { palette ->
                                    palette?.let { pal ->
                                        // 1. سحب اللون المهيمن ليكون خلفية القسم الأيسر والكارت كله
                                        val extractedBg = pal.getDominantColor(Color(0xFF151515).toArgb())
                                        val resolvedBg = Color(extractedBg)
                                        backgroundColor = resolvedBg

                                        // 2. تحليل درجة السطوع (Luminance) لضمان تباين وتلوين النصوص بشكل مثالي
                                        if (resolvedBg.luminance() < 0.45f) {
                                            // إذا كانت الخلفية داكنة (مثل كارت Cataclysm): نصوص فاتحة ومبهجة
                                            titleColor = Color(pal.getLightVibrantColor(Color.White.toArgb()))
                                            artistColor = Color(pal.getLightMutedColor(Color(0xFFB0BEC5).toArgb()))
                                        } else {
                                            // إذا كانت الخلفية فاتحة (مثل كارت telepatía): نصوص غامقة وأنيقة
                                            titleColor = Color(pal.getDarkVibrantColor(Color(0xFF1A237E).toArgb()))
                                            artistColor = Color(pal.getDarkMutedColor(Color(0xFF4A148C).toArgb()))
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

            // زر التشغيل الشفاف الموضوع في منتصف الغلاف الأيمن تماماً
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
