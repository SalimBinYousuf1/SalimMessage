package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.ConversationCategory
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DividerColor
import com.example.ui.theme.InkBlack
import com.example.ui.theme.UrbanistFontFamily

data class ChipItem(
    val category: ConversationCategory,
    val titleRes: Int
)

@Composable
fun ChipRow(
    selectedCategory: ConversationCategory,
    onCategorySelected: (ConversationCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val chips = remember {
        listOf(
            ChipItem(ConversationCategory.ALL, R.string.chip_all),
            ChipItem(ConversationCategory.CONTACTS, R.string.chip_contacts),
            ChipItem(ConversationCategory.UNKNOWN, R.string.chip_unknown),
            ChipItem(ConversationCategory.NEW, R.string.chip_new)
        )
    }

    LazyRow(
        modifier = modifier.padding(vertical = 10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(chips, key = { it.category.name }) { chip ->
            val isSelected = chip.category == selectedCategory

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) InkBlack else CardWhite,
                animationSpec = tween(durationMillis = 200),
                label = "chipBg"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) CardWhite else InkBlack,
                animationSpec = tween(durationMillis = 200),
                label = "chipText"
            )

            Box(
                modifier = Modifier
                    .height(38.dp)
                    .shadow(
                        elevation = if (isSelected) 3.dp else 1.dp,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .then(
                        if (!isSelected) {
                            Modifier.border(width = 1.dp, color = DividerColor, shape = CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onCategorySelected(chip.category) }
                    )
                    .padding(horizontal = 20.dp)
                    .testTag("chip_${chip.category.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(chip.titleRes),
                    fontFamily = UrbanistFontFamily,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 14.sp,
                    color = textColor
                )
            }
        }
    }
}
