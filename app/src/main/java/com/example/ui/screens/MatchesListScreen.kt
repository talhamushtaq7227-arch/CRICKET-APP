package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlassCard
import com.example.ui.components.GoldButton
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.components.CricketBallSeamSpinner
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

    val filteredMatches = when (selectedTab) {
        1 -> matches.filter { !it.isCompleted }
        2 -> matches.filter { it.isCompleted }
        else -> matches
    }

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
                    CricketBallSeamSpinner(sizeDp = 28.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Match Archive",
                        color = tokens.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${matches.size} saved matches with scorecards & recordings",
                    color = tokens.textSecondary,
                    fontSize = 12.sp
                )
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = tokens.glassCardElevated,
                contentColor = tokens.accentGold,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = tokens.accentGold,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "All (${matches.size})",
                            color = if (selectedTab == 0) tokens.accentGold else tokens.textSecondary,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        val count = matches.count { !it.isCompleted }
                        Text(
                            "In Progress ($count)",
                            color = if (selectedTab == 1) tokens.accentGold else tokens.textSecondary,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        val count = matches.count { it.isCompleted }
                        Text(
                            "Completed ($count)",
                            color = if (selectedTab == 2) tokens.accentGold else tokens.textSecondary,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    }
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
                        Text("Start a match with the wizard to begin live scoring.", color = tokens.textSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMatches) { match ->
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
