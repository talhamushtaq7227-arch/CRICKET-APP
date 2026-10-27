package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.BattingHand
import com.example.data.model.BowlingStyle
import com.example.data.model.PlayerEntity
import com.example.data.model.PlayerRole
import com.example.ui.theme.CharcoalBlack
import com.example.ui.theme.CharcoalDark
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlayerProfileDialog(
    player: PlayerEntity,
    onDismiss: () -> Unit,
    onSave: (PlayerEntity) -> Unit
) {
    var name by remember { mutableStateOf(player.fullName) }
    var jerseyText by remember { mutableStateOf(if (player.jerseyNumber > 0) player.jerseyNumber.toString() else "") }
    var role by remember { mutableStateOf(player.role) }
    var battingHand by remember { mutableStateOf(player.battingHand) }
    var bowlingStyle by remember { mutableStateOf(player.bowlingStyle) }
    var photoUri by remember { mutableStateOf(player.photoUri) }
    var isCaptain by remember { mutableStateOf(player.isCaptain) }
    var isViceCaptain by remember { mutableStateOf(player.isViceCaptain) }
    var isActive by remember { mutableStateOf(player.isActive) }
    var notes by remember { mutableStateOf(player.notes ?: "") }

    var roleExpanded by remember { mutableStateOf(false) }
    var battingHandExpanded by remember { mutableStateOf(false) }
    var bowlingStyleExpanded by remember { mutableStateOf(false) }

    // Photo picker using Google Play compliant zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Edit Player Profile",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Player ID: #RCB-${player.id} • Stable ID",
                        color = GoldBright,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Photo Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CharcoalElevated, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(CharcoalBlack)
                            .border(2.dp, GoldPrimary, CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!photoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Player photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (!photoUri.isNullOrBlank()) "Photo Set" else "No Photo",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Tap to upload or replace image",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Pick Photo", tint = GoldBright)
                        }
                        if (!photoUri.isNullOrBlank()) {
                            IconButton(onClick = { photoUri = null }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove Photo", tint = Color(0xCCFF5252))
                            }
                        }
                    }
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color(0xFF173F70),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Jersey Number
                OutlinedTextField(
                    value = jerseyText,
                    onValueChange = { jerseyText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jersey Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color(0xFF173F70),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Primary Role Dropdown
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        value = role.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Primary Cricket Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF173F70),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
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

                // Batting Hand
                ExposedDropdownMenuBox(
                    expanded = battingHandExpanded,
                    onExpandedChange = { battingHandExpanded = !battingHandExpanded }
                ) {
                    OutlinedTextField(
                        value = battingHand.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Batting Hand") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = battingHandExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF173F70),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = battingHandExpanded,
                        onDismissRequest = { battingHandExpanded = false },
                        modifier = Modifier.background(CharcoalElevated)
                    ) {
                        BattingHand.values().forEach { h ->
                            DropdownMenuItem(
                                text = { Text(h.displayName, color = Color.White) },
                                onClick = {
                                    battingHand = h
                                    battingHandExpanded = false
                                }
                            )
                        }
                    }
                }

                // Bowling Style
                ExposedDropdownMenuBox(
                    expanded = bowlingStyleExpanded,
                    onExpandedChange = { bowlingStyleExpanded = !bowlingStyleExpanded }
                ) {
                    OutlinedTextField(
                        value = bowlingStyle.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bowling Style") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bowlingStyleExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color(0xFF173F70),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = bowlingStyleExpanded,
                        onDismissRequest = { bowlingStyleExpanded = false },
                        modifier = Modifier.background(CharcoalElevated)
                    ) {
                        BowlingStyle.values().forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.displayName, color = Color.White) },
                                onClick = {
                                    bowlingStyle = s
                                    bowlingStyleExpanded = false
                                }
                            )
                        }
                    }
                }

                // Team Leadership Checkboxes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isCaptain,
                            onCheckedChange = {
                                isCaptain = it
                                if (it) isViceCaptain = false
                            },
                            colors = CheckboxDefaults.colors(checkedColor = GoldPrimary, checkmarkColor = CharcoalBlack)
                        )
                        Text("Captain (C)", color = Color.White, fontSize = 13.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isViceCaptain,
                            onCheckedChange = {
                                isViceCaptain = it
                                if (it) isCaptain = false
                            },
                            colors = CheckboxDefaults.colors(checkedColor = GoldPrimary, checkmarkColor = CharcoalBlack)
                        )
                        Text("Vice-Captain (VC)", color = Color.White, fontSize = 13.sp)
                    }
                }

                // Active Squad Member Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CharcoalElevated, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Squad Status", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(if (isActive) "Available for selection" else "Marked inactive", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldBright,
                            checkedTrackColor = GoldDark
                        )
                    )
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Coach / Admin Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color(0xFF173F70),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            GoldButton(
                text = "Save Changes",
                onClick = {
                    if (name.isNotBlank()) {
                        val updated = player.copy(
                            fullName = name.trim(),
                            jerseyNumber = jerseyText.toIntOrNull() ?: 0,
                            role = role,
                            battingHand = battingHand,
                            bowlingStyle = bowlingStyle,
                            photoUri = photoUri,
                            isCaptain = isCaptain,
                            isViceCaptain = isViceCaptain,
                            isActive = isActive,
                            notes = notes.trim().ifEmpty { null }
                        )
                        onSave(updated)
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
