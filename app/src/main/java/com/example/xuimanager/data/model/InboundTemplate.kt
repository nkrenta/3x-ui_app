package com.example.xuimanager.data.model

data class InboundTemplate(
    val id: String,
    val name: String,              // Например: "VLESS Reality gRPC 443"
    val protocol: String,          // vless, shadowsocks, trojan
    val port: Int,
    val network: String,           // ws, grpc, tcp
    val remark: String,            // Заметка по умолчанию
    val isReality: Boolean = true,
    val sni: String = "yahoo.com"  // Маскировка
)