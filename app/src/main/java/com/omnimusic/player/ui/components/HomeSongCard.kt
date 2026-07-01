package com.omnimusic.player.ui.components

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
    // ألوان افتراضية بيضاء في حال لم ينتهِ تحليل ألوان الغلاف بعد
    var titleColor by remember(track.id) { mutableStateOf(Color.White) }
    var artistColor by remember(track.id) { mutableStateOf(Color.White.copy(alpha = 0.7f)) }

    Box(
        modifier = modifier
            .size(width = 250.dp, height = 140.dp) // الأبعاد المتناسقة تماماً مع شاشة الـ Home في الصورة 1000234443.jpg
            .clip(RoundedCornerShape(20.dp)) // حواف دائرية ناعمة تطابق التصميم المرجعي
            .clickable(onClick = onClick)
    ) {
        if (track.albumArtUri != null) {
            AsyncImage(
                model = track.albumArtUri,
                contentDescription = track.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onState = { state ->
                    // اقتناص الغلاف عند تحميله بنجاح لتحليل ألوانه ديناميكياً
                    if (state is AsyncImagePainter.State.Success) {
                        val bitmap = (state.result.drawable as? BitmapDrawable)?.bitmap
                        bitmap?.let { bmp ->
                            Palette.from(bmp).generate { palette ->
                                palette?.let { pal ->
                                    // فحص ذكي: إذا كان الغلاف فاتحاً أم غامقاً لتحديد تباين النصوص
                                    val isDark = pal.getDarkVibrantColor(0) != 0
                                    if (isDark) {
                                        // للأغلفة الغامقة (مثل كارت Cataclysm): نختار نصوصاً فاتحة ومبهجة
                                        titleColor = pal.getLightVibrantColor(0xFFFFFFFF.toInt()).let { Color(it) }
                                        artistColor = pal.getLightMutedColor(0xFFB0BEC5.toInt()).let { Color(it) }
                                    } else {
                                        // للأغلفة الفاتحة (مثل كارت telepatía): نختار نصوصاً داكنة أنيقة مطابقة للصورة
                                        titleColor = pal.getDarkVibrantColor(0xFF1A237E.toInt()).let { Color(it) }
                                        artistColor = pal.getDarkMutedColor(0xFF4A148C.toInt()).let { Color(it) }
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

        // زر التشغيل السريع في منتصف الكارت شفاف ونظيف وبدون تكتلات بصريّة مزعجة
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.Center)
                .size(32.dp)
        )

        // حاوية النصوص وموضعة على اليسار (CenterStart) لتطابق الصورة المرجعية 1000234443.jpg تماماً
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp, end = 60.dp) // مسافة أمان مريحة للعين تمنع تداخل النص مع زر التشغيل الوسطي
        ) {
            Text(
                text = track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = titleColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
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
    }
}
