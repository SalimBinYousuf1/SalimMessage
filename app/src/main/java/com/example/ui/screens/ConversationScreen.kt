package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.Conversation
import com.example.core.model.Message
import com.example.core.model.SimInfo
import com.example.core.util.DateFormatter
import com.example.core.util.PhoneNumberHelper
import com.example.ui.components.ComposerBarView
import com.example.ui.components.ConversationTopBar
import com.example.ui.components.DateHeaderChip
import com.example.ui.components.EmojiPickerPanel
import com.example.ui.components.MessageBubbleView
import com.example.ui.components.ScheduleSendBottomSheet
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ChatBackground
import com.example.ui.theme.InkBlack
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrbanistFontFamily
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    conversation: Conversation,
    messages: List<Message>,
    activeSims: List<SimInfo>,
    onBackClick: () -> Unit,
    onTitleClick: () -> Unit,
    onSendMessage: (String, Int, String?) -> Unit,
    onRetryMessage: (Message) -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onStarMessage: (Long, Boolean) -> Unit,
    onScheduleMessage: (String, String, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf(conversation.draftMessage ?: "") }
    var selectedSimId by remember { mutableStateOf(if (activeSims.isNotEmpty()) activeSims.first().subscriptionId else -1) }
    var selectedMessageForAction by remember { mutableStateOf<Message?>(null) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showScheduleSendSheet by remember { mutableStateOf(false) }

    // Photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onSendMessage("", selectedSimId, uri.toString())
        }
    }

    BackHandler {
        if (showEmojiPicker) {
            showEmojiPicker = false
        } else {
            onBackClick()
        }
    }

    // Scroll to bottom when messages list size changes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChatBackground,
        topBar = {
            val title = conversation.contactName ?: PhoneNumberHelper.format(conversation.address)
            val subtitle = if (!conversation.contactName.isNullOrBlank()) {
                PhoneNumberHelper.format(conversation.address)
            } else {
                ""
            }

            ConversationTopBar(
                title = title,
                subtitle = subtitle,
                photoUri = conversation.contactPhotoUri,
                onBackClick = onBackClick,
                onTitleClick = onTitleClick,
                onVideoCallClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("tel:${conversation.address}")
                    }
                    context.startActivity(Intent.createChooser(intent, "Video call"))
                },
                onPhoneCallClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${conversation.address}")
                    }
                    context.startActivity(intent)
                }
            )
        },
        bottomBar = {
            Column {
                ComposerBarView(
                    text = textInput,
                    onTextChange = { textInput = it },
                    onSendClick = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput.trim(), selectedSimId, null)
                            textInput = ""
                            showEmojiPicker = false
                        }
                    },
                    onAttachClick = { showAttachmentSheet = true },
                    onCameraClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onMicClick = {
                        Toast.makeText(context, context.getString(R.string.recording), Toast.LENGTH_SHORT).show()
                    },
                    onEmojiClick = {
                        showEmojiPicker = !showEmojiPicker
                    },
                    activeSims = activeSims,
                    selectedSimId = selectedSimId,
                    onSimSelect = { selectedSimId = it }
                )

                if (showEmojiPicker) {
                    EmojiPickerPanel(
                        onEmojiSelected = { emoji ->
                            textInput += emoji
                        },
                        onBackspace = {
                            if (textInput.isNotEmpty()) {
                                textInput = textInput.dropLast(1)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            itemsIndexed(messages, key = { _, msg -> msg.id }) { index, msg ->
                val prevMsg = if (index > 0) messages[index - 1] else null
                val nextMsg = if (index < messages.size - 1) messages[index + 1] else null

                // Check if date chip is needed
                val showDateChip = if (prevMsg == null) {
                    true
                } else {
                    val prevCal = Calendar.getInstance().apply { timeInMillis = prevMsg.date }
                    val currCal = Calendar.getInstance().apply { timeInMillis = msg.date }
                    prevCal.get(Calendar.DAY_OF_YEAR) != currCal.get(Calendar.DAY_OF_YEAR) ||
                    prevCal.get(Calendar.YEAR) != currCal.get(Calendar.YEAR)
                }

                if (showDateChip) {
                    DateHeaderChip(timestamp = msg.date)
                }

                // Check if last in consecutive group
                val isLastInGroup = if (nextMsg == null) {
                    true
                } else {
                    nextMsg.isFromMe != msg.isFromMe || (nextMsg.date - msg.date > 120_000)
                }

                MessageBubbleView(
                    message = msg,
                    isLastInGroup = isLastInGroup,
                    onLongClick = { selectedMessageForAction = msg },
                    onRetryClick = { onRetryMessage(msg) }
                )
            }
        }
    }

    // Attachment Sheet
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            containerColor = CardWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = stringResource(R.string.attach),
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = InkBlack
                )
                Spacer(modifier = Modifier.height(16.dp))

                AttachmentSheetItem(
                    icon = Icons.Outlined.Image,
                    title = "Photo & Video",
                    onClick = {
                        showAttachmentSheet = false
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    }
                )
                AttachmentSheetItem(
                    icon = Icons.Outlined.CameraAlt,
                    title = "Camera",
                    onClick = {
                        showAttachmentSheet = false
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
                AttachmentSheetItem(
                    icon = Icons.Outlined.AccessTime,
                    title = stringResource(R.string.schedule_send),
                    onClick = {
                        showAttachmentSheet = false
                        showScheduleSendSheet = true
                    }
                )
            }
        }
    }

    if (showScheduleSendSheet) {
        ScheduleSendBottomSheet(
            prefilledRecipient = conversation.address,
            prefilledText = textInput,
            onSchedule = { recipient, body, time ->
                onScheduleMessage(recipient, body, time)
                textInput = ""
                showScheduleSendSheet = false
            },
            onDismiss = { showScheduleSendSheet = false }
        )
    }

    // Message Action Sheet (long-press)
    if (selectedMessageForAction != null) {
        val msg = selectedMessageForAction!!
        ModalBottomSheet(
            onDismissRequest = { selectedMessageForAction = null },
            containerColor = CardWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = stringResource(R.string.message_details),
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = InkBlack
                )
                Spacer(modifier = Modifier.height(12.dp))

                ActionSheetItem(
                    icon = Icons.Outlined.ContentCopy,
                    title = stringResource(R.string.copy),
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("SMS", msg.body))
                        Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
                        selectedMessageForAction = null
                    }
                )

                ActionSheetItem(
                    icon = Icons.Outlined.Share,
                    title = "Share",
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, msg.body)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share text"))
                        selectedMessageForAction = null
                    }
                )

                ActionSheetItem(
                    icon = if (msg.isStarred) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                    title = if (msg.isStarred) "Unstar" else "Star",
                    onClick = {
                        onStarMessage(msg.id, !msg.isStarred)
                        selectedMessageForAction = null
                    }
                )

                ActionSheetItem(
                    icon = Icons.Outlined.Delete,
                    title = stringResource(R.string.delete),
                    onClick = {
                        onDeleteMessage(msg.id)
                        selectedMessageForAction = null
                    }
                )
            }
        }
    }
}

@Composable
private fun AttachmentSheetItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = InkBlack,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = InkBlack
        )
    }
}

@Composable
private fun ActionSheetItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = InkBlack,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = InkBlack
        )
    }
}
