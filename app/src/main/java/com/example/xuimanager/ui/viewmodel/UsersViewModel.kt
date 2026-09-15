package com.example.xuimanager.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xuimanager.data.api.model.ApiClient
import com.example.xuimanager.data.api.model.ClientSettingsItem
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.data.repository.PanelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsersViewModel : ViewModel() {
    private val _clients = MutableStateFlow<List<ApiClient>>(emptyList())
    val clients = _clients.asStateFlow()

    private val _inbounds =
        MutableStateFlow<List<com.example.xuimanager.data.api.model.Inbound>>(emptyList())
    val inbounds = _inbounds.asStateFlow()

    private val _selectedInboundId = MutableStateFlow<Int?>(null)
    val selectedInboundId = _selectedInboundId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var currentConnection: PanelConnection? = null
    private val repository = PanelRepository()

    fun setConnection(connection: PanelConnection, context: android.content.Context) {
        currentConnection = connection
        loadAllClients(context)
    }

    fun loadAllClients(context: Context) {
        currentConnection?.let { connection ->
            _isLoading.value = true
            _error.value = null
            viewModelScope.launch {
                repository.getClientsList(context, connection).onSuccess { clientsList ->
                    _clients.value = clientsList
                    _isLoading.value = false
                }.onFailure { _ ->
                    loadInbounds(context)
                }
            }
        }
    }

    fun loadInbounds(context: android.content.Context) {
        currentConnection?.let { connection ->
            _isLoading.value = true
            viewModelScope.launch {
                repository.getInbounds(context, connection).onSuccess { inbounds ->
                    _inbounds.value = inbounds
                    if (_selectedInboundId.value == null && inbounds.isNotEmpty()) {
                        _selectedInboundId.value = inbounds[0].id
                        loadClients(context, inbounds[0].id)
                    }
                    _isLoading.value = false
                }.onFailure { e ->
                    _error.value = e.localizedMessage
                    _isLoading.value = false
                }
            }
        }
    }

    fun loadClients(context: android.content.Context, inboundId: Int) {
        currentConnection?.let { connection ->
            _selectedInboundId.value = inboundId
            _isLoading.value = true
            viewModelScope.launch {
                repository.getClients(context, connection, inboundId).onSuccess { clients ->
                    _clients.value = clients
                    _isLoading.value = false
                }.onFailure { e ->
                    _error.value = e.localizedMessage
                    _isLoading.value = false
                }
            }
        }
    }

    fun addClient(context: android.content.Context, client: ClientSettingsItem) {
        _selectedInboundId.value?.let { inboundId ->
            currentConnection?.let { connection ->
                _isLoading.value = true
                viewModelScope.launch {
                    repository.addClient(context, connection, inboundId, client).onSuccess { _ ->
                        loadClients(context, inboundId)
                    }.onFailure { e ->
                        _error.value = e.localizedMessage
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    fun deleteClient(context: android.content.Context, clientId: String) {
        _selectedInboundId.value?.let { inboundId ->
            currentConnection?.let { connection ->
                _isLoading.value = true
                viewModelScope.launch {
                    repository.deleteClient(context, connection, inboundId, clientId)
                        .onSuccess { _ ->
                            loadClients(context, inboundId)
                        }.onFailure { e ->
                        _error.value = e.localizedMessage
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    fun toggleClient(context: android.content.Context, client: ApiClient) {
        _selectedInboundId.value?.let { inboundId ->
            currentConnection?.let { connection ->
                viewModelScope.launch {
                    repository.toggleClient(context, connection, client).onSuccess { _ ->
                        loadClients(context, inboundId)
                    }.onFailure { e ->
                        _error.value = e.localizedMessage
                    }
                }
            }
        }
    }

    fun resetClientTraffic(context: android.content.Context, clientId: String) {
        currentConnection?.let { connection ->
            viewModelScope.launch {
                repository.resetClientTraffic(context, connection, clientId).onSuccess { _ ->
                    _selectedInboundId.value?.let { loadClients(context, it) }
                }.onFailure { e ->
                    _error.value = e.localizedMessage
                }
            }
        }
    }
}