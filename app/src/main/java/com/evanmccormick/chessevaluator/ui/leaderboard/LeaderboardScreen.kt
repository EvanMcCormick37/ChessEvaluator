package com.evanmccormick.chessevaluator.ui.leaderboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.evanmccormick.chessevaluator.utils.navigation.ScreenWithNavigation
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LeaderboardScreen(
    navController: NavController,
    viewModel: LeaderboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = state.currentTabIndex) { state.tabs.size }
    val coroutineScope = rememberCoroutineScope()

    // Keep the ViewModel's current tab in sync with the pager
    LaunchedEffect(pagerState.currentPage) {
        viewModel.changeTab(pagerState.currentPage)
    }

    ScreenWithNavigation(
        navController = navController,
        currentRoute = "leaderboard_screen"
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Leaderboard",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Tab Row for Time Controls
                if (state.tabs.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        edgePadding = 16.dp,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        divider = { HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary) }
                    ) {
                        state.tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = tab.tabTitle,
                                        fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    // Pager for different time control leaderboards
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { page ->
                        LeaderboardContent(
                            tab = state.tabs[page],
                            onFindMe = { viewModel.findMe() }
                        )
                    }

                    // Tag Filter Bar
                    TagFilterBar(
                        selectedTags = state.selectedTags,
                        onAddTag = { /* Will be implemented later */ },
                        onRemoveTag = { viewModel.removeTag(it) }
                    )
                } else if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.errorMessage != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.errorMessage!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardContent(
    tab: LeaderboardTab,
    onFindMe: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Title for current leaderboard
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xFF004D40))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Leaderboard entries
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(tab.entries) { index, entry ->
                    LeaderboardEntryRow(
                        entry = entry,
                        backgroundColor = when {
                            entry.isCurrentUser -> MaterialTheme.colorScheme.primaryContainer
                            index % 2 == 0 -> Color(0xFFF0F0F0)
                            else -> Color.White
                        }
                    )
                }
            }
        }

        // Find Me Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            FilledTonalButton(
                onClick = onFindMe,
                shape = CircleShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Find Me"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Find Me")
            }
        }
    }
}

@Composable
fun LeaderboardEntryRow(
    entry: LeaderboardEntry,
    backgroundColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank
        Text(
            text = "#${entry.rank}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = when (entry.rank) {
                1 -> Color(0xFFFFD700) // Gold
                2 -> Color(0xFFC0C0C0) // Silver
                3 -> Color(0xFFCD7F32) // Bronze
                else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.width(48.dp)
        )

        // Name
        Text(
            text = entry.name,
            fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        // ELO
        Text(
            text = entry.elo.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
fun TagFilterBar(
    selectedTags: List<String>,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Filter by Tags",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedTags.forEach { tag ->
                FilterChip(
                    selected = true,
                    onClick = { onRemoveTag(tag) },
                    label = { Text(tag, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }
        }

        IconButton(
            onClick = onAddTag,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Tag",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}