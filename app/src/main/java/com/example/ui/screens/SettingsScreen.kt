package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.findActivity
import com.example.data.model.OverlayStyle
import com.example.data.repository.GlassThemeMode
import com.example.ui.components.GlassCard
import com.example.ui.components.GoldButton
import com.example.ui.components.LiquidGlassThemeToggle
import com.example.ui.components.OutlinedGoldButton
import com.example.ui.components.liquid3DEntrance
import com.example.ui.theme.AccentFour
import com.example.ui.theme.AccentWicket
import com.example.ui.theme.LiquidGlassTheme
import com.example.viewmodel.CricketViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CricketViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokens = LiquidGlassTheme.tokens

    val currentGlassTheme by viewModel.glassThemeSetting.collectAsStateWithLifecycle()
    val currentTeamName by viewModel.teamNameSetting.collectAsStateWithLifecycle()
    val currentOverlayStyle by viewModel.overlayStyleSetting.collectAsStateWithLifecycle()
    val overlayScale by viewModel.overlayScaleSetting.collectAsStateWithLifecycle()
    val muteAudio by viewModel.muteAudioSetting.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.preferencesRepository.hapticFeedbackFlow.collectAsStateWithLifecycle(true)
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAuthenticating by viewModel.isAuthenticating.collectAsStateWithLifecycle()
    val authStatusMessage by viewModel.authStatusMessage.collectAsStateWithLifecycle()

    var teamNameInput by remember(currentTeamName) { mutableStateOf(currentTeamName) }
    var styleExpanded by remember { mutableStateOf(false) }
    var isCloudSyncing by remember { mutableStateOf(false) }

    var showExportDialog by remember { mutableStateOf(false) }
    var jsonExportContent by remember { mutableStateOf("") }
    var jsonImportText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    com.example.ui.components.RcbCrestLogo(size = 40.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Settings & Club Admin",
                            color = tokens.textPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 21.sp
                        )
                        Text(
                            text = "Google Cloud Sync, Midnight Navy Theme & Broadcast Controls",
                            color = tokens.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                LiquidGlassThemeToggle(
                    currentMode = currentGlassTheme,
                    onToggle = { viewModel.setGlassTheme(it) }
                )
            }
        }

        // Section: Google Account & Cloud Sync (Placed prominently at top for easy access)
        item {
            Text(
                text = "GOOGLE ACCOUNT & CLOUD SYNC",
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
                borderColor = tokens.accentGold,
                borderWidth = 1.5.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (currentUser == null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(tokens.glassCardInner)
                                    .border(1.dp, tokens.accentGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Google Account",
                                    tint = tokens.accentGold,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Google Account Sign-In & Cloud Backup",
                                    color = tokens.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Authenticate with Google to sync Rajpoot Cricket Boys squad, matches & statistics across devices.",
                                    color = tokens.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (!authStatusMessage.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x22FF3B5C))
                                    .border(1.dp, AccentWicket, RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = authStatusMessage!!,
                                    color = Color(0xFFFFB2C0),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        if (isAuthenticating) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = tokens.accentGold,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Connecting to Google Identity Services...",
                                    color = tokens.accentGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            GoldButton(
                                text = "Sign in with Google",
                                onClick = {
                                    val act = context.findActivity()
                                    if (act != null) {
                                        viewModel.signInWithGoogle(act)
                                    } else {
                                        Toast.makeText(context, "Unable to resolve Activity context for Sign-In", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = null,
                                        tint = tokens.buttonContent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    } else {
                        // Signed-in state
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(tokens.accentGold.copy(alpha = 0.2f))
                                    .border(1.5.dp, tokens.accentGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "User Avatar",
                                    tint = tokens.accentGold,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser?.displayName ?: "Cricket Captain",
                                    color = tokens.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = currentUser?.email ?: "Google Account Connected",
                                    color = tokens.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x2200E676))
                                    .border(1.dp, AccentFour, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Verified", color = AccentFour, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (isCloudSyncing) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = tokens.accentGold, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Syncing with Cloud Firestore...", color = tokens.accentGold, fontSize = 12.sp)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedGoldButton(
                                    text = "Restore Cloud",
                                    onClick = {
                                        isCloudSyncing = true
                                        viewModel.restoreFromCloud { _, msg ->
                                            isCloudSyncing = false
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = tokens.accentGold, modifier = Modifier.size(16.dp))
                                    }
                                )

                                GoldButton(
                                    text = "Backup Cloud",
                                    onClick = {
                                        isCloudSyncing = true
                                        viewModel.backupToCloud { _, msg ->
                                            isCloudSyncing = false
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = tokens.buttonContent, modifier = Modifier.size(16.dp))
                                    }
                                )
                            }
                        }

                        TextButton(
                            onClick = { viewModel.signOut() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = AccentWicket, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sign Out", color = AccentWicket, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: App Theme Mode
        item {
            Text(
                text = "APP APPEARANCE & THEME",
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
                borderColor = tokens.glassBorder
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Midnight Navy & Bright Cyan Sports Theme",
                        color = tokens.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Switch between Midnight Navy with glowing Electric Cyan accents or Daylight Ice mode.",
                        color = tokens.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isDarkSelected = currentGlassTheme == GlassThemeMode.DARK
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDarkSelected) tokens.accentGold.copy(alpha = 0.2f) else tokens.glassCardInner)
                                .border(
                                    width = if (isDarkSelected) 2.dp else 1.dp,
                                    color = if (isDarkSelected) tokens.accentGold else tokens.glassBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.setGlassTheme(GlassThemeMode.DARK) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = "Midnight Navy Theme",
                                    tint = if (isDarkSelected) tokens.accentGold else tokens.textSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Midnight Navy",
                                    color = if (isDarkSelected) tokens.accentGold else tokens.textPrimary,
                                    fontWeight = if (isDarkSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Cyan Glow",
                                    color = tokens.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        val isWhiteSelected = currentGlassTheme == GlassThemeMode.WHITE
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isWhiteSelected) tokens.accentGold.copy(alpha = 0.2f) else tokens.glassCardInner)
                                .border(
                                    width = if (isWhiteSelected) 2.dp else 1.dp,
                                    color = if (isWhiteSelected) tokens.accentGold else tokens.glassBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.setGlassTheme(GlassThemeMode.WHITE) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.LightMode,
                                    contentDescription = "Crystal Aqua Theme",
                                    tint = if (isWhiteSelected) tokens.accentGold else tokens.textSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Crystal Aqua",
                                    color = if (isWhiteSelected) tokens.accentGold else tokens.textPrimary,
                                    fontWeight = if (isWhiteSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Daylight Ice",
                                    color = tokens.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        val isSystemSelected = currentGlassTheme == GlassThemeMode.SYSTEM
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSystemSelected) tokens.accentGold.copy(alpha = 0.2f) else tokens.glassCardInner)
                                .border(
                                    width = if (isSystemSelected) 2.dp else 1.dp,
                                    color = if (isSystemSelected) tokens.accentGold else tokens.glassBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.setGlassTheme(GlassThemeMode.SYSTEM) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Brightness4,
                                    contentDescription = "System Theme",
                                    tint = if (isSystemSelected) tokens.accentGold else tokens.textSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Auto System",
                                    color = if (isSystemSelected) tokens.accentGold else tokens.textPrimary,
                                    fontWeight = if (isSystemSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "OS Dynamic",
                                    color = tokens.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Team & Identity
        item {
            Text("CLUB BRANDING", color = tokens.accentGold, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = tokens.glassCardBackground, borderColor = tokens.glassBorder) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = teamNameInput,
                        onValueChange = { teamNameInput = it },
                        label = { Text("Club / Team Name", color = tokens.textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = tokens.textPrimary,
                            unfocusedTextColor = tokens.textPrimary,
                            focusedBorderColor = tokens.accentGold,
                            unfocusedBorderColor = tokens.glassBorder
                        )
                    )

                    GoldButton(
                        text = "Save Team Name",
                        onClick = {
                            scope.launch {
                                viewModel.preferencesRepository.setTeamName(teamNameInput.trim())
                                Toast.makeText(context, "Team name updated", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = teamNameInput.isNotBlank() && teamNameInput != currentTeamName
                    )
                }
            }
        }

        // Section: Scoreboard Overlay Settings
        item {
            Text("BROADCAST SCOREBOARD OVERLAY", color = tokens.accentGold, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = tokens.glassCardBackground, borderColor = tokens.glassBorder) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = styleExpanded,
                        onExpandedChange = { styleExpanded = !styleExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentOverlayStyle.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Scoreboard Layout", color = tokens.textSecondary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = styleExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = tokens.textPrimary,
                                unfocusedTextColor = tokens.textPrimary,
                                focusedBorderColor = tokens.accentGold,
                                unfocusedBorderColor = tokens.glassBorder
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = styleExpanded,
                            onDismissRequest = { styleExpanded = false },
                            modifier = Modifier.background(tokens.glassCardBackground)
                        ) {
                            OverlayStyle.values().forEach { style ->
                                DropdownMenuItem(
                                    text = { Text(style.displayName, color = tokens.textPrimary) },
                                    onClick = {
                                        scope.launch { viewModel.preferencesRepository.setOverlayStyle(style) }
                                        styleExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Column {
                        Text("Overlay Scale: ${String.format("%.1f", overlayScale)}x", color = tokens.textPrimary, fontSize = 13.sp)
                        Slider(
                            value = overlayScale,
                            onValueChange = { scale ->
                                scope.launch { viewModel.preferencesRepository.setOverlayScale(scale) }
                            },
                            valueRange = 0.8f..1.4f,
                            steps = 5,
                            colors = SliderDefaults.colors(thumbColor = tokens.accentGold, activeTrackColor = tokens.accentGold)
                        )
                    }
                }
            }
        }

        // Section: Camera & Recording Settings
        item {
            Text("CAMERA & AUDIO RECORDING", color = tokens.accentGold, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = tokens.glassCardBackground, borderColor = tokens.glassBorder) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mute Audio in Recordings", color = tokens.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Record silent cricket match video footage", color = tokens.textSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = muteAudio,
                            onCheckedChange = { mute ->
                                scope.launch { viewModel.preferencesRepository.setMuteAudio(mute) }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = tokens.accentGold, checkedTrackColor = tokens.accentGold.copy(alpha = 0.4f))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Haptic Touch Feedback", color = tokens.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Vibrate on boundaries and wickets", color = tokens.textSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = { haptic ->
                                scope.launch { viewModel.preferencesRepository.setHapticFeedback(haptic) }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = tokens.accentGold, checkedTrackColor = tokens.accentGold.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }

        // Section: Local JSON Backup
        item {
            Text("LOCAL DATA & BACKUP", color = tokens.accentGold, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = tokens.glassCardBackground, borderColor = tokens.glassBorder) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Instant On-Device Storage & JSON Export",
                        color = tokens.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "All match scorecards, squad rosters and video recordings are stored on-device with Room SQLite and can be exported or synced to Cloud Firestore.",
                        color = tokens.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedGoldButton(
                            text = "Import JSON",
                            onClick = { showImportDialog = true },
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = tokens.accentGold, modifier = Modifier.size(16.dp)) }
                        )

                        GoldButton(
                            text = "Export Backup",
                            onClick = {
                                scope.launch {
                                    val firstMatch = viewModel.allMatches.value.firstOrNull()
                                    jsonExportContent = if (firstMatch != null) {
                                        viewModel.repository.exportMatchJson(firstMatch.id)
                                    } else {
                                        "{\"app\":\"Rajpoot Cricket Boys\",\"status\":\"ready\"}"
                                    }
                                    showExportDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Default.Upload, contentDescription = null, tint = tokens.buttonContent, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }

        // App Info
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = tokens.glassCardBackground, borderColor = tokens.glassBorder) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rajpoot Cricket Boys — Official Club Edition", color = tokens.accentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Version 2.0 (Midnight Navy & Bright Cyan Pro Edition)", color = tokens.textSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Engineered for real cricket clubs, live ball-by-ball scoring, pitch video recording, and broadcast compositing.", color = tokens.textSecondary, fontSize = 11.sp)
                }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = tokens.glassCardBackground,
            title = { Text("Backup Data", color = tokens.accentGold, fontWeight = FontWeight.Bold) },
            text = {
                Text("Match data and scorecards can be safely shared and restored across devices.", color = tokens.textSecondary)
            },
            confirmButton = {
                GoldButton(
                    text = "Share JSON",
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, jsonExportContent)
                            type = "application/json"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export Cricket Data"))
                        showExportDialog = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Close", color = tokens.textSecondary) }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = tokens.glassCardBackground,
            title = { Text("Import Match JSON", color = tokens.accentGold, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = jsonImportText,
                    onValueChange = { jsonImportText = it },
                    label = { Text("Paste Match JSON Here", color = tokens.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = tokens.textPrimary,
                        unfocusedTextColor = tokens.textPrimary,
                        focusedBorderColor = tokens.accentGold,
                        unfocusedBorderColor = tokens.glassBorder
                    )
                )
            },
            confirmButton = {
                GoldButton(
                    text = "Import",
                    onClick = {
                        scope.launch {
                            val res = viewModel.repository.importMatchJson(jsonImportText)
                            if (res.isSuccess) {
                                Toast.makeText(context, "Match imported successfully", Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                            } else {
                                Toast.makeText(context, "Invalid JSON format", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = jsonImportText.isNotBlank()
                )
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancel", color = tokens.textSecondary) }
            }
        )
    }
}
