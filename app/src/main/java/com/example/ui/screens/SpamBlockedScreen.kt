package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.BlockedNumber
import com.example.core.model.Conversation
import com.example.ui.components.CircularIconButton
import com.example.ui.components.ConversationRow
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DividerColor
import com.example.ui.theme.InkBlack
import com.example.ui.theme.ScreenBackground
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrbanistFontFamily

@Composable
fun SpamBlockedScreen(
    spamConversations: List<Conversation>,
    blockedNumbers: List<BlockedNumber>,
    onReportNotSpam: (Long) -> Unit,
    onUnblockNumber: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    BackHandler {
        onBackClick()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ScreenBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    CircularIconButton(
                        icon = Icons.Outlined.ChevronLeft,
                        contentDescription = "Back",
                        onClick = onBackClick
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Spam & Blocked",
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = InkBlack
                    )
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ScreenBackground,
                    contentColor = InkBlack,
                    divider = { HorizontalDivider(color = DividerColor) }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Spam (${spamConversations.size})",
                                fontFamily = UrbanistFontFamily,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Blocked (${blockedNumbers.size})",
                                fontFamily = UrbanistFontFamily,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        if (selectedTab == 0) {
            // Spam Conversations
            if (spamConversations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No spam messages detected",
                        fontFamily = UrbanistFontFamily,
                        color = TextSecondary,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                ) {
                    items(spamConversations, key = { it.threadId }) { conv ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                                .shadow(elevation = 1.dp, shape = RoundedCornerShape(18.dp))
                                .clip(RoundedCornerShape(18.dp))
                                .background(CardWhite)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = conv.address,
                                fontFamily = UrbanistFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = InkBlack
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = conv.lastMessageBody,
                                fontFamily = UrbanistFontFamily,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onReportNotSpam(conv.threadId) },
                                colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
                                shape = CircleShape,
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.report_not_spam),
                                    fontFamily = UrbanistFontFamily,
                                    fontSize = 12.sp,
                                    color = CardWhite
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Blocked Numbers
            if (blockedNumbers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No blocked numbers",
                        fontFamily = UrbanistFontFamily,
                        color = TextSecondary,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                ) {
                    items(blockedNumbers, key = { it.id }) { blocked ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                                .shadow(elevation = 1.dp, shape = RoundedCornerShape(18.dp))
                                .clip(RoundedCornerShape(18.dp))
                                .background(CardWhite)
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Block,
                                contentDescription = null,
                                tint = InkBlack,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = blocked.number,
                                    fontFamily = UrbanistFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = InkBlack
                                )
                                if (!blocked.reason.isNullOrBlank()) {
                                    Text(
                                        text = blocked.reason,
                                        fontFamily = UrbanistFontFamily,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Button(
                                onClick = { onUnblockNumber(blocked.number) },
                                colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
                                shape = CircleShape,
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.unblock),
                                    fontFamily = UrbanistFontFamily,
                                    fontSize = 12.sp,
                                    color = CardWhite
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
