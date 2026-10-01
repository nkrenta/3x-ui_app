package com.example.xuimanager.data.api.model

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import java.util.UUID

// Auth models
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("obj") val obj: String? = null
)

// Server usage stats
data class ServerUsageStats(
    @SerializedName("current") val current: Long = 0,
    @SerializedName("total") val total: Long = 0
)

data class XrayStatusInfo(
    @SerializedName("state") val state: String? = null,
    @SerializedName("errorMsg") val errorMsg: String? = null,
    @SerializedName("version") val version: String? = null
)

data class PublicIpInfo(
    @SerializedName("ipv4") val ipv4: String? = null,
    @SerializedName("ipv6") val ipv6: String? = null
)

data class NetIoInfo(
    @SerializedName("up") val up: Long = 0,
    @SerializedName("down") val down: Long = 0,
    @SerializedName("pktUp") val pktUp: Long = 0,
    @SerializedName("pktDown") val pktDown: Long = 0
)

data class NetTrafficInfo(
    @SerializedName("sent") val sent: Long = 0,
    @SerializedName("recv") val recv: Long = 0,
    @SerializedName("pktSent") val pktSent: Long = 0,
    @SerializedName("pktRecv") val pktRecv: Long = 0
)

data class AppStatsInfo(
    @SerializedName("threads") val threads: Int = 0,
    @SerializedName("mem") val mem: Long = 0,
    @SerializedName("uptime") val uptime: Long = 0
)

// Panel info response and rich server status object
data class PanelInfoResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("obj") val obj: PanelInfo? = null
)

data class PanelInfo(
    @SerializedName("cpu") val cpu: Double = 0.0,
    @SerializedName("cpuCores") val cpuCores: Int = 1,
    @SerializedName("logicalPro") val logicalPro: Int = 1,
    @SerializedName("cpuSpeedMhz") val cpuSpeedMhz: Double = 0.0,
    @SerializedName("mem") val mem: ServerUsageStats? = null,
    @SerializedName("swap") val swap: ServerUsageStats? = null,
    @SerializedName("disk") val disk: ServerUsageStats? = null,
    @SerializedName("xray") val xray: XrayStatusInfo? = null,
    @SerializedName("panelVersion") val panelVersion: String? = null,
    @SerializedName("panelGuid") val panelGuid: String? = null,
    @SerializedName("uptime") val uptime: Long = 0,
    @SerializedName("tcpCount") val tcpCount: Int = 0,
    @SerializedName("udpCount") val udpCount: Int = 0,
    @SerializedName("publicIP") val publicIP: PublicIpInfo? = null,
    @SerializedName("netIO") val netIO: NetIoInfo? = null,
    @SerializedName("netTraffic") val netTraffic: NetTrafficInfo? = null,
    @SerializedName("appStats") val appStats: AppStatsInfo? = null,
    @SerializedName("total_client") val totalClient: Int = 0,
    @SerializedName("online_client") val onlineClient: Int = 0,

    // Legacy fields fallback
    @SerializedName("xray_version") val xrayVersionLegacy: String? = null,
    @SerializedName("panel_version") val panelVersionLegacy: String? = null,
    @SerializedName("sys_uptime") val sysUptimeLegacy: Long = 0
)

data class InboundClientInfo(
    val email: String,
    val id: String,
    val flow: String = ""
)

// Inbound models
data class InboundListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("obj") val obj: List<Inbound>? = null
)

data class Inbound(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("up") val up: Long = 0,
    @SerializedName("down") val down: Long = 0,
    @SerializedName("total") val total: Long = 0,
    @SerializedName("remark") val remark: String? = null,
    @SerializedName("subSortIndex") val subSortIndex: Int = 1,
    @SerializedName("sub_sort_index") val subSortIndexLegacy: Int = 1,
    @SerializedName("enable") val enable: Boolean = true,
    @SerializedName("expiryTime") val expiryTime: Long = 0,
    @SerializedName("expiry_time") val expiryTimeLegacy: Long = 0,
    @SerializedName("listen") val listen: String? = null,
    @SerializedName("port") val port: Int = 0,
    @SerializedName("protocol") val protocol: String? = null,
    @SerializedName("settings") val settings: JsonElement? = null,
    @SerializedName("streamSettings") val streamSettings: JsonElement? = null,
    @SerializedName("stream_settings") val streamSettingsLegacy: JsonElement? = null,
    @SerializedName("tag") val tag: String? = null,
    @SerializedName("sniffing") val sniffing: JsonElement? = null,
    @SerializedName("allocate") val allocate: JsonElement? = null,
    @SerializedName("stats") val stats: JsonElement? = null,
    @SerializedName("clientStats") val clientStats: List<ClientTraffic>? = null
) {
    fun getSettingsAsString(): String {
        return when {
            settings == null || settings.isJsonNull -> "{}"
            settings.isJsonPrimitive -> settings.asString
            else -> settings.toString()
        }
    }

    fun getStreamSettingsAsString(): String {
        return when {
            streamSettings == null || streamSettings.isJsonNull -> "{}"
            streamSettings.isJsonPrimitive -> streamSettings.asString
            else -> streamSettings.toString()
        }
    }

    fun getSniffingAsString(): String {
        return when {
            sniffing == null || sniffing.isJsonNull -> "{}"
            sniffing.isJsonPrimitive -> sniffing.asString
            else -> sniffing.toString()
        }
    }

    fun getStreamSettingsJsonObject(): JsonObject? {
        return try {
            when {
                streamSettings == null || streamSettings.isJsonNull -> null
                streamSettings.isJsonObject -> streamSettings.asJsonObject
                streamSettings.isJsonPrimitive -> {
                    val str = streamSettings.asString.trim()
                    if (str.startsWith("{")) {
                        JsonParser.parseString(str).asJsonObject
                    } else null
                }

                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun getNetworkType(): String {
        val streamObj = getStreamSettingsJsonObject() ?: return "TCP"
        if (streamObj.has("network") && !streamObj.get("network").isJsonNull) {
            val net = streamObj.get("network").asString
            if (net.isNotBlank()) return net
        }
        return "TCP"
    }

    fun getSecurityType(): String {
        val streamObj = getStreamSettingsJsonObject() ?: return "none"
        if (streamObj.has("security") && !streamObj.get("security").isJsonNull) {
            val sec = streamObj.get("security").asString
            if (sec.isNotBlank()) return sec
        }
        return "none"
    }

    fun getRealityTarget(): String {
        val streamObj = getStreamSettingsJsonObject() ?: return "—"
        if (streamObj.has("realitySettings") && streamObj.get("realitySettings").isJsonObject) {
            val realityObj = streamObj.getAsJsonObject("realitySettings")
            if (realityObj.has("target") && !realityObj.get("target").isJsonNull) {
                return realityObj.get("target").asString
            }
        }
        if (streamObj.has("target") && !streamObj.get("target").isJsonNull) {
            return streamObj.get("target").asString
        }
        if (streamObj.has("tlsSettings") && streamObj.get("tlsSettings").isJsonObject) {
            val tlsObj = streamObj.getAsJsonObject("tlsSettings")
            if (tlsObj.has("serverName") && !tlsObj.get("serverName").isJsonNull) {
                return tlsObj.get("serverName").asString
            }
        }
        return "—"
    }

    fun getRealityPublicKey(): String {
        return try {
            val streamObj = getStreamSettingsJsonObject() ?: return ""
            if (streamObj.has("realitySettings") && streamObj.get("realitySettings").isJsonObject) {
                val realityObj = streamObj.getAsJsonObject("realitySettings")
                if (realityObj.has("settings") && realityObj.get("settings").isJsonObject) {
                    val innerSettings = realityObj.getAsJsonObject("settings")
                    if (innerSettings.has("publicKey") && !innerSettings.get("publicKey").isJsonNull) {
                        return innerSettings.get("publicKey").asString
                    }
                }
            }
            ""
        } catch (_: Exception) {
            ""
        }
    }

    fun getRealityPrivateKey(): String {
        return try {
            val streamObj = getStreamSettingsJsonObject() ?: return ""
            if (streamObj.has("realitySettings") && streamObj.get("realitySettings").isJsonObject) {
                val realityObj = streamObj.getAsJsonObject("realitySettings")
                if (realityObj.has("privateKey") && !realityObj.get("privateKey").isJsonNull) {
                    return realityObj.get("privateKey").asString
                }
            }
            ""
        } catch (_: Exception) {
            ""
        }
    }

    fun getRealityShortIds(): String {
        return try {
            val streamObj = getStreamSettingsJsonObject() ?: return "—"
            if (streamObj.has("realitySettings") && streamObj.get("realitySettings").isJsonObject) {
                val realityObj = streamObj.getAsJsonObject("realitySettings")
                if (realityObj.has("shortIds") && realityObj.get("shortIds").isJsonArray) {
                    val arr = realityObj.getAsJsonArray("shortIds")
                    val list = arr.mapNotNull {
                        if (it != null && !it.isJsonNull && it.isJsonPrimitive) it.asString else null
                    }.filter { it.isNotBlank() }
                    if (list.isNotEmpty()) return list.joinToString(", ")
                }
            }
            "—"
        } catch (_: Exception) {
            "—"
        }
    }

    fun getRealitySpiderX(): String {
        return try {
            val streamObj = getStreamSettingsJsonObject() ?: return "—"
            if (streamObj.has("realitySettings") && streamObj.get("realitySettings").isJsonObject) {
                val realityObj = streamObj.getAsJsonObject("realitySettings")
                if (realityObj.has("settings") && realityObj.get("settings").isJsonObject) {
                    val innerSettings = realityObj.getAsJsonObject("settings")
                    if (innerSettings.has("spiderX") && !innerSettings.get("spiderX").isJsonNull) {
                        return innerSettings.get("spiderX").asString
                    }
                }
            }
            "—"
        } catch (_: Exception) {
            "—"
        }
    }

    fun getRealityFingerprint(): String {
        return try {
            val streamObj = getStreamSettingsJsonObject() ?: return "chrome"
            if (streamObj.has("realitySettings") && streamObj.get("realitySettings").isJsonObject) {
                val realityObj = streamObj.getAsJsonObject("realitySettings")
                if (realityObj.has("settings") && realityObj.get("settings").isJsonObject) {
                    val innerSettings = realityObj.getAsJsonObject("settings")
                    if (innerSettings.has("fingerprint") && !innerSettings.get("fingerprint").isJsonNull) {
                        return innerSettings.get("fingerprint").asString
                    }
                }
            }
            "chrome"
        } catch (_: Exception) {
            "chrome"
        }
    }

    fun isSniffingEnabled(): Boolean {
        return try {
            val sniffObj = when {
                sniffing == null || sniffing.isJsonNull -> null
                sniffing.isJsonObject -> sniffing.asJsonObject
                sniffing.isJsonPrimitive -> JsonParser.parseString(sniffing.asString).asJsonObject
                else -> null
            }
            if (sniffObj?.has("enabled") == true && !sniffObj.get("enabled").isJsonNull) {
                sniffObj.get("enabled").asBoolean
            } else false
        } catch (_: Exception) {
            false
        }
    }

    fun getClientsListFromSettings(): List<InboundClientInfo> {
        return try {
            val settingsObj = when {
                settings == null || settings.isJsonNull -> null
                settings.isJsonObject -> settings.asJsonObject
                settings.isJsonPrimitive -> JsonParser.parseString(settings.asString).asJsonObject
                else -> null
            }
            if (settingsObj?.has("clients") == true && settingsObj.get("clients").isJsonArray) {
                val clientsArray = settingsObj.getAsJsonArray("clients")
                val list = mutableListOf<InboundClientInfo>()
                for (elem in clientsArray) {
                    if (elem.isJsonObject) {
                        val c = elem.asJsonObject
                        val email =
                            if (c.has("email") && !c.get("email").isJsonNull) c.get("email").asString else "Client"
                        val cId =
                            if (c.has("id") && !c.get("id").isJsonNull) c.get("id").asString else ""
                        val flow =
                            if (c.has("flow") && !c.get("flow").isJsonNull) c.get("flow").asString else ""
                        list.add(InboundClientInfo(email, cId, flow))
                    }
                }
                list
            } else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getElementPrettyJson(element: JsonElement?): String {
        return try {
            when {
                element == null || element.isJsonNull -> "{}"
                element.isJsonPrimitive -> element.asString
                else -> {
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    gson.toJson(element)
                }
            }
        } catch (_: Exception) {
            "{}"
        }
    }
}

// Client models
data class ClientListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("obj") val obj: List<ApiClient>? = null
)

data class ClientTraffic(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("inboundId") val inboundId: Int = 0,
    @SerializedName("enable") val enable: Boolean = true,
    @SerializedName("email") val email: String? = null,
    @SerializedName("up") val up: Long = 0,
    @SerializedName("down") val down: Long = 0,
    @SerializedName("expiryTime") val expiryTime: Long = 0,
    @SerializedName("total") val total: Long = 0,
    @SerializedName("lastOnline") val lastOnline: Long = 0,
    @SerializedName("lastSubFetch") val lastSubFetch: Long = 0
)

data class ApiClient(
    @SerializedName("id") val id: JsonElement? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("subId") val subId: String? = null,
    @SerializedName("uuid") val uuid: String? = null,
    @SerializedName("password") val password: String? = null,
    @SerializedName("auth") val auth: String? = null,
    @SerializedName("flow") val flow: String? = null,
    @SerializedName("limitIp") val limitIp: Int = 0,
    @SerializedName("limitHwid") val limitHwid: Int = 0,
    @SerializedName("totalGB") val totalGB: Long = 0,
    @SerializedName("expiryTime") val expiryTime: Long = 0,
    @SerializedName("enable") val enable: Boolean = true,
    @SerializedName("tgId") val tgId: JsonElement? = null,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("inboundIds") val inboundIds: List<Int>? = null,
    @SerializedName("traffic") val traffic: ClientTraffic? = null,
    @SerializedName("createdAt") val createdAt: Long = 0,
    @SerializedName("updatedAt") val updatedAt: Long = 0,

    // Fallbacks for snake_case fields
    @SerializedName("expiry_time") val expiryTimeLegacy: Long = 0,
    @SerializedName("sub_id") val subIdLegacy: String? = null,
    @SerializedName("total") val totalLegacy: Long = 0,
    @SerializedName("up") val upLegacy: Long = 0,
    @SerializedName("down") val downLegacy: Long = 0,
    @SerializedName("inbound_id") val inboundIdLegacy: Int = 0
) {
    fun getIdAsString(): String {
        return when {
            id == null || id.isJsonNull -> ""
            id.isJsonPrimitive -> id.asString
            else -> id.toString()
        }
    }

    fun getEffectiveSubId(): String {
        return subId ?: subIdLegacy ?: ""
    }

    fun getEffectiveExpiryTime(): Long {
        return if (expiryTime != 0L) expiryTime else expiryTimeLegacy
    }

    fun getUpTraffic(): Long {
        return traffic?.up ?: upLegacy
    }

    fun getDownTraffic(): Long {
        return traffic?.down ?: downLegacy
    }

    fun getTotalTrafficLimit(): Long {
        return if (totalGB > 0) totalGB * 1024 * 1024 * 1024 else totalLegacy
    }
}

// Add client request (REST API v2.x)
data class ApiClientItem(
    @SerializedName("email") val email: String,
    @SerializedName("totalGB") val totalGB: Long = 0,
    @SerializedName("expiryTime") val expiryTime: Long = 0,
    @SerializedName("tgId") val tgId: Int = 0,
    @SerializedName("limitIp") val limitIp: Int = 0,
    @SerializedName("limitHwid") val limitHwid: Int = 0,
    @SerializedName("enable") val enable: Boolean = true
)

data class AddClientApiRequest(
    @SerializedName("client") val client: ApiClientItem,
    @SerializedName("inboundIds") val inboundIds: List<Int>
)

data class AddClientRequest(
    @SerializedName("id") val inboundId: Int,
    @SerializedName("settings") val settings: String
)

data class AddClientSettings(
    @SerializedName("clients") val clients: List<ClientSettingsItem>
)

data class ClientSettingsItem(
    @SerializedName("id") val id: String = UUID.randomUUID().toString(),
    @SerializedName("email") val email: String,
    @SerializedName("flow") val flow: String? = null,
    @SerializedName("limit_ip") val limitIp: Int = 0,
    @SerializedName("total_gb") val totalGb: Long = 0,
    @SerializedName("expiry_time") val expiryTime: Long = 0,
    @SerializedName("enable") val enable: Boolean = true,
    @SerializedName("tg_id") val tgId: String? = null,
    @SerializedName("sub_id") val subId: String? = null,
    @SerializedName("reset") val reset: Long = 0
)

// Generic response
data class GenericResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("obj") val obj: JsonElement? = null
) {
    fun getObjAsString(): String? {
        return when {
            obj == null || obj.isJsonNull -> null
            obj.isJsonPrimitive -> obj.asString
            else -> obj.toString()
        }
    }
}

// System stats
data class SystemStatsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("obj") val obj: SystemStats? = null
)

data class SystemStats(
    @SerializedName("cpu") val cpu: Double = 0.0,
    @SerializedName("mem") val mem: Double = 0.0,
    @SerializedName("disk") val disk: Double = 0.0,
    @SerializedName("network") val network: NetworkStats? = null
)

data class NetworkStats(
    @SerializedName("up") val up: Long = 0,
    @SerializedName("down") val down: Long = 0
)

// Client Group, HWID & Bulk DTOs
data class ClientGroup(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("count") val count: Int = 0
)

data class ClientHwid(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("hwid") val hwid: String = "",
    @SerializedName("ip") val ip: String? = null,
    @SerializedName("updatedAt") val updatedAt: Long = 0
)

data class ClientIpInfo(
    @SerializedName("ip") val ip: String = "",
    @SerializedName("lastSeen") val lastSeen: Long = 0
)

data class GroupNameRequest(
    @SerializedName("name") val name: String
)

data class GroupRenameRequest(
    @SerializedName("oldName") val oldName: String,
    @SerializedName("newName") val newName: String
)

data class GroupAddClientsRequest(
    @SerializedName("group") val group: String,
    @SerializedName("emails") val emails: List<String>
)

data class BulkAdjustRequest(
    @SerializedName("emails") val emails: List<String>,
    @SerializedName("addDays") val addDays: Int = 0,
    @SerializedName("addBytes") val addBytes: Long = 0,
    @SerializedName("flow") val flow: String? = null,
    @SerializedName("limitHwid") val limitHwid: Int? = null,
    @SerializedName("adTag") val adTag: String? = null
)