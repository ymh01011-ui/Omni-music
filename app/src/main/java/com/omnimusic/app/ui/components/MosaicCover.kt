package com.omnimusic.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MosaicCover(
    artUris: List<String?>,
    modifier: Modifier = Modifier
) {
    val surfaceColor = Color(0xFF121212)
    val borderColor = Color(0xFF000000)
    val validUris = artUris.filterNotNull().take(4)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(surfaceColor),
        contentAlignment = Alignment.Center
    ) {
        if (validUris.size >= 4) {
            // ستايل أبل ميوزيك وسبوتيفاي: 4 مربعات متساوية
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, borderColor).background(Color(0xFF1A1A1A)))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, borderColor).background(Color(0xFF222222)))
                }
                Row(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, borderColor).background(Color(0xFF2A2A2A)))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().border(0.5.dp, borderColor).background(Color(0xFF333333)))
                }
            }
        } else if (validUris.isNotEmpty()) {
            // غلاف مفرود بالكامل لو مفيش غير أغنية واحدة أو اتنين بس
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E))) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF1ED760),
                    modifier = Modifier.size(48.dp).align(Alignment.Center)
                )
            }
        } else {
            // Placeholder أساسي لو القائمة فارغة تماماً
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFF181818)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFF727272),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}
