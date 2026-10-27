package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.PlayerEntity
import com.example.data.model.PlayerRole
import com.example.domain.engine.PlayerStats
import com.example.ui.components.CyanPillTabs
import com.example.ui.components.EditPlayerProfileDialog
import com.example.ui.components.InteractiveGlassCard
import com.example.ui.components.RcbCrestLogo
import com.example.ui.components.liquid3DEntrance
import com.example.ui.theme.LiquidGlassTheme
import com.example.viewmodel.CricketViewModel

@Composable
fun SquadScreen(
    viewModel: CricketViewModel,
    onNavigateToProfile: (Long) -> Unit,
    onNavigateToComparison: (Long) -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    val players by viewModel.allPlayers.collectAsStateWithLifecycle()
    val rankings by viewModel.rankings.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableStateOf(0) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingPlayer by remember { mutableStateOf<PlayerEntity?>(null) }

    val roles = remember { listOf<PlayerRole?>(null) + PlayerRole.values().toList() }
    val selectedFilterRole = roles.getOrNull(selectedFilterIndex)

    val filteredPlayers = remember(players, searchQuery, selectedFilterRole) {
        players.filter { player ->
            val matchesQuery = player.fullName.contains(searchQuery, ignoreCase = true)
            val matchesRole = selectedFilterRole == null || player.role == selectedFilterRole
            matchesQuery && matchesRole
        }
    }

    val roleTabLabels = remember(players) {
        listOf("All (${players.size})") + PlayerRole.values().map { role ->
            val count = players.count { it.role == role }
            "${role.displayName} ($count)"
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = tokens.accentGold,
                contentColor = tokens.buttonContent,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Player")
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Row with RCB Crest and Compare action
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RcbCrestLogo(size = 42.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "My Squad",
                                color = tokens.textPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 21.sp
                            )
                            Text(
                                text = "${players.size} Rajpoot Cricket Boys • Tap for full stats",
                                color = tokens.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Compare Players button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(tokens.glassCardElevated)
                            .border(1.dp, tokens.accentGold, RoundedCornerShape(18.dp))
                            .clickable { onNavigateToComparison(0L) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = "Compare",
                                tint = tokens.accentGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Compare",
                                color = tokens.accentGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search player by name or jersey...", color = tokens.textSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = tokens.accentGold) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = tokens.textSecondary)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tokens.accentGold,
                        unfocusedBorderColor = tokens.glassCardBorder,
                        focusedContainerColor = tokens.glassCardBackground,
                        unfocusedContainerColor = tokens.glassCardBackground,
                        focusedTextColor = tokens.textPrimary,
                        unfocusedTextColor = tokens.textPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cyan Pill Role Filter Tabs
                CyanPillTabs(
                    tabs = roleTabLabels,
                    selectedIndex = selectedFilterIndex,
                    onTabSelected = { selectedFilterIndex = it }
                )
            }

            // Player List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 84.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(filteredPlayers, key = { _, p -> p.id }) { index, player ->
                    val stats = rankings.find { it.playerName.equals(player.fullName, ignoreCase = true) }
                    PlayerCard(
                        player = player,
                        stats = stats,
                        index = index,
                        onClick = { onNavigateToProfile(player.id) },
                        onEdit = { editingPlayer = player },
                        onDelete = { viewModel.deletePlayer(player.id) }
                    )
                }
            }
        }
    }

    // Add Player Dialog
    if (showAddDialog) {
        val newPlayer = remember {
            PlayerEntity(
                fullName = "",
                jerseyNumber = 0,
                role = PlayerRole.ALL_ROUNDER
            )
        }
        EditPlayerProfileDialog(
            player = newPlayer,
            onDismiss = { showAddDialog = false },
            onSave = { created ->
                viewModel.insertPlayer(created)
                showAddDialog = false
            }
        )
    }

    // Edit Player Dialog
    if (editingPlayer != null) {
        EditPlayerProfileDialog(
            player = editingPlayer!!,
            onDismiss = { editingPlayer = null },
            onSave = { updated ->
                viewModel.updatePlayer(updated)
                editingPlayer = null
            }
        )
    }
}

@Composable
fun PlayerCard(
    player: PlayerEntity,
    stats: PlayerStats?,
    index: Int = 0,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    InteractiveGlassCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .liquid3DEntrance(delayMs = (index * 25).coerceAtMost(200)),
        borderColor = tokens.glassCardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Jersey / Avatar / Photo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(tokens.glassCardElevated)
                    .border(1.5.dp, tokens.accentGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!player.photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = player.photoUri,
                        contentDescription = player.fullName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else if (player.jerseyNumber > 0) {
                    Text(
                        text = "#${player.jerseyNumber}",
                        color = tokens.accentGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = tokens.accentGold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.fullName,
                        color = tokens.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (player.isCaptain) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(tokens.accentGold)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text("C", color = tokens.buttonContent, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp)
                        }
                    } else if (player.isViceCaptain) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(tokens.glassCardElevated)
                                .border(0.5.dp, tokens.accentGold, RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text("VC", color = tokens.accentGold, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        }
                    }
                }

                Text(
                    text = "${player.role.displayName} • ${player.battingHand.displayName}",
                    color = tokens.textSecondary,
                    fontSize = 12.sp
                )

                // Quick stats summary
                if (stats != null && (stats.runs > 0 || stats.wickets > 0)) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${stats.runs} runs (HS: ${stats.highestScore})  •  ${stats.wickets} wkts",
                        color = tokens.accentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tap to view profile & match history",
                        color = tokens.textMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Edit & Delete actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = tokens.textSecondary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0x99FF5252), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
