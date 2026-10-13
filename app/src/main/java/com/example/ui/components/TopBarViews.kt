package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.InkBlack
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrbanistFontFamily

@Composable
fun MessagesTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularIconButton(
            icon = Icons.Outlined.ChevronLeft,
            contentDescription = "Back",
            onClick = onBackClick,
            testTag = "messages_back_button"
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.title_messages),
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = InkBlack
        )
    }
}

@Composable
fun ConversationTopBar(
    title: String,
    subtitle: String,
    photoUri: String?,
    onBackClick: () -> Unit,
    onTitleClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onPhoneCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            CircularIconButton(
                icon = Icons.Outlined.ChevronLeft,
                contentDescription = "Back",
                onClick = onBackClick,
                testTag = "conversation_back_button"
            )

            Spacer(modifier = Modifier.width(12.dp))

            AvatarView(
                name = title,
                photoUri = photoUri,
                size = 42.dp,
                modifier = Modifier.clickable { onTitleClick() }
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTitleClick() }
            ) {
                Text(
                    text = title,
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = InkBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        fontFamily = UrbanistFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularIconButton(
                icon = Icons.Outlined.Videocam,
                contentDescription = stringResource(R.string.video_call),
                onClick = onVideoCallClick,
                testTag = "video_call_button"
            )
            CircularIconButton(
                icon = Icons.Outlined.Call,
                contentDescription = stringResource(R.string.call),
                onClick = onPhoneCallClick,
                testTag = "phone_call_button"
            )
        }
    }
}
