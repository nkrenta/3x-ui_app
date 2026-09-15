package com.example.xuimanager.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xuimanager.data.api.model.Inbound
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.data.model.RealityKeyGenerator
import com.example.xuimanager.data.model.RealityTarget
import com.example.xuimanager.data.repository.PanelRepository
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class JsonTemplateItem(
    val id: String,
    val name: String,
    val protocol: String,
    val port: Int,
    val network: String,
    val inbound: Inbound,
    val rawJson: String,
    val isReality: Boolean = false
)

class TemplatesViewModel : ViewModel() {
    private val _templates = MutableStateFlow<List<JsonTemplateItem>>(emptyList())
    val templates = _templates.asStateFlow()

    private val _isApplying = MutableStateFlow(false)
    val isApplying = _isApplying.asStateFlow()

    private val _applyResult = MutableStateFlow<String?>(null)
    val applyResult = _applyResult.asStateFlow()

    private val repository = PanelRepository()

    // 1. Подключение №1: Reality | TCP
    private val defaultTcpRealityJson = """
    {
      "id": 1,
      "up": 0,
      "down": 0,
      "total": 0,
      "remark": "Reality | TCP",
      "enable": true,
      "expiryTime": 0,
      "listen": "",
      "port": 21717,
      "protocol": "vless",
      "settings": {
        "clients": [],
        "decryption": "none",
        "encryption": "none"
      },
      "streamSettings": {
        "network": "tcp",
        "security": "reality",
        "realitySettings": {
          "show": false,
          "xver": 0,
          "privateKey": "",
          "shortIds": [],
          "settings": {
            "publicKey": "",
            "fingerprint": "firefox",
            "spiderX": ""
          }
        }
      },
      "tag": "in-21717-tcp",
      "sniffing": { "enabled": false }
    }
    """.trimIndent()

    // 2. Подключение №2: Reality | xHTTP
    private val defaultXhttpRealityJson = """
    {
      "id": 2,
      "up": 0,
      "down": 0,
      "total": 0,
      "remark": "Reality | xHTTP",
      "enable": true,
      "expiryTime": 0,
      "listen": "",
      "port": 22615,
      "protocol": "vless",
      "settings": {
        "clients": [],
        "decryption": "none",
        "encryption": "none"
      },
      "streamSettings": {
        "network": "xhttp",
        "xhttpSettings": {
          "path": "/",
          "host": "",
          "mode": "auto",
          "xPaddingBytes": "100-1000",
          "scMaxBufferedPosts": 30,
          "scStreamUpServerSecs": "20-80"
        },
        "security": "reality",
        "realitySettings": {
          "show": false,
          "xver": 0,
          "privateKey": "",
          "shortIds": [],
          "settings": {
            "publicKey": "",
            "fingerprint": "firefox",
            "spiderX": ""
          }
        }
      },
      "tag": "in-22615-tcp",
      "sniffing": { "enabled": false }
    }
    """.trimIndent()

    // 3. Подключение №3: Reality | gRPC
    private val defaultGrpcRealityJson = """
    {
      "id": 3,
      "up": 0,
      "down": 0,
      "total": 0,
      "remark": "Reality | gRPC",
      "enable": true,
      "expiryTime": 0,
      "listen": "",
      "port": 44436,
      "protocol": "vless",
      "settings": {
        "clients": [],
        "decryption": "none",
        "encryption": "none"
      },
      "streamSettings": {
        "network": "grpc",
        "grpcSettings": {
          "serviceName": "",
          "authority": "",
          "multiMode": false
        },
        "security": "reality",
        "realitySettings": {
          "show": false,
          "xver": 0,
          "privateKey": "",
          "shortIds": [],
          "settings": {
            "publicKey": "",
            "fingerprint": "firefox",
            "spiderX": ""
          }
        }
      },
      "tag": "in-44436-tcp",
      "sniffing": { "enabled": false }
    }
    """.trimIndent()

    // 4. Подключение №4: Reality | TCP | trojan
    private val defaultTrojanTcpRealityJson = """
    {
      "id": 4,
      "up": 0,
      "down": 0,
      "total": 0,
      "remark": "Reality | TCP | trojan",
      "enable": true,
      "expiryTime": 0,
      "listen": "",
      "port": 44935,
      "protocol": "trojan",
      "settings": {
        "clients": []
      },
      "streamSettings": {
        "network": "tcp",
        "tcpSettings": {
          "acceptProxyProtocol": false,
          "header": {
            "type": "none"
          }
        },
        "security": "reality",
        "realitySettings": {
          "show": false,
          "xver": 0,
          "privateKey": "",
          "shortIds": [],
          "settings": {
            "publicKey": "",
            "fingerprint": "chrome",
            "spiderX": ""
          }
        }
      },
      "tag": "in-44935-tcp",
      "sniffing": { "enabled": false }
    }
    """.trimIndent()

    init {
        importJsonTemplate(defaultTcpRealityJson, "Подключение №1: Reality | TCP")
        importJsonTemplate(defaultXhttpRealityJson, "Подключение №2: Reality | xHTTP")
        importJsonTemplate(defaultGrpcRealityJson, "Подключение №3: Reality | gRPC")
        importJsonTemplate(defaultTrojanTcpRealityJson, "Подключение №4: Reality | TCP | trojan")
    }

    fun importJsonTemplate(jsonString: String, customName: String? = null): Boolean {
        return try {
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject

            val rawRemark = if (jsonObject.has("remark") && !jsonObject.get("remark").isJsonNull) {
                jsonObject.get("remark").asString
            } else "Reality | TCP"

            val remark = customName ?: if (rawRemark.startsWith("Подключение №")) {
                rawRemark
            } else {
                "Подключение №${_templates.value.size + 1}: $rawRemark"
            }

            val port = if (jsonObject.has("port") && !jsonObject.get("port").isJsonNull) {
                jsonObject.get("port").asInt
            } else 21717

            val protocol =
                if (jsonObject.has("protocol") && !jsonObject.get("protocol").isJsonNull) {
                    jsonObject.get("protocol").asString
                } else "vless"

            val enable = if (jsonObject.has("enable") && !jsonObject.get("enable").isJsonNull) {
                jsonObject.get("enable").asBoolean
            } else true

            val tag = if (jsonObject.has("tag") && !jsonObject.get("tag").isJsonNull) {
                jsonObject.get("tag").asString
            } else "in-$port"

            val settingsElem =
                if (jsonObject.has("settings")) jsonObject.get("settings") else JsonParser.parseString(
                    "{}"
                )
            val streamSettingsElem =
                if (jsonObject.has("streamSettings")) jsonObject.get("streamSettings") else JsonParser.parseString(
                    "{}"
                )
            val sniffingElem =
                if (jsonObject.has("sniffing")) jsonObject.get("sniffing") else JsonParser.parseString(
                    "{}"
                )

            var network = "tcp"
            var isReality = false
            if (jsonObject.has("streamSettings") && jsonObject.get("streamSettings").isJsonObject) {
                val streamObj = jsonObject.get("streamSettings").asJsonObject
                if (streamObj.has("network") && !streamObj.get("network").isJsonNull) {
                    network = streamObj.get("network").asString
                }
                if (streamObj.has("security") && !streamObj.get("security").isJsonNull) {
                    isReality =
                        streamObj.get("security").asString.equals("reality", ignoreCase = true)
                }
            }

            val inbound = Inbound(
                id = 1,
                up = 0,
                down = 0,
                total = 0,
                remark = remark,
                enable = enable,
                expiryTime = 0,
                listen = "",
                port = port,
                protocol = protocol,
                settings = settingsElem,
                streamSettings = streamSettingsElem,
                tag = tag,
                sniffing = sniffingElem
            )

            val item = JsonTemplateItem(
                id = UUID.randomUUID().toString(),
                name = remark,
                protocol = protocol,
                port = port,
                network = network,
                inbound = inbound,
                rawJson = jsonString,
                isReality = isReality || protocol.equals("vless", ignoreCase = true)
            )

            _templates.value = _templates.value + item
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteTemplate(id: String) {
        _templates.value = _templates.value.filter { it.id != id }
    }

    fun applyRealityTemplateWithTarget(
        context: Context,
        connection: PanelConnection,
        template: JsonTemplateItem,
        target: RealityTarget
    ) {
        _isApplying.value = true
        _applyResult.value = null

        val generatedPort = RealityKeyGenerator.generateRandomPort()
        val (privateKey, publicKey) = RealityKeyGenerator.generateX25519KeyPair()
        val shortIds = RealityKeyGenerator.generateShortIds()
        val spiderX = RealityKeyGenerator.generateSpiderX()

        try {
            val rootObj = JsonParser.parseString(template.rawJson).asJsonObject
            rootObj.addProperty("enable", true)
            rootObj.addProperty("remark", template.name)
            rootObj.addProperty("port", generatedPort)
            rootObj.addProperty("tag", "in-$generatedPort-tcp")

            val sniffingObj = JsonObject()
            sniffingObj.addProperty("enabled", false)
            rootObj.add("sniffing", sniffingObj)

            if (rootObj.has("streamSettings") && rootObj.get("streamSettings").isJsonObject) {
                val streamObj = rootObj.get("streamSettings").asJsonObject
                streamObj.addProperty("security", "reality")

                val realityObj =
                    if (streamObj.has("realitySettings") && streamObj.get("realitySettings").isJsonObject) {
                        streamObj.get("realitySettings").asJsonObject
                    } else {
                        JsonObject()
                    }

                realityObj.addProperty("target", target.target)

                val gson = Gson()
                realityObj.add("serverNames", gson.toJsonTree(target.serverNames))
                realityObj.addProperty("privateKey", privateKey)
                realityObj.add("shortIds", gson.toJsonTree(shortIds))

                val innerSettingsObj =
                    if (realityObj.has("settings") && realityObj.get("settings").isJsonObject) {
                        realityObj.get("settings").asJsonObject
                    } else {
                        JsonObject()
                    }
                innerSettingsObj.addProperty("publicKey", publicKey)
                if (!innerSettingsObj.has("fingerprint") || innerSettingsObj.get("fingerprint").isJsonNull) {
                    innerSettingsObj.addProperty("fingerprint", "firefox")
                }
                innerSettingsObj.addProperty("spiderX", spiderX)
                realityObj.add("settings", innerSettingsObj)

                streamObj.add("realitySettings", realityObj)
            }

            val fullPayloadJson = rootObj.toString()

            viewModelScope.launch {
                repository.addInboundJson(context, connection, fullPayloadJson)
                    .onSuccess {
                        _isApplying.value = false
                        val netName = when {
                            template.network.equals("tcp", ignoreCase = true) -> "TCP"
                            template.network.equals("xhttp", ignoreCase = true) -> "xHTTP"
                            template.network.equals("grpc", ignoreCase = true) -> "gRPC"
                            else -> template.network.uppercase()
                        }
                        _applyResult.value = "Reality|$netName создано"
                    }
                    .onFailure { e ->
                        _isApplying.value = false
                        val msg = e.localizedMessage ?: e.message ?: "Ошибка создания"
                        _applyResult.value = "Ошибка создания: $msg"
                    }
            }
        } catch (e: Exception) {
            _isApplying.value = false
            _applyResult.value = "Ошибка обработки шаблона: ${e.localizedMessage}"
        }
    }
}