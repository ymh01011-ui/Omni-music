package com.omnimusic.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.omnimusic.player.ui.theme.AccentFavoritesRed
import com.omnimusic.player.ui.theme.AccentHistoryBlue
import com.omnimusic.player.ui.theme.AccentMostPlayedGreen
import com.omnimusic.player.ui.theme.AccentShuffleOrange

/**
 * The 4 quick-access circles shown at the top of Home, per spec section 2's
 * accent colors: History (blue), Favorites (red/pink), Most played (green),
 * Shuffle (orange).
 */
@Composable
fun QuickAccessRow(
    onHistoryClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onMostPlayedClick: () -> Unit,
    onShuffleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // تم إزالة الـ horizontal padding الداخلي لمنع التضاعف مع الـ HomeScreen
        // السر هنا: تجميع العناصر من البداية (Start) بتباعد متقارب وثابت (16.dp) بدلاً من تمطيطها
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickAccessItem("History", Icons.Filled.History, AccentHistoryBlue, onHistoryClick)
        QuickAccessItem("Favorites", Icons.Filled.Favorite, AccentFavoritesRed, onFavoritesClick)
        QuickAccessItem("Most played", Icons.Filled.TrendingUp, AccentMostPlayedGreen, onMostPlayedClick)
        QuickAccessItem("Shuffle", Icons.Filled.Shuffle, AccentShuffleOrange, onShuffleClick)
    }
}

@Composable
private fun QuickAccessItem(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(26.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
