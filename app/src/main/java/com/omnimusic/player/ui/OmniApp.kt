package com.omnimusic.player.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.omnimusic.player.ui.albums.AlbumsScreen
import com.omnimusic.player.ui.artists.ArtistsScreen
import com.omnimusic.player.ui.components.MiniPlayer
import com.omnimusic.player.ui.components.MiniPlayerState
import com.omnimusic.player.ui.home.HomeScreen
import com.omnimusic.player.ui.navigation.OmniDestination
import com.omnimusic.player.ui.playlists.PlaylistsScreen
import com.omnimusic.player.ui.songs.SongsScreen
import com.omnimusic.player.ui.theme.*
import kotlin.math.roundToInt

private const val TRANSITION_DURATION_MS = 250
private const val NAV_ITEM_ANIM_MS = 250
private const val NAV_INDICATOR_ALPHA = 0.12f
private val NAV_ICON_LIFT = (-8).dp

val LocalPlaybackViewModel = compositionLocalOf<PlaybackViewModel> {
    error("LocalPlaybackViewModel not provided - must be set in OmniApp")
}

// CompositionLocal لتمرير الـ Padding العلوي لكل الشاشات بدون تعقيد
val LocalOmniTopPadding = compositionLocalOf<androidx.compose.ui.unit.Dp> { 0.dp }

@Composable
fun OmniApp() {
    val navController = rememberNavController()
    val playbackViewModel: PlaybackViewModel = hiltViewModel()
    val playbackState by playbackViewModel.playbackState.collectAsState()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var selectedFilter by rememberSaveable { mutableStateOf("All") }

    val density = LocalDensity.current
    val maxOverlapPx = remember { with(density) { OmniDimensions.TotalTopClearance.toPx() } }
    var topBarOffsetHeightPx by rememberSaveable { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isSearchActive) return Offset.Zero
                val delta = available.y
                val newOffset = topBarOffsetHeightPx + delta
                topBarOffsetHeightPx = newOffset.coerceIn(-maxOverlapPx, 0f)
                return Offset.Zero
            }
        }
    }

    CompositionLocalProvider(LocalPlaybackViewModel provides playbackViewModel) {
        Scaffold(
            modifier = Modifier.nestedScroll(nestedScrollConnection),
            containerColor = OmniBlack,
            contentWindowInsets = WindowInsets.systemBars, // حساب ذكي لحواف الشاشة
            bottomBar = {
                Column {
                    MiniPlayer(
                        state = playbackState.currentTrack?.let { track ->
                            MiniPlayerState(
                                title = track.title,
                                artist = track.artistDisplay,
                                isPlaying = playbackState.isPlaying,
                                progressFraction = if (playbackState.durationMs > 0) {
                                    playbackState.positionMs.toFloat() / playbackState.durationMs.toFloat()
                                } else 0f,
                            )
                        },
                        onClick = { /* TODO */ },
                        onPlayPauseClick = playbackViewModel::togglePlayPause,
                        onNextClick = playbackViewModel::skipToNext,
                        onPreviousClick = playbackViewModel::skipToPrevious,
                    )
                    OmniBottomNavBar(navController)
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                // تمرير المسافة العلوية للشاشات الداخلية لضمان عدم التداخل
                val topPadding = innerPadding.calculateTopPadding() + OmniDimensions.TotalTopClearance
                
                CompositionLocalProvider(LocalOmniTopPadding provides topPadding) {
                    NavHost(
                        navController = navController,
                        startDestination = OmniDestination.Home.route,
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) },
                        exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION_MS)) },
                        popEnterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) },
                        popExitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION_MS)) },
                    ) {
                        composable(OmniDestination.Home.route) { HomeScreen() }
                        composable(OmniDestination.Albums.route) { AlbumsScreen() }
                        composable(OmniDestination.Songs.route) { SongsScreen() }
                        composable(OmniDestination.Playlists.route) { PlaylistsScreen() }
                        composable(OmniDestination.Artists.route) { ArtistsScreen() }
                    }
                }

                // الـ Search Bar العائم المستقل
                if (!isSearchActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(x = 0, y = topBarOffsetHeightPx.roundToInt()) }
                            .padding(
                                top = innerPadding.calculateTopPadding() + OmniDimensions.SearchBarVerticalPadding,
                                start = 16.dp,
                                end = 16.dp
                            )
                            .height(OmniDimensions.SearchBarHeight)
                            .clip(RoundedCornerShape(28.dp))
                            .background(OmniSurfaceElevated)
                            .clickable { isSearchActive = true }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = OmniTextSecondary
                        )
                        Text(
                            text = "Search your music",
                            style = MaterialTheme.typography.bodyLarge,
                            color = OmniTextSecondary,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }

                // صفحة البحث الكاملة (Full-Screen Search)
                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200))
                ) {
                    FullScreenSearch(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectedFilter = it },
                        onClose = {
                            isSearchActive = false
                            searchQuery = ""
                        },
                        topInset = innerPadding.calculateTopPadding()
                    )
                }
            }
        }
    }
}

@Composable
private fun FullScreenSearch(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    onClose: () -> Unit,
    topInset: androidx.compose.ui.unit.Dp
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OmniBlack)
            .padding(top = topInset)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = OmniDimensions.SearchBarVerticalPadding)
                    .height(OmniDimensions.SearchBarHeight)
                    .clip(RoundedCornerShape(28.dp))
                    .background(OmniSurfaceElevated)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        onClose()
                    }
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OmniTextPrimary)
                }

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = OmniTextPrimary),
                    cursorBrush = SolidColor(OmniGreen),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text("Search your music", style = MaterialTheme.typography.bodyLarge, color = OmniTextSecondary)
                        }
                        innerTextField()
                    }
                )
            }

            val filters = listOf("All", "Songs", "Albums", "Artists")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { filter ->
                    val isSelected = filter == selectedFilter
                    AssistChip(
                        onClick = { onFilterSelected(filter) },
                        label = { Text(filter, color = if (isSelected) OmniBlack else OmniTextPrimary) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelected) OmniGreen else Color.Transparent
                        ),
                        border = if (isSelected) null else BorderStroke(1.dp, OmniSurfaceElevatedHigh),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (searchQuery.isEmpty()) {
                    Text("Search for songs, albums, or artists", style = MaterialTheme.typography.bodyMedium, color = OmniTextSecondary)
                } else {
                    Text("Results for \"$searchQuery\"", style = MaterialTheme.typography.bodyMedium, color = OmniGreen)
                }
            }
        }
    }
}

// أضف دالة مساعدة لاستدعاء الـ Padding في باقي الشاشات
@Composable
fun rememberOmniTopPadding() = LocalOmniTopPadding.current

// (احتفظ بـ OmniBottomNavBar كما هو في الكود القديم الخاص بك، لا يحتاج لتعديل جذري)
