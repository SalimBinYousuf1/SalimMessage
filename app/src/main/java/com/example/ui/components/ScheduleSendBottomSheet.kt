package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DividerColor
import com.example.ui.theme.InkBlack
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrbanistFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSendBottomSheet(
    prefilledRecipient: String = "",
    prefilledText: String = "",
    onSchedule: (recipient: String, body: String, scheduledTime: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var recipient by remember { mutableStateOf(prefilledRecipient) }
    var messageBody by remember { mutableStateOf(prefilledText) }

    // Date offset in days: 0 = Today, 1 = Tomorrow, 2 = Day after
    var dayOffset by remember { mutableIntStateOf(0) }

    // Selected Time
    val nowCal = remember { Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) } }
    var hour by remember { mutableIntStateOf(nowCal.get(Calendar.HOUR).let { if (it == 0) 12 else it }) }
    var minute by remember { mutableIntStateOf((nowCal.get(Calendar.MINUTE) / 5) * 5) }
    var isPm by remember { mutableStateOf(nowCal.get(Calendar.AM_PM) == Calendar.PM) }

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
                text = stringResource(R.string.schedule_send),
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = InkBlack
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recipient field if not prefilled
            if (prefilledRecipient.isBlank()) {
                Text(
                    text = "Recipient",
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(androidx.compose.ui.graphics.Color(0xFFF1F1F1))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (recipient.isEmpty()) {
                        Text(
                            text = "Phone number",
                            fontFamily = UrbanistFontFamily,
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                    BasicTextField(
                        value = recipient,
                        onValueChange = { recipient = it },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = UrbanistFontFamily, fontSize = 14.sp, color = InkBlack),
                        cursorBrush = SolidColor(InkBlack),
                        modifier = Modifier.fillMaxWidth().testTag("schedule_recipient_input")
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Message Body input holder
            Text(
                text = "Message",
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(androidx.compose.ui.graphics.Color(0xFFF1F1F1))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (messageBody.isEmpty()) {
                    Text(
                        text = "Write your message to be scheduled...",
                        fontFamily = UrbanistFontFamily,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = messageBody,
                    onValueChange = { messageBody = it },
                    textStyle = TextStyle(fontFamily = UrbanistFontFamily, fontSize = 14.sp, color = InkBlack),
                    cursorBrush = SolidColor(InkBlack),
                    modifier = Modifier.fillMaxWidth().testTag("schedule_body_input")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Selection Pills
            Text(
                text = "Select Date",
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0 to "Today", 1 to "Tomorrow", 2 to "In 2 days").forEach { (offset, label) ->
                    val isSelected = dayOffset == offset
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) InkBlack else androidx.compose.ui.graphics.Color(0xFFF1F1F1))
                            .clickable { dayOffset = offset }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            fontFamily = UrbanistFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) CardWhite else InkBlack
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clock Time Selection (Hours : Minutes AM/PM)
            Text(
                text = "Select Time (Clock)",
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Hour Selector Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(androidx.compose.ui.graphics.Color(0xFFF1F1F1))
                        .clickable { hour = if (hour >= 12) 1 else hour + 1 }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "${hour.toString().padStart(2, '0')} hr",
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = InkBlack
                    )
                }

                Text(text = ":", fontFamily = UrbanistFontFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = InkBlack)

                // Minute Selector Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(androidx.compose.ui.graphics.Color(0xFFF1F1F1))
                        .clickable { minute = (minute + 15) % 60 }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "${minute.toString().padStart(2, '0')} min",
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = InkBlack
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // AM / PM Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (!isPm) InkBlack else androidx.compose.ui.graphics.Color(0xFFF1F1F1))
                        .clickable { isPm = false }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(text = "AM", fontFamily = UrbanistFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (!isPm) CardWhite else InkBlack)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isPm) InkBlack else androidx.compose.ui.graphics.Color(0xFFF1F1F1))
                        .clickable { isPm = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(text = "PM", fontFamily = UrbanistFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isPm) CardWhite else InkBlack)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = {
                    if (recipient.isBlank() && prefilledRecipient.isBlank()) {
                        Toast.makeText(context, "Please specify a recipient", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (messageBody.isBlank()) {
                        Toast.makeText(context, "Please enter message text", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val cal = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, dayOffset)
                        set(Calendar.HOUR, if (hour == 12) 0 else hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        set(Calendar.AM_PM, if (isPm) Calendar.PM else Calendar.AM)
                    }

                    if (cal.timeInMillis <= System.currentTimeMillis()) {
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                    }

                    val targetRecipient = if (prefilledRecipient.isNotBlank()) prefilledRecipient else recipient.trim()
                    onSchedule(targetRecipient, messageBody.trim(), cal.timeInMillis)
                    Toast.makeText(context, "Message scheduled successfully!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("confirm_schedule_button")
            ) {
                Text(
                    text = "Schedule SMS",
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CardWhite
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
