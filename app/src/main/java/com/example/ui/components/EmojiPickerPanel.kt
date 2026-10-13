package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DividerColor
import com.example.ui.theme.InkBlack
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrbanistFontFamily

private val EMOJI_CATEGORIES = listOf(
    "Smileys" to listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃",
        "😉", "😊", "😇", "🥰", "😍", "🤩", "😘", "😗", "😚", "😙",
        "😋", "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔",
        "🤐", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄", "😬", "🤥",
        "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮",
        "🤧", "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳", "🥸", "😎",
        "🤓", "🧐", "😕", "😟", "🙁", "😮", "😯", "😲", "😳", "🥺",
        "😦", "😧", "😨", "😰", "😥", "😢", "😭", "😱", "😖", "😣"
    ),
    "Gestures" to listOf(
        "👍", "👎", "👏", "🙌", "👐", "🤲", "🤝", "👊", "✊", "🤛",
        "🤜", "🤞", "✌️", "🤟", "🤘", "👌", "🤌", "🤏", "👈", "👉",
        "👆", "👇", "☝️", "✋", "🤚", "🖐️", "🖖", "👋", "🤙", "💪",
        "🦾", "🖕", "✍️", "🙏", "💅", "🤳", "💃", "🕺", "🚶", "🏃"
    ),
    "Hearts" to listOf(
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
        "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "💌",
        "💋", "💯", "🔥", "✨", "🌟", "⭐", "💫", "💥", "💢", "💦"
    ),
    "Objects" to listOf(
        "🎉", "🎊", "🎈", "🎂", "🎁", "🏆", "🥇", "🥈", "🥉", "⚽",
        "🏀", "🏈", "⚾", "🎾", "🍕", "🍔", "🍟", "🍦", "🍩", "☕",
        "🍺", "🍷", "🚗", "🚕", "✈️", "🚀", "💡", "📱", "💻", "⌚"
    )
)

@Composable
fun EmojiPickerPanel(
    onEmojiSelected: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val currentEmojis = EMOJI_CATEGORIES[selectedCategoryIndex].second

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(CardWhite)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Category Tabs Row with Backspace Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                ScrollableTabRow(
                    selectedTabIndex = selectedCategoryIndex,
                    containerColor = CardWhite,
                    contentColor = InkBlack,
                    edgePadding = 8.dp,
                    divider = {},
                    modifier = Modifier.weight(1f)
                ) {
                    EMOJI_CATEGORIES.forEachIndexed { index, (name, _) ->
                        Tab(
                            selected = selectedCategoryIndex == index,
                            onClick = { selectedCategoryIndex = index },
                            text = {
                                Text(
                                    text = name,
                                    fontFamily = UrbanistFontFamily,
                                    fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (selectedCategoryIndex == index) InkBlack else TextSecondary
                                )
                            }
                        )
                    }
                }

                // Backspace button
                IconButton(
                    onClick = onBackspace,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Backspace,
                        contentDescription = "Backspace",
                        tint = InkBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(color = DividerColor)

            // Emoji Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(currentEmojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onEmojiSelected(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 22.sp
                        )
                    }
                }
            }
        }
    }
}
