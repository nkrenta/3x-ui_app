package com.example.xuimanager.data.api

import android.content.Context
import com.example.xuimanager.data.api.model.AddClientRequest
import com.example.xuimanager.data.api.model.AddClientSettings
import com.example.xuimanager.data.api.model.ApiClient
import com.example.xuimanager.data.api.model.ClientSettingsItem
import com.example.xuimanager.data.api.model.Inbound
import com.example.xuimanager.data.api.model.LoginRequest
import com.example.xuimanager.data.api.model.PanelInfo
import com.example.xuimanager.data.api.model.SystemStats
import com.example.xuimanager.data.model.PanelConnection
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class XuiApiClient private constructor(
    private val service: XuiApiService,
    private val connection: PanelConnection,
    private val cookieJar: CookieJar,
    private val csrfHolder: CsrfHolder
) {

    private class CsrfHolder {
        @Volatile
        var token: String? = null
    }

    companion object {
        private val instances = mutableMapOf<String, XuiApiClient>()

        fun getInstance(context: Context, connection: PanelConnection): XuiApiClient {
            val formattedPath = connection.path.trim().trim('/')
            val key =
                "${connection.id}_${connection.protocol}_${connection.host}_${connection.port}_${formattedPath}_${connection.token}"
            return instances.getOrPut(key) {
                createClient(context, connection)
            }
        }

        private fun createClient(context: Context, connection: PanelConnection): XuiApiClient {
            val cookieJar = PersistentCookieJar()
            val csrfHolder = CsrfHolder()

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val headersInterceptor = Interceptor { chain ->
                val original = chain.request()
                val origin = "${original.url.scheme}://${original.url.host}:${original.url.port}"
                val formattedPath = connection.path.trim().trim('/')
                val refererUrl = if (formattedPath.isNotEmpty()) {
                    "$origin/$formattedPath/"
                } else {
                    "$origin/"
                }

                val requestBuilder = original.newBuilder()
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
                    )
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Origin", origin)
                    .header("Referer", refererUrl)

                if (connection.token.isNotBlank()) {
                    val cleanToken = connection.token.trim()
                    requestBuilder.header("X-Api-Token", cleanToken)
                    requestBuilder.header("Authorization", "Bearer $cleanToken")
                }

                val currentCsrf = csrfHolder.token
                if (!currentCsrf.isNullOrBlank()) {
                    requestBuilder.header("X-CSRF-Token", currentCsrf)
                    requestBuilder.header("X-XSRF-TOKEN", currentCsrf)
                }

                chain.proceed(requestBuilder.build())
            }

            val clientBuilder = OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .addInterceptor(headersInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)

            if (connection.skipCertVerify || connection.protocol.equals(
                    "https",
                    ignoreCase = true
                )
            ) {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                })

                try {
                    val sslContext = SSLContext.getInstance("TLS")
                    sslContext.init(null, trustAllCerts, SecureRandom())
                    clientBuilder.sslSocketFactory(
                        sslContext.socketFactory,
                        trustAllCerts[0] as X509TrustManager
                    )
                    clientBuilder.hostnameVerifier { _, _ -> true }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val formattedPath = connection.path.trim().trim('/')
            val baseUrl = if (formattedPath.isNotEmpty()) {
                "${connection.protocol}://${connection.host}:${connection.port}/$formattedPath/"
            } else {
                "${connection.protocol}://${connection.host}:${connection.port}/"
            }

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return XuiApiClient(
                retrofit.create(XuiApiService::class.java),
                connection,
                cookieJar,
                csrfHolder
            )
        }

        fun clearInstance(connectionId: String) {
            instances.remove(connectionId)
        }
    }

    suspend fun checkPing(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = service.getRootPage()
            response.isSuccessful || response.code() in 200..404
        } catch (_: Exception) {
            false
        }
    }

    suspend fun login(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (connection.token.isNotBlank()) {
                val infoRes = getPanelInfo()
                if (infoRes.isSuccess) {
                    return@withContext Result.success(true)
                }
                val inboundsRes = getInbounds()
                if (inboundsRes.isSuccess) {
                    return@withContext Result.success(true)
                }
            }

            try {
                val rootResponse = service.getRootPage()
                if (rootResponse.isSuccessful) {
                    val html = rootResponse.body()?.string() ?: ""
                    val match = Regex(
                        """<meta\s+name=["']csrf-token["']\s+content=["']([^"']+)["']""",
                        RegexOption.IGNORE_CASE
                    ).find(html)
                    if (match != null) {
                        csrfHolder.token = match.groupValues[1]
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val formResponse = service.loginForm(connection.username, connection.password)
            if (formResponse.isSuccessful && formResponse.body()?.success == true) {
                return@withContext Result.success(true)
            }

            val formSlashResponse = service.loginFormSlash(connection.username, connection.password)
            if (formSlashResponse.isSuccessful && formSlashResponse.body()?.success == true) {
                return@withContext Result.success(true)
            }

            val jsonResponse =
                service.loginJson(LoginRequest(connection.username, connection.password))
            if (jsonResponse.isSuccessful && jsonResponse.body()?.success == true) {
                return@withContext Result.success(true)
            }

            val errorMsg = when {
                formResponse.code() == 403 -> "Ошибка 403 Forbidden: Защита 3x-ui заблокировала запрос"
                formResponse.code() == 404 -> "Ошибка 404 Not Found: Неверный путь к панели"
                formResponse.body()?.msg != null -> formResponse.body()!!.msg
                jsonResponse.body()?.msg != null -> jsonResponse.body()!!.msg
                else -> "Ошибка подключения (HTTP ${formResponse.code()})"
            }
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Неизвестная ошибка авторизации"
            Result.failure(Exception(msg))
        }
    }

    suspend fun getPanelInfo(): Result<PanelInfo> = withContext(Dispatchers.IO) {
        try {
            val res1 = service.getServerStatusApiGet()
            if (res1.isSuccessful && res1.body()?.success == true && res1.body()?.obj != null) {
                return@withContext Result.success(res1.body()!!.obj!!)
            }
            val res2 = service.getServerStatusApiPost()
            if (res2.isSuccessful && res2.body()?.success == true && res2.body()?.obj != null) {
                return@withContext Result.success(res2.body()!!.obj!!)
            }
            val res3 = service.getPanelInfo()
            if (res3.isSuccessful && res3.body()?.success == true && res3.body()?.obj != null) {
                return@withContext Result.success(res3.body()!!.obj!!)
            }
            val res4 = service.getPanelInfoAlt()
            if (res4.isSuccessful && res4.body()?.success == true && res4.body()?.obj != null) {
                return@withContext Result.success(res4.body()!!.obj!!)
            }
            val code = if (!res1.isSuccessful) res1.code() else res2.code()
            Result.failure(Exception("HTTP $code"))
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Ошибка получения статуса панели"
            Result.failure(Exception(msg))
        }
    }

    suspend fun getXrayVersion(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val res1 = service.getXrayVersionApi()
            if (res1.isSuccessful && res1.body()?.success == true) {
                val ver = res1.body()?.getObjAsString() ?: res1.body()?.msg ?: "v1.8.x"
                return@withContext Result.success(ver)
            }
            val res2 = service.getXrayVersion()
            if (res2.isSuccessful && res2.body()?.success == true) {
                val ver = res2.body()?.getObjAsString() ?: res2.body()?.msg ?: "v1.8.x"
                return@withContext Result.success(ver)
            }
            Result.failure(Exception("HTTP ${res1.code()}"))
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Ошибка получения версии Xray"
            Result.failure(Exception(msg))
        }
    }

    suspend fun restartPanel(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val res1 = service.restartPanelApi()
            if (res1.isSuccessful && res1.body()?.success == true) {
                return@withContext Result.success(true)
            }
            val res2 = service.restartPanel()
            if (res2.isSuccessful && res2.body()?.success == true) {
                return@withContext Result.success(true)
            }
            Result.failure(Exception("HTTP ${res1.code()}"))
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Ошибка перезапуска панели"
            Result.failure(Exception(msg))
        }
    }

    suspend fun restartXrayService(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val res1 = service.restartXrayServiceApi()
            if (res1.isSuccessful && res1.body()?.success == true) {
                return@withContext Result.success(true)
            }
            val res2 = service.restartXrayService()
            if (res2.isSuccessful && res2.body()?.success == true) {
                return@withContext Result.success(true)
            }
            Result.failure(Exception("HTTP ${res1.code()}"))
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Ошибка перезапуска Xray"
            Result.failure(Exception(msg))
        }
    }

    suspend fun getSystemStats(): Result<SystemStats> = withContext(Dispatchers.IO) {
        try {
            val infoRes = getPanelInfo()
            if (infoRes.isSuccess) {
                val info = infoRes.getOrNull()!!
                val stats = SystemStats(cpu = info.cpu, mem = 0.0, disk = 0.0, network = null)
                return@withContext Result.success(stats)
            }
            Result.failure(Exception("Failed to get system stats"))
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Ошибка получения статистики"
            Result.failure(Exception(msg))
        }
    }

    suspend fun getInbounds(): Result<List<Inbound>> = withContext(Dispatchers.IO) {
        try {
            val response = service.getInbounds()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Result.success(body.obj ?: emptyList())
                } else {
                    Result.failure(Exception(body?.msg ?: "Failed to get inbounds"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Ошибка получения списка инбаундов"
            Result.failure(Exception(msg))
        }
    }

    suspend fun addInboundJson(rawJson: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val jsonObject = JsonParser.parseString(rawJson).asJsonObject

            val res1 = service.addInboundApi(jsonObject)
            if (res1.isSuccessful && res1.body()?.success == true) {
                return@withContext Result.success(true)
            }

            val res2 = service.addInboundApiSlash(jsonObject)
            if (res2.isSuccessful && res2.body()?.success == true) {
                return@withContext Result.success(true)
            }

            val remark =
                if (jsonObject.has("remark") && !jsonObject.get("remark").isJsonNull) jsonObject.get(
                    "remark"
                ).asString else "Inbound"
            val port =
                if (jsonObject.has("port") && !jsonObject.get("port").isJsonNull) jsonObject.get("port").asInt else 21717
            val protocol =
                if (jsonObject.has("protocol") && !jsonObject.get("protocol").isJsonNull) jsonObject.get(
                    "protocol"
                ).asString else "vless"
            val enable =
                if (jsonObject.has("enable") && !jsonObject.get("enable").isJsonNull) jsonObject.get(
                    "enable"
                ).asBoolean else true
            val tag =
                if (jsonObject.has("tag") && !jsonObject.get("tag").isJsonNull) jsonObject.get("tag").asString else "in-$port"

            val settingsElem =
                if (jsonObject.has("settings")) jsonObject.get("settings") else JsonParser.parseString(
                    "{}"
                )
            val streamSettingsElem =
                if (jsonObject.has("streamSettings")) jsonObject.get("streamSettings") else JsonParser.parseString(
                    "{}"
                )
            val sniffingElem =
                if (jsonObject.has("sniffing")) jsonObject.get("sniffing") else JsonParser.parseString(
                    "{}"
                )

            val inbound = Inbound(
                id = 0,
                up = 0,
                down = 0,
                total = 0,
                remark = remark,
                enable = enable,
                expiryTime = 0,
                listen = "",
                port = port,
                protocol = protocol,
                settings = settingsElem,
                streamSettings = streamSettingsElem,
                tag = tag,
                sniffing = sniffingElem
            )

            val res3 = addInbound(inbound)
            if (res3.isSuccess) {
                return@withContext Result.success(true)
            }

            val errMsg = res1.body()?.msg
                ?: res2.body()?.msg
                ?: res3.exceptionOrNull()?.message
                ?: "HTTP ${res1.code()}"
            Result.failure(Exception(errMsg))
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Неизвестная ошибка добавления инбаунда"
            Result.failure(Exception(msg))
        }
    }

    suspend fun addInbound(inbound: Inbound): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val res1 = service.addInbound(inbound)
            if (res1.isSuccessful && res1.body()?.success == true) {
                return@withContext Result.success(true)
            }

            val res2 = service.addInboundSlash(inbound)
            if (res2.isSuccessful && res2.body()?.success == true) {
                return@withContext Result.success(true)
            }

            val res3 = service.addInboundForm(
                up = inbound.up,
                down = inbound.down,
                total = inbound.total,
                remark = inbound.remark ?: "",
                enable = inbound.enable,
                expiryTime = inbound.expiryTime,
                listen = inbound.listen ?: "",
                port = inbound.port,
                protocol = inbound.protocol ?: "vless",
                settings = inbound.getSettingsAsString(),
                streamSettings = inbound.getStreamSettingsAsString(),
                sniffing = inbound.getSniffingAsString()
            )
            if (res3.isSuccessful && res3.body()?.success == true) {
                return@withContext Result.success(true)
            }

            val res4 = service.addInboundFormSlash(
                up = inbound.up,
                down = inbound.down,
                total = inbound.total,
                remark = inbound.remark ?: "",
                enable = inbound.enable,
                expiryTime = inbound.expiryTime,
                listen = inbound.listen ?: "",
                port = inbound.port,
                protocol = inbound.protocol ?: "vless",
                settings = inbound.getSettingsAsString(),
                streamSettings = inbound.getStreamSettingsAsString(),
                sniffing = inbound.getSniffingAsString()
            )
            if (res4.isSuccessful && res4.body()?.success == true) {
                return@withContext Result.success(true)
            }

            val errMsg = res1.body()?.msg
                ?: res2.body()?.msg
                ?: res3.body()?.msg
                ?: res4.body()?.msg
                ?: "HTTP ${res1.code()}"
            Result.failure(Exception(errMsg))
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Неизвестная ошибка добавления инбаунда"
            Result.failure(Exception(msg))
        }
    }

    suspend fun getClientsList(): Result<List<ApiClient>> = withContext(Dispatchers.IO) {
        try {
            val res1 = service.getClientsListApiGet()
            if (res1.isSuccessful && res1.body()?.success == true && res1.body()?.obj != null) {
                return@withContext Result.success(res1.body()!!.obj!!)
            }
            val res2 = service.getClientsListApiPost()
            if (res2.isSuccessful && res2.body()?.success == true && res2.body()?.obj != null) {
                return@withContext Result.success(res2.body()!!.obj!!)
            }
            val code = if (!res1.isSuccessful) res1.code() else res2.code()
            Result.failure(Exception("HTTP $code"))
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Ошибка получения клиентов"
            Result.failure(Exception(msg))
        }
    }

    suspend fun getClients(inboundId: Int): Result<List<ApiClient>> = withContext(Dispatchers.IO) {
        try {
            val response = service.getInboundClients(inboundId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Result.success(body.obj ?: emptyList())
                } else {
                    Result.failure(Exception(body?.msg ?: "Failed to get clients"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: "Ошибка получения клиентов"
            Result.failure(Exception(msg))
        }
    }

    suspend fun addClient(inboundId: Int, client: ClientSettingsItem): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val settings = AddClientSettings(listOf(client))
                val settingsJson = Gson().toJson(settings)
                val request = AddClientRequest(inboundId, settingsJson)

                val response = service.addClient(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.success(true)
                    } else {
                        Result.failure(Exception(body?.msg ?: "Failed to add client"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}"))
                }
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: e.message ?: "Ошибка добавления клиента"
                Result.failure(Exception(msg))
            }
        }

    suspend fun deleteClient(inboundId: Int, clientId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val response = service.deleteClient(inboundId, clientId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.success(true)
                    } else {
                        Result.failure(Exception(body?.msg ?: "Failed to delete client"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}"))
                }
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: e.message ?: "Ошибка удаления клиента"
                Result.failure(Exception(msg))
            }
        }

    suspend fun updateClient(clientId: String, client: ClientSettingsItem): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val settingsJson = Gson().toJson(client)
                val response = service.updateClient(clientId, settingsJson)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.success(true)
                    } else {
                        Result.failure(Exception(body?.msg ?: "Failed to update client"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}"))
                }
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: e.message ?: "Ошибка обновления клиента"
                Result.failure(Exception(msg))
            }
        }

    suspend fun resetClientTraffic(clientId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val response = service.resetClientTraffic(clientId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Result.success(true)
                    } else {
                        Result.failure(Exception(body?.msg ?: "Failed to reset traffic"))
                    }
                } else {
                    Result.failure(Exception("HTTP ${response.code()}"))
                }
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: e.message ?: "Ошибка сброса трафика"
                Result.failure(Exception(msg))
            }
        }

    fun getCookieJar(): CookieJar = cookieJar
}

private class PersistentCookieJar : CookieJar {
    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isNotEmpty()) {
            val existing = cookieStore[url.host] ?: mutableListOf()
            for (newCookie in cookies) {
                existing.removeAll { it.name == newCookie.name }
                existing.add(newCookie)
            }
            cookieStore[url.host] = existing
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host] ?: emptyList()
    }
}