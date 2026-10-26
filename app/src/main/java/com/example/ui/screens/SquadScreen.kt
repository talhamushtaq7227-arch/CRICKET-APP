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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.BattingHand
import com.example.data.model.BowlingStyle
import com.example.data.model.PlayerEntity
import com.example.data.model.PlayerRole
import com.example.domain.engine.PlayerStats
import com.example.ui.components.GlassCard
import com.example.ui.components.GoldButton
import com.example.ui.theme.AccentWicket
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CricketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadScreen(
    viewModel: CricketViewModel
) {
    val players by viewModel.allPlayers.collectAsStateWithLifecycle()
    val rankings by viewModel.rankings.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterRole by remember { mutableStateOf<PlayerRole?>(null) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingPlayer by remember { mutableStateOf<PlayerEntity?>(null) }
    var viewingPlayerStats by remember { mutableStateOf<Pair<PlayerEntity, PlayerStats?>?>(null) }

    val filteredPlayers = players.filter { player ->
        val matchesQuery = player.fullName.contains(searchQuery, ignoreCase = true)
        val matchesRole = selectedFilterRole == null || player.role == selectedFilterRole
        matchesQuery && matchesRole
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GoldPrimary,
                contentColor = CharcoalBlack,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Player")
            }
        },
        containerColor = CharcoalBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Squad Management",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = "${players.size} registered players • Real stats computed from scorecards",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search player by name...", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldLight) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color(0x44D4AF37),
                        focusedContainerColor = CharcoalDark,
                        unfocusedContainerColor = CharcoalDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Role Filter Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            label = "All (${players.size})",
                            isSelected = selectedFilterRole == null,
                            onClick = { selectedFilterRole = null }
                        )
                    }
                    items(PlayerRole.values()) { role ->
                        val count = players.count { it.role == role }
                        FilterChip(
                            label = "${role.displayName} ($count)",
                            isSelected = selectedFilterRole == role,
                            onClick = { selectedFilterRole = role }
                        )
                    }
                }
            }

            // Player List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPlayers) { player ->
                    val stats = rankings.find { it.playerName.equals(player.fullName, ignoreCase = true) }
                    PlayerCard(
                        player = player,
                        stats = stats,
                        onClick = { viewingPlayerStats = Pair(player, stats) },
                        onEdit = { editingPlayer = player },
                        onDelete = { viewModel.deletePlayer(player.id) }
                    )
                }
            }
        }
    }

    // Add / Edit Player Dialog
    if (showAddDialog || editingPlayer != null) {
        val player = editingPlayer
        PlayerEditDialog(
            initialPlayer = player,
            onDismiss = {
                showAddDialog = false
                editingPlayer = null
            },
            onSave = { name, jersey, role, hand, style, notes ->
                if (player == null) {
                    viewModel.addPlayer(name, jersey, role, hand, style, notes)
                } else {
                    viewModel.updatePlayer(
                        player.copy(
                            fullName = name,
                            jerseyNumber = jersey,
                            role = role,
                            battingHand = hand,
                            bowlingStyle = style,
                            notes = notes
                        )
                    )
                }
                showAddDialog = false
                editingPlayer = null
            }
        )
    }

    // Player Profile & Detailed Stats Dialog
    if (viewingPlayerStats != null) {
        val (player, stats) = viewingPlayerStats!!
        PlayerProfileDialog(
            player = player,
            stats = stats,
            onDismiss = { viewingPlayerStats = null }
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) GoldPrimary else CharcoalElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) CharcoalBlack else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun PlayerCard(
    player: PlayerEntity,
    stats: PlayerStats?,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = Color(0xCC161622),
        borderColor = Color(0x33D4AF37)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Jersey / Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x33D4AF37)),
                contentAlignment = Alignment.Center
            ) {
                if (player.jerseyNumber > 0) {
                    Text(
                        text = "#${player.jerseyNumber}",
                        color = GoldBright,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null, tint = GoldLight)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.fullName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${player.role.displayName} • ${player.battingHand.displayName}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                // Quick stats summary
                if (stats != null && (stats.runs > 0 || stats.wickets > 0)) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${stats.runs} runs (HS: ${stats.highestScore}) | ${stats.wickets} wkts",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "No match data yet",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Edit & Delete actions
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0x88FF5252), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerEditDialog(
    initialPlayer: PlayerEntity?,
    onDismiss: () -> Unit,
    onSave: (String, Int, PlayerRole, BattingHand, BowlingStyle, String?) -> Unit
) {
    var name by remember { mutableStateOf(initialPlayer?.fullName ?: "") }
    var jerseyText by remember { mutableStateOf(initialPlayer?.jerseyNumber?.takeIf { it > 0 }?.toString() ?: "") }
    var role by remember { mutableStateOf(initialPlayer?.role ?: PlayerRole.ALL_ROUNDER) }
    var hand by remember { mutableStateOf(initialPlayer?.battingHand ?: BattingHand.RIGHT_HAND) }
    var style by remember { mutableStateOf(initialPlayer?.bowlingStyle ?: BowlingStyle.RIGHT_ARM_MEDIUM) }
    var notes by remember { mutableStateOf(initialPlayer?.notes ?: "") }

    var roleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalDark,
        title = {
            Text(
                text = if (initialPlayer == null) "Add Squad Player" else "Edit Player",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = jerseyText,
                    onValueChange = { jerseyText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jersey Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                // Role Dropdown
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        value = role.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Primary Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false },
                        modifier = Modifier.background(CharcoalElevated)
                    ) {
                        PlayerRole.values().forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.displayName, color = Color.White) },
                                onClick = {
                                    role = r
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            GoldButton(
                text = "Save",
                onClick = {
                    if (name.isNotBlank()) {
                        val jersey = jerseyText.toIntOrNull() ?: 0
                        onSave(name.trim(), jersey, role, hand, style, notes)
                    }
                },
                enabled = name.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun PlayerProfileDialog(
    player: PlayerEntity,
    stats: PlayerStats?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("#${player.jerseyNumber}", color = CharcoalBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(player.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(player.role.displayName, color = GoldLight, fontSize = 12.sp)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("BATTING CAREER", color = GoldBright, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatMetricItem("Innings", "${stats?.inningsBatted ?: 0}")
                    StatMetricItem("Runs", "${stats?.runs ?: 0}")
                    StatMetricItem("HS", "${stats?.highestScore ?: 0}")
                    StatMetricItem("Avg", stats?.formattedBattingAverage ?: "—")
                    StatMetricItem("SR", stats?.formattedStrikeRate ?: "—")
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatMetricItem("Fours", "${stats?.fours ?: 0}")
                    StatMetricItem("Sixes", "${stats?.sixes ?: 0}")
                    StatMetricItem("50s", "${stats?.fifties ?: 0}")
                    StatMetricItem("100s", "${stats?.hundreds ?: 0}")
                    StatMetricItem("Not Out", "${stats?.notOuts ?: 0}")
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("BOWLING CAREER", color = GoldBright, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatMetricItem("Overs", stats?.formattedOversBowled ?: "0.0")
                    StatMetricItem("Wickets", "${stats?.wickets ?: 0}")
                    StatMetricItem("Econ", stats?.formattedBowlingEconomy ?: "—")
                    StatMetricItem("Best", stats?.bestBowlingFigures ?: "—")
                    StatMetricItem("Runs", "${stats?.runsConceded ?: 0}")
                }
            }
        },
        confirmButton = {
            GoldButton(text = "Close", onClick = onDismiss)
        }
    )
}

@Composable
fun StatMetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = label, color = TextMuted, fontSize = 10.sp)
    }
}
