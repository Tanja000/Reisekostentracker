package de.saveiq.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.saveiq.app.model.ExpenseCategory

@Composable
fun SaveIQAppIcon(
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    val cornerRadius = size * 0.28f
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E3A8A),
                        Color(0xFF2563EB)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xFF60A5FA).copy(alpha = 0.45f),
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.74f)) {
            val w = this.size.width
            val h = this.size.height

            // 1. Golden Savings Coin peeking into top of Smart Wallet
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = w * 0.24f,
                center = Offset(w * 0.44f, h * 0.30f)
            )
            drawCircle(
                color = Color(0xFFFEF08A),
                radius = w * 0.17f,
                center = Offset(w * 0.44f, h * 0.30f)
            )

            // 2. Smart Wallet Body (Crisp White)
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.12f, h * 0.34f),
                size = Size(w * 0.74f, h * 0.54f),
                cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
            )

            // 3. Magnetic Smart Clasp on right side of wallet
            drawRoundRect(
                color = Color(0xFF1D4ED8),
                topLeft = Offset(w * 0.58f, h * 0.49f),
                size = Size(w * 0.32f, h * 0.24f),
                cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
            )

            // 4. Golden IQ Star Sparkle inside the clasp
            val claspStar = Path().apply {
                val cx = w * 0.71f
                val cy = h * 0.61f
                val rOut = w * 0.09f
                val rIn = w * 0.03f
                moveTo(cx, cy - rOut)
                lineTo(cx + rIn, cy - rIn)
                lineTo(cx + rOut, cy)
                lineTo(cx + rIn, cy + rIn)
                lineTo(cx, cy + rOut)
                lineTo(cx - rIn, cy + rIn)
                lineTo(cx - rOut, cy)
                lineTo(cx - rIn, cy - rIn)
                close()
            }
            drawPath(path = claspStar, color = Color(0xFFFDE047))

            // 5. Top-Right AI Sparkle Star
            val aiStar = Path().apply {
                val cx = w * 0.80f
                val cy = h * 0.18f
                val rOut = w * 0.11f
                val rIn = w * 0.035f
                moveTo(cx, cy - rOut)
                lineTo(cx + rIn, cy - rIn)
                lineTo(cx + rOut, cy)
                lineTo(cx + rIn, cy + rIn)
                lineTo(cx, cy + rOut)
                lineTo(cx - rIn, cy + rIn)
                lineTo(cx - rOut, cy)
                lineTo(cx - rIn, cy - rIn)
                close()
            }
            drawPath(path = aiStar, color = Color(0xFF38BDF8))
        }
    }
}

@Composable
fun CategoryIconBadge(
    category: ExpenseCategory,
    size: Int = 40,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(category.color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = category.name,
            tint = category.color,
            modifier = Modifier.size((size * 0.55).dp)
        )
    }
}

@Composable
fun CategoryChip(
    category: ExpenseCategory,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) category.color else category.color.copy(alpha = 0.12f),
        contentColor = if (isSelected) Color.White else category.color,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
