package com.example.xuimanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.data.ssh.SSHInstaller
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class SSHInstallerViewModel : ViewModel() {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs = _logs.asStateFlow()

    private val _isInstalling = MutableStateFlow(false)
    val isInstalling = _isInstalling.asStateFlow()

    private val _installResult = MutableStateFlow<String?>(null)
    val installResult = _installResult.asStateFlow()

    private val _installedConnection = MutableStateFlow<PanelConnection?>(null)
    val installedConnection = _installedConnection.asStateFlow()

    private val sshInstaller = SSHInstaller()

    fun install3xUI(
        host: String,
        port: Int,
        username: String,
        password: String
    ) {
        _isInstalling.value = true
        _installResult.value = null
        _installedConnection.value = null
        _logs.value = emptyList()

        var detectedPort = 2053
        var detectedPath = ""
        var detectedUser = "admin"
        var detectedPass = "admin"

        viewModelScope.launch {
            sshInstaller.install3xUI(
                host = host,
                port = port,
                user = username,
                password = password,
                onLogReceived = { log ->
                    _logs.value = _logs.value + log

                    val lower = log.lowercase()
                    if (lower.contains("web port:") || lower.contains("port:")) {
                        log.substringAfter(":").trim().toIntOrNull()?.let { detectedPort = it }
                    }
                    if (lower.contains("web path:") || lower.contains("url path:") || lower.contains(
                            "path:"
                        )
                    ) {
                        val parsed = log.substringAfter(":").trim().trim('/')
                        if (parsed.isNotBlank()) {
                            detectedPath = parsed
                        }
                    }
                    if (lower.contains("username:")) {
                        val parsed = log.substringAfter(":").trim()
                        if (parsed.isNotBlank()) detectedUser = parsed
                    }
                    if (lower.contains("password:")) {
                        val parsed = log.substringAfter(":").trim()
                        if (parsed.isNotBlank()) detectedPass = parsed
                    }
                }
            ).onSuccess { success ->
                _isInstalling.value = false
                if (success) {
                    _installResult.value = "Установка 3x-ui успешно завершена!"
                    _installedConnection.value = PanelConnection(
                        id = UUID.randomUUID().toString(),
                        name = "3x-ui ($host)",
                        host = host,
                        port = detectedPort,
                        username = detectedUser,
                        password = detectedPass,
                        protocol = "http",
                        path = detectedPath,
                        skipCertVerify = true
                    )
                } else {
                    _installResult.value = "Установка не удалась"
                }
            }.onFailure { e ->
                _isInstalling.value = false
                _installResult.value = "Ошибка: ${e.localizedMessage}"
            }
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
        _installResult.value = null
        _installedConnection.value = null
    }
}