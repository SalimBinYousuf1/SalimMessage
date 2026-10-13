package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.core.model.Message
import com.example.core.model.MessageStatus
import com.example.core.util.DateFormatter
import com.example.ui.theme.CardWhite
import com.example.ui.theme.InkBlack
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrbanistFontFamily

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubbleView(
    message: Message,
    isLastInGroup: Boolean,
    onLongClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isFromMe = message.isFromMe

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 2.dp),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        // Main bubble
        Box(
            modifier = Modifier
                .widthIn(min = 60.dp, max = 290.dp)
                .shadow(elevation = 1.dp, shape = RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(CardWhite)
                .combinedClickable(
                    onClick = {
                        if (message.status == MessageStatus.FAILED && isFromMe) {
                            onRetryClick()
                        }
                    },
                    onLongClick = onLongClick
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("bubble_${message.id}")
        ) {
            Column {
                // If attachment exists
                if (!message.attachmentUri.isNullOrBlank()) {
                    AsyncImage(
                        model = message.attachmentUri,
                        contentDescription = "Attachment",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (message.body.isNotBlank()) {
                    Text(
                        text = message.body,
                        fontFamily = UrbanistFontFamily,
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        color = InkBlack
                    )
                }

                // Quick OTP Copy Pill if OTP code detected
                if (!message.otpCode.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(InkBlack)
                            .combinedClickable(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("OTP", message.otpCode))
                                    Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy",
                                tint = CardWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${stringResource(R.string.copy_code)} (${message.otpCode})",
                                fontFamily = UrbanistFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = CardWhite
                            )
                        }
                    }
                }
            }
        }

        // Timestamp and status shown under the last bubble of the group
        if (isLastInGroup) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = DateFormatter.formatBubbleTime(message.date),
                    fontFamily = UrbanistFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                if (message.isStarred) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = "Starred",
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                }

                if (isFromMe) {
                    when (message.status) {
                        MessageStatus.SENDING -> {
                            Text(
                                text = stringResource(R.string.status_sending),
                                fontFamily = UrbanistFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        MessageStatus.SENT -> {
                            Text(
                                text = stringResource(R.string.status_sent),
                                fontFamily = UrbanistFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        MessageStatus.DELIVERED -> {
                            Text(
                                text = stringResource(R.string.status_delivered),
                                fontFamily = UrbanistFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        MessageStatus.FAILED -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.ErrorOutline,
                                    contentDescription = "Failed",
                                    tint = androidx.compose.ui.graphics.Color(0xFFE53935),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = stringResource(R.string.status_failed),
                                    fontFamily = UrbanistFontFamily,
                                    fontSize = 11.sp,
                                    color = androidx.compose.ui.graphics.Color(0xFFE53935)
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
fun DateHeaderChip(
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .shadow(elevation = 1.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(CardWhite)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = DateFormatter.formatDayHeader(timestamp),
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}
