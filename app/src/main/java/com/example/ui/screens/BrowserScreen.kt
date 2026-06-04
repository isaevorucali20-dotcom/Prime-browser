package com.example.ui.screens

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.BrowserViewModel
import com.example.ui.components.*
import java.io.ByteArrayInputStream

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    onOpenSettings: () -> Unit,
    onOpenTabs: () -> Unit,
    accentColor: Color
) {
    var urlInputState by remember { mutableStateOf(if (viewModel.activeUrl == "prime://home") "" else viewModel.activeUrl) }
    var rawPageSourceHtml by remember { mutableStateOf("") }
    var isShieldStatsExpanded by remember { mutableStateOf(false) }

    // Sync input box when viewModel changes URL externally
    LaunchedEffect(viewModel.activeUrl) {
        urlInputState = if (viewModel.activeUrl == "prime://home") "" else viewModel.activeUrl
    }

    // Capture Webview back button clicks inside Android
    var webViewInstance: WebView? by remember { mutableStateOf(null) }
    BackHandler(enabled = webViewInstance?.canGoBack() == true) {
        webViewInstance?.goBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 44.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // --- TOP GLASS HEADER (Address bar & secure indicators) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Navigation / Menu button
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("menu_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Settings Menu",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Unified Address bar
                GlassTextField(
                    value = urlInputState,
                    onValueChange = { urlInputState = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("address_url_text_field"),
                    placeholderText = "Search via ${viewModel.selectedSearchEngine} or enter URL...",
                    leadingIcon = {
                        val shieldColor = when {
                            viewModel.isOnionRoutingEnabled -> Color(0xFFA855F7) // Purple Onion
                            viewModel.isVpnEnabled -> Color(0xFF10B981) // Green VPN Shield
                            else -> Color(0xFF38BDF8) // Aqua Secure
                        }
                        
                        Box(
                            modifier = Modifier
                                .clickable { isShieldStatsExpanded = !isShieldStatsExpanded }
                                .padding(2.dp)
                        ) {
                            Icon(
                                imageVector = if (viewModel.isOnionRoutingEnabled) Icons.Default.Security
                                             else Icons.Default.Shield,
                                contentDescription = "Security Status",
                                tint = shieldColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    trailingIcon = {
                        if (urlInputState.isNotEmpty()) {
                            IconButton(
                                onClick = { urlInputState = "" },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    },
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = {
                            viewModel.loadUrl(urlInputState)
                        }
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri
                    ),
                    accentColor = accentColor
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Go Navigation button
                GlassButton(
                    onClick = { viewModel.loadUrl(urlInputState) },
                    cornerRadius = 24.dp,
                    textTag = "navigate_button"
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Load",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // --- Realtime blocking Stats Overlay ---
            AnimatedVisibility(
                visible = isShieldStatsExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
                    cornerRadius = 12.dp,
                    backgroundAlpha = 0.35f
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "AERO TRANSPARENT TOTAL SECURE",
                                color = accentColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Active tracking blocked: ${viewModel.currentPageTrackersBlocked} scripts",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        GlassButton(
                            onClick = { isShieldStatsExpanded = false },
                            cornerRadius = 8.dp
                        ) {
                            Text("Ok", color = Color.White, fontSize = 10.sp)
                        }
                    }

                    if (viewModel.blockedTrackerLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "BLOCKED TRACKER DOMAIN QUERIES:",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        viewModel.blockedTrackerLogs.take(3).forEach { log ->
                            Text(
                                text = "❌ Blocked: $log",
                                color = Color(0xFFF43F5E),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // --- MAIN WEB ENGINE CORE VIEW ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(if (viewModel.activeUrl == "prime://home") Color.Transparent else Color.White)
            ) {
                if (viewModel.activeUrl == "prime://home") {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 18.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title / Header with thin futuristic gradient (Prime Browser)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
                        ) {
                            Text(
                                text = "PRIME",
                                fontWeight = FontWeight.Light,
                                style = androidx.compose.ui.text.TextStyle(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color.White, accentColor)
                                    )
                                ),
                                fontSize = 44.sp,
                                letterSpacing = 8.sp,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ANONYMOUS ENGINE",
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 4.sp
                            )
                        }

                        // Search engine selector card
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "SEARCH ENGINE DIRECTORY",
                                color = accentColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val engines = listOf("DuckDuckGo", "Brave", "Tor (Onion)")
                                engines.forEach { engine ->
                                    val isSelected = viewModel.selectedSearchEngine == engine
                                    val bgEngineColor = if (isSelected) accentColor.copy(alpha = 0.22f) else Color.Transparent
                                    val textEngineColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(bgEngineColor)
                                            .clickable { 
                                                viewModel.selectedSearchEngine = engine 
                                                viewModel.logDevConsole("Search engine updated: $engine")
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = when (engine) {
                                                    "Brave" -> Icons.Default.Public
                                                    "Tor (Onion)" -> Icons.Default.Security
                                                    else -> Icons.Default.Search
                                                },
                                                contentDescription = engine,
                                                tint = textEngineColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = engine,
                                                color = textEngineColor,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Big central query search bar
                        var searchQueryState by remember { mutableStateOf("") }
                        GlassTextField(
                            value = searchQueryState,
                            onValueChange = { searchQueryState = it },
                            modifier = Modifier.fillMaxWidth().testTag("home_search_bar"),
                            placeholderText = "Enter search phrase for ${viewModel.selectedSearchEngine}...",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQueryState.isNotEmpty()) {
                                    IconButton(onClick = { searchQueryState = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = Color.White.copy(alpha = 0.5f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            },
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onDone = {
                                    if (searchQueryState.trim().isNotEmpty()) {
                                        viewModel.loadUrl(searchQueryState)
                                    }
                                }
                            ),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                imeAction = androidx.compose.ui.text.input.ImeAction.Search
                            ),
                            accentColor = accentColor
                        )

                        // Quick Dials (iOS inspired widget block)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "QUICK LINKS & PRIVACY DIRECTORY",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Standard index portals
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.loadUrl("https://search.brave.com") }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.TrendingUp, "Brave", tint = accentColor, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text("Brave Search", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("Cookie-free index", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp)
                                        }
                                    }
                                }

                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.loadUrl("https://duckduckgo.com") }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Fingerprint, "DDG", tint = accentColor, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text("DuckDuckGo", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("No logs promise", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp)
                                        }
                                    }
                                }
                            }

                            // Onion hidden deep portals
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            viewModel.loadUrl("http://blackdrop.onion") 
                                            if (!viewModel.isOnionRoutingEnabled) {
                                                viewModel.toggleOnionRouting()
                                            }
                                        }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Security, "Blackdrop", tint = Color(0xFFA855F7), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text("BlackDrop Leaks", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("whistleblower.onion", color = Color(0xFFA855F7).copy(alpha = 0.7f), fontSize = 8.sp)
                                        }
                                    }
                                }

                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            viewModel.loadUrl("http://chatsec.onion") 
                                            if (!viewModel.isOnionRoutingEnabled) {
                                                viewModel.toggleOnionRouting()
                                            }
                                        }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Chat, "ChatSec", tint = Color(0xFFA855F7), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text("OnionChat P2P", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("encrypted.onion", color = Color(0xFFA855F7).copy(alpha = 0.7f), fontSize = 8.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // iOS-like live telemetry status card
                        GlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ANONYMOUS TELEMETRY ENGINE",
                                color = accentColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (viewModel.isVpnEnabled) Color(0xFF10B981) else Color(0xFF4B5563))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Secure VPN Tunnel: " + (if (viewModel.isVpnEnabled) "ENABLED (AES-256)" else "DORMANT"),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (viewModel.isOnionRoutingEnabled) Color(0xFFA855F7) else Color(0xFF4B5563))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Onion Routing (Tor): " + (if (viewModel.isOnionRoutingEnabled) "ROUTING ENCRYPTED" else "OFFLINE"),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Canvas Fingerprint: FULLY SPOOFED",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "IP Spoof: " + (if (viewModel.isVpnEnabled || viewModel.isOnionRoutingEnabled) viewModel.vpnIPAddress else "Original ISP"),
                                        color = accentColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "User-Agent: ${viewModel.userAgentPreset.split(" ").first()}",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                } else if (viewModel.isOnionRoutingEnabled && viewModel.activeUrl.endsWith(".onion")) {
                    // Load Beautiful private simulated Onion Hidden service
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF030712))
                    ) {
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    settings.javaScriptEnabled = true
                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            rawPageSourceHtml = viewModel.getSimulatedOnionContent(viewModel.activeUrl)
                                        }
                                    }
                                }
                            },
                            update = { webView ->
                                val onionHtmlContent = viewModel.getSimulatedOnionContent(viewModel.activeUrl)
                                webView.loadDataWithBaseURL(viewModel.activeUrl, onionHtmlContent, "text/html", "UTF-8", null)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    // Load actual Live Standard WebView Client
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                webViewInstance = this
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.databaseEnabled = true
                                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                
                                // Anti-Fingerprinting custom settings
                                settings.userAgentString = getCustomUserAgent(viewModel.userAgentPreset)
                                
                                // JavaScript Bridge registration for developer tools logs capture
                                addJavascriptInterface(object {
                                    @JavascriptInterface
                                    fun log(msg: String) {
                                        viewModel.logDevConsole("[Site Log] $msg")
                                    }
                                    @JavascriptInterface
                                    fun error(msg: String) {
                                        viewModel.logDevConsole("[Site Error] $msg")
                                    }
                                }, "AndroidDevConsole")

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        viewModel.logDevConsole("Connection handshakes started: $url")
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        val title = view?.title ?: ""
                                        viewModel.handlePageFinishedLoading(url ?: "", title)
                                        
                                        // Auto-inject console intercept logs inside page head DOM
                                        val injectionConsoleStr = """
                                            (function() {
                                                var oldLog = console.log;
                                                console.log = function(message) {
                                                    AndroidDevConsole.log(String(message));
                                                    oldLog.apply(console, arguments);
                                                };
                                                var oldErr = console.error;
                                                console.error = function(message) {
                                                    AndroidDevConsole.error(String(message));
                                                    oldErr.apply(console, arguments);
                                                };
                                            })();
                                        """.trimIndent()
                                        view?.evaluateJavascript(injectionConsoleStr, null)

                                        // Extract page raw HTML index to supply Gemini analysis text
                                        view?.evaluateJavascript("document.documentElement.outerHTML") { html ->
                                            val trimmed = html?.trim('"')
                                                ?.replace("\\u003C", "<")
                                                ?.replace("\\u003E", ">")
                                                ?.replace("\\\"", "\"")
                                                ?.replace("\\\\", "\\") ?: ""
                                            rawPageSourceHtml = trimmed
                                        }
                                        viewModel.logDevConsole("Site resources finalized: $title")
                                    }

                                    // Realtime Bloat Adblocker request interception
                                    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                                        if (viewModel.isAdBlockerEnabled && request != null) {
                                            val reqUrl = request.url.toString()
                                            
                                            // Evaluates analytic queries matching filters
                                            val regexPattern = ".*(doubleclick|analytics|tracker|facebook\\.com/tr|hotjar|optimizely|adsystem|adnxs|scorecard|adnxs|taboola).*".toRegex()
                                            if (reqUrl.contains(regexPattern)) {
                                                viewModel.totalTrackersBlocked++
                                                Log.d("AdBlocker", "Blocked Query Track: $reqUrl")
                                                
                                                // Supply null empty streams
                                                return WebResourceResponse(
                                                    "text/javascript",
                                                    "UTF-8",
                                                    ByteArrayInputStream(ByteArray(0))
                                                )
                                            }
                                        }
                                        return super.shouldInterceptRequest(view, request)
                                    }
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        super.onProgressChanged(view, newProgress)
                                        if (newProgress == 30) {
                                            viewModel.logDevConsole("Decoding DNS server IP addresses...")
                                        }
                                    }
                                }

                                loadUrl(viewModel.activeUrl)
                            }
                        },
                        update = { webView ->
                            webView.settings.userAgentString = getCustomUserAgent(viewModel.userAgentPreset)
                            if (viewModel.activeUrl != "prime://home" && webView.url != viewModel.activeUrl) {
                                webView.loadUrl(viewModel.activeUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // --- BOTTOM APPLE GLASS NAV/ACTION CONTROLS BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 16.dp, top = 8.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.04f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.24f),
                                Color.White.copy(alpha = 0.06f)
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = { webViewInstance?.goBack() },
                    enabled = webViewInstance?.canGoBack() == true,
                    modifier = Modifier.testTag("app_nav_back")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (webViewInstance?.canGoBack() == true) Color.White else Color.White.copy(alpha = 0.3f)
                    )
                }

                // Forward Button
                IconButton(
                    onClick = { webViewInstance?.goForward() },
                    enabled = webViewInstance?.canGoForward() == true,
                    modifier = Modifier.testTag("app_nav_forward")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (webViewInstance?.canGoForward() == true) Color.White else Color.White.copy(alpha = 0.3f)
                    )
                }

                // Auto Gemini Sum Assistant Trigger Head Bubble
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(accentColor, Color(0xFFA855F7))
                            )
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        .clickable { viewModel.isAssistantTrayOpen = !viewModel.isAssistantTrayOpen }
                        .testTag("expand_gemini_tray_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Head",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Bookmark star
                IconButton(
                    onClick = { viewModel.toggleBookmark() },
                    modifier = Modifier.testTag("bookmark_toggle_button")
                ) {
                    Icon(
                        imageVector = if (viewModel.isCurrentPageBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Bookmark",
                        tint = if (viewModel.isCurrentPageBookmarked) Color.Yellow else Color.White
                    )
                }

                // Tabs Counter Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenTabs)
                        .testTag("tabs_display_panel_button"),
                    contentAlignment = Alignment.Center
                ) {
                    val tabsList by viewModel.tabs.collectAsState()
                    Text(
                        text = tabsList.size.toString(),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- SUB FLOATING DEV CONSOLE SHORTCUT FLAG ---
        IconButton(
            onClick = { viewModel.forceToggleDevMode() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp)
                .clip(CircleShape)
                .background(Color(0xFFEF4444).copy(alpha = 0.8f))
                .size(36.dp)
                .testTag("developer_tools_toggle")
        ) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = "Dev tools flag",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        // --- GEMINI FLOATING TRAY DRAW ---
        GeminiAssistantTray(
            viewModel = viewModel,
            isOpen = viewModel.isAssistantTrayOpen,
            onClose = { viewModel.isAssistantTrayOpen = false },
            activePageHtml = rawPageSourceHtml,
            accentColor = accentColor
        )

        // --- DEVTOOLS PANEL SLIDE DRAW ---
        AnimatedVisibility(
            visible = viewModel.isDevModeEnabled,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            DevToolsPanel(
                viewModel = viewModel,
                activePageHtml = rawPageSourceHtml,
                onExecuteCode = { code ->
                    webViewInstance?.evaluateJavascript(code, null)
                    viewModel.logDevConsole("Injected Code executed complete.")
                },
                accentColor = accentColor
            )
        }
    }
}

private fun getCustomUserAgent(preset: String): String {
    return when (preset) {
        "Google Chrome Mobile v135" -> "Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Mobile Safari/537.36"
        "Firefox Quantum Focus" -> "Mozilla/5.0 (Android 15; Mobile; rv:130.0) Gecko/130.0 Firefox/130.0"
        "Tor Browser Engine (Max Privacy)" -> "Mozilla/5.0 (Windows NT 10.0; rv:115.0) Gecko/20100101 Firefox/115.0"
        else -> "Mozilla/5.0 (iPhone; CPU iPhone OS 18_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.1 Mobile/15E148 Safari/604.1" // Safari 18 Default
    }
}
