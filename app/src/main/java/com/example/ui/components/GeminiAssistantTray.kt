package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BrowserViewModel

@Composable
fun GeminiAssistantTray(
    viewModel: BrowserViewModel,
    isOpen: Boolean,
    onClose: () -> Unit,
    activePageHtml: String,
    accentColor: Color
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color(0xD9020617)) // Frosted dark glass in line with bg-[#020617]
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top header handle
                Box(
                    modifier = Modifier
                        .size(40.dp, 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Tray Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.2f))
                                .border(1.dp, accentColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Gemini",
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "GEMINI SECURE CO-PILOT",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Automated page semantic auditing & real chat",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_gemini_assistant_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons: Trigger Analysis / Summary
                if (viewModel.aiAnalysisData == null && !viewModel.isAiAnalysisLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Read",
                                tint = accentColor.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Analyze Webpage with Gemini AI",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Extracts structure, summaries, and privacy dark patterns.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            GlassButton(
                                onClick = { viewModel.runAiAnalysis(activePageHtml) },
                                glowAccentColor = accentColor,
                                isSelected = true,
                                textTag = "start_analysis_button"
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Action",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Synthesize content audit", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                } else if (viewModel.isAiAnalysisLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = accentColor)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Gemini is examining elements...",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Reading DOM structure, assessing cookie vectors",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    // Display Audit results & Conversation Box
                    val analysis = viewModel.aiAnalysisData!!
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        item {
                            // Summary Box
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ShortText,
                                        contentDescription = "summary",
                                        tint = accentColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI EXECUTIVE SUMMARY",
                                        color = accentColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = analysis.summary,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        item {
                            // Safety Score Card
                            Row(modifier = Modifier.fillMaxWidth()) {
                                GlassCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(130.dp)
                                ) {
                                    Text(
                                        text = "PRIVACY SCORE",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${analysis.safetyScore}/100",
                                        color = if (analysis.safetyScore >= 75) Color(0xFF10B981) else Color(0xFFF43F5E),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = analysis.safetyRating,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                GlassCard(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(130.dp)
                                ) {
                                    Text(
                                        text = "TRACKERS ANALYSIS",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = analysis.detectedTrackersSummary,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (analysis.darkPatterns.isNotEmpty()) {
                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundAlpha = 0.15f
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "dark pattern",
                                            tint = Color(0xFFF43F5E),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "DETECTED PRIVACY ANOMALIES",
                                            color = Color(0xFFF43F5E),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    analysis.darkPatterns.forEach { dp ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFF43F5E))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = dp,
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        // CO-PILOT CHAT SEGMENT
                        item {
                            Text(
                                text = "INTERACTIVE CO-PILOT INQUIRY",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        items(viewModel.aiChatHistory) { messagePair ->
                            val isUser = messagePair.second
                            val bubbleBg = if (isUser) accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f)
                            val bubbleBorder = if (isUser) accentColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (isUser) 12.dp else 2.dp,
                                                bottomEnd = if (isUser) 2.dp else 12.dp
                                            )
                                        )
                                        .background(bubbleBg)
                                        .border(
                                            1.dp,
                                            bubbleBorder,
                                            RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (isUser) 12.dp else 2.dp,
                                                bottomEnd = if (isUser) 2.dp else 12.dp
                                            )
                                        )
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = messagePair.first,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        if (viewModel.isAiChatLoading) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = accentColor,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Assistant is formulating answers...",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Quick Action Chips for Gemini
                    Text(
                        text = "SUGGESTED ANALYTICAL AUDITS",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val quickPrompts = listOf(
                            "🔍 Threat Analysis" to "Analyze the security and privacy threat level of this page, highlight risks.",
                            "📝 Summarize TL;DR" to "Provide a detailed TL;DR summary and main bullet points of this webpage.",
                            "🕵️ Look for Scams" to "Verify if this page shows signs of phishing, scams, or dark patterns.",
                            "🔒 Cookie Check" to "Summarize key data structures, trackers, and cookies this site attempts to deposit."
                        )
                        items(quickPrompts) { (label, promptText) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(accentColor.copy(alpha = 0.12f))
                                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.runGeminiQuickQuery(promptText, activePageHtml)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Chat Input Box
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassTextField(
                            value = viewModel.aiChatMessageInput,
                            onValueChange = { viewModel.aiChatMessageInput = it },
                            modifier = Modifier.weight(1f),
                            placeholderText = "Ask anything about this page content...",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                viewModel.submitAiChatMessage(activePageHtml)
                            }),
                            accentColor = accentColor
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { viewModel.submitAiChatMessage(activePageHtml) },
                            enabled = viewModel.aiChatMessageInput.trim().isNotEmpty() && !viewModel.isAiChatLoading,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(accentColor)
                                .size(40.dp)
                                .testTag("submit_chat_message_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
