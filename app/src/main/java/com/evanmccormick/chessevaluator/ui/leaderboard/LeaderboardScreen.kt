package com.evanmccormick.chessevaluator.ui.leaderboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { page ->
                        LeaderboardContent(
                            tab = state.tabs[page],
                            onFindMe = { viewModel.findMe() },
                            isLoading = state.isLoading && page == state.currentTabIndex
                        )
                    }

                    // Tag Filter Bar
                    TagFilterBar(
                        selectedTags = state.selectedTags,
                        availableTags = state.availableTags,
                        onAddTag = { viewModel.showTagSelector() },
                        onRemoveTag = { viewModel.removeTag(it) }
                    )

                    // Tag selector dialog
                    if (state.showTagSelector) {
                        TagSelectorDialog(
                            availableTags = state.availableTags.filter { tag ->
                                !state.selectedTags.contains(tag)
                            },
                            onTagSelected = { tag ->
                                viewModel.addTag(tag)
                                viewModel.hideTagSelector()
                            },
                            onDismiss = { viewModel.hideTagSelector() }
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
    onFindMe: () -> Unit,
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
                // Find the index of the current user
                val currentUserIndex = tab.entries.indexOfFirst { it.isCurrentUser }
                val listState = rememberLazyListState()

                // Scroll to user's position if "Find Me" was pressed
                LaunchedEffect(currentUserIndex) {
                    if (currentUserIndex >= 0) {
                        listState.animateScrollToItem(
                            index = currentUserIndex,
                            scrollOffset = -100 // Offset to position the item more centrally
                        )
                    }
                }

                // Leaderboard entries
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(tab.entries) { index, entry ->
                        LeaderboardEntryRow(
                            entry = entry,
                            index = index
                        )
                    }
                }
            }
        }

        // Find Me Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Button(
                onClick = onFindMe,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
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
    index: Int
) {
    // Determine background color based on whether it's an odd or even row
    val backgroundColor = when {
        entry.isCurrentUser -> MaterialTheme.colorScheme.primaryContainer
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
            fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Normal,
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
fun TagFilterBar(
    selectedTags: List<String>,
    availableTags: List<String>,
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

        // Only show the add button if there are available tags not yet selected
        if (availableTags.any { !selectedTags.contains(it) }) {
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
}

@Composable
fun TagSelectorDialog(
    availableTags: List<String>,
    onTagSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select a Tag") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                availableTags.forEach { tag ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTagSelected(tag) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (availableTags.indexOf(tag) < availableTags.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}