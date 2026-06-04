package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BrowserViewModel

@Composable
fun DevToolsPanel(
    viewModel: BrowserViewModel,
    activePageHtml: String,
    onExecuteCode: (String) -> Unit,
    accentColor: Color
) {
    // Tab selector inside Dev Panel: 0 = Inspector, 1 = JS Console, 2 = Storage & Cookies
    var activeDevTab by remember { mutableStateOf(0) }
    var jsConsoleInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xEB020617)) // Frosted Slate terminal background
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(20.dp)
    ) {
        // Drag Indicator
        Box(
            modifier = Modifier
                .size(40.dp, 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.3f))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Red Warning Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Terminal",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "AERO HTML ELEMENT INSPECTOR",
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Unchecked DOM execution scope & debugger bridges",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }
            
            IconButton(onClick = { viewModel.forceToggleDevMode() }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!viewModel.isDevModeAcceptedRisk) {
            // Security acceptance panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = "Alert",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "DATA DRAIN WARNING",
                        color = Color(0xFFEF4444),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enabling Developer Tools turns off sandboxed site isolating firewalls. Raw scripts can intercept cookie sessions, capture your typed passwords, and spoof network connections. Intended solely for HTML developers.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp)).padding(12.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    GlassButton(
                        onClick = {
                            viewModel.isDevModeAcceptedRisk = true
                            viewModel.exportDomTree(activePageHtml)
                        },
                        glowAccentColor = Color(0xFFEF4444),
                        isSelected = true,
                        textTag = "accept_risk_button"
                    ) {
                        Text(
                            text = "Accept terms & open Inspector",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Active Developer Console
            // Tab Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassButton(
                    onClick = {
                        activeDevTab = 0
                        viewModel.exportDomTree(activePageHtml)
                    },
                    modifier = Modifier.weight(1f),
                    isSelected = activeDevTab == 0,
                    glowAccentColor = accentColor,
                    cornerRadius = 8.dp
                ) {
                    Text("HTML Inspector", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                GlassButton(
                    onClick = { activeDevTab = 1 },
                    modifier = Modifier.weight(1f),
                    isSelected = activeDevTab == 1,
                    glowAccentColor = accentColor,
                    cornerRadius = 8.dp
                ) {
                    Text("JS Console", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                GlassButton(
                    onClick = { activeDevTab = 2 },
                    modifier = Modifier.weight(1.2f),
                    isSelected = activeDevTab == 2,
                    glowAccentColor = accentColor,
                    cornerRadius = 8.dp
                ) {
                    Text("Audit Logs / Scripts", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (activeDevTab) {
                0 -> {
                    // HTML Code DOM tree inspector
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE BODY DOM TREE SCHEMA:",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { viewModel.exportDomTree(activePageHtml) }) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "reload DOM", tint = accentColor, modifier = Modifier.size(16.dp))
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            LazyColumn {
                                item {
                                    Text(
                                        text = viewModel.devWorkspaceSourceCode.ifEmpty { "Extracting active elements..." },
                                        color = Color(0xFFA5F3FC),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Javascript injection Console
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "JS BRIDGE LOGS:",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        // Console Log Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(viewModel.devDomConsoleLogs) { log ->
                                    val logColor = when {
                                        log.contains("AntiTracker") -> Color(0xFF10B981)
                                        log.contains("Error") || log.contains("DANGER") -> Color(0xFFEF4444)
                                        log.contains("Injecting") -> Color(0xFF38BDF8)
                                        else -> Color(0xFFE2E8F0)
                                    }
                                    Text(
                                        text = log,
                                        color = logColor,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Trigger Console Shell
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassTextField(
                                value = jsConsoleInput,
                                onValueChange = { jsConsoleInput = it },
                                modifier = Modifier.weight(1f),
                                placeholderText = "Execute custom JS payload (e.g. alert(3))...",
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (jsConsoleInput.trim().isNotEmpty()) {
                                        viewModel.injectCustomScript(jsConsoleInput, onExecuteCode)
                                        jsConsoleInput = ""
                                    }
                                }),
                                accentColor = Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            IconButton(
                                onClick = {
                                    if (jsConsoleInput.trim().isNotEmpty()) {
                                        viewModel.injectCustomScript(jsConsoleInput, onExecuteCode)
                                        jsConsoleInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                                    .size(40.dp)
                                    .testTag("execute_custom_js_button")
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", tint = Color.Black)
                            }
                        }
                    }
                }
                2 -> {
                    // precompiled templates and blocked third-party script logs
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Text(
                            text = "PRECOMPILED INJECTABLE TEMPLATES",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(viewModel.devCustomScriptsList) { (title, script) ->
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    backgroundAlpha = 0.15f
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(title, color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(script, color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
                                        }

                                        IconButton(onClick = { viewModel.injectCustomScript(script, onExecuteCode) }) {
                                            Icon(imageVector = Icons.Default.OfflineBolt, contentDescription = "Run quick script", tint = Color.Yellow)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
