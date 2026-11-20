package com.example.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.InstalledAppInfo
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceBright
import com.example.ui.theme.CarbonSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldVolt
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.BoosterUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameVaultScreen(
    uiState: BoosterUiState,
    onBoostAndLaunch: (InstalledAppInfo) -> Unit,
    onTogglePinApp: (String) -> Unit,
    onTogglePerGame60Fps: (String) -> Unit,
    onOpenFpsLab: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAppPickerSheet by remember { mutableStateOf(false) }
    val vaultGames = uiState.vaultGames

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("game_vault_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GAME VAULT",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Auto-sweep RAM & lock 60 FPS on launch",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                OutlinedButton(
                    onClick = { showAppPickerSheet = true },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("add_app_to_vault_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add app to game vault",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ADD APP", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        if (vaultGames.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = CarbonSurface,
                    border = BorderStroke(1.dp, BorderSlate)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = ElectricCyan.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SportsEsports,
                                contentDescription = "No games detected yet",
                                tint = ElectricCyan,
                                modifier = Modifier
                                    .padding(14.dp)
                                    .size(36.dp)
                            )
                        }
                        Text(
                            text = "No Games Detected Automatically",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Pin any installed game or app using ADD APP above, or test your 60 FPS Lock & Performance engine right now in the 60 FPS Lab.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { showAppPickerSheet = true },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("empty_vault_add_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCyan,
                                    contentColor = ObsidianBg
                                )
                            ) {
                                Text("PIN INSTALLED APP", style = MaterialTheme.typography.labelLarge)
                            }
                            OutlinedButton(
                                onClick = onOpenFpsLab,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("empty_vault_fps_lab_button"),
                                border = BorderStroke(1.dp, EmeraldVolt.copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldVolt)
                            ) {
                                Text("60 FPS LAB", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        } else {
            items(vaultGames, key = { it.packageName }) { game ->
                VaultGameCard(
                    app = game,
                    onBoostAndLaunch = { onBoostAndLaunch(game) },
                    onToggle60Fps = { onTogglePerGame60Fps(game.packageName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                )
            }
        }
    }

    if (showAppPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAppPickerSheet = false },
            containerColor = CarbonSurfaceElevated,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            AppPickerSheetContent(
                apps = uiState.installedApps,
                onTogglePin = onTogglePinApp
            )
        }
    }
}

@Composable
private fun VaultGameCard(
    app: InstalledAppInfo,
    onBoostAndLaunch: () -> Unit,
    onToggle60Fps: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag("vault_game_card_${app.packageName}"),
        shape = RoundedCornerShape(18.dp),
        color = CarbonSurface,
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppIconImage(
                drawable = app.icon,
                label = app.label,
                modifier = Modifier.size(48.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (app.isDetectedGame) "GAME" else "PINNED",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    FilterChip(
                        selected = app.is60FpsLocked,
                        onClick = onToggle60Fps,
                        label = {
                            Text(
                                text = "60 FPS",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Speed,
                                contentDescription = "60 FPS lock toggle",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        modifier = Modifier.testTag("game_60fps_chip_${app.packageName}"),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricCyan.copy(alpha = 0.16f),
                            selectedLabelColor = ElectricCyan,
                            selectedLeadingIconColor = ElectricCyan
                        )
                    )
                }
            }

            Button(
                onClick = onBoostAndLaunch,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("boost_launch_${app.packageName}"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldVolt,
                    contentColor = ObsidianBg
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Boost and launch ${app.label}",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("BOOST", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun AppPickerSheetContent(
    apps: List<InstalledAppInfo>,
    onTogglePin: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(apps, query) {
        if (query.isBlank()) apps
        else apps.filter {
            it.label.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Pin Apps to Game Vault",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Filter installed apps...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vault_app_search_input"),
            shape = RoundedCornerShape(14.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.packageName }) { app ->
                val pinned = app.isPinnedToVault || app.isDetectedGame
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = CarbonSurface,
                    border = BorderStroke(1.dp, if (pinned) ElectricCyan.copy(alpha = 0.5f) else BorderSlate)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppIconImage(
                            drawable = app.icon,
                            label = app.label,
                            modifier = Modifier.size(38.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = app.packageName,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        OutlinedButton(
                            onClick = { onTogglePin(app.packageName) },
                            enabled = !app.isDetectedGame,
                            modifier = Modifier.minimumInteractiveComponentSize(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (pinned) EmeraldVolt else ElectricCyan
                            )
                        ) {
                            Icon(
                                imageVector = if (pinned) Icons.Filled.Check else Icons.Filled.Add,
                                contentDescription = if (pinned) "Pinned" else "Pin app",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (app.isDetectedGame) "AUTO" else if (app.isPinnedToVault) "PINNED" else "PIN",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun AppIconImage(
    drawable: Drawable?,
    label: String,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(drawable) {
        runCatching { drawable?.toBitmap(width = 96, height = 96)?.asImageBitmap() }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "$label icon",
            modifier = modifier.clip(RoundedCornerShape(12.dp))
        )
    } else {
        Surface(
            modifier = modifier,
            color = CarbonSurfaceBright,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.SportsEsports,
                contentDescription = "$label icon",
                tint = ElectricCyan,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}
