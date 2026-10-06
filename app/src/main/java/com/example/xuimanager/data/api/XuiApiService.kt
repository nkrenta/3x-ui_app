package com.example.xuimanager.data.api

import com.example.xuimanager.data.api.model.AddClientApiRequest
import com.example.xuimanager.data.api.model.AddClientRequest
import com.example.xuimanager.data.api.model.BulkAdjustRequest
import com.example.xuimanager.data.api.model.ClientListResponse
import com.example.xuimanager.data.api.model.GenericResponse
import com.example.xuimanager.data.api.model.GroupAddClientsRequest
import com.example.xuimanager.data.api.model.GroupNameRequest
import com.example.xuimanager.data.api.model.Inbound
import com.example.xuimanager.data.api.model.InboundListResponse
import com.example.xuimanager.data.api.model.LoginRequest
import com.example.xuimanager.data.api.model.LoginResponse
import com.example.xuimanager.data.api.model.PanelInfoResponse
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface XuiApiService {

    // Root page GET to initialize 3x-ui secret base path session & cookies
    @GET(".")
    suspend fun getRootPage(): Response<ResponseBody>

    // Auth Form-UrlEncoded (основной метод 3x-ui / x-ui)
    @FormUrlEncoded
    @POST("login")
    suspend fun loginForm(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST("login/")
    suspend fun loginFormSlash(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    // Auth JSON (альтернативный метод)
    @POST("login")
    suspend fun loginJson(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    // Panel info (/panel/api/server/status и альтернативы)
    @GET("panel/api/server/status")
    suspend fun getServerStatusApiGet(): Response<PanelInfoResponse>

    @POST("panel/api/server/status")
    suspend fun getServerStatusApiPost(): Response<PanelInfoResponse>

    @POST("panel/server/status")
    suspend fun getPanelInfo(): Response<PanelInfoResponse>

    @POST("panel/status")
    suspend fun getPanelInfoAlt(): Response<PanelInfoResponse>

    // Xray Version
    @POST("panel/api/server/getXrayVersion")
    suspend fun getXrayVersionApi(): Response<GenericResponse>

    @POST("panel/server/getXrayVersion")
    suspend fun getXrayVersion(): Response<GenericResponse>

    // Restart Panel Service
    @POST("panel/api/setting/restartPanel")
    suspend fun restartPanelApi(): Response<GenericResponse>

    @POST("panel/setting/restartPanel")
    suspend fun restartPanel(): Response<GenericResponse>

    @GET("panel/setting/all")
    suspend fun getAllSettings(): Response<GenericResponse>

    // Restart Xray Service
    @POST("panel/api/server/restartXrayService")
    suspend fun restartXrayServiceApi(): Response<GenericResponse>

    @POST("panel/server/restartXrayService")
    suspend fun restartXrayService(): Response<GenericResponse>

    // 3x-ui API Inbounds Add (Путь REST API v2.x: /panel/api/inbounds/add)
    @POST("panel/api/inbounds/add")
    suspend fun addInboundApi(
        @Body body: JsonObject
    ): Response<GenericResponse>

    @POST("panel/api/inbounds/add/")
    suspend fun addInboundApiSlash(
        @Body body: JsonObject
    ): Response<GenericResponse>

    // Inbounds List REST API v2.x (/panel/api/inbounds/list)
    @GET("panel/api/inbounds/list")
    suspend fun getInboundsListApiGet(): Response<InboundListResponse>

    @POST("panel/api/inbounds/list")
    suspend fun getInboundsListApiPost(): Response<InboundListResponse>

    // Toggle Inbound Enable Status
    @POST("panel/api/inbounds/setEnable/{id}")
    suspend fun setInboundEnableApi(
        @Path("id") id: Int,
        @Body body: JsonObject
    ): Response<GenericResponse>

    @POST("panel/inbound/setEnable/{id}")
    suspend fun setInboundEnableLegacy(
        @Path("id") id: Int,
        @Body body: JsonObject
    ): Response<GenericResponse>

    // Delete Inbound (POST /panel/api/inbounds/del/{id})
    @POST("panel/api/inbounds/del/{id}")
    suspend fun deleteInboundApi(
        @Path("id") id: Int
    ): Response<GenericResponse>

    @POST("panel/inbound/del/{id}")
    suspend fun deleteInboundLegacy(
        @Path("id") id: Int
    ): Response<GenericResponse>

    // Legacy Inbounds Add
    @POST("panel/inbound/list")
    suspend fun getInbounds(): Response<InboundListResponse>

    @POST("panel/inbound/add")
    suspend fun addInbound(
        @Body inbound: Inbound
    ): Response<GenericResponse>

    @POST("panel/inbound/add/")
    suspend fun addInboundSlash(
        @Body inbound: Inbound
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("panel/inbound/add")
    suspend fun addInboundForm(
        @Field("up") up: Long,
        @Field("down") down: Long,
        @Field("total") total: Long,
        @Field("remark") remark: String,
        @Field("enable") enable: Boolean,
        @Field("expiryTime") expiryTime: Long,
        @Field("listen") listen: String,
        @Field("port") port: Int,
        @Field("protocol") protocol: String,
        @Field("settings") settings: String,
        @Field("streamSettings") streamSettings: String,
        @Field("sniffing") sniffing: String
    ): Response<GenericResponse>

    @FormUrlEncoded
    @POST("panel/inbound/add/")
    suspend fun addInboundFormSlash(
        @Field("up") up: Long,
        @Field("down") down: Long,
        @Field("total") total: Long,
        @Field("remark") remark: String,
        @Field("enable") enable: Boolean,
        @Field("expiryTime") expiryTime: Long,
        @Field("listen") listen: String,
        @Field("port") port: Int,
        @Field("protocol") protocol: String,
        @Field("settings") settings: String,
        @Field("streamSettings") streamSettings: String,
        @Field("sniffing") sniffing: String
    ): Response<GenericResponse>

    // Clients
    // Clients List
    @GET("panel/api/clients/list")
    suspend fun getClientsListApiGet(): Response<ClientListResponse>

    @POST("panel/api/clients/list")
    suspend fun getClientsListApiPost(): Response<ClientListResponse>

    @POST("panel/inbound/get/{id}")
    suspend fun getInboundClients(
        @Path("id") id: Int
    ): Response<ClientListResponse>

    @POST("panel/api/clients/add")
    suspend fun addClientApi(
        @Body request: AddClientApiRequest
    ): Response<GenericResponse>

    @POST("panel/inbound/addClient")
    suspend fun addClient(
        @Body request: AddClientRequest
    ): Response<GenericResponse>

    @POST("panel/inbound/delClient/{inboundId}/{clientId}")
    suspend fun deleteClient(
        @Path("inboundId") inboundId: Int,
        @Path("clientId") clientId: String
    ): Response<GenericResponse>

    @POST("panel/inbound/updateClient/{clientId}")
    suspend fun updateClient(
        @Path("clientId") clientId: String,
        @Body settings: String
    ): Response<GenericResponse>

    // REST API v2.x Client Operations
    @GET("panel/api/clients/links/{email}")
    suspend fun getClientLinksApi(
        @Path("email") email: String
    ): Response<GenericResponse>

    @POST("panel/api/clients/del/{email}")
    suspend fun deleteClientApiByEmail(
        @Path("email") email: String
    ): Response<GenericResponse>

    @POST("panel/api/clients/resetTraffic/{email}")
    suspend fun resetClientTrafficApiByEmail(
        @Path("email") email: String
    ): Response<GenericResponse>

    @DELETE("panel/api/clients/hwids/{email}")
    suspend fun clearClientHwidsApiByEmail(
        @Path("email") email: String
    ): Response<GenericResponse>

    @POST("panel/api/clients/bulkEnable")
    suspend fun bulkEnableClients(
        @Body emails: List<String>
    ): Response<GenericResponse>

    @POST("panel/api/clients/bulkDisable")
    suspend fun bulkDisableClients(
        @Body emails: List<String>
    ): Response<GenericResponse>

    // Client Groups
    @GET("panel/api/clients/groups")
    suspend fun listClientGroups(): Response<GenericResponse>

    @POST("panel/api/clients/groups/create")
    suspend fun createClientGroup(@Body body: GroupNameRequest): Response<GenericResponse>

    @POST("panel/api/clients/groups/bulkAdd")
    suspend fun addClientsToGroup(@Body body: GroupAddClientsRequest): Response<GenericResponse>

    @POST("panel/api/clients/groups/bulkRemove")
    suspend fun removeClientsFromGroup(@Body emails: List<String>): Response<GenericResponse>

    // HWID & IP Tracking
    @POST("panel/api/clients/ips/{email}")
    suspend fun getClientIps(@Path("email") email: String): Response<GenericResponse>

    @POST("panel/api/clients/clearIps/{email}")
    suspend fun clearClientIps(@Path("email") email: String): Response<GenericResponse>

    @POST("panel/api/clients/hwids/{email}")
    suspend fun listClientHwids(@Path("email") email: String): Response<GenericResponse>

    @DELETE("panel/api/clients/hwids/{email}/{id}")
    suspend fun deleteClientHwid(@Path("email") email: String, @Path("id") id: Int): Response<GenericResponse>

    // Bulk Adjust & Export/Import
    @POST("panel/api/clients/bulkAdjust")
    suspend fun bulkAdjustClients(@Body body: BulkAdjustRequest): Response<GenericResponse>

    @POST("panel/api/clients/delOrphans")
    suspend fun deleteOrphanClients(): Response<GenericResponse>

    @GET("panel/api/clients/export")
    suspend fun exportClients(): Response<GenericResponse>

    @POST("panel/api/clients/happLink/{id}")
    suspend fun getHappLink(@Path("id") id: Int): Response<GenericResponse>

    // Traffic reset
    @POST("panel/inbound/resetClientTraffic/{clientId}")
    suspend fun resetClientTraffic(
        @Path("clientId") clientId: String
    ): Response<GenericResponse>
}