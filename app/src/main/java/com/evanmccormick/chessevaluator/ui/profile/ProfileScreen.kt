package com.evanmccormick.chessevaluator.ui.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import com.evanmccormick.chessevaluator.utils.navigation.ScreenWithNavigation
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ScreenWithNavigation(
        navController = navController,
        currentRoute = "profile_screen"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
        ) {
            // Header
            Text(
                text = "Personal Stats",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            // Time Control Tabs
            TimeControlTabs(
                timeControls = state.timeControls,
                selectedIndex = state.currentTimeControlIndex,
                onTabSelected = { viewModel.onTimeControlSelected(it) }
            )

            // Strengths/Weaknesses Pager
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

            // Star Chart Placeholder (to be implemented later)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder for the star chart
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    // Simple star placeholder
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(StarShape())
                            .background(MaterialTheme.colorScheme.onTertiaryContainer)
                    )
                }

                // Add text labels around the star
                Text(
                    text = "Opening",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    modifier = Modifier.offset(x = (-100).dp)
                )

                Text(
                    text = "Closed",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    modifier = Modifier.offset(x = 100.dp)
                )

                Text(
                    text = "Open",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    modifier = Modifier.offset(y = (-120).dp)
                )

                Text(
                    text = "Endgame",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    modifier = Modifier.offset(x = 100.dp, y = 120.dp)
                )

                Text(
                    text = "Middlegame",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    modifier = Modifier.offset(x = (-70).dp, y = 120.dp)
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
                        title = "User Strengths",
                        items = state.strengthsByTag
                    )
                } else {
                    // Weaknesses page
                    StatsListContent(
                        title = "User Weaknesses",
                        items = state.weaknessesByTag
                    )
                }
            }

            // Pager Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0..1) {
                        val isSelected = pagerState.currentPage == i
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(if (isSelected) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(i)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeControlTabs(
    timeControls: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.onPrimaryContainer),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        timeControls.forEachIndexed { index, timeControl ->
            val isSelected = index == selectedIndex

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    .clickable { onTabSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeControl,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun StatsListContent(
    title: String,
    items: List<TagEloData>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { statItem ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statItem.tag,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = statItem.elo.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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