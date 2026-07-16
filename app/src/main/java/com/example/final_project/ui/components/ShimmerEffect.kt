package com.example.final_project.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Locked shimmer API — skeleton loading for lists/cards while data loads.
 *
 * Prefer [ShimmerBox] for fixed-size placeholders.
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerX",
    )

    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface
    val brush = Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(shimmerX - 200f, shimmerX - 200f),
        end = Offset(shimmerX, shimmerX),
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush),
    )
}

/**
 * Fixed-size shimmer block used in section skeletons.
 *
 * ```
 * ShimmerBox(width = MelodifySpacing.songCardCover, height = MelodifySpacing.songCardCover)
 * ```
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    width: Dp = 120.dp,
    height: Dp = 16.dp,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    ShimmerEffect(
        modifier = modifier
            .width(width)
            .height(height),
        shape = shape,
    )
}
