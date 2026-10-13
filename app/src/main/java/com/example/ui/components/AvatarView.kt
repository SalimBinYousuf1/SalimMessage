package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.CardWhite
import com.example.ui.theme.InkBlack
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrbanistFontFamily

@Composable
fun AvatarView(
    name: String?,
    photoUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFE2E2E2)),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUri.isNullOrBlank()) {
            AsyncImage(
                model = photoUri,
                contentDescription = name ?: "Contact Avatar",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else if (!name.isNullOrBlank()) {
            val initials = name.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .map { it.first().uppercase() }
                .joinToString("")
            
            if (initials.isNotEmpty()) {
                Text(
                    text = initials,
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.38f).sp,
                    color = InkBlack
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = name,
                    tint = TextSecondary,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Unknown",
                tint = TextSecondary,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}
