package com.example.xuimanager.data.model

data class PanelConnection(
    val id: String,
    val name: String,
    val host: String,
    val port: Int = 2053,
    val username: String = "admin",
    val password: String = "",
    val protocol: String = "http",
    val path: String = "",
    val token: String = "",
    val skipCertVerify: Boolean = true,
    val isConnected: Boolean = false,
    val xrayVersion: String? = null,
    val pingMs: Int? = null,
    val sshPort: Int? = 22,
    val sshUsername: String? = "root",
    val sshPassword: String? = ""
)