package com.gitmanager.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gitmanager.app.core.theme.MonoDanger
import com.gitmanager.app.core.theme.MonoDangerMuted
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SlideToLogoutButton(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    text: String = "Slide to sign out"
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val knobSize = 44.dp
    val trackHeight = 54.dp

    val offsetX = remember { Animatable(0f) }
    var isTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        if (!isLoading && isTriggered) {
            isTriggered = false
            offsetX.animateTo(0f, tween(300))
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                1.dp,
                MonoDangerMuted.copy(alpha = 0.4f),
                CircleShape
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidthPx = with(density) { maxWidth.toPx() }
        val knobSizePx = with(density) { knobSize.toPx() }
        val maxDragDistance = (totalWidthPx - knobSizePx - with(density) { 10.dp.toPx() }).coerceAtLeast(0f)

        val progress = if (maxDragDistance > 0f) (offsetX.value / maxDragDistance).coerceIn(0f, 1f) else 0f

        // Red danger progress track behind the thumb
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(with(density) { (offsetX.value + knobSizePx + 6.dp.toPx()).toDp() })
                .clip(CircleShape)
                .background(MonoDanger.copy(alpha = 0.12f))
        )

        // Center Hint Text
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isLoading) "Signing out..." else text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.4.sp
                ),
                color = MonoDangerMuted.copy(alpha = (1f - progress * 1.5f).coerceIn(0f, 0.9f)),
                modifier = Modifier.alpha(if (isLoading) 0f else 1f)
            )
        }

        // Draggable Knob (Danger Red)
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt() + with(density) { 5.dp.toPx() }.roundToInt(), 0) }
                .size(knobSize)
                .clip(CircleShape)
                .background(MonoDanger)
                .draggable(
                    enabled = !isLoading && !isTriggered,
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            val target = (offsetX.value + delta).coerceIn(0f, maxDragDistance)
                            offsetX.snapTo(target)
                        }
                    },
                    onDragStopped = {
                        coroutineScope.launch {
                            if (offsetX.value >= maxDragDistance * 0.75f) {
                                offsetX.animateTo(maxDragDistance, tween(150))
                                isTriggered = true
                                onLogout()
                            } else {
                                offsetX.animateTo(0f, tween(200))
                            }
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = androidx.compose.ui.graphics.Color.White,
                    strokeWidth = 2.dp
                )
            } else if (isTriggered) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = "Slide to Logout",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
