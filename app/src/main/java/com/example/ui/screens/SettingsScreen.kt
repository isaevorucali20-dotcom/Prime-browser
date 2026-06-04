package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BrowserViewModel
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.VpnDashboard
import com.example.ui.components.TorCircuitView

@Composable
fun SettingsScreen(
    viewModel: BrowserViewModel,
    onCloseSettings: () -> Unit,
    accentColor: Color
) {
    val presets = listOf(
        "Safari 18 Engine",
        "Google Chrome Mobile v135",
        "Firefox Quantum Focus",
        "Tor Browser Engine (Max Privacy)"
    )
    val hardwareConcurrencyProfiles = listOf(
        "8 Core Intel Skylake Mobile",
        "12 Core Apple M3 Max",
        "6 Core Qualcomm Snapdragon Secure",
        "Block Concurrency metrics"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 44.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "settings",
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "BROWSER CUSTOMIZER",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Liquid glass & fingerprint spoof registers",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }

            GlassButton(
                onClick = onCloseSettings,
                glowAccentColor = accentColor,
                textTag = "close_settings"
            ) {
                Text(
                    text = "Close",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Anonymity Protection Suite
            item {
                VpnDashboard(viewModel = viewModel, accentColor = accentColor)
            }

            item {
                TorCircuitView(viewModel = viewModel, accentColor = accentColor)
            }

            // Section 1: Liquid Glass aesthetics Customization
            item {
                Text(
                    text = "LIQUID GLASS THEME ACCENTS",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    // Accent Color Presets
                    Text(
                        text = "Select Accent Identity Signature",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val colorsList = listOf(
                            "#38BDF8" to "Aqua Aurora", // custom light-blue sky
                            "#A855F7" to "Cosmic Slate", // Deep violet purple
                            "#10B981" to "Mint Shield", // Mint vibrant green
                            "#EF4444" to "Red Developer" // Red Danger
                        )
                        colorsList.forEach { (colorHex) ->
                            val isSelected = viewModel.activeAccentColorHex == colorHex
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(colorHex)))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.activeAccentColorHex = colorHex }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Blur strength Slider
                    Text(
                        text = "Glass Backdrop Blur Depth: ${viewModel.glassBlurStrength.toInt()} dp",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                    Slider(
                        value = viewModel.glassBlurStrength,
                        onValueChange = { viewModel.glassBlurStrength = it },
                        valueRange = { 5f..40f },
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )

                    // Gradient speed Slider
                    Text(
                        text = "Liquid Ambient Flow Speed Rate: ${String.format("%.1f", viewModel.liquidGradientSpeed)}x",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                    Slider(
                        value = viewModel.liquidGradientSpeed,
                        onValueChange = { viewModel.liquidGradientSpeed = it },
                        valueRange = { 0.2f..3.0f },
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }
            }

            // Section 2: Fingerprint Override & Spoofer (Anonymity Core)
            item {
                Text(
                    text = "DIGITAL FINGERPRINT SHIELDING",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    // User Agent Presets
                    Text(
                        text = "Spoof Browser User-Agent Header",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    presets.forEach { preset ->
                        val isSelected = viewModel.userAgentPreset == preset
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.userAgentPreset = preset
                                    viewModel.logDevConsole("User Agent spoof altered to: $preset")
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.userAgentPreset = preset
                                    viewModel.logDevConsole("User Agent spoof altered to: $preset")
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(preset, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hardware Cores multiplier spoofer
                    Text(
                        text = "Hardware Concurrency Spoof Core Profile",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    hardwareConcurrencyProfiles.forEach { profile ->
                        val isSelected = viewModel.hardwareConcurrencyProfile == profile
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.hardwareConcurrencyProfile = profile }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.hardwareConcurrencyProfile = profile },
                                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(profile, color = Color.White, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Battery status spoofer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Battery Level Exposure Mocking",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Spoofs navigator.getBattery() to block precise tracking",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 9.sp
                            )
                        }

                        GlassButton(
                            onClick = {
                                val ran = (20..99).random()
                                viewModel.mockBatteryLevel = "$ran%"
                                viewModel.logDevConsole("Battery level sensor mock set to $ran%")
                            },
                            cornerRadius = 8.dp
                        ) {
                            Text(viewModel.mockBatteryLevel, color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Section 2.5: Deep Anonymity Operations
            item {
                Text(
                    text = "DEEP ANONYMITY CRYPTO PROTOCOLS",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    // Option 1: Canvas Fingerprint Noise
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Canvas API Fingerprint Noise", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Adds random math perturbation grids onto HTML5 elements to confuse user profiling algorithms.", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                        }
                        Switch(
                            checked = viewModel.isCanvasNoiseEnabled,
                            onCheckedChange = { viewModel.isCanvasNoiseEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 2: WebRTC IP Leak Guard
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("WebRTC IP Leak Guard", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Blocks internal network IP leaks on WebRTC media stream queries.", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                        }
                        Switch(
                            checked = viewModel.isWebRTCLeakGuardEnabled,
                            onCheckedChange = { viewModel.isWebRTCLeakGuardEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 3: HTTP Referrer Sanitization
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("HTTP Referrer Trim", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Truncates outgoing page references to absolute domain boundaries only.", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                        }
                        Switch(
                            checked = viewModel.isReferrerSpoofingEnabled,
                            onCheckedChange = { viewModel.isReferrerSpoofingEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 4: Session Sandbox
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Transient Session Sandbox", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Isolates cache buffers, purging dynamic storage scopes instantly on tab close.", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                        }
                        Switch(
                            checked = viewModel.isSessionSandboxingEnabled,
                            onCheckedChange = { viewModel.isSessionSandboxingEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 5: Tor / DoH DNS Tunnel
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Encrypted DNS Tunneling", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Intercepts domain queries into Quad9 Encrypted TLS / DoH tunnels.", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                        }
                        Switch(
                            checked = viewModel.isDohCryptTunnelEnabled,
                            onCheckedChange = { viewModel.isDohCryptTunnelEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                        )
                    }
                }
            }

            // Section 3: Ad Blocking & Tracking Toggles
            item {
                Text(
                    text = "BLOCKLIST FILTER REGISTERS",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("EasyList Ad Filter Rules", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Blocks banners, popups, and intrusive CSS nodes", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                        }
                        Switch(
                            checked = viewModel.trackerFilterEasyList,
                            onCheckedChange = { viewModel.trackerFilterEasyList = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("NoScript (Strict Cookie Shield)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Prunes inline storage calls and analytical tracking events", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                        }
                        Switch(
                            checked = viewModel.trackerFilterNoScript,
                            onCheckedChange = { viewModel.trackerFilterNoScript = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                        )
                    }
                }
            }

            // Section 3.25: Discrete Camouflage / Disguise setup
            item {
                Text(
                    text = "DISCRETE CAMOUFLAGE DECOY",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Calculator Camouflage Mode", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Enables an interactive iOS-style calculator overlay to mask your browsing session immediately.", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                        }
                        Switch(
                            checked = viewModel.isCamouflageEnabled,
                            onCheckedChange = { viewModel.isCamouflageEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                        )
                    }

                    if (viewModel.isCamouflageEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))

                        GlassButton(
                            onClick = { 
                                viewModel.activeCamouflageMode = true
                                onCloseSettings() 
                            },
                            glowAccentColor = accentColor,
                            isSelected = true,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Activate Camouflage Screen Now",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "🔑 HOW TO UNLOCK:\nLong press the 'C' (Clear) key or type '1337' and tap the '=' key on the calculator screen to instantly return to Prime Browser.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Section 4: Secure Cleaners and Exit
            item {
                Text(
                    text = "DATA SANITIZATION",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundAlpha = 0.15f
                ) {
                    Text(
                        text = "Local Storage & Session Wiper",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Wipes out all dynamic localized index record databases, site cookie stores, search histories, bookmarks, and developer diagnostics securely.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    GlassButton(
                        onClick = { viewModel.clearBrowsingData() },
                        glowAccentColor = Color(0xFFEF4444),
                        isSelected = true,
                        modifier = Modifier.fillMaxWidth(),
                        textTag = "clean_history_button"
                    ) {
                        Text(
                            text = "Execute Deep Data Clean",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: () -> ClosedFloatingPointRange<Float>,
    colors: SliderColors
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange(),
        colors = colors
    )
}
