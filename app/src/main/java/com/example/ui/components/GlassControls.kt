package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp, // 3XL rounding
    borderAlpha: Float = 0.22f, // High-contrast crisp border
    backgroundAlpha: Float = 0.12f, // Lower opacity to let the blurs flow through
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = backgroundAlpha),
                        Color.White.copy(alpha = backgroundAlpha * 0.5f)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = borderAlpha),
                            Color.White.copy(alpha = borderAlpha * 0.3f)
                        )
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(18.dp),
        content = content
    )
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp, // Matches nice theme rounding
    glowAccentColor: Color = Color(0xFF38BDF8),
    isSelected: Boolean = false,
    textTag: String = "",
    content: @Composable BoxScope.() -> Unit
) {
    val finalBorderAlpha = if (isSelected) 0.60f else 0.18f
    val finalBgAlpha = if (isSelected) 0.25f else 0.08f

    Box(
        modifier = modifier
            .testTag(textTag)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(
                            glowAccentColor.copy(alpha = 0.25f),
                            glowAccentColor.copy(alpha = 0.05f)
                        )
                    )
                } else {
                    SolidColor(Color.White.copy(alpha = finalBgAlpha))
                }
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (isSelected) {
                        SolidColor(glowAccentColor.copy(alpha = finalBorderAlpha))
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = finalBorderAlpha),
                                Color.White.copy(alpha = finalBorderAlpha * 0.3f)
                            )
                        )
                    }
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = glowAccentColor),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = TextStyle(color = Color.White, fontSize = 14.sp),
    accentColor: Color = Color(0xFF38BDF8)
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.06f)) // Frosted bg-white/5 feel
            .border(
                BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.20f), // Higher contrast border
                            accentColor.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.06f)
                        )
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty() && placeholderText.isNotEmpty()) {
                Text(
                    text = placeholderText,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = textStyle,
                cursorBrush = SolidColor(accentColor),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingIcon()
        }
    }
}
