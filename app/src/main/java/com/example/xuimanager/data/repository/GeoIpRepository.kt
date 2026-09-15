package com.example.xuimanager.data.repository

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object GeoIpRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val countryNameMap = mapOf(
        "DE" to "Германия",
        "FR" to "Франция",
        "US" to "США",
        "NL" to "Нидерланды",
        "FI" to "Финляндия",
        "GB" to "Великобритания",
        "UK" to "Великобритания",
        "TR" to "Турция",
        "SE" to "Швеция",
        "PL" to "Польша",
        "AT" to "Австрия",
        "CH" to "Швейцария",
        "IT" to "Италия",
        "ES" to "Испания",
        "CZ" to "Чехия",
        "RO" to "Румыния",
        "UA" to "Украина",
        "KZ" to "Казахстан",
        "RU" to "Россия",
        "JP" to "Япония",
        "SG" to "Сингапур",
        "CA" to "Канада"
    )

    suspend fun getIpLocation(ipOrHost: String): String? = withContext(Dispatchers.IO) {
        if (ipOrHost == "127.0.0.1" || ipOrHost.equals("localhost", ignoreCase = true)) {
            return@withContext "Localhost"
        }

        // 1. ipwho.is
        try {
            val req = Request.Builder().url("https://ipwho.is/$ipOrHost?lang=ru").build()
            val res = client.newCall(req).execute()
            if (res.isSuccessful) {
                val bodyStr = res.body?.string() ?: ""
                val json = JsonParser.parseString(bodyStr).asJsonObject
                if (json.has("success") && json.get("success").asBoolean) {
                    val country = json.get("country")?.asString ?: ""
                    val code = json.get("country_code")?.asString ?: ""
                    val flag = getFlagEmoji(code)
                    val ruCountry = countryNameMap[code.uppercase()] ?: country

                    return@withContext "$flag $ruCountry ($ipOrHost)"
                }
            }
        } catch (_: Exception) {
        }

        // 2. ip-api.com
        try {
            val req = Request.Builder()
                .url("http://ip-api.com/json/$ipOrHost?fields=status,country,countryCode").build()
            val res = client.newCall(req).execute()
            if (res.isSuccessful) {
                val bodyStr = res.body?.string() ?: ""
                val json = JsonParser.parseString(bodyStr).asJsonObject
                if (json.has("status") && json.get("status").asString == "success") {
                    val country = json.get("country")?.asString ?: ""
                    val code = json.get("countryCode")?.asString ?: ""
                    val flag = getFlagEmoji(code)
                    val ruCountry = countryNameMap[code.uppercase()] ?: country

                    return@withContext "$flag $ruCountry ($ipOrHost)"
                }
            }
        } catch (_: Exception) {
        }

        // 3. ipapi.co
        try {
            val req = Request.Builder().url("https://ipapi.co/$ipOrHost/json/").build()
            val res = client.newCall(req).execute()
            if (res.isSuccessful) {
                val bodyStr = res.body?.string() ?: ""
                val json = JsonParser.parseString(bodyStr).asJsonObject
                if (json.has("country_code")) {
                    val country = json.get("country_name")?.asString ?: ""
                    val code = json.get("country_code")?.asString ?: ""
                    val flag = getFlagEmoji(code)
                    val ruCountry = countryNameMap[code.uppercase()] ?: country

                    return@withContext "$flag $ruCountry ($ipOrHost)"
                }
            }
        } catch (_: Exception) {
        }

        null
    }

    private fun getFlagEmoji(countryCode: String): String {
        if (countryCode.length != 2) return "🌐"
        val upper = countryCode.uppercase()
        val firstChar = Character.codePointAt(upper, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(upper, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }
}