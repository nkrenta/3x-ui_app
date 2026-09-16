package com.example.xuimanager.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.data.repository.PanelRepository
import com.example.xuimanager.data.repository.SettingsRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ConnectionsViewModel : ViewModel() {
    private val _connections = MutableStateFlow<List<PanelConnection>>(emptyList())
    val connections = _connections.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val repository = PanelRepository()
    private var autoPingJob: Job? = null
    private var isCacheLoaded = false

    fun initPersistence(context: Context) {
        if (isCacheLoaded) return
        isCacheLoaded = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val settingsRepo = SettingsRepository(context)
                val json = settingsRepo.savedConnectionsJson.first()
                if (json.isNotBlank()) {
                    val type = object : TypeToken<List<PanelConnection>>() {}.type
                    val list: List<PanelConnection>? = Gson().fromJson(json, type)
                    if (list != null) {
                        _connections.value = list
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun persistConnections(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val settingsRepo = SettingsRepository(context)
                val json = Gson().toJson(_connections.value)
                settingsRepo.setSavedConnectionsJson(json)
            } catch (_: Exception) {
            }
        }
    }

    fun startAutoPingLoop(context: Context) {
        initPersistence(context)
        if (autoPingJob?.isActive == true) return
        autoPingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val currentList = _connections.value
                    if (currentList.isNotEmpty()) {
                        var hasChanges = false
                        val updatedList = currentList.map { connection ->
                            val (isAlive, ping) = repository.measurePing(context, connection)
                            if (connection.isConnected != isAlive || connection.pingMs != ping) {
                                hasChanges = true
                                connection.copy(
                                    isConnected = isAlive,
                                    pingMs = ping
                                )
                            } else {
                                connection
                            }
                        }
                        if (hasChanges) {
                            _connections.value = updatedList
                        }
                    }
                } catch (_: Exception) {
                    // Игнорируем сетевые ошибки в цикле пинга
                }
                delay(2000)
            }
        }
    }

    fun addConnection(connection: PanelConnection, context: Context? = null) {
        _connections.value = _connections.value + connection
        context?.let { persistConnections(it) }
    }

    fun updateConnection(connection: PanelConnection, context: Context? = null) {
        _connections.value =
            _connections.value.map { if (it.id == connection.id) connection else it }
        context?.let { persistConnections(it) }
    }

    fun deleteConnection(connectionId: String, context: Context? = null) {
        _connections.value = _connections.value.filter { it.id != connectionId }
        context?.let { persistConnections(it) }
    }

    fun testConnection(context: Context, connection: PanelConnection, onFinished: () -> Unit = {}) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val success = repository.testConnection(context, connection)
            val measuredPing = (System.currentTimeMillis() - startTime).toInt()
            var updatedName = connection.name
            var xrayVer: String? = connection.xrayVersion

            if (success) {
                repository.fetchGeoLocation(connection.host)?.let { geoName ->
                    updatedName = geoName
                }
                repository.getXrayVersion(context, connection).onSuccess { ver ->
                    xrayVer = ver
                }
            }

            _connections.value = _connections.value.map {
                if (it.id == connection.id) {
                    it.copy(
                        isConnected = success,
                        name = updatedName,
                        xrayVersion = xrayVer,
                        pingMs = if (success) measuredPing else null
                    )
                } else it
            }
            persistConnections(context)
            _isLoading.value = false
            if (!success) {
                _error.value = "Не удалось подключиться к панели"
            }
            onFinished()
        }
    }

    fun testAllConnections(context: Context) {
        initPersistence(context)
        viewModelScope.launch {
            _connections.value.forEach { connection ->
                testConnection(context, connection)
            }
        }
    }

    fun restartPanel(
        context: Context,
        connection: PanelConnection,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            repository.restartPanel(context, connection)
                .onSuccess { onResult(true, "Панель перезапускается...") }
                .onFailure { e -> onResult(false, "Ошибка: ${e.localizedMessage}") }
        }
    }

    fun restartXrayService(
        context: Context,
        connection: PanelConnection,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            repository.restartXrayService(context, connection)
                .onSuccess { onResult(true, "Служба Xray перезапускается...") }
                .onFailure { e -> onResult(false, "Ошибка: ${e.localizedMessage}") }
        }
    }
}