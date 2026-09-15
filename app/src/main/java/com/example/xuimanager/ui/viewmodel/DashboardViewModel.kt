package com.example.xuimanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xuimanager.data.api.model.PanelInfo
import com.example.xuimanager.data.api.model.SystemStats
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.data.repository.PanelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val _panelInfo = MutableStateFlow<PanelInfo?>(null)
    val panelInfo = _panelInfo.asStateFlow()

    private val _systemStats = MutableStateFlow<SystemStats?>(null)
    val systemStats = _systemStats.asStateFlow()

    private val _trafficHistory =
        MutableStateFlow<List<Float>>(listOf(10f, 25f, 45f, 30f, 80f, 55f, 90f, 60f))
    val trafficHistory = _trafficHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var currentConnection: PanelConnection? = null
    private val repository = PanelRepository()

    fun setConnection(connection: PanelConnection, context: android.content.Context) {
        currentConnection = connection
        loadData(context)
    }

    fun loadData(context: android.content.Context) {
        currentConnection?.let { connection ->
            _isLoading.value = true
            viewModelScope.launch {
                repository.getPanelInfo(context, connection).onSuccess { info ->
                    _panelInfo.value = info
                }.onFailure { e ->
                    _error.value = e.localizedMessage
                }
                repository.getSystemStats(context, connection).onSuccess { stats ->
                    _systemStats.value = stats
                    updateTrafficHistory(stats)
                }.onFailure { e ->
                    _error.value = e.localizedMessage
                }
                _isLoading.value = false
            }
        }
    }

    private fun updateTrafficHistory(stats: SystemStats) {
        val currentHistory = _trafficHistory.value
        val newPoint = ((stats.network?.down?.toFloat() ?: 0f) / 1024 / 1024).coerceIn(0f, 100f)
        _trafficHistory.value = if (currentHistory.size >= 20) {
            currentHistory.drop(1) + newPoint
        } else {
            currentHistory + newPoint
        }
    }

    fun refresh(context: android.content.Context) {
        loadData(context)
    }
}