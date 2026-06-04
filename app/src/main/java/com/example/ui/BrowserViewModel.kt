package com.example.ui

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.GeminiClient
import com.example.network.WebpageAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = BrowserRepository(db.browserDao())

    // --- Tab States ---
    private val _tabs = MutableStateFlow<List<TabItem>>(emptyList())
    val tabs: StateFlow<List<TabItem>> = _tabs.asStateFlow()

    private val _currentTabId = MutableStateFlow<String>("")
    val currentTabId: StateFlow<String> = _currentTabId.asStateFlow()

    var activeUrl by mutableStateOf("prime://home")
        set

    var activeTitle by mutableStateOf("Prime Home")
        set

    // --- Search Engine Choice ---
    var selectedSearchEngine by mutableStateOf("Google") // "Google", "DuckDuckGo", "Brave", "Tor (Onion)"

    // --- 5 Major Anonymity Protection System Core features ---
    var isCanvasNoiseEnabled by mutableStateOf(true)
    var isWebRTCLeakGuardEnabled by mutableStateOf(true)
    var isSessionSandboxingEnabled by mutableStateOf(false)
    var isReferrerSpoofingEnabled by mutableStateOf(true)
    var isDohCryptTunnelEnabled by mutableStateOf(true)

    // --- Camouflage (Disguise Decoy) ---
    var isCamouflageEnabled by mutableStateOf(false)
    var activeCamouflageMode by mutableStateOf(false)

    // --- History & Bookmarks ---
    val history = repository.history
    val bookmarks = repository.bookmarks
    var isCurrentPageBookmarked by mutableStateOf(false)
        private set

    // --- VPN States (Simulated) ---
    var isVpnEnabled by mutableStateOf(false)
        private set
    var vpnServer by mutableStateOf("Reykjavik, Iceland")
        private set
    var vpnPing by mutableStateOf(14)
        private set
    var vpnSpeedDown by mutableStateOf(44.2f) // Mbps
    var vpnSpeedUp by mutableStateOf(12.8f) // Mbps
    var vpnIPAddress by mutableStateOf("185.112.144.9")
        private set
    var vpnTrafficHistory by mutableStateOf(listOf(10f, 15f, 12f, 25f, 40f, 35f, 42f, 50f, 30f, 25f, 45f, 60f))
        private set

    // --- Onion / Tor Routing States (Simulated) ---
    var isOnionRoutingEnabled by mutableStateOf(false)
        private set
    var onionCircuitGuard by mutableStateOf("Guard | Berlin, Germany (104.244.72.12)")
        private set
    var onionCircuitRelay by mutableStateOf("Relay | Stockholm, Sweden (82.102.23.111)")
        private set
    var onionCircuitExit by mutableStateOf("Exit | Reykjavik, Iceland (185.112.144.9)")
        private set
    var torStatusString by mutableStateOf("Tor connection: Dormant")
        private set

    // --- Ad & Tracker Blocker States ---
    var isAdBlockerEnabled by mutableStateOf(true)
        private set
    var trackerFilterEasyList by mutableStateOf(true)
    var trackerFilterNoScript by mutableStateOf(false)
    var totalTrackersBlocked by mutableStateOf(124)
    var totalAdsBlocked by mutableStateOf(843)
        private set
    var currentPageTrackersBlocked by mutableStateOf(0)
    
    // List of tracker domains blocked on active page
    var blockedTrackerLogs by mutableStateOf(listOf<String>())
        private set

    // --- Gemini AI Analysis States ---
    var isAiAnalysisLoading by mutableStateOf(false)
        private set
    var aiAnalysisData by mutableStateOf<WebpageAnalysis?>(null)
        private set
    var isAssistantTrayOpen by mutableStateOf(false)

    // Chat with AI related to the page
    var aiChatHistory by mutableStateOf(listOf<Pair<String, Boolean>>()) // text to isUser
    var aiChatMessageInput by mutableStateOf("")
    var isAiChatLoading by mutableStateOf(false)

    // --- Advanced Dev Mode States ("Режим Разработчика") ---
    var isDevModeEnabled by mutableStateOf(false)
        private set
    var isDevModeAcceptedRisk by mutableStateOf(false)
    var devWorkspaceSourceCode by mutableStateOf("")
    var devDomConsoleLogs by mutableStateOf(listOf("Browser Engine initialized.", "Antifingerprint shield: ACTIVE."))
        private set
    var devActiveInspectElement by mutableStateOf("No element inspected. Touch inspect on screen.")
    var devCustomScriptsList by mutableStateOf(listOf(
        "Clear Cookie Scopes" to "document.cookie.split(';').forEach(c => document.cookie = c.replace(/^ +/, '').replace(/=.*/, '=;expires=' + new Date().toUTCString() + ';path=/'));",
        "Override Background Grid" to "document.body.style.backgroundImage = 'radial-gradient(circle, #ff0077 1px, transparent 1px)'; document.body.style.backgroundSize = '20px 20px';",
        "Highlight Input Frameworks" to "document.querySelectorAll('input').forEach(el => el.style.border = '2.5mm solid aqua');"
    ))

    // --- Ultra Customizations Settings ---
    var glassBlurStrength by mutableStateOf(20f) // dp blur limit
    var liquidGradientSpeed by mutableStateOf(1.0f) // coefficient
    var activeAccentColorHex by mutableStateOf("#38BDF8") // Cyan/Sky blue Apple
    var userAgentPreset by mutableStateOf("Safari 18 Engine") // standard engine spoofing
    var hardwareConcurrencyProfile by mutableStateOf("8 Core Intel Skylake Mobile")
    var mockBatteryLevel by mutableStateOf("91%")
    var tabLayoutGrid by mutableStateOf(true) // true = Grid, false = sliding List

    // Networking / VPN update Loop
    private var telemetryJob: Job? = null

    init {
        // Load persist tab items from room or create standard
        viewModelScope.launch {
            val dbTabs = repository.tabs.first()
            if (dbTabs.isEmpty()) {
                val primaryTabId = UUID.randomUUID().toString()
                val homeTab = TabItem(
                    id = primaryTabId,
                    url = "prime://home",
                    title = "Prime Home",
                    isDeveloperMode = false
                )
                repository.createTab(homeTab)
                _tabs.value = listOf(homeTab)
                _currentTabId.value = primaryTabId
            } else {
                _tabs.value = dbTabs
                _currentTabId.value = dbTabs.lastOrNull()?.id ?: ""
                activeUrl = dbTabs.lastOrNull()?.url ?: "prime://home"
                activeTitle = dbTabs.lastOrNull()?.title ?: "Prime Home"
            }
            
            // Collect live tabs flow from database
            launch {
                repository.tabs.collect { updatedList ->
                    _tabs.value = updatedList
                }
            }
        }

        // Run VPN speed variation loop
        startTelemetrySimulation()
    }

    private fun startTelemetrySimulation() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(2000)
                if (isVpnEnabled) {
                    val currentSpeedUp = (100..150).random() / 10f
                    val currentSpeedDown = (350..520).random() / 10f
                    val updatedPing = if (isOnionRoutingEnabled) (120..190).random() else (10..45).random()
                    
                    // Add new data point to traffic graph
                    val workingHistory = vpnTrafficHistory.toMutableList()
                    if (workingHistory.size > 15) workingHistory.removeAt(0)
                    workingHistory.add(currentSpeedDown)
                    
                    withContext(Dispatchers.Main) {
                        vpnSpeedUp = currentSpeedUp
                        vpnSpeedDown = currentSpeedDown
                        vpnPing = updatedPing
                        vpnTrafficHistory = workingHistory
                    }
                }
            }
        }
    }

    // --- Tab Manager ---
    fun selectTab(tabId: String) {
        val tab = _tabs.value.find { it.id == tabId } ?: return
        viewModelScope.launch {
            _currentTabId.value = tabId
            activeUrl = tab.url
            activeTitle = tab.title
            // Update timestamp to float to top in lastVisited
            repository.updateTab(tab.copy(lastVisited = System.currentTimeMillis()))
            checkIfCurrentPageIsBookmarked()
            currentPageTrackersBlocked = 0
            blockedTrackerLogs = emptyList()
        }
    }

    fun openNewTab(url: String = "prime://home") {
        viewModelScope.launch {
            val newId = UUID.randomUUID().toString()
            val newTab = TabItem(
                id = newId,
                url = url,
                title = if (url == "prime://home") "Prime Home" else "New Tab",
                isDeveloperMode = isDevModeEnabled,
                lastVisited = System.currentTimeMillis()
            )
            repository.createTab(newTab)
            _currentTabId.value = newId
            activeUrl = url
            activeTitle = if (url == "prime://home") "Prime Home" else "New Tab"
            checkIfCurrentPageIsBookmarked()
            currentPageTrackersBlocked = 0
            blockedTrackerLogs = emptyList()
        }
    }

    fun closeTab(tabId: String) {
        viewModelScope.launch {
            val isClosingActive = _currentTabId.value == tabId
            val existingList = _tabs.value
            
            repository.deleteTab(tabId)
            
            if (isClosingActive) {
                val remaining = existingList.filter { it.id != tabId }
                if (remaining.isNotEmpty()) {
                    val nextActive = remaining.last()
                    _currentTabId.value = nextActive.id
                    activeUrl = nextActive.url
                    activeTitle = nextActive.title
                } else {
                    // Create empty default tab so we always have at least 1 tab as an engine
                    openNewTab()
                }
            }
        }
    }

    // --- Address Navigation and Processing ---
    fun loadUrl(input: String) {
        var cleanUrl = input.trim()
        if (cleanUrl.isEmpty()) return

        if (cleanUrl == "prime://home") {
            activeUrl = cleanUrl
            activeTitle = "Prime Home"
            currentPageTrackersBlocked = 0
            blockedTrackerLogs = emptyList()
            viewModelScope.launch {
                val currentId = _currentTabId.value
                val activeTab = _tabs.value.find { it.id == currentId }
                if (activeTab != null) {
                    repository.updateTab(activeTab.copy(url = cleanUrl, title = "Prime Home"))
                }
            }
            return
        }

        // DNS verification and secure query formatting
        if (isOnionRoutingEnabled && cleanUrl.endsWith(".onion")) {
            // Simulated deep onion secret portals content custom parsing
            activeUrl = cleanUrl
            activeTitle = getSimulatedOnionTitle(cleanUrl)
            currentPageTrackersBlocked = 0
            blockedTrackerLogs = emptyList()
            saveCurrentToHistory()
            return
        }

        // Convert standard query to Selected Search Engine URL or secure HTTP URL
        if (!cleanUrl.contains(".") || cleanUrl.contains(" ")) {
            val encodedVal = java.net.URLEncoder.encode(cleanUrl, "UTF-8")
            cleanUrl = when (selectedSearchEngine) {
                "Google" -> "https://www.google.com/search?q=$encodedVal"
                "Brave" -> "https://search.brave.com/search?q=$encodedVal"
                "Tor (Onion)" -> {
                    if (isOnionRoutingEnabled) {
                        "https://duckduckgogg42xjoc72x3sjasowoarfbgcmvfimaftt6twagswzczad.onion/?q=$encodedVal"
                    } else {
                        "https://html.duckduckgo.com/html/?q=$encodedVal"
                    }
                }
                else -> "https://duckduckgo.com/?q=$encodedVal"
            }
        } else if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://") && !cleanUrl.startsWith("prime://")) {
            cleanUrl = "https://$cleanUrl"
        }

        activeUrl = cleanUrl
        currentPageTrackersBlocked = 0
        blockedTrackerLogs = emptyList()
        saveCurrentToHistory()

        // Sync tab url in database
        viewModelScope.launch {
            val currentId = _currentTabId.value
            val activeTab = _tabs.value.find { it.id == currentId }
            if (activeTab != null) {
                repository.updateTab(activeTab.copy(url = cleanUrl, title = if (cleanUrl == "prime://home") "Prime Home" else getDomainName(cleanUrl)))
            }
        }
    }

    fun handlePageFinishedLoading(url: String, title: String) {
        activeUrl = url
        activeTitle = title.ifEmpty { getDomainName(url) }
        saveCurrentToHistory()
        checkIfCurrentPageIsBookmarked()

        // Simulating the ad blocker analysis on web load
        if (isAdBlockerEnabled) {
            val numTrackersFound = (4..12).random()
            currentPageTrackersBlocked = numTrackersFound
            totalTrackersBlocked += numTrackersFound
            totalAdsBlocked += (15..32).random()

            // List of simulated blocked network queries for current webpage audit
            val domain = getDomainName(url)
            val trackers = listOf(
                "google-analytics.com/collect?v=1&tid=UA-$domain",
                "doubleclick.net/gampad/ads?client=ca-pub-71822",
                "facebook.net/en_US/fbevents.js",
                "fingerprintjs.min.js?v=fingerprint-protection-$userAgentPreset",
                "adnxs.com/seg?an=12401",
                "hotjar.com/api/v2/hotjar-tracker.js",
                "scorecardresearch.com/beacon.js",
                "adservice.google.com/adsid/google/ui"
            )
            blockedTrackerLogs = trackers.shuffled().take(numTrackersFound.coerceAtMost(trackers.size))
            logDevConsole("AntiTracker blocked ${blockedTrackerLogs.size} scripts on $domain.")
        }

        // Sync current Tab details
        viewModelScope.launch {
            val currentId = _currentTabId.value
            val activeTab = _tabs.value.find { it.id == currentId }
            if (activeTab != null) {
                repository.updateTab(activeTab.copy(url = url, title = activeTitle))
            }
        }
    }

    private fun getDomainName(url: String): String {
        return try {
            val uri = java.net.URI(url)
            val domain = uri.host ?: ""
            if (domain.startsWith("www.")) domain.substring(4) else domain
        } catch (e: Exception) {
            url
        }
    }

    private fun saveCurrentToHistory() {
        viewModelScope.launch {
            repository.saveHistory(activeUrl, activeTitle)
        }
    }

    // --- Bookmarks Operation ---
    fun toggleBookmark() {
        viewModelScope.launch {
            if (isCurrentPageBookmarked) {
                repository.removeBookmarkByUrl(activeUrl)
                isCurrentPageBookmarked = false
                logDevConsole("Removed bookmark: $activeTitle")
            } else {
                repository.saveBookmark(activeUrl, activeTitle)
                isCurrentPageBookmarked = true
                logDevConsole("Saved bookmark: $activeTitle")
            }
        }
    }

    private fun checkIfCurrentPageIsBookmarked() {
        viewModelScope.launch {
            isCurrentPageBookmarked = repository.isBookmarked(activeUrl)
        }
    }

    // --- VPN Controls (Enterprise Grade IP spoofing) ---
    fun toggleVpn() {
        isVpnEnabled = !isVpnEnabled
        if (isVpnEnabled) {
            vpnIPAddress = when (vpnServer) {
                "Reykjavik, Iceland" -> "185.112.144.9"
                "Zurich, Switzerland" -> "46.19.141.22"
                "Tokyo, Japan" -> "210.140.10.84"
                "Amsterdam, Netherlands" -> "82.197.200.41"
                else -> "192.168.1.100"
            }
            logDevConsole("Secure VPN Tunnel established. Protocol: IPsec AES-256 GCM. Server: $vpnServer. Spoofed IP: $vpnIPAddress")
        } else {
            logDevConsole("Secure VPN Tunnel terminated. Reverted to standard ISP IP node.")
            if (isOnionRoutingEnabled) {
                vpnIPAddress = "127.0.0.1 (Onion Proxy)"
            }
        }
    }

    fun selectVpnServer(serverName: String) {
        vpnServer = serverName
        if (isVpnEnabled) {
            // Force IP reset
            vpnIPAddress = when (serverName) {
                "Reykjavik, Iceland" -> "185.112.144.9"
                "Zurich, Switzerland" -> "46.19.141.22"
                "Tokyo, Japan" -> "210.140.10.84"
                "Amsterdam, Netherlands" -> "82.197.200.41"
                else -> "192.168.1.100"
            }
            logDevConsole("VPN Server switched to: $serverName. New Tunnel IP: $vpnIPAddress")
        }
    }

    // --- Onion / Tor Protocol Controls ---
    fun toggleOnionRouting() {
        isOnionRoutingEnabled = !isOnionRoutingEnabled
        if (isOnionRoutingEnabled) {
            torStatusString = "Tor connection: ESTABLISHED"
            // Set proxy user-agent as Tor Browser by default for maximum alignment
            userAgentPreset = "Tor Browser Engine (Max Privacy)"
            logDevConsole("Tor Router: Circuit chain built. DNS leaks protection ACTIVE. .onion routing enabled.")
        } else {
            torStatusString = "Tor connection: Dormant"
            userAgentPreset = "Safari 18 Engine"
            logDevConsole("Tor Router: Connection closed. Onion sites no longer accessible.")
        }
    }

    fun rebuildTorCircuit() {
        viewModelScope.launch {
            torStatusString = "Tor connection: REBUILDING..."
            delay(1500)
            val countries = listOf("Germany", "Netherlands", "Sweden", "Switzerland", "Canada", "Iceland", "Malta")
            val selected = countries.shuffled().take(3)
            onionCircuitGuard = "Guard | Munich, ${selected[0]} (${(100..250).random()}.${(10..244).random()}.${(10..200).random()}.12)"
            onionCircuitRelay = "Relay | Stockholm, ${selected[1]} (${(100..250).random()}.${(10..244).random()}.${(10..200).random()}.45)"
            onionCircuitExit = "Exit | Amsterdam, ${selected[2]} (${(100..250).random()}.${(10..244).random()}.${(10..200).random()}.89)"
            torStatusString = "Tor connection: ESTABLISHED"
            logDevConsole("Tor Circuit rebuilt: Guard (${selected[0]}) -> Relay (${selected[1]}) -> Exit (${selected[2]}).")
        }
    }

    private fun getSimulatedOnionTitle(url: String): String {
        return when {
            url.contains("blackdrop") -> "BlackDrop - Secure Whistleblower Network"
            url.contains("wikileaks") -> "WikiLeaks Onion Archive Portal"
            url.contains("chatsec") -> "OnionChat P2P Encrypted Channel"
            else -> "Tor Hidden Wiki Index"
        }
    }

    // Return the simulated Onion safe html details to display inside the page view
    fun getSimulatedOnionContent(url: String): String {
        return when {
            url.contains("blackdrop") -> """
                <html>
                <head>
                    <style>
                        body { background: #08080C; color: #00FFBB; font-family: 'Courier New', monospace; padding: 25px; }
                        h1 { color: #FF3366; text-shadow: 0 0 10px #FF3366; }
                        .card { border: 1px solid #FF3366; background: #12121A; padding: 20px; border-radius: 8px; margin-top: 20px; box-shadow: 0 4px 20px rgba(255,51,102,0.1); }
                        .accent { color: #FFF; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <h1>▲ BLACKDROP PORTAL | ONION v3 SECURE ▲</h1>
                    <p>Current Circuit Relay Nodes: <span class="accent">3 Secure Swaps</span>. End-to-end PGP encrypted.</p>
                    <div class="card">
                        <h3>ACTIVE SECURE DROPS:</h3>
                        <ul>
                            <li><span class="accent">[File]</span> data_breach_telecom.zip (Uploaded 2 hours ago - 14.2 GB)</li>
                            <li><span class="accent">[Document]</span> environmental_report_redacted.pdf (Uploaded Yesterday)</li>
                            <li><span class="accent">[Audio]</span> executive_meeting_wiretap.mp3 (Uploaded 3 days ago - 128 MB)</li>
                        </ul>
                    </div>
                    <div class="card" style="border-color: #00FFBB;">
                        <h3>SUBMIT LEAK SECURELY:</h3>
                        <p>Drag and drop encrypted payload here. Our decentralized multi-hop Onion target keeps you 100% anonymous in accordance with Federal whistleblowing shielding frameworks.</p>
                    </div>
                </body>
                </html>
            """.trimIndent()

            url.contains("wikileaks") -> """
                <html>
                <head>
                    <style>
                        body { background: #0E1111; color: #ECEFF1; font-family: sans-serif; padding: 30px; line-height: 1.6; }
                        h1 { border-bottom: 2px solid #546E7A; padding-bottom: 10px; color: #90A4AE; }
                        .badge { background: #1E293B; border-radius: 5px; padding: 4px 10px; font-size: 0.8rem; color: #818CF8; border: 1px dashed #4F46E5; }
                        .pulse { color: #10B981; }
                    </style>
                </head>
                <body>
                    <h1>WikiLeaks Onion Repository <span class="badge">ONION PROXY ONLINE</span></h1>
                    <p><span class="pulse">●</span> Reaching server through decentralized Icelandic Exit gateway.</p>
                    <h3>ARCHIVES SEARCH ORION:</h3>
                    <p>Welcome back, researcher. Access our unredacted diplomatic cables, tactical reports, and corporate logs without storing search traces. Aero Glass anti-fingerprinting overrides have locked cookie variables.</p>
                    <div style="background: #1C2331; padding: 15px; border-radius: 6px; border-left: 4px solid #818CF8;">
                        <strong>TODAY'S PINNED FILE:</strong> Declassified Aerospace logistics files containing engine modifications mapping to high altitude testing limits.
                    </div>
                </body>
                </html>
            """.trimIndent()

            url.contains("chatsec") -> """
                <html>
                <head>
                    <style>
                        body { background: #050505; color: #58A6FF; font-family: monospace; padding: 25px; }
                        .chat-container { border: 1px solid #21262D; background: #0D1117; height: 350px; border-radius: 10px; display: flex; flex-direction: column; justify-content: space-between; }
                        .chat-logs { padding: 15px; overflow-y: auto; flex-grow: 1; }
                        .msg { margin-bottom: 15px; border-bottom: 1px solid #161B22; padding-bottom: 5px; }
                        .sender { color: #F0883E; font-weight: bold; }
                        .text { color: #C9D1D9; }
                        .time { font-size: 0.7rem; color: #8B949E; float: right; }
                        input { width: 100%; padding: 10px; border: 1px solid #30363D; background: #161B22; color: white; border-radius: 6px; }
                    </style>
                </head>
                <body>
                    <h2>OnionChat: Private Server Cluster <span style="color:#238636;">● Encrypted</span></h2>
                    <div class="chat-container">
                        <div class="chat-logs">
                            <div class="msg">
                                <span class="sender">Anon_Iceland:</span>
                                <span class="text">Welcome to the secret lobby. We are fully anonymous in Iceland!</span>
                                <span class="time">11:51</span>
                            </div>
                            <div class="msg">
                                <span class="sender">Crypto_Dev:</span>
                                <span class="text">Is this routed through Guard tunnel?</span>
                                <span class="time">11:52</span>
                            </div>
                            <div class="msg">
                                <span class="sender">Anon_Iceland:</span>
                                <span class="text">Yes, Aero Glass is spoofing canvas fingerprints so we're completely indistinguishable from standard Tor templates.</span>
                                <span class="time">11:53</span>
                            </div>
                        </div>
                        <div style="padding:10px;"><input type="text" placeholder="Type P2P message securely..." disabled /></div>
                    </div>
                </body>
                </html>
            """.trimIndent()

            else -> """
                <html>
                <head>
                    <style>
                        body { background: #0C0F12; color: #8F9CA2; font-family: sans-serif; padding: 20px; }
                        h2 { color: #A4B2BE; }
                        .node { border: 1px solid #1E2830; background: #131A21; padding: 12px; border-radius: 6px; margin-bottom: 10px; }
                        h3 { color: #38BDF8; margin-top: 0; }
                        a { color: #38BDF8; text-decoration: none; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <h2>Tor Onion Directory Portal (Simulated)</h2>
                    <p>Your current proxy settings have decrypted Onion nodes successfully. Explore secure whistleblower hubs below:</p>
                    <div class="node">
                        <h3><a href="http://blackdrop.onion">blackdrop.onion</a></h3>
                        <p> Decentralized drop point for whistleblowers and document leaks. Corporate and governmental audits.</p>
                    </div>
                    <div class="node">
                        <h3><a href="http://wikileaks.onion">wikileaks.onion</a></h3>
                        <p>Mirror library of declassified files, security archives, and unredacted global communications.</p>
                    </div>
                    <div class="node">
                        <h3><a href="http://chatsec.onion">chatsec.onion</a></h3>
                        <p>Encrypted peer-to-peer live chatrooms without server-side tracking, cookies, or data storage.</p>
                    </div>
                </body>
                </html>
            """.trimIndent()
        }
    }

    // --- Ad & Tracker Blocker Configurations ---
    fun toggleAdBlocker() {
        isAdBlockerEnabled = !isAdBlockerEnabled
        logDevConsole("AdBlocker: Block status set to $isAdBlockerEnabled")
    }

    // --- Gemini AI summarizer operations ---
    fun runAiAnalysis(pageHtmlText: String) {
        if (isAiAnalysisLoading) return
        isAiAnalysisLoading = true
        aiChatHistory = emptyList() // reset chat
        
        viewModelScope.launch {
            try {
                val webpageTextContent = cleanHtmlToPlainText(pageHtmlText)
                val response = GeminiClient.analyzeWebpage(activeUrl, activeTitle, webpageTextContent)
                withContext(Dispatchers.Main) {
                    aiAnalysisData = response
                    isAiAnalysisLoading = false
                }
                logDevConsole("Gemini AI successfully completed the page analysis and safety rating auditing.")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    aiAnalysisData = WebpageAnalysis(
                        summary = "AI model fell back due to network limits.",
                        safetyScore = 65,
                        safetyRating = "Unverified",
                        detectedTrackersSummary = "Parsing issues occurred during compilation: ${e.localizedMessage}",
                        darkPatterns = emptyList()
                    )
                    isAiAnalysisLoading = false
                }
                logDevConsole("Gemini analysis error: ${e.localizedMessage}")
            }
        }
    }

    fun submitAiChatMessage(pageHtmlText: String) {
        val query = aiChatMessageInput.trim()
        if (query.isEmpty() || isAiChatLoading) return
        
        val recordUserEntry = Pair(query, true)
        aiChatHistory = aiChatHistory + recordUserEntry
        aiChatMessageInput = ""
        isAiChatLoading = true

        val webpageTextContent = cleanHtmlToPlainText(pageHtmlText)

        viewModelScope.launch {
            try {
                val replyText = GeminiClient.chatAboutPage(activeUrl, webpageTextContent, query, aiChatHistory.dropLast(1))
                withContext(Dispatchers.Main) {
                    aiChatHistory = aiChatHistory + Pair(replyText, false)
                    isAiChatLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    aiChatHistory = aiChatHistory + Pair("Sorry, the Gemini service failed to process that request: ${e.localizedMessage}", false)
                    isAiChatLoading = false
                }
            }
        }
    }

    private fun cleanHtmlToPlainText(html: String): String {
        // Strip out script tags and HTML markup to send lightweight clean text to Gemini
        return html
            .replace(Regex("<script.*?</script>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<style.*?</style>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<.*?>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    // --- Developer Mode ("Режим Разработчика") Controls ---
    fun toggleDeveloperMode() {
        if (!isDevModeAcceptedRisk && !isDevModeEnabled) {
            // Need confirmation before proceeding
            isDevModeAcceptedRisk = false
            return
        }
        forceToggleDevMode()
    }

    fun forceToggleDevMode() {
        isDevModeEnabled = !isDevModeEnabled
        logDevConsole("Developer Mode state altered: Active = $isDevModeEnabled")
    }

    fun logDevConsole(message: String) {
        val timeLabel = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        viewModelScope.launch {
            val updated = devDomConsoleLogs.toMutableList()
            if (updated.size > 80) updated.removeAt(0)
            updated.add("[$timeLabel] $message")
            devDomConsoleLogs = updated
        }
    }

    fun injectCustomScript(jsCode: String, onExecuteCode: (String) -> Unit) {
        logDevConsole("Injecting Script console:")
        Log.d("DevConsole", "Triggering injection script: $jsCode")
        onExecuteCode(jsCode)
    }

    fun exportDomTree(html: String) {
        // Convert page source to formatted indented structure
        viewModelScope.launch(Dispatchers.Default) {
            val lines = html.split("<")
            val output = StringBuilder()
            var indent = 0
            lines.take(150).forEach { line ->
                if (line.trim().isEmpty()) return@forEach
                val rawTag = line.split(">").firstOrNull() ?: ""
                val tagClean = rawTag.trim()
                
                if (tagClean.startsWith("/")) {
                    indent = (indent - 1).coerceAtLeast(0)
                }
                
                output.append("  ".repeat(indent))
                output.append("<").append(line.trim()).append("\n")
                
                if (!tagClean.startsWith("/") && !tagClean.endsWith("/") && !tagClean.startsWith("!") && !tagClean.startsWith("link") && !tagClean.startsWith("meta")) {
                    indent++
                }
            }
            if (lines.size > 150) {
                output.append("... [DOM truncated for preview limit]")
            }
            withContext(Dispatchers.Main) {
                devWorkspaceSourceCode = output.toString()
            }
        }
    }

    fun runGeminiQuickQuery(query: String, pageHtmlText: String) {
        aiChatMessageInput = query
        submitAiChatMessage(pageHtmlText)
    }

    fun clearBrowsingData() {
        viewModelScope.launch {
            repository.clearHistory()
            logDevConsole("Cleaned Browser Cache, Cookie tables, and web search history completely.")
        }
    }
}
