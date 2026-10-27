package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.TossDecision
import com.example.ui.components.GlassCard
import com.example.ui.components.GoldButton
import com.example.ui.components.liquid3DEntrance
import com.example.ui.theme.AccentWicket
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LiquidGlassTheme
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CricketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSetupScreen(
    viewModel: CricketViewModel,
    onBack: () -> Unit,
    onMatchCreated: (Long) -> Unit
) {
    val tokens = LiquidGlassTheme.tokens
    val defaultTeamName by viewModel.teamNameSetting.collectAsStateWithLifecycle()
    val allPlayers by viewModel.allPlayers.collectAsStateWithLifecycle()

    var tournamentName by remember { mutableStateOf("Champions Trophy 2026") }
    var season by remember { mutableStateOf("2026") }
    var homeTeam by remember(defaultTeamName) { mutableStateOf(defaultTeamName) }
    var awayTeam by remember { mutableStateOf("Lahore Strikers") }
    var venue by remember { mutableStateOf("RCB Cricket Stadium") }
    var format by remember { mutableStateOf(MatchFormat.T20) }
    var totalOversText by remember { mutableStateOf("20") }
    var playersPerSideText by remember { mutableStateOf("11") }

    var tossWinner by remember(homeTeam) { mutableStateOf(homeTeam) }
    var tossDecision by remember { mutableStateOf(TossDecision.BAT) }

    var openingStriker by remember { mutableStateOf("") }
    var openingNonStriker by remember { mutableStateOf("") }
    var openingBowler by remember { mutableStateOf("") }
    var setupError by remember { mutableStateOf<String?>(null) }

    val selectedPlayerIds = remember {
        mutableStateOf(emptySet<Long>())
    }

    // Automatically pre-select up to 11 players and default openers once allPlayers loads
    LaunchedEffect(allPlayers) {
        if (allPlayers.isNotEmpty()) {
            if (selectedPlayerIds.value.isEmpty()) {
                selectedPlayerIds.value = allPlayers.take(11).map { it.id }.toSet()
            }
            if (openingStriker.isEmpty()) {
                openingStriker = allPlayers.getOrNull(0)?.fullName.orEmpty()
            }
            if (openingNonStriker.isEmpty()) {
                openingNonStriker = allPlayers.getOrNull(1)?.fullName.orEmpty()
            }
            if (openingBowler.isEmpty()) {
                openingBowler = allPlayers.lastOrNull()?.fullName.orEmpty()
            }
        }
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
                title = { Text("Match Setup & Lineup", color = tokens.textPrimary, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = tokens.accentGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "TEAMS & STADIUM VENUE",
                    color = tokens.accentGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquid3DEntrance(),
                    backgroundColor = tokens.glassCardBackground,
                    borderColor = tokens.glassCardBorder
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = homeTeam,
                            onValueChange = {
                                homeTeam = it
                                if (tossWinner.isEmpty()) tossWinner = it
                            },
                            label = { Text("Home Team") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = tokens.textPrimary,
                                unfocusedTextColor = tokens.textPrimary,
                                focusedBorderColor = tokens.accentGold
                            )
                        )

                        OutlinedTextField(
                            value = awayTeam,
                            onValueChange = { awayTeam = it },
                            label = { Text("Opponent Team") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = tokens.textPrimary,
                                unfocusedTextColor = tokens.textPrimary,
                                focusedBorderColor = tokens.accentGold
                            )
                        )

                        OutlinedTextField(
                            value = venue,
                            onValueChange = { venue = it },
                            label = { Text("Stadium / Venue") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = tokens.textPrimary,
                                unfocusedTextColor = tokens.textPrimary,
                                focusedBorderColor = tokens.accentGold
                            )
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = tournamentName,
                                onValueChange = { tournamentName = it },
                                label = { Text("Tournament / Series") },
                                singleLine = true,
                                modifier = Modifier.weight(1.4f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = tokens.textPrimary,
                                    unfocusedTextColor = tokens.textPrimary,
                                    focusedBorderColor = tokens.accentGold
                                )
                            )

                            OutlinedTextField(
                                value = season,
                                onValueChange = { season = it },
                                label = { Text("Season") },
                                singleLine = true,
                                modifier = Modifier.weight(0.6f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = tokens.textPrimary,
                                    unfocusedTextColor = tokens.textPrimary,
                                    focusedBorderColor = tokens.accentGold
                                )
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "OPENING BATSMEN & BOWLER",
                    color = tokens.accentGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = tokens.glassCardBackground,
                    borderColor = tokens.glassCardBorder
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Set initial Striker, Non-Striker & Opening Bowler (you can also change them anytime during the match):",
                            color = tokens.textSecondary,
                            fontSize = 12.sp
                        )

                        OutlinedTextField(
                            value = openingStriker,
                            onValueChange = {
                                openingStriker = it
                                setupError = null
                            },
                            label = { Text("Opening Striker *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = tokens.textPrimary,
                                unfocusedTextColor = tokens.textPrimary,
                                focusedBorderColor = tokens.accentGold
                            )
                        )

                        OutlinedTextField(
                            value = openingNonStriker,
                            onValueChange = {
                                openingNonStriker = it
                                setupError = null
                            },
                            label = { Text("Opening Non-Striker") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = tokens.textPrimary,
                                unfocusedTextColor = tokens.textPrimary,
                                focusedBorderColor = tokens.accentGold
                            )
                        )

                        OutlinedTextField(
                            value = openingBowler,
                            onValueChange = {
                                openingBowler = it
                                setupError = null
                            },
                            label = { Text("Opening Bowler") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = tokens.textPrimary,
                                unfocusedTextColor = tokens.textPrimary,
                                focusedBorderColor = tokens.accentGold
                            )
                        )

                        if (setupError != null) {
                            Text(setupError!!, color = AccentWicket, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "MATCH FORMAT, OVERS & TOSS",
                    color = tokens.accentGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = tokens.glassCardBackground,
                    borderColor = tokens.glassCardBorder
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = tokens.textPrimary,
                                    unfocusedTextColor = tokens.textPrimary
                                )
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
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = tokens.textPrimary,
                                    unfocusedTextColor = tokens.textPrimary
                                )
                            )

                            OutlinedTextField(
                                value = playersPerSideText,
                                onValueChange = { playersPerSideText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Players Per Side") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = tokens.textPrimary,
                                    unfocusedTextColor = tokens.textPrimary
                                )
                            )
                        }

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
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = tokens.textPrimary,
                                    unfocusedTextColor = tokens.textPrimary
                                )
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
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = tokens.textPrimary,
                                    unfocusedTextColor = tokens.textPrimary
                                )
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x2800E5FF))
                                .border(1.dp, tokens.accentGold, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Batting First: $battingFirstTeam",
                                color = tokens.accentGoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "SELECT SQUAD LINEUP (${selectedPlayerIds.value.size} SELECTED)",
                    color = tokens.accentGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            items(allPlayers, key = { it.id }) { player ->
                val isSelected = selectedPlayerIds.value.contains(player.id)
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val set = selectedPlayerIds.value.toMutableSet()
                            if (isSelected) set.remove(player.id) else set.add(player.id)
                            selectedPlayerIds.value = set
                        },
                    backgroundColor = if (isSelected) Color(0x2800E5FF) else tokens.glassCardBackground,
                    borderColor = if (isSelected) tokens.accentGold else tokens.glassCardBorderSubtle
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
                                color = tokens.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${player.role.displayName} • ${player.battingHand.displayName}",
                                color = tokens.textSecondary,
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
                        val s = openingStriker.trim()
                        val ns = openingNonStriker.trim()
                        if (s.isNotEmpty() && ns.isNotEmpty() && s.equals(ns, ignoreCase = true)) {
                            setupError = "Opening Striker and Non-Striker cannot be the same player."
                            return@GoldButton
                        }
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
                            openingStriker = s,
                            openingNonStriker = ns,
                            openingBowler = openingBowler.trim(),
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
