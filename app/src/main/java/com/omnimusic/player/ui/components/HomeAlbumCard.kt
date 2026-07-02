package com.omnimusic.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.omnimusic.player.data.model.Album

/**
 * بطاقة ألبوم مخصصة لقسم "Recently played albums" بناءً على التصميم المرجعي 1000234825.jpg
 */
@Composable
fun HomeAlbumCard(
    album: Album,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    onMenuClick: () -> Unit, // أضفنا هذا الحدث لزر النقاط الثلاثة
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(155.dp) // العرض المناسب لعناصر القائمة الأفقية
            .clickable(onClick = onClick)
    ) {
        // حاوية الصورة والأزرار المرفوعة فوقها
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // الحفاظ على شكل مربع تماماً للغلاف
                .clip(RoundedCornerShape(16.dp)) // تدوير حواف ناعم ومطابق للصورة المرجعية
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (album.albumArtUri != null) {
                AsyncImage(
                    model = album.albumArtUri,
                    contentDescription = album.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // 1. زر النقاط الثلاثة (المينيو) في أعلى اليمين
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onMenuClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More options",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 2. زر التشغيل (Play) في أسفل اليسار بحواف دائرية ناعمة (مربع مستدير الزوايا)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp) // مسافة متناسقة من الحواف
                    .size(36.dp) // حجم الزر واضح ومناسب للضغط
                    .clip(RoundedCornerShape(10.dp)) // حواف الزر مستديرة وليست دائرة كاملة
                    .background(Color.Black.copy(alpha = 0.55f)) // خلفية داكنة شفافة
                    .clickable(onClick = onPlayClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Play album",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        // نصوص البيانات أسفل الكارد
        Text(
            text = album.name,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal // النص في الصورة المرجعية يبدو سمكاً عادياً وليس عريضاً جداً
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp, start = 2.dp),
        )
        
        Text(
            text = album.albumArtist ?: "Unknown Artist",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}
