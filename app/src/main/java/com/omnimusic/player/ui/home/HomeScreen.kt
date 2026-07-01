package com.omnimusic.player.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omnimusic.player.data.repository.HomeData
import com.omnimusic.player.ui.components.HomeAlbumCard
import com.omnimusic.player.ui.components.HomeArtistItem
import com.omnimusic.player.ui.components.HomeSongCard
import com.omnimusic.player.ui.components.QuickAccessRow
import com.omnimusic.player.ui.components.SearchBar
import com.omnimusic.player.ui.components.SectionHeader

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        // سر التباعد الاحترافي: إعطاء مسافة 28.dp بين كل قسم والتالي له
        // مع ترك مساحة علوية وسفلية مريحة للشاشة بأكملها
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // 1. شريط البحث مع بادئة جانبية متناسقة
            item {
                SearchBar(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 2. أزرار الوصول السريع الأربعة
            item {
                QuickAccessRow()
            }

            // 3. الأغاني المضافة حديثاً
            item {
                RecentlyAddedSection(data = uiState.data)
            }

            // 4. الألبومات المشغلة مؤخراً
            item {
                RecentAlbumsSection(data = uiState.data)
            }

            // 5. الفنانين الحاليين
            item {
                RecentArtistsSection(data = uiState.data, viewModel = viewModel)
            }

            // 6. القائمة المفضلة
            item {
                FavoritesSection(data = uiState.data)
            }
        }
    }
}

@Composable
private fun RecentlyAddedSection(data: HomeData) {
    if (data.recentlyAddedSongs.isEmpty()) return

    // تجميع الـ Header والـ List داخل Column لضبط المسافة الداخلية بين العنوان والكروت بدقة
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(
            title = "Recently added", 
            onSeeAllClick = { /* TODO */ }
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(data.recentlyAddedSongs, key = { it.id }) { track ->
                HomeSongCard(
                    track = track,
                    onClick = { /* TODO */ },
                )
            }
        }
    }
}

@Composable
private fun RecentAlbumsSection(data: HomeData) {
    if (data.recentAlbums.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(
            title = "Recently played albums", 
            onSeeAllClick = { /* TODO */ }
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(data.recentAlbums, key = { it.name }) { album ->
                HomeAlbumCard(
                    album = album,
                    onClick = { /* TODO */ },
                    onPlayClick = { /* TODO */ }
                )
            }
        }
    }
}

@Composable
private fun RecentArtistsSection(data: HomeData, viewModel: HomeViewModel) {
    if (data.recentArtists.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(
            title = "Recent Artists", 
            onSeeAllClick = { /* TODO */ }
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(data.recentArtists, key = { it.name }) { artist ->
                HomeArtistItem(
                    artist = artist,
                    resolveImageUrl = viewModel::getArtistImageUrl,
                    onClick = { /* TODO */ },
                )
            }
        }
    }
}

@Composable
private fun FavoritesSection(data: HomeData) {
    if (data.favoriteSongs.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(
            title = "Favorites", 
            onSeeAllClick = { /* TODO */ }
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(data.favoriteSongs, key = { it.id }) { track ->
                HomeSongCard(
                    track = track,
                    onClick = { /* TODO */ },
                )
            }
        }
    }
}
