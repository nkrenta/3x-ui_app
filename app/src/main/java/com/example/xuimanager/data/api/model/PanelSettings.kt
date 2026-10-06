package com.example.xuimanager.data.api.model

import com.google.gson.JsonObject
import java.net.URI

data class PanelSettings(
    val subEnable: Boolean = true,
    val subURI: String = "",
    val subPath: String = "/sub/",
    val subPort: Int = 0,
    val subDomain: String = "",
    val subCertFile: String = "",
    val subKeyFile: String = "",
    val happLinkEnable: Boolean = false
) {
    fun subscriptionUrl(panelHost: String, subId: String): String? {
        if (subId.isBlank()) return null
        val base = if (subURI.isNotBlank()) {
            if (subURI.endsWith("/")) subURI else "$subURI/"
        } else {
            val tls = subCertFile.isNotBlank() && subKeyFile.isNotBlank()
            val scheme = if (tls) "https" else "http"
            val domain = subDomain.ifBlank { panelHost }
            val authority = when {
                subPort == 443 && tls -> domain
                subPort == 80 && !tls -> domain
                subPort > 0 -> "$domain:$subPort"
                else -> "$domain:2096"
            }
            val path = subPath.ifBlank { "/sub/" }
                .let { if (it.startsWith("/")) it else "/$it" }
                .let { if (it.endsWith("/")) it else "$it/" }
            "$scheme://$authority$path"
        }
        return base + subId
    }

    companion object {
        fun hostOf(hostOrUrl: String): String {
            return try {
                if (hostOrUrl.startsWith("http://") || hostOrUrl.startsWith("https://")) {
                    URI(hostOrUrl).host ?: hostOrUrl
                } else {
                    hostOrUrl.substringBefore(":").substringBefore("/")
                }
            } catch (_: Exception) {
                hostOrUrl
            }
        }

        fun fromJson(json: JsonObject?): PanelSettings {
            if (json == null) return PanelSettings(subEnable = true)
            return PanelSettings(
                subEnable = json.get("subEnable")?.run {
                    try {
                        if (isJsonPrimitive && asJsonPrimitive.isBoolean) asBoolean
                        else asString.toBooleanStrictOrNull()
                            ?: (asString == "true" || asString == "1")
                    } catch (_: Exception) {
                        true
                    }
                } ?: true,
                subURI = json.get("subURI")?.run {
                    try {
                        asString
                    } catch (_: Exception) {
                        ""
                    }
                } ?: "",
                subPath = json.get("subPath")?.run {
                    try {
                        asString
                    } catch (_: Exception) {
                        "/sub/"
                    }
                } ?: "/sub/",
                subPort = json.get("subPort")?.run {
                    try {
                        if (isJsonPrimitive && asJsonPrimitive.isNumber) asInt
                        else asString.toIntOrNull() ?: 0
                    } catch (_: Exception) {
                        0
                    }
                } ?: 0,
                subDomain = json.get("subDomain")?.run {
                    try {
                        asString
                    } catch (_: Exception) {
                        ""
                    }
                } ?: "",
                subCertFile = json.get("subCertFile")?.run {
                    try {
                        asString
                    } catch (_: Exception) {
                        ""
                    }
                } ?: "",
                subKeyFile = json.get("subKeyFile")?.run {
                    try {
                        asString
                    } catch (_: Exception) {
                        ""
                    }
                } ?: "",
                happLinkEnable = json.get("happLinkEnable")?.run {
                    try {
                        if (isJsonPrimitive && asJsonPrimitive.isBoolean) asBoolean
                        else asString.toBooleanStrictOrNull()
                            ?: (asString == "true" || asString == "1")
                    } catch (_: Exception) {
                        false
                    }
                } ?: false
            )
        }
    }
}