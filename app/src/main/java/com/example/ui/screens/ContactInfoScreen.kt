package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.core.model.Conversation
import com.example.core.util.PhoneNumberHelper
import com.example.ui.components.AvatarView
import com.example.ui.components.CircularIconButton
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DividerColor
import com.example.ui.theme.InkBlack
import com.example.ui.theme.ScreenBackground
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrbanistFontFamily

@Composable
fun ContactInfoScreen(
    conversation: Conversation,
    onBackClick: () -> Unit,
    onTogglePin: (Boolean) -> Unit,
    onToggleMute: (Boolean) -> Unit,
    onToggleFavourite: (Boolean) -> Unit,
    onBlockContact: (String) -> Unit,
    onDeleteConversation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val displayName = conversation.contactName ?: PhoneNumberHelper.format(conversation.address)

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
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large Avatar
            AvatarView(
                name = displayName,
                photoUri = conversation.contactPhotoUri,
                size = 96.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Name
            Text(
                text = displayName,
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = InkBlack
            )

            // Phone Number
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = PhoneNumberHelper.format(conversation.address),
                fontFamily = UrbanistFontFamily,
                fontSize = 15.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons Row: Call, Video, Message
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionCircleButton(
                    icon = Icons.Outlined.Call,
                    label = stringResource(R.string.call),
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${conversation.address}")
                        }
                        context.startActivity(intent)
                    }
                )
                ActionCircleButton(
                    icon = Icons.Outlined.Videocam,
                    label = stringResource(R.string.video_call),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("tel:${conversation.address}")
                        }
                        context.startActivity(Intent.createChooser(intent, "Video call"))
                    }
                )
                ActionCircleButton(
                    icon = Icons.Outlined.Sms,
                    label = "Message",
                    onClick = onBackClick
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Conversation Preferences Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardWhite)
            ) {
                Column {
                    InfoOptionRow(
                        icon = Icons.Outlined.PushPin,
                        title = if (conversation.isPinned) stringResource(R.string.unpin) else stringResource(R.string.pin),
                        subtitle = if (conversation.isPinned) "Pinned to top" else "Pin thread to top of list",
                        onClick = { onTogglePin(!conversation.isPinned) }
                    )
                    HorizontalDivider(color = DividerColor)
                    InfoOptionRow(
                        icon = if (conversation.isMuted) Icons.Outlined.NotificationsOff else Icons.Outlined.Notifications,
                        title = if (conversation.isMuted) stringResource(R.string.unmute) else stringResource(R.string.mute),
                        subtitle = if (conversation.isMuted) "Notifications silenced" else "Mute notifications for this contact",
                        onClick = { onToggleMute(!conversation.isMuted) }
                    )
                    HorizontalDivider(color = DividerColor)
                    InfoOptionRow(
                        icon = if (conversation.isFavourite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        title = if (conversation.isFavourite) stringResource(R.string.remove_from_favourite) else stringResource(R.string.add_to_favourite),
                        subtitle = if (conversation.isFavourite) "In favourites list" else "Add to favourite conversations",
                        onClick = { onToggleFavourite(!conversation.isFavourite) }
                    )
                    HorizontalDivider(color = DividerColor)
                    InfoOptionRow(
                        icon = Icons.Outlined.Block,
                        title = stringResource(R.string.block),
                        subtitle = "Block messages from this number",
                        onClick = { onBlockContact(conversation.address) }
                    )
                    HorizontalDivider(color = DividerColor)
                    InfoOptionRow(
                        icon = Icons.Outlined.Delete,
                        title = stringResource(R.string.delete),
                        subtitle = "Delete all messages in this conversation",
                        onClick = onDeleteConversation
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ActionCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularIconButton(
            icon = icon,
            contentDescription = label,
            onClick = onClick,
            size = 52.dp,
            iconSize = 24.dp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = InkBlack
        )
    }
}

@Composable
private fun InfoOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = InkBlack,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = InkBlack
            )
            Text(
                text = subtitle,
                fontFamily = UrbanistFontFamily,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}
