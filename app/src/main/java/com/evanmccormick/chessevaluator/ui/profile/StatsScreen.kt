package com.evanmccormick.chessevaluator.ui.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.evanmccormick.chessevaluator.ui.theme.ExtendedTheme
import com.evanmccormick.chessevaluator.utils.navigation.ScreenWithNavigation
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ScreenWithNavigation(
        navController = navController,
        currentRoute = "stats_screen"
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Time Control Tabs
                ScrollableTabRow(
                    selectedTabIndex = state.currentTimeControlIndex,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    divider = { HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary) }
                ) {
                    state.timeControls.forEachIndexed { index, timeControl ->
                        Tab(
                            selected = state.currentTimeControlIndex == index,
                            onClick = { viewModel.onTimeControlSelected(index) },
                            text = {
                                Text(
                                    text = timeControl,
                                    fontWeight = if (state.currentTimeControlIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // Star Chart Section with Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Star chart
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(StarShape())
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                        }

                        // Chart labels
                        Text(
                            text = "Opening",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            modifier = Modifier.offset(x = (-100).dp)
                        )

                        Text(
                            text = "Closed",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            modifier = Modifier.offset(x = 100.dp)
                        )

                        Text(
                            text = "Open",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            modifier = Modifier.offset(y = (-120).dp)
                        )

                        Text(
                            text = "Endgame",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            modifier = Modifier.offset(x = 100.dp, y = 120.dp)
                        )

                        Text(
                            text = "Middlegame",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            modifier = Modifier.offset(x = (-70).dp, y = 120.dp)
                        )
                    }
                }

                // Strengths/Weaknesses Tab Row - Moved above the list
                val pagerState = rememberPagerState(
                    initialPage = if (state.isShowingStrengths) 0 else 1,
                    pageCount = { 2 }
                )
                val coroutineScope = rememberCoroutineScope()

                // Keep pager and view model state in sync
                LaunchedEffect(pagerState.currentPage) {
                    viewModel.setShowingStrengths(pagerState.currentPage == 0)
                }

                LaunchedEffect(state.isShowingStrengths) {
                    if ((state.isShowingStrengths && pagerState.currentPage != 0) ||
                        (!state.isShowingStrengths && pagerState.currentPage != 1)) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(if (state.isShowingStrengths) 0 else 1)
                        }
                    }
                }

                // Tab Row for Strengths/Weaknesses
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier
                        .fillMaxWidth(),
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        text = { Text("Strengths") }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        text = { Text("Weaknesses") }
                    )
                }

                // Stats content with horizontal pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    if (page == 0) {
                        // Strengths page
                        StatsListContent(
                            items = state.strengthsByTag
                        )
                    } else {
                        // Weaknesses page
                        StatsListContent(
                            items = state.weaknessesByTag
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsListContent(
    items: List<TagEloData>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(items) { index, statItem ->
            // Use alternating background colors like in LeaderboardScreen
            val backgroundColor = if (index % 2 == 0)
                ExtendedTheme.colors.rowBackgroundEven
            else
                ExtendedTheme.colors.rowBackgroundOdd

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statItem.tag,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = statItem.elo.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// Simple star shape placeholder
private class StarShape : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val path = androidx.compose.ui.graphics.Path().apply {
            // For simplicity, just create a pentagon shape as placeholder
            // In a real app, you would create a proper star shape
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = size.width.coerceAtMost(size.height) / 2f

            // Move to first point
            moveTo(centerX, centerY - radius)

            // Draw star points (simplified as pentagon for this example)
            for (i in 1..4) {
                val angle = Math.PI * 2 * i / 5 - Math.PI / 2
                val x = centerX + radius * kotlin.math.cos(angle).toFloat()
                val y = centerY + radius * kotlin.math.sin(angle).toFloat()
                lineTo(x, y)
            }

            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}