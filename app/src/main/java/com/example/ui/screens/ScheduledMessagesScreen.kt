package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.ScheduledMessage
import com.example.core.util.DateFormatter
import com.example.core.util.PhoneNumberHelper
import com.example.ui.components.CircularIconButton
import com.example.ui.components.ScheduleSendBottomSheet
import com.example.ui.theme.CardWhite
import com.example.ui.theme.InkBlack
import com.example.ui.theme.ScreenBackground
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrbanistFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScheduledMessagesScreen(
    scheduledMessages: List<ScheduledMessage>,
    onDeleteScheduled: (Long) -> Unit,
    onScheduleNew: (String, String, Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showScheduleSheet by remember { mutableStateOf(false) }

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
                    text = stringResource(R.string.scheduled_messages),
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = InkBlack,
                    modifier = Modifier.weight(1f)
                )
                CircularIconButton(
                    icon = Icons.Outlined.Add,
                    contentDescription = "Add",
                    onClick = { showScheduleSheet = true }
                )
            }
        }
    ) { innerPadding ->
        if (scheduledMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CardWhite)
                            .shadow(2.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = InkBlack,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_scheduled_messages),
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = InkBlack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Messages scheduled to send automatically at a chosen date and time will appear here.",
                        fontFamily = UrbanistFontFamily,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showScheduleSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
                        shape = CircleShape,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.schedule_new_message),
                            fontFamily = UrbanistFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CardWhite
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
            ) {
                items(scheduledMessages, key = { it.id }) { item ->
                    val dateFormatted = SimpleDateFormat("EEE, MMM d 'at' h:mm a", Locale.getDefault()).format(Date(item.scheduledTime))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardWhite)
                            .padding(16.dp)
                            .testTag("scheduled_item_${item.id}")
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = InkBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Send to ${PhoneNumberHelper.format(item.recipient)}",
                                    fontFamily = UrbanistFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = InkBlack,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onDeleteScheduled(item.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Cancel",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.body,
                                fontFamily = UrbanistFontFamily,
                                fontSize = 14.sp,
                                color = InkBlack
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Scheduled for: $dateFormatted",
                                fontFamily = UrbanistFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showScheduleSheet) {
        ScheduleSendBottomSheet(
            prefilledRecipient = "",
            prefilledText = "",
            onSchedule = { recipient, body, time ->
                onScheduleNew(recipient, body, time)
                showScheduleSheet = false
            },
            onDismiss = { showScheduleSheet = false }
        )
    }
}
