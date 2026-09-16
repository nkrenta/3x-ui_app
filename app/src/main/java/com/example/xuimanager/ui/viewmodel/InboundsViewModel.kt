package com.example.xuimanager.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xuimanager.data.api.model.Inbound
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.data.repository.PanelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InboundsViewModel : ViewModel() {
    private val _inbounds = MutableStateFlow<List<Inbound>>(emptyList())
    val inbounds = _inbounds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var currentConnection: PanelConnection? = null
    private val repository = PanelRepository()

    fun setConnection(connection: PanelConnection, context: Context) {
        currentConnection = connection
        loadInbounds(context)
    }

    fun loadInbounds(context: Context) {
        currentConnection?.let { connection ->
            _isLoading.value = true
            _error.value = null
            viewModelScope.launch {
                repository.getInboundsList(context, connection)
                    .onSuccess { list ->
                        _inbounds.value = list
                        _isLoading.value = false
                    }
                    .onFailure { e ->
                        _error.value = e.localizedMessage
                        _isLoading.value = false
                    }
            }
        }
    }
}