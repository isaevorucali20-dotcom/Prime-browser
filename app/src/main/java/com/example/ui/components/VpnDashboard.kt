package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BrowserViewModel

@Composable
fun VpnDashboard(
    viewModel: BrowserViewModel,
    accentColor: Color
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val servers = listOf("Reykjavik, Iceland", "Zurich, Switzerland", "Tokyo, Japan", "Amsterdam, Netherlands")

    val infiniteTransition = rememberInfiniteTransition(label = "VpnPulsing")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaPulse"
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AERO MILITARY-GRADE VPN",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (viewModel.isVpnEnabled) Color(0xFF10B981).copy(alpha = pulseAlpha)
                                else Color(0x66FFFFFF)
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (viewModel.isVpnEnabled) "SECURE TUNNEL: ACTIVE" else "VPN TUNNEL: INACTIVE",
                        color = if (viewModel.isVpnEnabled) Color(0xFF10B981) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            GlassButton(
                onClick = { viewModel.toggleVpn() },
                glowAccentColor = Color(0xFF10B981),
                isSelected = viewModel.isVpnEnabled
            ) {
                Text(
                    text = if (viewModel.isVpnEnabled) "Disconnect" else "Establish",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // IP & Country Selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SPOOFED DESTINATION",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Box {
                    GlassButton(
                        onClick = { isDropdownExpanded = true },
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth(),
                        isSelected = false
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Country",
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = viewModel.vpnServer,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.background(Color(0xFF1F2937))
                    ) {
                        servers.forEach { server ->
                            DropdownMenuItem(
                                text = { Text(server, color = Color.White) },
                                onClick = {
                                    viewModel.selectVpnServer(server)
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SPOOFED IP POOL",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                GlassButton(
                    onClick = {},
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = if (viewModel.isVpnEnabled) viewModel.vpnIPAddress else "Reverted to ISP IP",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (viewModel.isVpnEnabled) {
            Spacer(modifier = Modifier.height(16.dp))

            // Latency & Speeds
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ping
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = "latency",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${viewModel.vpnPing} ms",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // AES Encryption Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Shield",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AES-256 GCM",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Network throughput Speeds down/up
                Text(
                    text = "↓ ${viewModel.vpnSpeedDown} Mbps  ↑ ${viewModel.vpnSpeedUp} Mbps",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Animated Wireframe Traffic Loop using Canvas curves
            Text(
                text = "ENCRYPTED CHANNELS TELEMETRY SPEED",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .padding(vertical = 4.dp)
            ) {
                val dataPoints = viewModel.vpnTrafficHistory
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (dataPoints.size < 2) return@Canvas
                    val width = size.width
                    val height = size.height

                    val stepX = width / (dataPoints.size - 1)
                    val maxVal = (dataPoints.maxOrNull() ?: 100f).coerceAtLeast(30f)
                    
                    val path = Path()
                    val pathFill = Path()

                    dataPoints.forEachIndexed { i, value ->
                        val x = i * stepX
                        val y = height - (value / maxVal) * (height * 0.85f)
                        if (i == 0) {
                            path.moveTo(x, y)
                            pathFill.moveTo(x, height)
                            pathFill.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            pathFill.lineTo(x, y)
                        }
                    }
                    pathFill.lineTo(width, height)
                    pathFill.close()

                    // Draw filling gradient
                    drawPath(
                        path = pathFill,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF10B981).copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )

                    // Draw actual stroke
                    drawPath(
                        path = path,
                        color = Color(0xFF10B981),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}
