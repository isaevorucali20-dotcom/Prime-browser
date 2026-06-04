package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BrowserViewModel

@Composable
fun TorCircuitView(
    viewModel: BrowserViewModel,
    accentColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TorCircuitSignals")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RelayPulsing"
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
                    text = "ONION PROTOCOL (TOR DECENTRALIZATION)",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = viewModel.torStatusString,
                    color = if (viewModel.isOnionRoutingEnabled) Color(0xFFA855F7) else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            GlassButton(
                onClick = { viewModel.toggleOnionRouting() },
                glowAccentColor = Color(0xFFA855F7),
                isSelected = viewModel.isOnionRoutingEnabled
            ) {
                Text(
                    text = if (viewModel.isOnionRoutingEnabled) "Disable Tor" else "Enable Tor",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (viewModel.isOnionRoutingEnabled) {
            Spacer(modifier = Modifier.height(16.dp))

            // Graph representation of circuit
            Text(
                text = "ACTIVE LAYER MULTI-HOP PATHWAY",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Circuit Flow Nodes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                // Node 1: Me
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(accentColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "You | Local Sandboxed Scope (Device Localhost)",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Node link
                VerticalDashedIndicator(accentColor)

                // Node 2: Guard
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(modifier = Modifier.size(12.dp), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF10B981), CircleShape)
                        )
                        Canvas(modifier = Modifier.size(24.dp)) {
                            drawCircle(
                                color = Color(0xFF10B981).copy(alpha = 0.4f),
                                radius = size.minDimension / 1.8f * pulseScale,
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = viewModel.onionCircuitGuard,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }

                VerticalDashedIndicator(Color(0xFFA855F7))

                // Node 3: Relay
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(modifier = Modifier.size(12.dp), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFA855F7), CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = viewModel.onionCircuitRelay,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }

                VerticalDashedIndicator(Color(0xFFF43F5E))

                // Node 4: Exit
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(modifier = Modifier.size(12.dp), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF43F5E), CircleShape)
                        )
                        Canvas(modifier = Modifier.size(24.dp)) {
                            drawCircle(
                                color = Color(0xFFF43F5E).copy(alpha = 0.4f),
                                radius = size.minDimension / 1.8f * pulseScale,
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = viewModel.onionCircuitExit,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                VerticalDashedIndicator(Color.Yellow)

                // Node 5: Onion Website Destination
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Onion Target",
                        tint = Color.Yellow,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Destination: ${viewModel.activeUrl} (Parsed Inside Secure sandbox)",
                        color = Color.Yellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Encrypted in 3 discrete shells.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )

                GlassButton(
                    onClick = { viewModel.rebuildTorCircuit() },
                    cornerRadius = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "refresh circuit",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Refresh circuit",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VerticalDashedIndicator(
    color: Color
) {
    Column(
        modifier = Modifier
            .padding(start = 5.dp)
            .height(14.dp)
            .width(2.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(2.dp)
                    .background(color, CircleShape)
            )
        }
    }
}
