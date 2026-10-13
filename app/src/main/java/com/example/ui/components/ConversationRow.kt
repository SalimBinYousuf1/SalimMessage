package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.Conversation
import com.example.core.util.DateFormatter
import com.example.ui.theme.CardWhite
import com.example.ui.theme.InkBlack
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrbanistFontFamily

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ConversationRow(
    conversation: Conversation,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = conversation.contactName ?: conversation.address
    val preview = if (!conversation.draftMessage.isNullOrBlank()) {
        "${stringResource(R.string.draft_prefix)}${conversation.draftMessage}"
    } else {
        conversation.lastMessageBody
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .shadow(
                elevation = if (isSelected) 4.dp else 1.5.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) InkBlack.copy(alpha = 0.08f) else CardWhite)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(14.dp)
            .testTag("conversation_row_${conversation.threadId}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Round avatar
            AvatarView(
                name = displayName,
                photoUri = conversation.contactPhotoUri,
                size = 52.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Contact Name & Preview
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayName,
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = InkBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = preview,
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Time & Unread Badge
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = DateFormatter.formatRelativeTime(conversation.lastMessageDate),
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (conversation.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(InkBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (conversation.unreadCount > 99) "99+" else conversation.unreadCount.toString(),
                            fontFamily = UrbanistFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = CardWhite
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}
