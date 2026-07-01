package com.omnimusic.player.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            // تم تصفير الـ top padding تماماً لمنع نزول العناصر لتحت بزيادة
            contentPadding = PaddingValues(top = 0.dp, bottom = 32.dp)
        ) {
            
            // 1. شريط البحث - تم تقليل الـ padding لـ 10.dp ليكون أعرض ويطابق المرجع تماماً
            item {
                SearchBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, top = 8.dp),
                    onClick = { /* TODO */ }
                )
            }

            // مسافة رأسية صغيرة ومكبوسة بين البحث والدوائر
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 2. الدوائر الملونة مسنترة وملمومة
            item {
                QuickAccessRow(
                    modifier = Modifier.fillMaxWidth(),
                    onHistoryClick = { /* TODO */ },
                    onFavoritesClick = { /* TODO */ },
                    onMostPlayedClick = { /* TODO */ },
                    onShuffleClick = { /* TODO */ }
                )
            }

            // مسافة مكبوسة لإخراج قسم Recently added لفوق ومنعه من السقوط لأسفل
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. قسم الأغاني المضافة حديثاً
            item {
                RecentlyAddedSection(data = uiState.data)
                Spacer(modifier = Modifier.height(28.dp))
            }

            // 4. قسم الألبومات
            item {
                RecentAlbumsSection(data = uiState.data)
                Spacer(modifier = Modifier.height(28.dp))
            }

            // 5. قسم الفنانين
            item {
                RecentArtistsSection(data = uiState.data, viewModel = viewModel)
                Spacer(modifier = Modifier.height(28.dp))
            }

            // 6. القسم المفضل
            item {
                FavoritesSection(data = uiState.data)
            }
        }
    }
}

@Composable
private fun RecentlyAddedSection(data: HomeData) {
    if (data.recentlyAddedSongs.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = "Recently added", onSeeAllClick = { /* TODO */ })
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(data.recentlyAddedSongs, key = { it.id }) { track ->
                HomeSongCard(track = track, onClick = { /* TODO */ })
            }
        }
    }
}

@Composable
private fun RecentAlbumsSection(data: HomeData) {
    if (data.recentlyPlayedAlbums.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = "Recently played albums", onSeeAllClick = { /* TODO */ })
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(data.recentlyPlayedAlbums, key = { it.name }) { album ->
                HomeAlbumCard(album = album, onClick = { /* TODO */ }, onPlayClick = { /* TODO */ })
            }
        }
    }
}

@Composable
private fun RecentArtistsSection(data: HomeData, viewModel: HomeViewModel) {
    if (data.recentArtists.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = "Recent Artists", onSeeAllClick = { /* TODO */ })
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = "Favorites", onSeeAllClick = { /* TODO */ })
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(data.favoriteSongs, key = { it.id }) { track ->
                HomeSongCard(track = track, onClick = { /* TODO */ })
            }
        }
    }
}
