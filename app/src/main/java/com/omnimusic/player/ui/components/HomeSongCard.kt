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
import androidx.compose.ui.graphics.Brush
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
    
    // الألوان الافتراضية للكارت
    var backgroundColor by remember(track.id) { mutableStateOf(Color(0xFF1C1C1E)) }
    var titleColor by remember(track.id) { mutableStateOf(Color.White) }
    var artistColor by remember(track.id) { mutableStateOf(Color.White.copy(alpha = 0.65f)) }

    val imageRequest = remember(track.albumArtUri) {
        ImageRequest.Builder(context)
            .data(track.albumArtUri)
            .allowHardware(false)
            .crossfade(true)
            .build()
    }

    Row(
        modifier = modifier
            .size(width = 245.dp, height = 115.dp) // الأبعاد المتطابقة تماماً مع التصميم المرجعي
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        
        // الجانب الأيسر: مساحة النصوص الملونة
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp)
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

        // الجانب الأيمن: غلاف الأغنية المدمج بسلاسة بدون فواصل
        Box(
            modifier = Modifier
                .size(115.dp)
                .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
        ) {
            if (track.albumArtUri != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = track.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onState = { state ->
                        if (state is AsyncImagePainter.State.Success) {
                            val bitmap = (state.result.drawable as? BitmapDrawable)?.bitmap
                            bitmap?.let { bmp ->
                                Palette.from(bmp).generate { palette ->
                                    palette?.let { pal ->
                                        val extractedBg = pal.getDominantColor(
                                            pal.getMutedColor(
                                                pal.getVibrantColor(Color(0xFF1C1C1E).toArgb())
                                            )
                                        )
                                        val resolvedBg = Color(extractedBg)
                                        backgroundColor = resolvedBg

                                        if (resolvedBg.luminance() > 0.45f) {
                                            titleColor = Color(pal.getDarkVibrantColor(Color(0xFF121212).toArgb()))
                                            artistColor = Color(pal.getDarkMutedColor(Color(0xFF333333).toArgb()))
                                        } else {
                                            titleColor = Color(pal.getLightVibrantColor(Color.White.toArgb()))
                                            artistColor = Color(pal.getLightMutedColor(Color(0xFFCCCCCC).toArgb()))
                                        }
                                    }
                                }
                            }
                        }
                    }
                )

                // سر الصنعة: ماسك التسييح التدريجي المعتمد على الـ Proportional Color Stops
                // بيبدأ مصمت تماماً بنفس لون الخلفية من الشمال، ويدوب تدريجياً لغاية 60% من عرض الغلاف ليصبح شفافاً
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                0.0f to backgroundColor,                 // دمج كامل عند الحافة المشتركة
                                0.2f to backgroundColor.copy(alpha = 0.85f),
                                0.6f to Color.Transparent                 // يبدأ الغلاف يظهر بنقائه الكامل هنا
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            // زر التحكم الدائري الشفاف في منتصف الغلاف
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
