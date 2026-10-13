package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CardWhite
import com.example.ui.theme.InkBlack
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrbanistFontFamily

import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.filled.AccessTime

enum class BottomBarDestination(
    val titleRes: Int,
    val outlinedIcon: ImageVector,
    val filledIcon: ImageVector
) {
    MESSAGE(R.string.nav_message, Icons.Outlined.ChatBubbleOutline, Icons.Default.ChatBubble),
    SCHEDULED(R.string.nav_scheduled, Icons.Outlined.AccessTime, Icons.Filled.AccessTime),
    CONTACTS(R.string.nav_contacts, Icons.Outlined.Contacts, Icons.Outlined.Contacts),
    FAVOURITE(R.string.nav_favourite, Icons.Outlined.FavoriteBorder, Icons.Outlined.FavoriteBorder),
    SETTINGS(R.string.nav_settings, Icons.Outlined.Settings, Icons.Outlined.Settings)
}

@Composable
fun SalimBottomBar(
    currentDestination: BottomBarDestination,
    onNavigate: (BottomBarDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .navigationBarsPadding()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(CardWhite)
            .padding(vertical = 10.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarDestination.values().forEach { destination ->
                val isSelected = destination == currentDestination
                val icon = if (isSelected) destination.filledIcon else destination.outlinedIcon
                val color = if (isSelected) InkBlack else TextSecondary

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false, radius = 24.dp),
                            onClick = { onNavigate(destination) }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("nav_${destination.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(destination.titleRes),
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(destination.titleRes),
                        fontFamily = UrbanistFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp,
                        color = color
                    )
                }
            }
        }
    }
}
