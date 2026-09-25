package com.example.xuimanager.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.data.ssh.SSHInstaller
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StepStatus { PENDING, IN_PROGRESS, COMPLETED, FAILED }

data class DeploymentStepItem(
    val id: Int,
    val title: String,
    val description: String,
    val status: StepStatus = StepStatus.PENDING
)

class SSHInstallerViewModel : ViewModel() {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs = _logs.asStateFlow()

    private val _isInstalling = MutableStateFlow(false)
    val isInstalling = _isInstalling.asStateFlow()

    private val _installResult = MutableStateFlow<String?>(null)
    val installResult = _installResult.asStateFlow()

    private val _installedConnection = MutableStateFlow<PanelConnection?>(null)
    val installedConnection = _installedConnection.asStateFlow()

    private val _detectedCredentials = MutableStateFlow<Pair<String, String>?>(null)
    val detectedCredentials = _detectedCredentials.asStateFlow()

    private val initialSteps = listOf(
        DeploymentStepItem(1, "1. Подключение по SSH", "Handshake Ed25519 OK • Авторизация root", StepStatus.PENDING),
        DeploymentStepItem(2, "2. Смена порта SSH в sshd_config", "Генерация случайного порта и перезапуск sshd", StepStatus.PENDING),
        DeploymentStepItem(3, "3. Обновление установленных пакетов", "sudo apt update && sudo apt upgrade -y", StepStatus.PENDING),
        DeploymentStepItem(4, "4. Установка 3X-UI & SSL (ACME)", "MHSanaei 3x-ui + Let's Encrypt IP SSL", StepStatus.PENDING),
        DeploymentStepItem(5, "5. Финализация & Сохранение логов", "Экспорт файла {IP}_{Date}.txt и сохранение", StepStatus.PENDING)
    )

    private val _steps = MutableStateFlow(initialSteps)
    val steps = _steps.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    private val _logFilePath = MutableStateFlow<String?>(null)
    val logFilePath = _logFilePath.asStateFlow()

    private val sshInstaller = SSHInstaller()

    private var currentStep = 1

    private fun updateStepStatus(stepId: Int, status: StepStatus, progressVal: Float, customDesc: String? = null) {
        if (stepId < currentStep) return
        currentStep = stepId
        _steps.value = _steps.value.map { step ->
            if (step.id == stepId) {
                step.copy(
                    status = status,
                    description = customDesc ?: step.description
                )
            } else if (step.id < stepId) {
                step.copy(status = StepStatus.COMPLETED)
            } else step
        }
        _progress.value = progressVal
    }

    private fun updateAllStepsCompleted() {
        _steps.value = _steps.value.map { step ->
            step.copy(status = StepStatus.COMPLETED)
        }
        _progress.value = 1.0f
    }

    private fun markActiveStepFailed() {
        _steps.value = _steps.value.map { step ->
            if (step.status == StepStatus.IN_PROGRESS) step.copy(status = StepStatus.FAILED) else step
        }
    }

    fun install3xUI(
        context: Context,
        host: String,
        port: Int,
        username: String,
        password: String
    ) {
        _isInstalling.value = true
        _installResult.value = null
        _installedConnection.value = null
        _detectedCredentials.value = null
        _logFilePath.value = null
        _logs.value = emptyList()
        _steps.value = initialSteps
        _progress.value = 0.05f
        currentStep = 1

        updateStepStatus(1, StepStatus.IN_PROGRESS, 0.08f)

        var parsedUser = ""
        var parsedPass = ""

        viewModelScope.launch {
            sshInstaller.install3xUI(
                context = context,
                host = host,
                port = port,
                user = username,
                password = password,
                onLogReceived = { log ->
                    _logs.value = _logs.value + log

                    val cleanLog = log.trim()
                    val lower = cleanLog.lowercase()

                    // Step 1: Handshake
                    if (lower.contains("успешно установлено") || lower.contains("ssh соединение")) {
                        updateStepStatus(1, StepStatus.COMPLETED, 0.18f)
                        updateStepStatus(2, StepStatus.IN_PROGRESS, 0.22f)
                    }

                    // Step 2: SSH Port Change
                    if (lower.contains("порт ssh изменен на") || lower.contains("sshd_config")) {
                        val portMatch = Regex("порт SSH изменен на (\\d+)").find(cleanLog)
                        val portDesc = if (portMatch != null) "Новый порт SSH: ${portMatch.groupValues[1]} • sshd перезапущен" else "Порт обновлен в /etc/ssh/sshd_config"
                        updateStepStatus(2, StepStatus.COMPLETED, 0.38f, customDesc = portDesc)
                        updateStepStatus(3, StepStatus.IN_PROGRESS, 0.40f)
                    }

                    // Step 3: Package Updates
                    if (lower.contains("обновление установленных пакетов") || lower.contains("apt update")) {
                        updateStepStatus(3, StepStatus.IN_PROGRESS, 0.50f)
                    }
                    if (lower.contains("пакеты системы успешно обновлены")) {
                        updateStepStatus(3, StepStatus.COMPLETED, 0.62f)
                        updateStepStatus(4, StepStatus.IN_PROGRESS, 0.65f)
                    }

                    // Step 4: 3X-UI & SSL
                    if (lower.contains("database selection") || lower.contains("acme") || lower.contains("issuing ip certificate") || lower.contains("let's encrypt")) {
                        updateStepStatus(4, StepStatus.IN_PROGRESS, 0.82f)
                    }

                    // Step 5: Finalization
                    if (lower.contains("panel installation complete") || lower.contains("username:")) {
                        updateStepStatus(4, StepStatus.COMPLETED, 0.90f)
                        updateStepStatus(5, StepStatus.IN_PROGRESS, 0.95f)
                    }

                    // Parse credentials for screen display
                    if (lower.contains("username:")) {
                        parsedUser = cleanLog.substringAfter("Username:").trim()
                    }
                    if (lower.contains("password:")) {
                        parsedPass = cleanLog.substringAfter("Password:").trim()
                    }
                    if (parsedUser.isNotBlank() || parsedPass.isNotBlank()) {
                        _detectedCredentials.value = Pair(parsedUser, parsedPass)
                    }
                }
            ).onSuccess { (connection, file) ->
                _isInstalling.value = false
                updateAllStepsCompleted()
                _logFilePath.value = file.absolutePath
                _installResult.value = "Установка 3x-ui успешно завершена!"
                _installedConnection.value = connection
            }.onFailure { e ->
                _isInstalling.value = false
                markActiveStepFailed()
                _installResult.value = "Ошибка: ${e.localizedMessage}"
            }
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
        _installResult.value = null
        _installedConnection.value = null
        _detectedCredentials.value = null
        _steps.value = initialSteps
        _progress.value = 0f
        currentStep = 1
    }
}