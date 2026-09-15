package com.example.xuimanager.data.model

data class Client(
    val id: String,
    val email: String,
    val protocol: String,        // vless, vmess, trojan, shadowsocks
    val flow: String = "",       // xtls-rprx-vision, etc.
    val total: Long = 0,         // Total traffic limit in bytes (0 = unlimited)
    val expiryTime: Long = 0,    // Expiry timestamp in ms (0 = never)
    val enable: Boolean = true,  // Is client enabled
    val up: Long = 0,            // Upload bytes
    val down: Long = 0,          // Download bytes
    val listen: String = "",     // Inbound listen address
    val port: Int = 0,           // Inbound port
    val settings: String = "",   // JSON settings
    val streamSettings: String = "", // JSON stream settings
    val tag: String = ""         // Inbound tag
)