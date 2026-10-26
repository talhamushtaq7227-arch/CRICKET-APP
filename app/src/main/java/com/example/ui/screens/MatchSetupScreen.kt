package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.data.model.MatchFormat
import com.example.data.model.PlayerEntity
import com.example.data.model.TossDecision
import com.example.ui.components.GlassCard
import com.example.ui.components.GoldButton
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
fun MatchSetupScreen(
    viewModel: CricketViewModel,
    onBack: () -> Unit,
    onMatchCreated: (Long) -> Unit
) {
    val defaultTeamName by viewModel.teamNameSetting.collectAsStateWithLifecycle()
    val allPlayers by viewModel.allPlayers.collectAsStateWithLifecycle()

    var matchTitle by remember { mutableStateOf("Rajpoot Cricket Boys vs Challengers") }
    var tournamentName by remember { mutableStateOf("Champions Trophy 2026") }
    var season by remember { mutableStateOf("2026") }
    var homeTeam by remember { mutableStateOf(defaultTeamName) }
    var awayTeam by remember { mutableStateOf("Lahore Strikers") }
    var venue by remember { mutableStateOf("RCB Cricket Stadium") }
    var format by remember { mutableStateOf(MatchFormat.T20) }
    var totalOversText by remember { mutableStateOf("20") }
    var playersPerSideText by remember { mutableStateOf("11") }

    var tossWinner by remember { mutableStateOf(homeTeam) }
    var tossDecision by remember { mutableStateOf(TossDecision.BAT) }

    // Squad selection for Home team
    val selectedPlayerIds = remember {
        mutableStateOf(allPlayers.take(11).map { it.id }.toSet())
    }

    var formatExpanded by remember { mutableStateOf(false) }
    var tossWinnerExpanded by remember { mutableStateOf(false) }
    var tossDecisionExpanded by remember { mutableStateOf(false) }

    val battingFirstTeam = if (tossDecision == TossDecision.BAT) tossWinner else {
        if (tossWinner == homeTeam) awayTeam else homeTeam
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Setup Wizard", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBlack)
            )
        },
        containerColor = CharcoalBlack
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "TEAMS & VENUE",
                    color = GoldBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xCC161622)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = homeTeam,
                            onValueChange = {
                                homeTeam = it
                                if (tossWinner.isEmpty()) tossWinner = it
                            },
                            label = { Text("Home Team") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = awayTeam,
                            onValueChange = { awayTeam = it },
                            label = { Text("Opponent Team") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = venue,
                            onValueChange = { venue = it },
                            label = { Text("Match Venue") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = tournamentName,
                            onValueChange = { tournamentName = it },
                            label = { Text("Tournament / Series (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        OutlinedTextField(
                            value = season,
                            onValueChange = { season = it },
                            label = { Text("Season Year (e.g. 2026)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "MATCH FORMAT & OVERS",
                    color = GoldBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xCC161622)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Format Selector
                        ExposedDropdownMenuBox(
                            expanded = formatExpanded,
                            onExpandedChange = { formatExpanded = !formatExpanded }
                        ) {
                            OutlinedTextField(
                                value = format.displayName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Format") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            ExposedDropdownMenu(
                                expanded = formatExpanded,
                                onDismissRequest = { formatExpanded = false },
                                modifier = Modifier.background(CharcoalElevated)
                            ) {
                                MatchFormat.values().forEach { fmt ->
                                    DropdownMenuItem(
                                        text = { Text(fmt.displayName, color = Color.White) },
                                        onClick = {
                                            format = fmt
                                            if (fmt != MatchFormat.CUSTOM) {
                                                totalOversText = fmt.defaultOvers.toString()
                                            }
                                            formatExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = totalOversText,
                                onValueChange = { totalOversText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Total Overs") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )

                            OutlinedTextField(
                                value = playersPerSideText,
                                onValueChange = { playersPerSideText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Players Per Side") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "TOSS & BATTING ORDER",
                    color = GoldBright,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xCC161622)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Toss Winner
                        ExposedDropdownMenuBox(
                            expanded = tossWinnerExpanded,
                            onExpandedChange = { tossWinnerExpanded = !tossWinnerExpanded }
                        ) {
                            OutlinedTextField(
                                value = tossWinner,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Toss Won By") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tossWinnerExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            ExposedDropdownMenu(
                                expanded = tossWinnerExpanded,
                                onDismissRequest = { tossWinnerExpanded = false },
                                modifier = Modifier.background(CharcoalElevated)
                            ) {
                                listOf(homeTeam, awayTeam).forEach { team ->
                                    DropdownMenuItem(
                                        text = { Text(team, color = Color.White) },
                                        onClick = {
                                            tossWinner = team
                                            tossWinnerExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Toss Decision
                        ExposedDropdownMenuBox(
                            expanded = tossDecisionExpanded,
                            onExpandedChange = { tossDecisionExpanded = !tossDecisionExpanded }
                        ) {
                            OutlinedTextField(
                                value = tossDecision.displayName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Elected To") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tossDecisionExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            ExposedDropdownMenu(
                                expanded = tossDecisionExpanded,
                                onDismissRequest = { tossDecisionExpanded = false },
                                modifier = Modifier.background(CharcoalElevated)
                            ) {
                                TossDecision.values().forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d.displayName, color = Color.White) },
                                        onClick = {
                                            tossDecision = d
                                            tossDecisionExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33D4AF37))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Batting First: $battingFirstTeam",
                                color = GoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT SQUAD MEMBERS (${selectedPlayerIds.value.size})",
                        color = GoldBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            items(allPlayers) { player ->
                val isSelected = selectedPlayerIds.value.contains(player.id)
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val set = selectedPlayerIds.value.toMutableSet()
                            if (isSelected) set.remove(player.id) else set.add(player.id)
                            selectedPlayerIds.value = set
                        },
                    backgroundColor = if (isSelected) Color(0x33D4AF37) else Color(0x99161620),
                    borderColor = if (isSelected) GoldPrimary else Color(0x22D4AF37)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                val set = selectedPlayerIds.value.toMutableSet()
                                if (checked) set.add(player.id) else set.remove(player.id)
                                selectedPlayerIds.value = set
                            },
                            colors = CheckboxDefaults.colors(checkedColor = GoldPrimary, checkmarkColor = CharcoalBlack)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${player.fullName} (#${player.jerseyNumber})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${player.role.displayName} • ${player.battingHand.displayName}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                GoldButton(
                    text = "Start Match & Live Scoring",
                    onClick = {
                        val overs = totalOversText.toIntOrNull() ?: 20
                        val playersCount = playersPerSideText.toIntOrNull() ?: 11
                        viewModel.createMatch(
                            title = "$homeTeam vs $awayTeam",
                            tournament = tournamentName,
                            season = season,
                            homeTeam = homeTeam,
                            awayTeam = awayTeam,
                            venue = venue,
                            format = format,
                            overs = overs,
                            playersPerSide = playersCount,
                            tossWinner = tossWinner,
                            tossDecision = tossDecision,
                            battingFirst = battingFirstTeam,
                            selectedSquadIds = selectedPlayerIds.value.toList(),
                            awayPlayers = listOf("Opener 1", "Opener 2", "Player 3", "Player 4", "Player 5"),
                            onCreated = onMatchCreated
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = homeTeam.isNotBlank() && awayTeam.isNotBlank() && (totalOversText.toIntOrNull() ?: 0) > 0
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
