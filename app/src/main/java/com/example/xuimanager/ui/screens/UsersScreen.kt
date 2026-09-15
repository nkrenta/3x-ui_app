package com.example.xuimanager.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xuimanager.data.api.model.ApiClient
import com.example.xuimanager.data.api.model.ClientSettingsItem
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.ui.theme.*
import com.example.xuimanager.ui.viewmodel.ConnectionsViewModel
import com.example.xuimanager.ui.viewmodel.UsersViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun UsersScreen(
    viewModel: UsersViewModel = viewModel(),
    connectionsViewModel: ConnectionsViewModel = viewModel()
) {
    val clients by viewModel.clients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val connections by connectionsViewModel.connections.collectAsState()
    val context = LocalContext.current

    var selectedConnection by remember { mutableStateOf<PanelConnection?>(connections.firstOrNull()) }
    var showServerMenu by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingClient by remember { mutableStateOf<ApiClient?>(null) }
    var qrContent by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(Unit) {
        connectionsViewModel.initPersistence(context)
    }

    LaunchedEffect(connections) {
        if (selectedConnection == null && connections.isNotEmpty()) {
            selectedConnection = connections.first()
        }
    }

    LaunchedEffect(selectedConnection) {
        selectedConnection?.let { conn ->
            viewModel.setConnection(conn, context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. ВЕРХНИЙ ЗАГОЛОВОК
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringRes("users"), color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Управление клиентами и подписками", color = TextSecondary, fontSize = 11.sp)
        }

        // 2. СЕКЦИЯ УПРАВЛЕНИЯ: Селектор серверов (слева) + Обновить и Добавить (справа)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Селектор серверов
            Box {
                Card(
                    onClick = { showServerMenu = true },
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                    border = BorderStroke(1.dp, DarkCardBorder),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Cloud, contentDescription = "", tint = AccentCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            selectedConnection?.name ?: "Выберите сервер",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                DropdownMenu(
                    expanded = showServerMenu,
                    onDismissRequest = { showServerMenu = false },
                    modifier = Modifier
                        .background(DarkCardBg)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                ) {
                    connections.forEach { conn ->
                        DropdownMenuItem(
                            text = { Text(conn.name, color = TextPrimary, fontSize = 13.sp) },
                            onClick = {
                                selectedConnection = conn
                                showServerMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Cloud, contentDescription = "", tint = if (selectedConnection?.id == conn.id) AccentCyan else TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                // Кнопка Обновить
                IconButton(
                    onClick = {
                        selectedConnection?.let { viewModel.loadAllClients(context) }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.size(36.dp)
                ) {
                    if (isLoading) {
                        Box(modifier = Modifier.size(18.dp)) {
                            CircularProgressIndicator(
                                color = AccentCyan,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить", tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                }

                // Кнопка Добавить клиента
                Button(
                    onClick = { editingClient = null; showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringRes("add"), color = Color.White, fontSize = 12.sp)
                }
            }
        }

        error?.let { err ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RedStatus.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "", tint = RedStatus, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(err, color = RedStatus, fontSize = 12.sp)
                }
            }
        }

        // 3. Список клиентов (Client Cards)
        if (clients.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "",
                        tint = TextSecondary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Нет зарегистрированных клиентов", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Нажмите '+ Добавить' для создания первого клиента", color = TextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(clients) { client ->
                    StitchClientCard(
                        client = client,
                        onEdit = { editingClient = client; showAddDialog = true },
                        onDelete = { viewModel.deleteClient(context, client.getIdAsString()) },
                        onToggle = { viewModel.toggleClient(context, client) },
                        onShowQR = {
                            val host = selectedConnection?.host ?: "server"
                            val port = selectedConnection?.port ?: 41667
                            val path = selectedConnection?.path ?: ""
                            val link = if (client.getEffectiveSubId().isNotBlank()) {
                                "https://$host:$port/$path/sub/${client.getEffectiveSubId()}"
                            } else {
                                "vless://${client.uuid ?: client.getIdAsString()}@$host:$port?security=reality#${client.email ?: "client"}"
                            }
                            qrContent = Pair(client.email ?: "Client", link)
                        },
                        onCopyUrl = {
                            val host = selectedConnection?.host ?: "server"
                            val port = selectedConnection?.port ?: 41667
                            val link = "vless://${client.uuid ?: client.getIdAsString()}@$host:$port?security=reality#${client.email ?: "client"}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Proxy URL", link))
                            Toast.makeText(context, "URL конфигурации скопирован", Toast.LENGTH_SHORT).show()
                        },
                        onCopySub = {
                            val subLink = client.getEffectiveSubId()
                            if (subLink.isNotBlank()) {
                                val host = selectedConnection?.host ?: "server"
                                val port = selectedConnection?.port ?: 41667
                                val path = selectedConnection?.path ?: ""
                                val fullSubUrl = "https://$host:$port/$path/sub/$subLink"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Sub URL", fullSubUrl))
                                Toast.makeText(context, "Ссылка подписки скопирована", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Sub ID отсутствует", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onResetTraffic = { viewModel.resetClientTraffic(context, client.getIdAsString()) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ClientDialog(
            client = editingClient,
            inboundId = 0,
            onSave = { newClient ->
                viewModel.addClient(context, newClient)
                showAddDialog = false
                editingClient = null
            },
            onDismiss = { showAddDialog = false; editingClient = null }
        )
    }

    qrContent?.let { (clientName, link) ->
        QrCodeDialog(
            title = "QR-код подписки: $clientName",
            content = link,
            onDismiss = { qrContent = null }
        )
    }
}

@Composable
fun StitchClientCard(
    client: ApiClient,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit,
    onShowQR: () -> Unit,
    onCopyUrl: () -> Unit,
    onCopySub: () -> Unit,
    onResetTraffic: () -> Unit
) {
    val totalUsedBytes = client.getUpTraffic() + client.getDownTraffic()
    val limitBytes = client.getTotalTrafficLimit()
    val trafficPercent = if (limitBytes > 0) ((totalUsedBytes.toDouble() / limitBytes.toDouble()) * 100).toInt().coerceIn(0, 100) else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Верхний уровень: Аватар пользователя + Имя/Email + Статус
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(AccentBlue.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "", tint = AccentCyan, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            client.email ?: client.comment ?: "Client",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                        client.uuid?.let { u ->
                            Text(
                                "UUID: ${u.take(12)}...",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Статус
                Box(
                    modifier = Modifier
                        .background(
                            if (client.enable) GreenStatus.copy(alpha = 0.15f) else RedStatus.copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (client.enable) GreenStatus.copy(alpha = 0.4f) else RedStatus.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (client.enable) GreenStatus else RedStatus, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (client.enable) stringRes("active") else stringRes("disabled"),
                            color = if (client.enable) GreenStatus else RedStatus,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                }
            }

            HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

            // 2. Индикатор Трафика (Шкала и счетчики с отступом 2-3 dp)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Использовано трафика",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                    Text(
                        "${formatBytes(totalUsedBytes)} / ${if (limitBytes > 0) formatBytes(limitBytes) else "Безлимит"}",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                }

                LinearProgressIndicator(
                    progress = { if (limitBytes > 0) (trafficPercent / 100f) else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = if (trafficPercent > 85) RedStatus else AccentCyan,
                    trackColor = DarkCardBorder
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "↑ ${formatBytes(client.getUpTraffic())} (Отдано)",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                    Text(
                        "↓ ${formatBytes(client.getDownTraffic())} (Загружено)",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                }
            }

            HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

            // 3. Дополнительные параметры (Sub ID + Срок действия c отступом 2-3 dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sub ID
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Sub ID",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                    Text(
                        if (client.getEffectiveSubId().isNotBlank()) client.getEffectiveSubId() else "—",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                }

                // Срок действия
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Срок действия",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                    Text(
                        formatDate(client.getEffectiveExpiryTime()),
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                }
            }

            HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

            // 4. Кнопки управления (QR, URL, Sub, Сброс, Настройки, Удалить)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = client.enable,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentBlue,
                            checkedTrackColor = AccentBlue.copy(alpha = 0.5f),
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkCardBorder
                        ),
                        modifier = Modifier.scale(0.85f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (client.enable) "Вкл" else "Выкл", color = TextSecondary, fontSize = 10.sp)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. QR-код
                    IconButtonWithTooltip(
                        onClick = onShowQR,
                        tooltipText = "Показать QR-код",
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR", tint = AccentCyan, modifier = Modifier.size(17.dp))
                    }

                    // 2. URL конфигурация
                    IconButtonWithTooltip(
                        onClick = onCopyUrl,
                        tooltipText = "Скопировать URL конфигурации",
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = "URL", tint = AccentCyan, modifier = Modifier.size(17.dp))
                    }

                    // 3. Sub подписка
                    IconButtonWithTooltip(
                        onClick = onCopySub,
                        tooltipText = "Скопировать ссылку Sub",
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Sub", tint = AccentBlue, modifier = Modifier.size(17.dp))
                    }

                    // 4. Сброс трафика
                    IconButtonWithTooltip(
                        onClick = onResetTraffic,
                        tooltipText = "Сбросить трафик",
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Сброс", tint = AccentBlue, modifier = Modifier.size(17.dp))
                    }

                    // 5. Настройки редактирования
                    IconButtonWithTooltip(
                        onClick = onEdit,
                        tooltipText = "Дополнительные настройки",
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Настройки", tint = TextSecondary, modifier = Modifier.size(17.dp))
                    }

                    // 6. Удалить
                    IconButtonWithTooltip(
                        onClick = onDelete,
                        tooltipText = "Удалить клиента",
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = RedStatus, modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QrCodeDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val qrBitmap = remember(content) { generateQrCodeBitmap(content) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                qrBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .size(200.dp)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                    )
                }
                Text(
                    content,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Link", content))
                    Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Скопировать", color = Color.White, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = TextSecondary, fontSize = 12.sp)
            }
        }
    )
}

fun generateQrCodeBitmap(content: String, size: Int = 512): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        bitmap
    } catch (_: Exception) {
        null
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ClientDialog(
    client: ApiClient?,
    inboundId: Int,
    onSave: (ClientSettingsItem) -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf(client?.email ?: "") }
    var id by remember { mutableStateOf(client?.getIdAsString() ?: UUID.randomUUID().toString()) }
    var flow by remember { mutableStateOf(client?.flow ?: "xtls-rprx-vision") }
    var totalGb by remember { mutableStateOf((client?.getTotalTrafficLimit() ?: 0L) / 1024 / 1024 / 1024) }
    var expiryTime by remember { mutableStateOf(client?.getEffectiveExpiryTime() ?: 0L) }
    var enable by remember { mutableStateOf(client?.enable ?: true) }
    var limitIp by remember { mutableStateOf(client?.limitIp ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (client != null) "Дополнительные настройки" else "Новый клиент",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email / Имя клиента", color = TextSecondary, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = flow,
                        onValueChange = { flow = it },
                        label = { Text("Flow", color = TextSecondary, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkCardBg,
                            unfocusedContainerColor = DarkCardBg,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = limitIp.toString(),
                        onValueChange = { limitIp = it.toIntOrNull() ?: 0 },
                        label = { Text("Лимит IP", color = TextSecondary, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkCardBg,
                            unfocusedContainerColor = DarkCardBg,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = totalGb.toString(),
                    onValueChange = { totalGb = it.toLongOrNull() ?: 0 },
                    label = { Text("Лимит трафика (ГБ, 0 = безлимит)", color = TextSecondary, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = expiryTime.toString(),
                    onValueChange = { expiryTime = it.toLongOrNull() ?: 0 },
                    label = {
                        Text(
                            "Срок действия (ms, 0 = бессрочно)",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Активен", color = TextPrimary, fontSize = 13.sp)
                    Switch(
                        checked = enable,
                        onCheckedChange = { enable = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentBlue,
                            checkedTrackColor = AccentBlue.copy(alpha = 0.5f),
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkCardBorder
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        ClientSettingsItem(
                            id = id,
                            email = email,
                            flow = flow,
                            limitIp = limitIp,
                            totalGb = totalGb * 1024 * 1024 * 1024,
                            expiryTime = expiryTime,
                            enable = enable
                        )
                    )
                }
            ) {
                Text("Сохранить", color = AccentCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextSecondary)
            }
        }
    )
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes <= 0 -> "0 B"
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(Locale.US, "%.2f KB", bytes.toDouble() / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.2f MB", bytes.toDouble() / (1024.0 * 1024.0))
        else -> String.format(Locale.US, "%.2f GB", bytes.toDouble() / (1024.0 * 1024.0 * 1024.0))
    }
}

private fun formatDate(timestampMs: Long): String {
    if (timestampMs <= 0) return "Безлимитно"
    val date = Date(timestampMs)
    return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)
}