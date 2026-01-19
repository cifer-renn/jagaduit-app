package com.example.jagaduit.utils

import android.graphics.Bitmap
import android.util.Log
import com.example.jagaduit.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ScanResult(
    val amount: Double,
    val date: String?,
    val category: String?
)

class GeminiHelper {
    // KITA PAKAI MODEL TERBARU DARI LOG ANDA
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun analyzeReceipt(bitmap: Bitmap): ScanResult? {
        return withContext(Dispatchers.IO) {
            try {
                // Prompt dipertajam agar format JSON lebih konsisten
                val prompt = """
                    You are a receipt scanner assistant. Analyze this image.
                    Extract these 3 fields into a pure JSON format:
                    1. "amount": The total price (numeric only, e.g., 50000.0).
                    2. "date": The transaction date in YYYY-MM-DD format. If not found, return null.
                    3. "category": Choose one based on items: "Makanan", "Transport", "Belanja", "Tagihan", "Lainnya".
                    
                    Return ONLY the JSON string. Do not use Markdown code blocks.
                """.trimIndent()

                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }

                Log.d("GeminiHelper", "Sending request to Gemini 2.5 Flash...")
                val response = generativeModel.generateContent(inputContent)

                val rawText = response.text ?: ""
                Log.d("GeminiHelper", "Raw Response: $rawText")

                // Bersihkan jika AI masih nakal pakai markdown ```json
                val jsonString = rawText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                if (jsonString.isNotEmpty()) {
                    Gson().fromJson(jsonString, ScanResult::class.java)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("GeminiHelper", "Error analyzing receipt", e)
                null
            }
        }
    }
}