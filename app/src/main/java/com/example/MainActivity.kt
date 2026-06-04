package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BrowserViewModel
import com.example.ui.components.LiquidGlassBackdrop
import com.example.ui.components.VpnDashboard
import com.example.ui.components.TorCircuitView
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TabsGridScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: BrowserViewModel = viewModel()
                
                // Parse dynamic active accent color selections
                val accentColor = remember(viewModel.activeAccentColorHex) {
                    try {
                        Color(android.graphics.Color.parseColor(viewModel.activeAccentColorHex))
                    } catch (e: Exception) {
                        Color(0xFF38BDF8) // Aqua custom default
                    }
                }

                // Main navigation layout: 0 = Browser view, 1 = settings/VPN view, 2 = Tab Grid manager
                var activeDisplayPage by remember { mutableStateOf(0) }

                // Edge-to-edge ambient container with liquid gradients and blur intensities modified on standard settings
                LiquidGlassBackdrop(
                    blurStrength = viewModel.glassBlurStrength,
                    gradientSpeed = viewModel.liquidGradientSpeed,
                    accentColor = accentColor
                ) {
                    AnimatedContent(
                        targetState = activeDisplayPage,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "MainLayoutSwitch"
                    ) { targetPage ->
                        when (targetPage) {
                            0 -> {
                                BrowserScreen(
                                    viewModel = viewModel,
                                    onOpenSettings = { activeDisplayPage = 1 },
                                    onOpenTabs = { activeDisplayPage = 2 },
                                    accentColor = accentColor
                                )
                            }
                            1 -> {
                                // Settings and network routing panel combined
                                Column(modifier = Modifier.fillMaxSize()) {
                                    SettingsScreen(
                                        viewModel = viewModel,
                                        onCloseSettings = { activeDisplayPage = 0 },
                                        accentColor = accentColor
                                    )
                                }
                            }
                            2 -> {
                                TabsGridScreen(
                                    viewModel = viewModel,
                                    onCloseTabsScreen = { activeDisplayPage = 0 },
                                    accentColor = accentColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
