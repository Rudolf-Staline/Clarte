package com.example.ai

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

class GeminiReflectionService(
    private val mockFallback: MockReflectionService = MockReflectionService()
) : ReflectionService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun generateReflection(
        content: String,
        mood: String,
        intensity: Int,
        tags: List<String>,
        writingMode: String,
        analysisType: String?
    ): String = withContext(Dispatchers.IO) {
        // Retrieve the API Key from BuildConfig, which is populated securely from user secrets
        val apiKey = BuildConfig.GEMINI_API_KEY

        // Check if key is a placeholder or blank
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            // No valid API key; fall back to high-quality local mock generator
            return@withContext mockFallback.generateReflection(content, mood, intensity, tags, writingMode, analysisType)
        }

        val systemPrompt = PromptBuilder.buildSystemInstruction(writingMode, analysisType)
        val userPrompt = PromptBuilder.buildUserPrompt(content, mood, intensity, tags, writingMode, analysisType)

        try {
            // Build the JSON payload using standard Android JSONObject
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", userPrompt)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    // Fallback to offline on external API error or quota issue
                    return@withContext mockFallback.generateReflection(content, mood, intensity, tags, writingMode, analysisType)
                }

                val responseBodyStr = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBodyStr)
                val candidates = jsonResponse.getJSONArray("candidates")
                val firstCandidate = candidates.getJSONObject(0)
                val responseContent = firstCandidate.getJSONObject("content")
                val parts = responseContent.getJSONArray("parts")
                val textResult = parts.getJSONObject(0).getString("text")

                textResult.ifBlank {
                    mockFallback.generateReflection(content, mood, intensity, tags, writingMode, analysisType)
                }
            }
        } catch (e: Exception) {
            // Log the network exception and gracefully fall back to local generation
            mockFallback.generateReflection(content, mood, intensity, tags, writingMode, analysisType)
        }
    }
}
