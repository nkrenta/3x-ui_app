package com.example.xuimanager.data.repository

import android.content.Context
import com.example.xuimanager.data.api.XuiApiClient
import com.example.xuimanager.data.api.model.ApiClient
import com.example.xuimanager.data.api.model.ApiClientItem
import com.example.xuimanager.data.api.model.BulkAdjustRequest
import com.example.xuimanager.data.api.model.ClientGroup
import com.example.xuimanager.data.api.model.ClientHwid
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
            var infoRes = client.getPanelInfo()
            if (infoRes.isFailure) {
                val loginResult = client.login()
                if (loginResult.isSuccess) {
                    infoRes = client.getPanelInfo()
                }
            }
            infoRes
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

    suspend fun toggleInbound(
        context: Context,
        connection: PanelConnection,
        inboundId: Int,
        enable: Boolean
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val client = XuiApiClient.getInstance(context, connection)
        var res = client.setInboundEnable(inboundId, enable)
        if (res.isFailure) {
            val loginResult = client.login()
            if (loginResult.isSuccess) {
                res = client.setInboundEnable(inboundId, enable)
            }
        }
        res
    }

    suspend fun deleteInbound(
        context: Context,
        connection: PanelConnection,
        inboundId: Int
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val client = XuiApiClient.getInstance(context, connection)
        var res = client.deleteInbound(inboundId)
        if (res.isFailure) {
            val loginResult = client.login()
            if (loginResult.isSuccess) {
                res = client.deleteInbound(inboundId)
            }
        }
        res
    }

    suspend fun getInboundsList(
        context: Context,
        connection: PanelConnection
    ): Result<List<Inbound>> =
        withContext(Dispatchers.IO) {
            val client = XuiApiClient.getInstance(context, connection)
            val loginResult = client.login()
            if (loginResult.isFailure) {
                Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
            } else {
                client.getInboundsList()
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

    suspend fun getClientsList(
        context: Context,
        connection: PanelConnection
    ): Result<List<ApiClient>> = withContext(Dispatchers.IO) {
        val client = XuiApiClient.getInstance(context, connection)
        val loginResult = client.login()
        if (loginResult.isFailure) {
            Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
        } else {
            client.getClientsList()
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

    suspend fun addClientApi(
        context: Context,
        connection: PanelConnection,
        clientItem: ApiClientItem,
        inboundIds: List<Int>
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val apiClient = XuiApiClient.getInstance(context, connection)
        var res = apiClient.addClientApi(clientItem, inboundIds)
        if (res.isFailure) {
            val loginResult = apiClient.login()
            if (loginResult.isSuccess) {
                res = apiClient.addClientApi(clientItem, inboundIds)
            }
        }
        res
    }

    suspend fun getClientLinks(
        context: Context,
        connection: PanelConnection,
        email: String
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        val apiClient = XuiApiClient.getInstance(context, connection)
        var res = apiClient.getClientLinks(email)
        if (res.isFailure) {
            val loginResult = apiClient.login()
            if (loginResult.isSuccess) {
                res = apiClient.getClientLinks(email)
            }
        }
        res
    }

    suspend fun deleteClientByEmail(
        context: Context,
        connection: PanelConnection,
        email: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val apiClient = XuiApiClient.getInstance(context, connection)
        var res = apiClient.deleteClientByEmail(email)
        if (res.isFailure) {
            val loginResult = apiClient.login()
            if (loginResult.isSuccess) {
                res = apiClient.deleteClientByEmail(email)
            }
        }
        res
    }

    suspend fun resetClientTrafficByEmail(
        context: Context,
        connection: PanelConnection,
        email: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val apiClient = XuiApiClient.getInstance(context, connection)
        var res = apiClient.resetClientTrafficByEmail(email)
        if (res.isFailure) {
            val loginResult = apiClient.login()
            if (loginResult.isSuccess) {
                res = apiClient.resetClientTrafficByEmail(email)
            }
        }
        res
    }

    suspend fun clearClientHwidsByEmail(
        context: Context,
        connection: PanelConnection,
        email: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val apiClient = XuiApiClient.getInstance(context, connection)
        var res = apiClient.clearClientHwidsByEmail(email)
        if (res.isFailure) {
            val loginResult = apiClient.login()
            if (loginResult.isSuccess) {
                res = apiClient.clearClientHwidsByEmail(email)
            }
        }
        res
    }

    suspend fun toggleClientEnabled(
        context: Context,
        connection: PanelConnection,
        email: String,
        enable: Boolean
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val apiClient = XuiApiClient.getInstance(context, connection)
        var res = apiClient.toggleClientEnabled(email, enable)
        if (res.isFailure) {
            val loginResult = apiClient.login()
            if (loginResult.isSuccess) {
                res = apiClient.toggleClientEnabled(email, enable)
            }
        }
        res
    }

    suspend fun listClientGroups(context: Context, connection: PanelConnection): Result<List<ClientGroup>> = withContext(Dispatchers.IO) {
        val client = XuiApiClient.getInstance(context, connection)
        var res = client.listClientGroups()
        if (res.isFailure) {
            val loginResult = client.login()
            if (loginResult.isSuccess) res = client.listClientGroups()
        }
        res
    }

    suspend fun createClientGroup(context: Context, connection: PanelConnection, name: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val client = XuiApiClient.getInstance(context, connection)
        var res = client.createClientGroup(name)
        if (res.isFailure) {
            val loginResult = client.login()
            if (loginResult.isSuccess) res = client.createClientGroup(name)
        }
        res
    }

    suspend fun getClientIps(context: Context, connection: PanelConnection, email: String): Result<List<String>> = withContext(Dispatchers.IO) {
        val client = XuiApiClient.getInstance(context, connection)
        var res = client.getClientIps(email)
        if (res.isFailure) {
            val loginResult = client.login()
            if (loginResult.isSuccess) res = client.getClientIps(email)
        }
        res
    }

    suspend fun listClientHwids(context: Context, connection: PanelConnection, email: String): Result<List<ClientHwid>> = withContext(Dispatchers.IO) {
        val client = XuiApiClient.getInstance(context, connection)
        var res = client.listClientHwids(email)
        if (res.isFailure) {
            val loginResult = client.login()
            if (loginResult.isSuccess) res = client.listClientHwids(email)
        }
        res
    }

    suspend fun deleteClientHwid(context: Context, connection: PanelConnection, email: String, hwidId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        val client = XuiApiClient.getInstance(context, connection)
        var res = client.deleteClientHwid(email, hwidId)
        if (res.isFailure) {
            val loginResult = client.login()
            if (loginResult.isSuccess) res = client.deleteClientHwid(email, hwidId)
        }
        res
    }

    suspend fun deleteOrphanClients(context: Context, connection: PanelConnection): Result<Boolean> = withContext(Dispatchers.IO) {
        val client = XuiApiClient.getInstance(context, connection)
        var res = client.deleteOrphanClients()
        if (res.isFailure) {
            val loginResult = client.login()
            if (loginResult.isSuccess) res = client.deleteOrphanClients()
        }
        res
    }

    suspend fun exportClients(context: Context, connection: PanelConnection): Result<String> = withContext(Dispatchers.IO) {
        val client = XuiApiClient.getInstance(context, connection)
        var res = client.exportClients()
        if (res.isFailure) {
            val loginResult = client.login()
            if (loginResult.isSuccess) res = client.exportClients()
        }
        res
    }

    suspend fun getHappLink(context: Context, connection: PanelConnection, clientId: Int): Result<String> = withContext(Dispatchers.IO) {
        val client = XuiApiClient.getInstance(context, connection)
        var res = client.getHappLink(clientId)
        if (res.isFailure) {
            val loginResult = client.login()
            if (loginResult.isSuccess) res = client.getHappLink(clientId)
        }
        res
    }

    suspend fun getAllSettings(
        context: Context,
        connection: PanelConnection
    ): Result<com.google.gson.JsonObject> = withContext(Dispatchers.IO) {
        val client = XuiApiClient.getInstance(context, connection)
        var res = client.getAllSettings()
        if (res.isFailure) {
            val loginResult = client.login()
            if (loginResult.isSuccess) res = client.getAllSettings()
        }
        res
    }

    suspend fun getSubscriptionUrl(
        context: Context,
        connection: PanelConnection,
        client: ApiClient
    ): String? = withContext(Dispatchers.IO) {
        val subId = client.getEffectiveSubId()
        if (subId.isBlank()) return@withContext null
        val settingsRes = getAllSettings(context, connection)
        val settings = settingsRes.getOrNull()
            ?.let { com.example.xuimanager.data.api.model.PanelSettings.fromJson(it) }
            ?: com.example.xuimanager.data.api.model.PanelSettings()
        val host = com.example.xuimanager.data.api.model.PanelSettings.hostOf(connection.host)
        settings.subscriptionUrl(host, subId)
    }

    suspend fun bulkAdjustClients(
        context: Context,
        connection: PanelConnection,
        emails: List<String>,
        addDays: Int,
        addGb: Long,
        limitHwid: Int? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val client = XuiApiClient.getInstance(context, connection)
        val addBytes = addGb * 1024 * 1024 * 1024L
        val req = BulkAdjustRequest(
            emails = emails,
            addDays = addDays,
            addBytes = addBytes,
            limitHwid = limitHwid
        )
        var res = client.bulkAdjustClients(req)
        if (res.isFailure) {
            val loginResult = client.login()
            if (loginResult.isSuccess) res = client.bulkAdjustClients(req)
        }
        res
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
                id = client.getIdAsString(),
                email = client.email ?: "",
                flow = client.flow,
                limitIp = client.limitIp,
                totalGb = client.getTotalTrafficLimit(),
                expiryTime = client.getEffectiveExpiryTime(),
                enable = !client.enable
            )
            apiClient.updateClient(client.getIdAsString(), settingsItem)
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