package com.evanmccormick.chessevaluator.ui.leaderboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.evanmccormick.chessevaluator.ui.theme.ExtendedTheme
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

    // Show dialog for errors
    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.dismissError()
            },
            title = { Text("Error") },
            text = { Text(state.errorMessage ?: "An error occurred") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissError()
                }) {
                    Text("OK")
                }
            }
        )
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
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (state.tabs.isNotEmpty()) {
                    // Tab Row for Time Controls
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
                    Box(modifier = Modifier.weight(1f)) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            LeaderboardContent(
                                tab = state.tabs[page],
                                userEntry = state.userEntry,
                                isLoading = state.isLoading && page == state.currentTabIndex
                            )
                        }
                    }
                    if (state.showUserInfoBox && state.userEntry != null) {
                        UserInfoBox(
                            entry = state.userEntry!!,
                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        )
                    }
                } else if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardContent(
    tab: LeaderboardTab,
    userEntry: LeaderboardEntry?,
    isLoading: Boolean
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
                    .background(color = ExtendedTheme.colors.leaderboardHeaderBg)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.title,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Loading indicator or leaderboard entries
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (tab.entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No entries found",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                val listState = rememberLazyListState()

                // Leaderboard entries
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    itemsIndexed(tab.entries) { index, entry ->
                        LeaderboardEntryRow(
                            entry = entry,
                            index = index,
                            isCurrentUser = userEntry != null && entry.name == userEntry.name
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserInfoBox(
    entry: LeaderboardEntry,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    rankColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    isCurrentUser: Boolean = true,
    modifier: Modifier? = null
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
            color = rankColor,
            modifier = Modifier.width(48.dp)
        )

        // Name
        Text(
            text = entry.name,
            fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        // ELO
        Text(
            text = entry.elo.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
fun LeaderboardEntryRow(
    entry: LeaderboardEntry,
    index: Int,
    isCurrentUser: Boolean
) {
    // Determine background color based on whether it's an odd or even row
    val backgroundColor = when {
        isCurrentUser -> MaterialTheme.colorScheme.primaryContainer
        index % 2 == 0 -> ExtendedTheme.colors.rowBackgroundEven
        else -> ExtendedTheme.colors.rowBackgroundOdd
    }

    // Special colors for medal positions
    val rankColor = when (entry.rank) {
        1 -> ExtendedTheme.colors.goldMedal
        2 -> ExtendedTheme.colors.silverMedal
        3 -> ExtendedTheme.colors.bronzeMedal
        else -> MaterialTheme.colorScheme.onSurface
    }
    UserInfoBox(
        entry = entry,
        backgroundColor = backgroundColor,
        rankColor = rankColor,
        isCurrentUser = isCurrentUser
    )

}