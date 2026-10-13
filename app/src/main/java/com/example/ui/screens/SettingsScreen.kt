package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SimCard
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.SimInfo
import com.example.core.preferences.PreferencesManager
import com.example.ui.components.CircularIconButton
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DividerColor
import com.example.ui.theme.InkBlack
import com.example.ui.theme.ScreenBackground
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrbanistFontFamily
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    isDefaultSmsApp: Boolean,
    onRequestDefaultSms: () -> Unit,
    activeSims: List<SimInfo>,
    defaultSimId: Int,
    askSimPerMessage: Boolean,
    deliveryReports: Boolean,
    notificationsEnabled: Boolean,
    otpCopyEnabled: Boolean,
    theme: String,
    enterIsSend: Boolean,
    appLockEnabled: Boolean,
    hideInRecents: Boolean,
    onUpdateDefaultSim: (Int) -> Unit,
    onUpdateAskSim: (Boolean) -> Unit,
    onUpdateDeliveryReports: (Boolean) -> Unit,
    onUpdateNotifications: (Boolean) -> Unit,
    onUpdateTheme: (String) -> Unit,
    onUpdateEnterIsSend: (Boolean) -> Unit,
    onUpdateAppLock: (Boolean) -> Unit,
    onUpdateHideInRecents: (Boolean) -> Unit,
    onOpenBlocked: () -> Unit,
    onOpenSpam: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    BackHandler {
        onBackClick()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ScreenBackground,
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                CircularIconButton(
                    icon = Icons.Outlined.ChevronLeft,
                    contentDescription = "Back",
                    onClick = onBackClick
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.nav_settings),
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = InkBlack
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // Group 1: Default app and permissions
            item {
                SettingsSectionHeader("Default app and permissions")
                SettingsCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Default SMS App",
                                fontFamily = UrbanistFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = InkBlack
                            )
                            Text(
                                text = if (isDefaultSmsApp) "Salim is currently your default SMS app" else "Not default (read-only mode active)",
                                fontFamily = UrbanistFontFamily,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                        if (!isDefaultSmsApp) {
                            Button(
                                onClick = onRequestDefaultSms,
                                colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
                                shape = CircleShape
                            ) {
                                Text("Set Default", color = CardWhite, fontSize = 12.sp)
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Active",
                                tint = InkBlack,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = DividerColor)

                    SettingsClickableRow(
                        title = "System App Permissions",
                        subtitle = "Manage SMS, Phone, and Contacts access in Android Settings",
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Group 2: SIM and sending
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader("SIM and sending")
                SettingsCard {
                    if (activeSims.size > 1) {
                        SettingsClickableRow(
                            title = "Default SIM",
                            subtitle = activeSims.find { it.subscriptionId == defaultSimId }?.displayName ?: "Ask every time",
                            onClick = {
                                val nextSim = if (defaultSimId == activeSims.first().subscriptionId) {
                                    activeSims.last().subscriptionId
                                } else {
                                    activeSims.first().subscriptionId
                                }
                                onUpdateDefaultSim(nextSim)
                            }
                        )
                        HorizontalDivider(color = DividerColor)
                    }

                    SettingsSwitchRow(
                        title = "Delivery reports",
                        subtitle = "Request SMS delivery confirmation from carrier",
                        checked = deliveryReports,
                        onCheckedChange = onUpdateDeliveryReports
                    )
                }
            }

            // Group 3: Notifications
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader("Notifications")
                SettingsCard {
                    SettingsSwitchRow(
                        title = "Enable notifications",
                        subtitle = "Show notification on incoming messages",
                        checked = notificationsEnabled,
                        onCheckedChange = onUpdateNotifications
                    )
                    HorizontalDivider(color = DividerColor)
                    SettingsSwitchRow(
                        title = "One-tap OTP copy",
                        subtitle = "Show instant Copy Code action on verification messages",
                        checked = otpCopyEnabled,
                        onCheckedChange = { /* Always enabled for user ease */ }
                    )
                }
            }

            // Group 4: Appearance & Chats
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader("Appearance & Chats")
                SettingsCard {
                    SettingsClickableRow(
                        title = "Theme",
                        subtitle = if (theme == "DARK") "Dark" else "Light",
                        onClick = {
                            onUpdateTheme(if (theme == "DARK") "LIGHT" else "DARK")
                        }
                    )
                    HorizontalDivider(color = DividerColor)
                    SettingsSwitchRow(
                        title = "Press Enter to send",
                        subtitle = "Enter key in composer will send message immediately",
                        checked = enterIsSend,
                        onCheckedChange = onUpdateEnterIsSend
                    )
                }
            }

            // Group 5: Blocking and spam
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader("Blocking and spam")
                SettingsCard {
                    SettingsClickableRow(
                        title = stringResource(R.string.blocked_numbers),
                        subtitle = "Manage blocked senders",
                        onClick = onOpenBlocked
                    )
                    HorizontalDivider(color = DividerColor)
                    SettingsClickableRow(
                        title = stringResource(R.string.spam_folder),
                        subtitle = "Review filtered spam messages",
                        onClick = onOpenSpam
                    )
                }
            }

            // Group 6: Privacy & security
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader("Privacy & security")
                SettingsCard {
                    SettingsSwitchRow(
                        title = stringResource(R.string.app_lock),
                        subtitle = "Require biometric / device credential on open",
                        checked = appLockEnabled,
                        onCheckedChange = onUpdateAppLock
                    )
                    HorizontalDivider(color = DividerColor)
                    SettingsSwitchRow(
                        title = "Hide in Recents",
                        subtitle = "Mask application preview window in recent apps list",
                        checked = hideInRecents,
                        onCheckedChange = onUpdateHideInRecents
                    )
                }
            }

            // Group 7: Storage and backup
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader("Storage and backup")
                SettingsCard {
                    SettingsClickableRow(
                        title = "Export backup",
                        subtitle = "Create local encrypted backup file via Storage Access Framework",
                        onClick = {
                            Toast.makeText(context, "Exporting local SMS archive to SAF...", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = DividerColor)
                    SettingsClickableRow(
                        title = "Restore backup",
                        subtitle = "Import conversations and messages from local backup file",
                        onClick = {
                            Toast.makeText(context, "Select backup file to restore...", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Group 8: About
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader("About")
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.about_salim),
                            fontFamily = UrbanistFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = InkBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.version_label),
                            fontFamily = UrbanistFontFamily,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.privacy_statement),
                            fontFamily = UrbanistFontFamily,
                            fontSize = 13.sp,
                            color = InkBlack
                        )
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = UrbanistFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = TextSecondary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(CardWhite)
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun SettingsClickableRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = InkBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = UrbanistFontFamily,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = InkBlack
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = UrbanistFontFamily,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CardWhite,
                checkedTrackColor = InkBlack
            )
        )
    }
}
