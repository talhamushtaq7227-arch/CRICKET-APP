package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CricketBallSeamSpinner
import com.example.ui.components.RcbCrestLogo
import com.example.ui.components.SegmentedFormatSelector
import com.example.ui.components.liquid3DEntrance
import com.example.ui.theme.LiquidGlassTheme
import com.example.viewmodel.CricketViewModel

@Composable
fun MatchesListScreen(
    viewModel: CricketViewModel,
    onNavigateToMatchSetup: () -> Unit,
    onNavigateToLiveScoring: (Long) -> Unit,
    onNavigateToScorecard: (Long) -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    val matches by viewModel.allMatches.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) } // 0: All, 1: Live, 2: Completed

    val filteredMatches = remember(matches, selectedTab) {
        when (selectedTab) {
            1 -> matches.filter { !it.isCompleted }
            2 -> matches.filter { it.isCompleted }
            else -> matches
        }
    }

    val liveCount = remember(matches) { matches.count { !it.isCompleted } }
    val completedCount = remember(matches) { matches.count { it.isCompleted } }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToMatchSetup,
                containerColor = tokens.accentGold,
                contentColor = tokens.buttonContent,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Match")
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RcbCrestLogo(size = 40.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Matches & Fixtures",
                            color = tokens.textPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 21.sp
                        )
                        Text(
                            text = "${matches.size} saved matches with live scorecards & recordings",
                            color = tokens.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                SegmentedFormatSelector(
                    options = listOf(
                        "All (${matches.size})",
                        "Live ($liveCount)",
                        "Completed ($completedCount)"
                    ),
                    selectedIndex = selectedTab,
                    onSelect = { selectedTab = it }
                )
            }

            if (filteredMatches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CricketBallSeamSpinner(sizeDp = 56.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("No Matches Found", color = tokens.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Tap + to start a new match and begin live scoring.", color = tokens.textSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(filteredMatches, key = { _, m -> m.id }) { index, match ->
                        Box(modifier = Modifier.liquid3DEntrance(delayMs = (index * 30).coerceAtMost(180))) {
                            MatchCardItem(
                                match = match,
                                onClick = {
                                    if (match.isCompleted) {
                                        onNavigateToScorecard(match.id)
                                    } else {
                                        viewModel.loadMatch(match.id)
                                        onNavigateToLiveScoring(match.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
