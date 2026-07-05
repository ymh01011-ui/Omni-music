package com.omnimusic.player.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.omnimusic.player.ui.theme.OmniBlack
import com.omnimusic.player.ui.theme.OmniGreen
import com.omnimusic.player.ui.theme.OmniSurfaceElevated
import com.omnimusic.player.ui.theme.OmniSurfaceElevatedHigh
import com.omnimusic.player.ui.theme.OmniTextPrimary
import com.omnimusic.player.ui.theme.OmniTextSecondary
import kotlin.math.roundToInt

private const val TRANSITION_DURATION_MS = 250
private const val NAV_ITEM_ANIM_MS = 250
private const val NAV_INDICATOR_ALPHA = 0.12f
private val NAV_ICON_LIFT = (-8).dp

// الارتفاع الفعلي الثابت لشريط البحث العائم النظيف في الشاشات العادية
val GLOBAL_BAR_HEIGHT = 80.dp

val LocalPlaybackViewModel = compositionLocalOf<PlaybackViewModel> {
    error("LocalPlaybackViewModel not provided - must be set in OmniApp")
}

@Composable
fun OmniApp() {
    val navController = rememberNavController()
    val playbackViewModel: PlaybackViewModel = hiltViewModel()
    val playbackState by playbackViewModel.playbackState.collectAsState()

    // حالات التحكم بالبحث الكامل والفعال
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var selectedFilter by rememberSaveable { mutableStateOf("All") }

    // التحكم في حركة اختفاء شريط البحث عند السحب لأعلى وظهوره عند السحب لأسفل
    val density = LocalDensity.current
    val maxOverlapPx = remember { with(density) { GLOBAL_BAR_HEIGHT.toPx() } }
    var topBarOffsetHeightPx by rememberSaveable { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (isSearchActive) return Offset.Zero // تعطيل الاختفاء أثناء الكتابة في صفحة البحث الكاملة
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
            Box(modifier = Modifier.fillMaxSize()) {
                
                // محتوى التطبيق الأساسي المستقر
                NavHost(
                    navController = navController,
                    startDestination = OmniDestination.Home.route,
                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
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

                // 1. شريط البحث العائم والنظيف جداً (يظهر فوق كل الشاشات ويختفي عند السحب)
                if (!isSearchActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(x = 0, y = topBarOffsetHeightPx.roundToInt()) }
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(OmniSurfaceElevated)
                            .clickable { isSearchActive = true } // عند الضغط يتحول لصفحة كاملة
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = OmniTextSecondary
                        )
                        Text(
                            text = "Search your music",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OmniTextSecondary,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }

                // 2. صفحة البحث الكاملة (Full-Screen Search View) الفخمة والمستقلة تماماً
                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200))
                ) {
                    val focusRequester = remember { FocusRequester() }
                    val keyboardController = LocalSoftwareKeyboardController.current

                    LaunchedEffect(isSearchActive) {
                        if (isSearchActive) {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(OmniBlack)
                            .statusBarsPadding()
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // حقل الكتابة مع سهم العودة
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(OmniSurfaceElevated)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        isSearchActive = false
                                        searchQuery = ""
                                        keyboardController?.hide()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = OmniTextPrimary
                                    )
                                }

                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp, end = 8.dp)
                                        .focusRequester(focusRequester),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = OmniTextPrimary),
                                    cursorBrush = SolidColor(OmniGreen),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                                    decorationBox = { innerTextField ->
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search your music",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = OmniTextSecondary
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }

                            // أزرار الفلترة الأنيقة تظهر هنا فقط داخل صفحة البحث الكاملة
                            val filters = listOf("All", "Songs", "Albums", "Artists")
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(filters) { filter ->
                                    val isSelected = filter == selectedFilter
                                    AssistChip(
                                        onClick = { selectedFilter = filter },
                                        label = {
                                            Text(
                                                text = filter,
                                                color = if (isSelected) OmniBlack else OmniTextPrimary
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = if (isSelected) OmniGreen else Color.Transparent
                                        ),
                                        border = if (isSelected) null else BorderStroke(1.dp, OmniSurfaceElevatedHigh),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }

                            // منطقة عرض نتائج البحث الفعالة والمحاذاة بشكل سليم
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search for songs, albums, or artists",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OmniTextSecondary
                                    )
                                } else {
                                    // هنا يتم ربط القائمة الفعلية بالبيانات المفلترة مستقبلاً
                                    Text(
                                        text = "Results for \"$searchQuery\" in $selectedFilter",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OmniGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OmniBottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(containerColor = OmniSurfaceElevated) {
        OmniDestination.bottomNavItems.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true

            OmniNavItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = destination.icon,
                label = destination.label,
            )
        }
    }
}

@Composable
private fun RowScope.OmniNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
) {
    val iconOffsetY by animateDpAsState(
        targetValue = if (selected) NAV_ICON_LIFT else 0.dp,
        animationSpec = tween(NAV_ITEM_ANIM_MS),
        label = "navIconOffset",
    )
    val indicatorColor by animateColorAsState(
        targetValue = if (selected) OmniGreen.copy(alpha = NAV_INDICATOR_ALPHA) else Color.Transparent,
        animationSpec = tween(NAV_ITEM_ANIM_MS),
        label = "navIndicatorColor",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) OmniGreen else OmniTextSecondary,
        animationSpec = tween(NAV_ITEM_ANIM_MS),
        label = "navContentColor",
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .offset(y = iconOffsetY)
                .size(width = 56.dp, height = 28.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(indicatorColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
            )
        }
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(NAV_ITEM_ANIM_MS)) + expandVertically(tween(NAV_ITEM_ANIM_MS)),
            exit = fadeOut(tween(NAV_ITEM_ANIM_MS)) + shrinkVertically(tween(NAV_ITEM_ANIM_MS)),
        ) {
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
