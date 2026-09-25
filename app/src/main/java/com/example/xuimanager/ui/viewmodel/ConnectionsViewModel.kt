package com.example.xuimanager.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xuimanager.data.api.model.PanelInfo
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
    private val persistenceMutex = Mutex()

    private val _serverStats = MutableStateFlow<Map<String, PanelInfo>>(emptyMap())
    val serverStats = _serverStats.asStateFlow()

    fun initPersistence(context: Context) {
        if (isCacheLoaded) return
        viewModelScope.launch(Dispatchers.IO) {
            persistenceMutex.withLock {
                if (isCacheLoaded) return@withLock
                try {
                    val settingsRepo = SettingsRepository(context)
                    val json = settingsRepo.savedConnectionsJson.first()
                    if (json.isNotBlank()) {
                        val type = object : TypeToken<List<PanelConnection>>() {}.type
                        val list: List<PanelConnection>? = Gson().fromJson(json, type)
                        if (list != null) {
                            val currentInMemory = _connections.value
                            val merged = (list + currentInMemory).distinctBy { it.id }
                            _connections.value = merged
                        }
                    }
                } catch (_: Exception) {
                } finally {
                    isCacheLoaded = true
                }
            }
        }
    }

    fun persistConnections(context: Context) {
        val snapshot = _connections.value
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val settingsRepo = SettingsRepository(context)
                val json = Gson().toJson(snapshot)
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
                        val newStats = _serverStats.value.toMutableMap()

                        val updatedList = currentList.map { connection ->
                            val (isAlive, ping) = repository.measurePing(context, connection)
                            if (isAlive) {
                                val infoRes = repository.getPanelInfo(context, connection)
                                if (infoRes.isSuccess) {
                                    infoRes.getOrNull()?.let { newStats[connection.id] = it }
                                }
                            }
                            connection.copy(
                                isConnected = isAlive,
                                pingMs = ping
                            )
                        }
                        val validIds = _connections.value.map { it.id }.toSet()
                        val filteredList = updatedList.filter { it.id in validIds }

                        _connections.value = filteredList
                        _serverStats.value = newStats.filterKeys { it in validIds }
                    }
                } catch (_: Exception) {
                    // Игнорируем сетевые ошибки в цикле пинга
                }
                delay(3000)
            }
        }
    }

    fun addConnection(connection: PanelConnection, context: Context? = null) {
        _connections.value = (_connections.value + connection).distinctBy { it.id }
        context?.let { persistConnections(it) }
    }

    fun updateConnection(connection: PanelConnection, context: Context? = null) {
        _connections.value = _connections.value.map { if (it.id == connection.id) connection else it }
        context?.let { persistConnections(it) }
    }

    fun deleteConnection(connectionId: String, context: Context? = null) {
        _connections.value = _connections.value.filter { it.id != connectionId }
        _serverStats.value = _serverStats.value.filterKeys { it != connectionId }
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
                if (connection.name == connection.host || connection.name.isBlank() || !connection.name.contains("(")) {
                    repository.fetchGeoLocation(connection.host)?.let { geoName ->
                        if (geoName.isNotBlank()) {
                            updatedName = geoName
                        }
                    }
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
            val listToTest = _connections.value
            listToTest.forEach { connection ->
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