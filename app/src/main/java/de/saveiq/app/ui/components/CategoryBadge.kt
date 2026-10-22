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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                        Color(0xFF065F46),
                        Color(0xFF0F172A),
                        Color(0xFF0D9488)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xFF34D399).copy(alpha = 0.45f),
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.72f)) {
            val w = this.size.width
            val h = this.size.height

            // Three ascending wealth bars
            val barWidth = w * 0.16f
            val baseLineY = h * 0.84f

            // Bar 1 (Left)
            val bar1H = h * 0.26f
            drawRoundRect(
                color = Color(0xFFA7F3D0),
                topLeft = Offset(w * 0.14f, baseLineY - bar1H),
                size = Size(barWidth, bar1H),
                cornerRadius = CornerRadius(barWidth * 0.3f)
            )

            // Bar 2 (Middle)
            val bar2H = h * 0.44f
            drawRoundRect(
                color = Color(0xFFD1FAE5),
                topLeft = Offset(w * 0.42f, baseLineY - bar2H),
                size = Size(barWidth, bar2H),
                cornerRadius = CornerRadius(barWidth * 0.3f)
            )

            // Bar 3 (Right - Tallest)
            val bar3H = h * 0.64f
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.70f, baseLineY - bar3H),
                size = Size(barWidth, bar3H),
                cornerRadius = CornerRadius(barWidth * 0.3f)
            )

            // Golden Upward Growth Trend Line
            val trendPath = Path().apply {
                moveTo(w * 0.14f, h * 0.46f)
                lineTo(w * 0.40f, h * 0.26f)
                lineTo(w * 0.56f, h * 0.34f)
                lineTo(w * 0.84f, h * 0.10f)
            }
            drawPath(
                path = trendPath,
                color = Color(0xFFFDE047),
                style = Stroke(
                    width = w * 0.095f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Arrowhead
            val arrowHead = Path().apply {
                moveTo(w * 0.64f, h * 0.10f)
                lineTo(w * 0.84f, h * 0.10f)
                lineTo(w * 0.84f, h * 0.30f)
            }
            drawPath(
                path = arrowHead,
                color = Color(0xFFFDE047),
                style = Stroke(
                    width = w * 0.095f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
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
