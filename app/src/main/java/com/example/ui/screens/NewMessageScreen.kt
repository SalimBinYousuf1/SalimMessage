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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.Contact
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
fun NewMessageScreen(
    contacts: List<Contact>,
    onBackClick: () -> Unit,
    onContactSelect: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var recipientInput by remember { mutableStateOf("") }

    BackHandler {
        onBackClick()
    }

    val filteredContacts = contacts.filter { contact ->
        if (recipientInput.isBlank()) true else {
            val q = recipientInput.lowercase()
            contact.name.lowercase().contains(q) || contact.number.contains(q)
        }
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
                    onClick = onBackClick,
                    testTag = "new_msg_back"
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.new_message),
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = InkBlack
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // "To" input box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(CircleShape)
                    .background(CardWhite)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${stringResource(R.string.to_recipient)}: ",
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (recipientInput.isEmpty()) {
                            Text(
                                text = stringResource(R.string.type_name_or_number),
                                fontFamily = UrbanistFontFamily,
                                color = TextSecondary,
                                fontSize = 15.sp
                            )
                        }
                        BasicTextField(
                            value = recipientInput,
                            onValueChange = { recipientInput = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = UrbanistFontFamily,
                                fontSize = 15.sp,
                                color = InkBlack
                            ),
                            cursorBrush = SolidColor(InkBlack),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("recipient_input")
                        )
                    }

                    // Direct number entry button if digits entered
                    if (recipientInput.any { it.isDigit() }) {
                        IconButton(
                            onClick = {
                                onContactSelect(recipientInput.trim(), null)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Select",
                                tint = InkBlack,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Direct input suggestion row if user entered digits
            if (recipientInput.isNotBlank() && recipientInput.any { it.isDigit() }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onContactSelect(recipientInput.trim(), null) }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Dialpad,
                        contentDescription = null,
                        tint = InkBlack,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Send to ${recipientInput.trim()}",
                            fontFamily = UrbanistFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = InkBlack
                        )
                        Text(
                            text = "Direct number",
                            fontFamily = UrbanistFontFamily,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(horizontal = 20.dp))
            }

            // Contacts List
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredContacts, key = { it.id + it.number }) { contact ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onContactSelect(contact.number, contact.name)
                            }
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        AvatarView(
                            name = contact.name,
                            photoUri = contact.photoUri,
                            size = 46.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = contact.name,
                                fontFamily = UrbanistFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = InkBlack
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = PhoneNumberHelper.format(contact.number),
                                fontFamily = UrbanistFontFamily,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
