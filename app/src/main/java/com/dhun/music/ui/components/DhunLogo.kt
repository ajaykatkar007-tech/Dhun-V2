package com.dhun.music.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhun.music.ui.theme.DhunPink
import com.dhun.music.ui.theme.DhunTextPrimary
import com.dhun.music.ui.theme.DhunViolet

/**
 * Stylized "D" combined with a musical-note insignia for Dhun.
 * Distinctive, vector-rendered, Indian-modern musical mark.
 */
@Composable
fun DhunLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    animated: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dhun_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val scale = if (animated) pulseScale else 1.0f

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width * scale
        val h = this.size.height * scale
        val offsetX = (this.size.width - w) / 2f
        val offsetY = (this.size.height - h) / 2f

        val strokeWidth = w * 0.16f
        val gradient = Brush.linearGradient(
            colors = listOf(DhunViolet, DhunPink),
            start = Offset(offsetX, offsetY),
            end = Offset(offsetX + w, offsetY + h)
        )

        // Draw note stem (the vertical backbone of the "D")
        drawLine(
            brush = gradient,
            start = Offset(offsetX + strokeWidth / 2f + w * 0.12f, offsetY + h * 0.1f),
            end = Offset(offsetX + strokeWidth / 2f + w * 0.12f, offsetY + h * 0.88f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Draw the curved bowl of "D" that transitions into a melodic loop
        val bowlPath = Path().apply {
            moveTo(offsetX + strokeWidth / 2f + w * 0.12f, offsetY + h * 0.14f)
            cubicTo(
                offsetX + w * 0.95f, offsetY + h * 0.14f,
                offsetX + w * 0.95f, offsetY + h * 0.82f,
                offsetX + strokeWidth / 2f + w * 0.12f, offsetY + h * 0.82f
            )
        }
        drawPath(
            path = bowlPath,
            brush = gradient,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Musical note head accent inside the bowl
        val noteHeadRadius = w * 0.13f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(DhunPink, DhunViolet),
                center = Offset(offsetX + w * 0.52f, offsetY + h * 0.50f),
                radius = noteHeadRadius
            ),
            radius = noteHeadRadius,
            center = Offset(offsetX + w * 0.52f, offsetY + h * 0.50f)
        )
    }
}

/**
 * Full branded header lockup for Dhun with logo mark and refined typography.
 */
@Composable
fun DhunBrandHeader(
    modifier: Modifier = Modifier,
    showTagline: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DhunLogoMark(size = 32.dp, animated = true)
        Spacer(modifier = Modifier.width(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "DHUN",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = DhunTextPrimary
            )
            if (showTagline) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(DhunPink)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Your music, your vibe.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DhunViolet,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
