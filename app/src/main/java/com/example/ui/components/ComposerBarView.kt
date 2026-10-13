package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.model.SimInfo
import com.example.core.util.GsmSegmentCounter
import com.example.ui.theme.CardWhite
import com.example.ui.theme.InkBlack
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrbanistFontFamily

@Composable
fun ComposerBarView(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit,
    onCameraClick: () -> Unit,
    onMicClick: () -> Unit,
    onEmojiClick: () -> Unit,
    activeSims: List<SimInfo>,
    selectedSimId: Int,
    onSimSelect: (Int) -> Unit,
    isRecording: Boolean = false,
    modifier: Modifier = Modifier
) {
    val segmentResult = GsmSegmentCounter.calculate(text)
    val hasText = text.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // SIM switcher & GSM segments indicator if dual SIM or multi-segment
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp, start = 8.dp, end = 8.dp)
        ) {
            if (activeSims.size > 1) {
                activeSims.forEach { sim ->
                    val isSimSelected = sim.subscriptionId == selectedSimId
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSimSelected) InkBlack else CardWhite)
                            .clickable { onSimSelect(sim.subscriptionId) }
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = sim.displayName,
                            fontFamily = UrbanistFontFamily,
                            fontSize = 11.sp,
                            color = if (isSimSelected) CardWhite else TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (text.isNotEmpty()) {
                Text(
                    text = "${segmentResult.charCount}/${segmentResult.segmentCount}",
                    fontFamily = UrbanistFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        // Main input bar row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // White rounded composer container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(CardWhite)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Emoji button
                    IconButton(
                        onClick = onEmojiClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SentimentSatisfiedAlt,
                            contentDescription = "Emoji",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Text Field
                    Box(modifier = Modifier.weight(1f)) {
                        if (text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.enter_text),
                                fontFamily = UrbanistFontFamily,
                                color = TextSecondary,
                                fontSize = 15.sp
                            )
                        }
                        BasicTextField(
                            value = text,
                            onValueChange = onTextChange,
                            textStyle = TextStyle(
                                fontFamily = UrbanistFontFamily,
                                fontSize = 15.sp,
                                color = InkBlack
                            ),
                            cursorBrush = SolidColor(InkBlack),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("composer_text_input")
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Paperclip attachment button
                    IconButton(
                        onClick = onAttachClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AttachFile,
                            contentDescription = stringResource(R.string.attach),
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Camera button (visible when empty text, as in Screen 2)
                    AnimatedVisibility(visible = !hasText) {
                        IconButton(
                            onClick = onCameraClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = stringResource(R.string.camera),
                                tint = TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Action button at right:
            // Empty text -> White circular 48dp Mic button (Screen 2)
            // Has text   -> Black circular 48dp Send button (Screen 3)
            Crossfade(targetState = hasText, label = "send_mic_swap") { typing ->
                if (typing) {
                    CircularIconButton(
                        icon = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.send),
                        onClick = onSendClick,
                        size = 52.dp,
                        iconSize = 22.dp,
                        backgroundColor = InkBlack,
                        iconTint = CardWhite,
                        testTag = "send_button"
                    )
                } else {
                    CircularIconButton(
                        icon = Icons.Outlined.Mic,
                        contentDescription = stringResource(R.string.record_audio),
                        onClick = onMicClick,
                        size = 52.dp,
                        iconSize = 24.dp,
                        backgroundColor = CardWhite,
                        iconTint = InkBlack,
                        testTag = "mic_button"
                    )
                }
            }
        }
    }
}
