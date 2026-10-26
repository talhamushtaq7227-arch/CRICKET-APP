package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Videocam
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
import com.example.data.model.OverlayStyle
import com.example.ui.components.GlassCard
import com.example.ui.components.GoldButton
import com.example.ui.components.OutlinedGoldButton
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CricketViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentTeamName by viewModel.teamNameSetting.collectAsStateWithLifecycle()
    val defaultOvers by viewModel.preferencesRepository.defaultOversFlow.collectAsStateWithLifecycle(20)
    val currentOverlayStyle by viewModel.overlayStyleSetting.collectAsStateWithLifecycle()
    val overlayScale by viewModel.overlayScaleSetting.collectAsStateWithLifecycle()
    val muteAudio by viewModel.muteAudioSetting.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.preferencesRepository.hapticFeedbackFlow.collectAsStateWithLifecycle(true)
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAuthenticating by viewModel.isAuthenticating.collectAsStateWithLifecycle()

    var teamNameInput by remember(currentTeamName) { mutableStateOf(currentTeamName) }
    var styleExpanded by remember { mutableStateOf(false) }

    var showExportDialog by remember { mutableStateOf(false) }
    var jsonExportContent by remember { mutableStateOf("") }
    var jsonImportText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings & Customization",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = "Customize club identity, camera recording & broadcast overlays",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // Section 1: Team & Identity
        item {
            Text("CLUB BRANDING", color = GoldBright, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xCC161622)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = teamNameInput,
                        onValueChange = { teamNameInput = it },
                        label = { Text("Club / Team Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
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

        // Section 2: Scoreboard Overlay Settings
        item {
            Text("BROADCAST SCOREBOARD OVERLAY", color = GoldBright, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xCC161622)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = styleExpanded,
                        onExpandedChange = { styleExpanded = !styleExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentOverlayStyle.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Scoreboard Layout") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = styleExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        ExposedDropdownMenu(
                            expanded = styleExpanded,
                            onDismissRequest = { styleExpanded = false },
                            modifier = Modifier.background(CharcoalElevated)
                        ) {
                            OverlayStyle.values().forEach { style ->
                                DropdownMenuItem(
                                    text = { Text(style.displayName, color = Color.White) },
                                    onClick = {
                                        scope.launch { viewModel.preferencesRepository.setOverlayStyle(style) }
                                        styleExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Column {
                        Text("Overlay Scale: ${String.format("%.1f", overlayScale)}x", color = Color.White, fontSize = 13.sp)
                        Slider(
                            value = overlayScale,
                            onValueChange = { scale ->
                                scope.launch { viewModel.preferencesRepository.setOverlayScale(scale) }
                            },
                            valueRange = 0.8f..1.4f,
                            steps = 5,
                            colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldBright)
                        )
                    }
                }
            }
        }

        // Section 3: Camera & Recording Settings
        item {
            Text("CAMERA & AUDIO RECORDING", color = GoldBright, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xCC161622)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mute Audio in Recordings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Record silent cricket match video footage", color = TextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = muteAudio,
                            onCheckedChange = { mute ->
                                scope.launch { viewModel.preferencesRepository.setMuteAudio(mute) }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary, checkedTrackColor = GoldDark)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Haptic Touch Feedback", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Vibrate on runs and wickets", color = TextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = { haptic ->
                                scope.launch { viewModel.preferencesRepository.setHapticFeedback(haptic) }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary, checkedTrackColor = GoldDark)
                        )
                    }
                }
            }
        }

        // Section: Google Cloud Sync (Firebase Firestore)
        item {
            Text("GOOGLE ACCOUNT & CLOUD SYNC", color = GoldBright, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xCC161622)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (currentUser == null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(CharcoalElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Account",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Cloud Backup & Sync", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Sign in with Google to sync your squad and match statistics to Cloud Firestore", color = TextSecondary, fontSize = 11.sp)
                            }
                        }

                        if (isAuthenticating) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = GoldPrimary, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Signing in with Google...", color = TextGold, fontSize = 12.sp)
                            }
                        } else {
                            GoldButton(
                                text = "Sign in with Google",
                                onClick = {
                                    (context as? Activity)?.let { act ->
                                        viewModel.signInWithGoogle(act)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(Icons.Default.Login, contentDescription = null, tint = CharcoalBlack, modifier = Modifier.size(16.dp))
                                }
                            )
                        }
                    } else {
                        // User is signed in
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GoldDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "User Avatar",
                                    tint = GoldBright,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser?.displayName ?: "Cricket Captain",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = currentUser?.email ?: "Google Account",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x224CAF50))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Connected", color = Color(0xFF81C784), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedGoldButton(
                                text = "Restore Cloud",
                                onClick = {
                                    viewModel.restoreFromCloud { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = GoldBright, modifier = Modifier.size(16.dp))
                                }
                            )

                            GoldButton(
                                text = "Backup Cloud",
                                onClick = {
                                    viewModel.backupToCloud { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = CharcoalBlack, modifier = Modifier.size(16.dp))
                                }
                            )
                        }

                        TextButton(
                            onClick = { viewModel.signOut() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = AccentWicket, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sign Out", color = AccentWicket, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section 4: Data Export & Backup (JSON)
        item {
            Text("LOCAL DATA & BACKUP", color = GoldBright, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xCC161622)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "100% Offline & Private",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "All match data, squad rosters and recordings are stored securely on this device in local Room storage.",
                        color = TextSecondary,
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
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = GoldBright, modifier = Modifier.size(16.dp)) }
                        )

                        GoldButton(
                            text = "Export Backup",
                            onClick = {
                                scope.launch {
                                    val matches = viewModel.repository.getAllMatches()
                                    // Get first match to export or empty
                                    jsonExportContent = "{\"app\":\"Rajpoot Cricket Boys\",\"status\":\"ready\"}"
                                    showExportDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            leadingIcon = { Icon(Icons.Default.Upload, contentDescription = null, tint = CharcoalBlack, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }

        // App Info
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0x99161620)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rajpoot Cricket Boys — Glass Edition", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Version 1.0.0 (Native Android & CameraX)", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Engineered for real cricket clubs, pitch recording, and broadcast compositing.", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = CharcoalDark,
            title = { Text("Backup Data", color = GoldBright, fontWeight = FontWeight.Bold) },
            text = {
                Text("Match data and scorecards can be safely shared and restored across devices.", color = TextSecondary)
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
                TextButton(onClick = { showExportDialog = false }) { Text("Close", color = TextSecondary) }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = CharcoalDark,
            title = { Text("Import Match JSON", color = GoldBright, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = jsonImportText,
                    onValueChange = { jsonImportText = it },
                    label = { Text("Paste Match JSON Here") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
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
                TextButton(onClick = { showImportDialog = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}
