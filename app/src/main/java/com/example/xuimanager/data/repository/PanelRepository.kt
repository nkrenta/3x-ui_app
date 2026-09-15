package com.example.xuimanager.data.repository

import android.content.Context
import com.example.xuimanager.data.api.XuiApiClient
import com.example.xuimanager.data.api.model.ApiClient
import com.example.xuimanager.data.api.model.ClientSettingsItem
import com.example.xuimanager.data.api.model.Inbound
import com.example.xuimanager.data.api.model.PanelInfo
import com.example.xuimanager.data.api.model.SystemStats
import com.example.xuimanager.data.model.PanelConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PanelRepository {

    suspend fun fetchGeoLocation(host: String): String? = withContext(Dispatchers.IO) {
        GeoIpRepository.getIpLocation(host)
    }

    suspend fun measurePing(context: Context, connection: PanelConnection): Pair<Boolean, Int?> =
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            try {
                val client = XuiApiClient.getInstance(context, connection)
                val isAlive = client.checkPing()
                val ping = (System.currentTimeMillis() - startTime).toInt()
                Pair(isAlive, if (isAlive) ping else null)
            } catch (_: Exception) {
                Pair(false, null)
            }
        }

    suspend fun testConnection(context: Context, connection: PanelConnection): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val client = XuiApiClient.getInstance(context, connection)
                val result = client.login()
                result.isSuccess
            } catch (_: Exception) {
                false
            }
        }

    suspend fun getXrayVersion(context: Context, connection: PanelConnection): Result<String> =
        withContext(Dispatchers.IO) {
            val client = XuiApiClient.getInstance(context, connection)
            val loginResult = client.login()
            if (loginResult.isFailure) {
                Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
            } else {
                client.getXrayVersion()
            }
        }

    suspend fun restartPanel(context: Context, connection: PanelConnection): Result<Boolean> =
        withContext(Dispatchers.IO) {
            val client = XuiApiClient.getInstance(context, connection)
            val loginResult = client.login()
            if (loginResult.isFailure) {
                Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
            } else {
                client.restartPanel()
            }
        }

    suspend fun restartXrayService(context: Context, connection: PanelConnection): Result<Boolean> =
        withContext(Dispatchers.IO) {
            val client = XuiApiClient.getInstance(context, connection)
            val loginResult = client.login()
            if (loginResult.isFailure) {
                Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
            } else {
                client.restartXrayService()
            }
        }

    suspend fun getPanelInfo(context: Context, connection: PanelConnection): Result<PanelInfo> =
        withContext(Dispatchers.IO) {
            val client = XuiApiClient.getInstance(context, connection)
            val loginResult = client.login()
            if (loginResult.isFailure) {
                Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
            } else {
                client.getPanelInfo()
            }
        }

    suspend fun getSystemStats(context: Context, connection: PanelConnection): Result<SystemStats> =
        withContext(Dispatchers.IO) {
            val client = XuiApiClient.getInstance(context, connection)
            val loginResult = client.login()
            if (loginResult.isFailure) {
                Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
            } else {
                client.getSystemStats()
            }
        }

    suspend fun getInbounds(context: Context, connection: PanelConnection): Result<List<Inbound>> =
        withContext(Dispatchers.IO) {
            val client = XuiApiClient.getInstance(context, connection)
            val loginResult = client.login()
            if (loginResult.isFailure) {
                Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
            } else {
                client.getInbounds()
            }
        }

    suspend fun addInboundJson(
        context: Context,
        connection: PanelConnection,
        rawJson: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val apiClient = XuiApiClient.getInstance(context, connection)
        val loginResult = apiClient.login()
        if (loginResult.isFailure) {
            Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
        } else {
            apiClient.addInboundJson(rawJson)
        }
    }

    suspend fun getClients(
        context: Context,
        connection: PanelConnection,
        inboundId: Int
    ): Result<List<ApiClient>> = withContext(Dispatchers.IO) {
        val client = XuiApiClient.getInstance(context, connection)
        val loginResult = client.login()
        if (loginResult.isFailure) {
            Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
        } else {
            client.getClients(inboundId)
        }
    }

    suspend fun addClient(
        context: Context,
        connection: PanelConnection,
        inboundId: Int,
        client: ClientSettingsItem
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val apiClient = XuiApiClient.getInstance(context, connection)
        val loginResult = apiClient.login()
        if (loginResult.isFailure) {
            Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
        } else {
            apiClient.addClient(inboundId, client)
        }
    }

    suspend fun deleteClient(
        context: Context,
        connection: PanelConnection,
        inboundId: Int,
        clientId: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val apiClient = XuiApiClient.getInstance(context, connection)
        val loginResult = apiClient.login()
        if (loginResult.isFailure) {
            Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
        } else {
            apiClient.deleteClient(inboundId, clientId)
        }
    }

    suspend fun toggleClient(
        context: Context,
        connection: PanelConnection,
        client: ApiClient
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val apiClient = XuiApiClient.getInstance(context, connection)
        val loginResult = apiClient.login()
        if (loginResult.isFailure) {
            Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
        } else {
            val settingsItem = ClientSettingsItem(
                id = client.id ?: "",
                email = client.email ?: "",
                flow = client.flow,
                limitIp = 0,
                totalGb = client.total,
                expiryTime = client.expiryTime,
                enable = !client.enable
            )
            apiClient.updateClient(client.id ?: "", settingsItem)
        }
    }

    suspend fun resetClientTraffic(
        context: Context,
        connection: PanelConnection,
        clientId: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val apiClient = XuiApiClient.getInstance(context, connection)
        val loginResult = apiClient.login()
        if (loginResult.isFailure) {
            Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
        } else {
            apiClient.resetClientTraffic(clientId)
        }
    }
}