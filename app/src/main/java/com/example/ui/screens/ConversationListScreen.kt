package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.core.model.Conversation
import com.example.core.model.ConversationCategory
import com.example.ui.components.BottomBarDestination
import com.example.ui.components.ChipRow
import com.example.ui.components.CircularIconButton
import com.example.ui.components.ConversationFilterOptions
import com.example.ui.components.ConversationRow
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.GoogleComposeFab
import com.example.ui.components.MessagesTopBar
import com.example.ui.components.SalimBottomBar
import com.example.ui.components.SearchBarView
import com.example.ui.theme.CardWhite
import com.example.ui.theme.InkBlack
import com.example.ui.theme.ScreenBackground
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrbanistFontFamily

@Composable
fun ConversationListScreen(
    conversations: List<Conversation>,
    isDefaultSmsApp: Boolean,
    currentDestination: BottomBarDestination,
    onNavigateDestination: (BottomBarDestination) -> Unit,
    onConversationClick: (Conversation) -> Unit,
    onComposeClick: () -> Unit,
    onCameraClick: () -> Unit,
    onRequestDefaultSms: () -> Unit,
    onPinConversation: (Long, Boolean) -> Unit,
    onArchiveConversation: (Long, Boolean) -> Unit,
    onDeleteConversation: (Long) -> Unit,
    onMarkAsRead: (Long) -> Unit,
    onFavouriteConversation: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ConversationCategory.ALL) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var filterOptions by remember { mutableStateOf(ConversationFilterOptions()) }
    var showDefaultBanner by remember { mutableStateOf(!isDefaultSmsApp) }

    val selectedThreadIds = remember { mutableStateListOf<Long>() }
    val isSelectionMode = selectedThreadIds.isNotEmpty()

    // Filter conversations
    val filteredConversations = conversations.filter { conv ->
        // Category filter
        val matchesCategory = when (selectedCategory) {
            ConversationCategory.ALL -> true
            ConversationCategory.CONTACTS -> !conv.contactName.isNullOrBlank()
            ConversationCategory.UNKNOWN -> conv.contactName.isNullOrBlank()
            ConversationCategory.NEW -> conv.unreadCount > 0
            else -> true
        }

        // Search query filter
        val matchesQuery = if (searchQuery.isBlank()) true else {
            val q = searchQuery.lowercase()
            conv.address.lowercase().contains(q) ||
            (conv.contactName?.lowercase()?.contains(q) == true) ||
            conv.lastMessageBody.lowercase().contains(q)
        }

        // Extra filter sheet options
        val matchesUnread = if (filterOptions.unreadOnly) conv.unreadCount > 0 else true
        val matchesStarred = if (filterOptions.starredOnly) conv.isFavourite else true

        matchesCategory && matchesQuery && matchesUnread && matchesStarred
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ScreenBackground,
        floatingActionButton = {
            if (!isSelectionMode) {
                GoogleComposeFab(
                    onClick = onComposeClick,
                    modifier = Modifier.padding(bottom = 16.dp, end = 4.dp)
                )
            }
        },
        bottomBar = {
            if (!isSelectionMode) {
                SalimBottomBar(
                    currentDestination = currentDestination,
                    onNavigate = onNavigateDestination
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Contextual selection bar or Normal TopBar
            if (isSelectionMode) {
                SelectionTopBar(
                    selectedCount = selectedThreadIds.size,
                    onClose = { selectedThreadIds.clear() },
                    onPin = {
                        selectedThreadIds.forEach { onPinConversation(it, true) }
                        selectedThreadIds.clear()
                    },
                    onArchive = {
                        selectedThreadIds.forEach { onArchiveConversation(it, true) }
                        selectedThreadIds.clear()
                    },
                    onDelete = {
                        selectedThreadIds.forEach { onDeleteConversation(it) }
                        selectedThreadIds.clear()
                    },
                    onMarkRead = {
                        selectedThreadIds.forEach { onMarkAsRead(it) }
                        selectedThreadIds.clear()
                    }
                )
            } else {
                MessagesTopBar(
                    onBackClick = { /* At root, system handles */ }
                )
            }

            // Default SMS banner if not default app
            if (showDefaultBanner && !isDefaultSmsApp && !isSelectionMode) {
                DefaultSmsBanner(
                    onRequestDefault = onRequestDefaultSms,
                    onDismiss = { showDefaultBanner = false }
                )
            }

            // Search bar
            if (!isSelectionMode) {
                SearchBarView(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onFilterClick = { showFilterSheet = true }
                )

                // Chips Row: All, Contacts, Unknown, New
                ChipRow(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }

            // Main Conversation List or Empty State
            if (filteredConversations.isEmpty()) {
                EmptyStateView(
                    onStartChat = onComposeClick,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredConversations, key = { it.threadId }) { conv ->
                        val isSelected = selectedThreadIds.contains(conv.threadId)
                        ConversationRow(
                            conversation = conv,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onClick = {
                                if (isSelectionMode) {
                                    if (isSelected) selectedThreadIds.remove(conv.threadId)
                                    else selectedThreadIds.add(conv.threadId)
                                } else {
                                    onConversationClick(conv)
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    selectedThreadIds.add(conv.threadId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilter = filterOptions,
            onApplyFilter = { filterOptions = it },
            onDismiss = { showFilterSheet = false }
        )
    }
}

@Composable
fun DefaultSmsBanner(
    onRequestDefault: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(CardWhite)
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = InkBlack,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.default_sms_banner_title),
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = InkBlack,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Dismiss",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.default_sms_banner_desc),
                fontFamily = UrbanistFontFamily,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onRequestDefault,
                colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
                shape = CircleShape,
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = stringResource(R.string.set_as_default),
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = CardWhite
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    onStartChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(CardWhite)
                    .shadow(elevation = 2.dp, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sms,
                    contentDescription = null,
                    tint = InkBlack,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.empty_messages_title),
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = InkBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.empty_messages_subtitle),
                fontFamily = UrbanistFontFamily,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onStartChat,
                colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
                shape = CircleShape,
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    text = stringResource(R.string.start_chat),
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CardWhite
                )
            }
        }
    }
}

@Composable
fun SelectionTopBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onPin: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onMarkRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularIconButton(
            icon = Icons.Outlined.Close,
            contentDescription = "Close",
            onClick = onClose
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "$selectedCount selected",
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = InkBlack,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CircularIconButton(
                icon = Icons.Outlined.PushPin,
                contentDescription = "Pin",
                onClick = onPin
            )
            CircularIconButton(
                icon = Icons.Outlined.Archive,
                contentDescription = "Archive",
                onClick = onArchive
            )
            CircularIconButton(
                icon = Icons.Outlined.MarkEmailRead,
                contentDescription = "Mark Read",
                onClick = onMarkRead
            )
            CircularIconButton(
                icon = Icons.Outlined.Delete,
                contentDescription = "Delete",
                onClick = onDelete
            )
        }
    }
}
