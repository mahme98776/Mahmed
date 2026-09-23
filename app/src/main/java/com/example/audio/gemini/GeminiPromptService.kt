package com.example.audio.gemini

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service for asking Gemini directly using Google Generative AI SDK
 * and fallback REST API when needed.
 */
class GeminiPromptService(private val context: Context? = null) {

    private val tag = "GeminiPromptService"

    /**
     * Resolves the API key securely:
     * 1. BuildConfig.GEMINI_API_KEY (injected by Secrets panel / .env)
     * 2. SharedPreferences fallback if user entered it in settings
     */
    private fun getResolvedApiKey(): String {
        var key = ""
        try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            key = (field.get(null) as? String) ?: ""
        } catch (_: Exception) {
            key = ""
        }

        if (key.isBlank() || key.contains("MY_GEMINI_API_KEY")) {
            context?.let { ctx ->
                val prefs = ctx.getSharedPreferences("dubbing_studio_prefs", Context.MODE_PRIVATE)
                val savedKey = prefs.getString("gemini_api_key", "") ?: ""
                if (savedKey.isNotBlank()) {
                    key = savedKey
                }
            }
        }
        return key.trim()
    }

    /**
     * Executes the exact prompt with Gemini 2.5 Flash as requested:
     *
     * val generativeModel = GenerativeModel(
     *     modelName = "gemini-2.5-flash",
     *     apiKey = "YOUR_GEMINI_API_KEY"
     * )
     */
    suspend fun askGemini(prompt: String = "كيف تصبح مطور أندرويد محترف؟"): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = getResolvedApiKey()
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    return@withContext Result.failure(
                        IllegalStateException("مفتاح Gemini API غير معين. يرجى إضافته في لوحة Secrets أو في إعدادات التطبيق.")
                    )
                }

                // Initialize official GenerativeModel SDK
                val generativeModel = GenerativeModel(
                    modelName = "gemini-2.5-flash",
                    apiKey = apiKey
                )

                val response = generativeModel.generateContent(prompt)
                val responseText = response.text?.trim()

                if (!responseText.isNullOrBlank()) {
                    println(responseText)
                    Result.success(responseText)
                } else {
                    Result.failure(Exception("لم يتم استلام نص من نموذج Gemini"))
                }
            } catch (e: Exception) {
                Log.e(tag, "Error in askGemini: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}
