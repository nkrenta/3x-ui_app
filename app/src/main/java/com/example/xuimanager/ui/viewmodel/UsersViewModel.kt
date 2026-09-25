package com.example.xuimanager.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xuimanager.data.api.model.ApiClient
import com.example.xuimanager.data.api.model.ApiClientItem
import com.example.xuimanager.data.api.model.ClientSettingsItem
import com.example.xuimanager.data.api.model.Inbound
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.data.repository.PanelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsersViewModel : ViewModel() {
    private val _clients = MutableStateFlow<List<ApiClient>>(emptyList())
    val clients = _clients.asStateFlow()

    private val _inbounds =
        MutableStateFlow<List<Inbound>>(emptyList())
    val inbounds = _inbounds.asStateFlow()

    private val _selectedInboundId = MutableStateFlow<Int?>(null)
    val selectedInboundId = _selectedInboundId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _highlightedClientId = MutableStateFlow<String?>(null)
    val highlightedClientId = _highlightedClientId.asStateFlow()

    private var currentConnection: PanelConnection? = null
    private val repository = PanelRepository()

    fun setConnection(connection: PanelConnection, context: Context) {
        currentConnection = connection
        loadAllClients(context)
    }

    fun highlightClient(clientId: String) {
        _highlightedClientId.value = clientId
    }

    fun clearHighlight() {
        _highlightedClientId.value = null
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

    fun loadInbounds(context: Context) {
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

    fun loadClients(context: Context, inboundId: Int) {
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

    fun addClientApi(
        context: Context,
        clientItem: ApiClientItem,
        inboundIds: List<Int>,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        currentConnection?.let { connection ->
            _isLoading.value = true
            viewModelScope.launch {
                repository.addClientApi(context, connection, clientItem, inboundIds)
                    .onSuccess {
                        loadAllClients(context)
                        onResult(true, null)
                    }
                    .onFailure { e ->
                        _error.value = e.localizedMessage
                        _isLoading.value = false
                        onResult(false, e.localizedMessage)
                    }
            }
        }
    }

    fun getClientLinks(context: Context, email: String, onResult: (List<String>) -> Unit) {
        currentConnection?.let { connection ->
            viewModelScope.launch {
                repository.getClientLinks(context, connection, email)
                    .onSuccess { links -> onResult(links) }
                    .onFailure { onResult(emptyList()) }
            }
        }
    }

    fun deleteClientByEmail(context: Context, email: String, onResult: (Boolean) -> Unit = {}) {
        currentConnection?.let { connection ->
            _isLoading.value = true
            viewModelScope.launch {
                repository.deleteClientByEmail(context, connection, email)
                    .onSuccess {
                        loadAllClients(context)
                        onResult(true)
                    }
                    .onFailure { e ->
                        _error.value = e.localizedMessage
                        _isLoading.value = false
                        onResult(false)
                    }
            }
        }
    }

    fun resetClientTrafficByEmail(context: Context, email: String, onResult: (Boolean) -> Unit = {}) {
        currentConnection?.let { connection ->
            viewModelScope.launch {
                repository.resetClientTrafficByEmail(context, connection, email)
                    .onSuccess {
                        loadAllClients(context)
                        onResult(true)
                    }
                    .onFailure { e ->
                        _error.value = e.localizedMessage
                        onResult(false)
                    }
            }
        }
    }

    fun clearClientHwidsByEmail(context: Context, email: String, onResult: (Boolean) -> Unit = {}) {
        currentConnection?.let { connection ->
            viewModelScope.launch {
                repository.clearClientHwidsByEmail(context, connection, email)
                    .onSuccess { onResult(true) }
                    .onFailure { e ->
                        _error.value = e.localizedMessage
                        onResult(false)
                    }
            }
        }
    }

    fun toggleClientEnabled(context: Context, email: String, enable: Boolean) {
        currentConnection?.let { connection ->
            viewModelScope.launch {
                repository.toggleClientEnabled(context, connection, email, enable)
                    .onSuccess { loadAllClients(context) }
                    .onFailure { e -> _error.value = e.localizedMessage }
            }
        }
    }

    fun addClient(context: Context, client: ClientSettingsItem) {
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

    fun deleteClient(context: Context, clientId: String) {
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

    fun toggleClient(context: Context, client: ApiClient) {
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

    fun resetClientTraffic(context: Context, clientId: String) {
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