package com.example.network

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Synthesize a summary and safety warning of a webpage based on its text content.
     */
    suspend fun analyzeWebpage(url: String, title: String, rawContent: String): WebpageAnalysis = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is missing or default")
            return@withContext WebpageAnalysis(
                summary = "API Key not configured. Please add GEMINI_API_KEY to the AI Studio Secrets panel.",
                safetyScore = 0,
                safetyRating = "Unverified",
                detectedTrackersSummary = "Unable to assess webpage details without a configured API key.",
                darkPatterns = emptyList()
            )
        }

        // Clean up text content to stay within limits
        val contentSample = if (rawContent.length > 8000) rawContent.take(8000) + "..." else rawContent
        
        val prompt = """
            You are an advanced AI security analyst built into a highly secure, privacy-focused browser styled after Apple Liquid Glass. 
            Analyze the following website context and return a structured analysis in JSON formats.
            
            Website URL: $url
            Website Title: $title
            Webpage Raw Text Snippet:
            $contentSample
            
            Return the response EXACTLY as a JSON object with the following keys. Do not include markdown wraps or anything except the raw JSON:
            {
               "summary": "A concise, engaging Apple-style 2-sentence summary of the page content.",
               "safetyScore": 85, // an integer from 0 to 100 on how trustworthy and safe this site is for user privacy and security
               "safetyRating": "Excellent" or "Constructive" or "High Risk" or "Phishing Warning", // descriptive safety evaluation
               "detectedTrackersSummary": "A brief sentence identifying potential tracking, ads, fingerprinting activity, or commercial data collection on this page.",
               "darkPatterns": ["Unsolicited email subscriptions", "Forced cookie consent gates", "Manipulative pricing scarcity alerts"] // list of any dark patterns, privacy risks, or anti-user interfaces detected, limit to 1-3. Or empty list.
            }
        """.trimIndent()

        val urlEndpoint = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
        
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("responseMimeType", "application/json")
            })
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(urlEndpoint)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorMsg = response.body?.string() ?: "Unknown error"
                    Log.e(TAG, "Gemini API response failure: Code ${response.code}, Msg: $errorMsg")
                    return@withContext WebpageAnalysis(
                        summary = "Error loading AI analysis: Endpoint returned code ${response.code}",
                        safetyScore = 50,
                        safetyRating = "Unavailable",
                        detectedTrackersSummary = "We could not reach the Gemini server to analyze this page.",
                        darkPatterns = emptyList()
                    )
                }

                val responseBodyStr = response.body?.string() ?: return@withContext emptyAnalysis("Empty response from AI engine.")
                val responseJson = JSONObject(responseBodyStr)
                
                val textResponse = responseJson
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                val cleanedTextJson = textResponse.trim()
                val parsedAnalysis = JSONObject(cleanedTextJson)
                
                val darkPatternsList = mutableListOf<String>()
                val jsonArr = parsedAnalysis.optJSONArray("darkPatterns")
                if (jsonArr != null) {
                    for (i in 0 until jsonArr.length()) {
                        darkPatternsList.add(jsonArr.getString(i))
                    }
                }
                
                WebpageAnalysis(
                    summary = parsedAnalysis.optString("summary", "Summarization complete."),
                    safetyScore = parsedAnalysis.optInt("safetyScore", 80),
                    safetyRating = parsedAnalysis.optString("safetyRating", "Safe"),
                    detectedTrackersSummary = parsedAnalysis.optString("detectedTrackersSummary", "No explicit tracking noticed."),
                    darkPatterns = darkPatternsList
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing Gemini analysis request", e)
            emptyAnalysis("Connection failed: ${e.localizedMessage}")
        }
    }

    /**
     * Ask a continuous chat question about a webpage's content.
     */
    suspend fun chatAboutPage(url: String, content: String, question: String, chatHistory: List<Pair<String, Boolean>>): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API key is not configured inside the Secrets panel."
        }

        val urlEndpoint = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
        
        val contentsArray = JSONArray()
        
        // Add context
        val contextPrompt = """
            You are the integrated Prime Secure Assistant, analyzing a page the user is currently viewing.
            Current URL: $url
            Page text contents:
            ${if (content.length > 5000) content.take(5000) + "..." else content}
            
            Help the user understand this site fully. Answer their questions clearly, with Apple-style premium brevity.
        """.trimIndent()
        
        contentsArray.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", contextPrompt) })
            })
        })
        
        contentsArray.put(JSONObject().apply {
            put("role", "model")
            put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", "Understood. I have deeply analyzed the page and I am ready to answer any questions premium and secure.") })
            })
        })
        
        // Append historic chat turns (User vs Model)
        chatHistory.forEach { (text, isUser) ->
            contentsArray.put(JSONObject().apply {
                put("role", if (isUser) "user" else "model")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", text) })
                })
            })
        }
        
        // Append final question
        contentsArray.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", question) })
            })
        })

        val requestJson = JSONObject().apply {
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
            })
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(urlEndpoint)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Error communicating with AI Assistant: ${response.code}"
                }
                val body = response.body?.string() ?: return@withContext "Empty response received."
                val responseJson = JSONObject(body)
                responseJson
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            }
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }

    private fun emptyAnalysis(details: String) = WebpageAnalysis(
        summary = "AI Summarization error occurred.",
        safetyScore = 50,
        safetyRating = "Unverified",
        detectedTrackersSummary = details,
        darkPatterns = emptyList()
    )
}

data class WebpageAnalysis(
    val summary: String,
    val safetyScore: Int,
    val safetyRating: String,
    val detectedTrackersSummary: String,
    val darkPatterns: List<String>
)
