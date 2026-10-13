package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CardWhite
import com.example.ui.theme.InkBlack
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrbanistFontFamily

data class ConversationFilterOptions(
    val unreadOnly: Boolean = false,
    val hasAttachments: Boolean = false,
    val starredOnly: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentFilter: ConversationFilterOptions,
    onApplyFilter: (ConversationFilterOptions) -> Unit,
    onDismiss: () -> Unit
) {
    var unreadOnly by remember { mutableStateOf(currentFilter.unreadOnly) }
    var hasAttachments by remember { mutableStateOf(currentFilter.hasAttachments) }
    var starredOnly by remember { mutableStateOf(currentFilter.starredOnly) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = stringResource(R.string.filter),
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = InkBlack
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Unread only toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Unread messages only",
                    fontFamily = UrbanistFontFamily,
                    fontSize = 16.sp,
                    color = InkBlack,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = unreadOnly,
                    onCheckedChange = { unreadOnly = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = CardWhite, checkedTrackColor = InkBlack)
                )
            }

            // Has attachments toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(
                    text = "With photos or attachments",
                    fontFamily = UrbanistFontFamily,
                    fontSize = 16.sp,
                    color = InkBlack,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = hasAttachments,
                    onCheckedChange = { hasAttachments = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = CardWhite, checkedTrackColor = InkBlack)
                )
            }

            // Starred only toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Starred messages only",
                    fontFamily = UrbanistFontFamily,
                    fontSize = 16.sp,
                    color = InkBlack,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = starredOnly,
                    onCheckedChange = { starredOnly = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = CardWhite, checkedTrackColor = InkBlack)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = {
                        unreadOnly = false
                        hasAttachments = false
                        starredOnly = false
                        onApplyFilter(ConversationFilterOptions())
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Reset",
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        onApplyFilter(
                            ConversationFilterOptions(
                                unreadOnly = unreadOnly,
                                hasAttachments = hasAttachments,
                                starredOnly = starredOnly
                            )
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text(
                        text = "Apply",
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = CardWhite
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
