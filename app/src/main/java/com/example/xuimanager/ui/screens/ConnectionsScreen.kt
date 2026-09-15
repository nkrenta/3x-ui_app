package com.example.xuimanager.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xuimanager.data.api.model.PanelInfo
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.data.repository.PanelRepository
import com.example.xuimanager.ui.theme.AccentBlue
import com.example.xuimanager.ui.theme.AccentCyan
import com.example.xuimanager.ui.theme.DarkBackground
import com.example.xuimanager.ui.theme.DarkCardBg
import com.example.xuimanager.ui.theme.DarkCardBorder
import com.example.xuimanager.ui.theme.GreenStatus
import com.example.xuimanager.ui.theme.RedStatus
import com.example.xuimanager.ui.theme.TextPrimary
import com.example.xuimanager.ui.theme.TextSecondary
import com.example.xuimanager.ui.theme.stringRes
import com.example.xuimanager.ui.viewmodel.ConnectionsViewModel
import java.util.Locale
import java.util.UUID

@Composable
fun ConnectionsScreen(viewModel: ConnectionsViewModel = viewModel()) {
    val connections by viewModel.connections.collectAsState()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingConnection by remember { mutableStateOf<PanelConnection?>(null) }
    var viewingServerInfo by remember { mutableStateOf<PanelConnection?>(null) }
    var testConnectionId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Подключения к 3x-ui", color = TextPrimary, fontSize = 20.sp)
            FloatingActionButton(
                onClick = { editingConnection = null; showAddDialog = true },
                containerColor = AccentBlue
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (connections.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Cloud,
                        contentDescription = "",
                        tint = TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Нет подключений", color = TextPrimary, fontSize = 18.sp)
                    Text(
                        "Добавьте панель 3x-ui для начала работы",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(connections) { connection ->
                    ConnectionCard(
                        connection = connection,
                        isTesting = testConnectionId == connection.id,
                        onEdit = { editingConnection = connection; showAddDialog = true },
                        onDelete = { viewModel.deleteConnection(connection.id) },
                        onTest = {
                            testConnectionId = connection.id
                            viewModel.testConnection(context, connection) {
                                testConnectionId = null
                            }
                        },
                        onRestartPanel = {
                            viewModel.restartPanel(context, connection) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onRestartXray = {
                            viewModel.restartXrayService(context, connection) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onClick = { viewingServerInfo = connection }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ConnectionDialog(
            connection = editingConnection,
            onSave = { newConnection ->
                if (editingConnection != null) {
                    viewModel.updateConnection(newConnection)
                } else {
                    viewModel.addConnection(newConnection)
                }
                showAddDialog = false
                editingConnection = null
            },
            onDismiss = { showAddDialog = false; editingConnection = null }
        )
    }

    viewingServerInfo?.let { connection ->
        ServerDetailsDialog(
            connection = connection,
            onDismiss = { viewingServerInfo = null }
        )
    }
}

@Composable
fun IconButtonWithTooltip(
    onClick: () -> Unit,
    tooltipText: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = {
                            Toast.makeText(context, tooltipText, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun ConnectionCard(
    connection: PanelConnection,
    isTesting: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit,
    onRestartPanel: () -> Unit = {},
    onRestartXray: () -> Unit = {},
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Cloud,
                    contentDescription = "",
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        connection.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    connection.xrayVersion?.let { ver ->
                        Text("Xray: $ver", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConnectionStatusChip(isConnected = connection.isConnected)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButtonWithTooltip(
                        onClick = onTest,
                        tooltipText = "Проверить подключение",
                        enabled = !isTesting,
                        modifier = Modifier.size(32.dp)
                    ) {
                        if (isTesting) {
                            Box(modifier = Modifier.size(16.dp)) {
                                CircularProgressIndicator(
                                    color = AccentCyan,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        } else {
                            Icon(
                                Icons.Default.Wifi,
                                contentDescription = "Проверить",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButtonWithTooltip(
                        onClick = onRestartPanel,
                        tooltipText = "Перезапуск панели",
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Перезапуск панели",
                            tint = AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButtonWithTooltip(
                        onClick = onRestartXray,
                        tooltipText = "Перезапуск Xray",
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Memory,
                            contentDescription = "Перезапуск Xray",
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButtonWithTooltip(
                        onClick = onEdit,
                        tooltipText = "Редактировать подключение",
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Редактировать",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButtonWithTooltip(
                        onClick = onDelete,
                        tooltipText = "Удалить подключение",
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = RedStatus,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusChip(isConnected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaPulse"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .background(
                if (isConnected) GreenStatus.copy(alpha = 0.2f) else RedStatus.copy(alpha = 0.2f),
                RoundedCornerShape(10.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = (if (isConnected) GreenStatus else RedStatus).copy(alpha = if (isConnected) alphaPulse else 1.0f),
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (isConnected) stringRes("online") else stringRes("offline"),
                color = if (isConnected) GreenStatus else RedStatus,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ServerDetailsDialog(
    connection: PanelConnection,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var panelInfo by remember { mutableStateOf<PanelInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(connection) {
        val repo = PanelRepository()
        repo.getPanelInfo(context, connection)
            .onSuccess { info ->
                panelInfo = info
                isLoading = false
            }
            .onFailure { e ->
                errorMsg = e.localizedMessage ?: "Ошибка получения статуса сервера"
                isLoading = false
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(0.92f),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 0.dp, bottom = 0.dp)
            ) {
                Icon(
                    Icons.Default.Cloud,
                    contentDescription = "",
                    tint = AccentCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    stringRes("server_info"),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    connection.name,
                    color = AccentCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AccentCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else if (errorMsg != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = RedStatus.copy(alpha = 0.2f))
                    ) {
                        Text(
                            errorMsg!!,
                            color = RedStatus,
                            modifier = Modifier.padding(10.dp),
                            fontSize = 11.sp
                        )
                    }
                } else {
                    panelInfo?.let { info ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // РЯД 1: Столбец 1 (Параметры CPU) | Столбец 2 (Память и Диск)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Столбец 1: Параметры CPU
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        stringRes("cpu_params"),
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    ServerCompactRow(
                                        stringRes("cores_threads"),
                                        "${info.cpuCores} / ${info.logicalPro}"
                                    )
                                    ServerCompactRow(
                                        stringRes("cpu_frequency"),
                                        "${info.cpuSpeedMhz.toInt()} MHz"
                                    )
                                }

                                // Столбец 2: Память и Диск (только общий объем)
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        stringRes("ram_disk"),
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    ServerCompactRow(
                                        stringRes("ram_memory"),
                                        formatBytesExact(info.mem?.total ?: 0)
                                    )
                                    ServerCompactRow(
                                        stringRes("disk_storage"),
                                        formatBytesExact(info.disk?.total ?: 0)
                                    )
                                }
                            }

                            HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

                            // РЯД 2: Столбец 3 (Xray) | Столбец 4 (Панель)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Столбец 3: Xray
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        stringRes("xray_title"),
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    ServerCompactRow(
                                        stringRes("xray_version"),
                                        info.xray?.version ?: info.xrayVersionLegacy ?: "N/A"
                                    )
                                    ServerCompactRow(
                                        stringRes("uptime_xray"),
                                        formatUptime(info.appStats?.uptime ?: info.uptime)
                                    )
                                }

                                // Столбец 4: Панель
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        stringRes("panel_title"),
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    ServerCompactRow(
                                        stringRes("panel_version"),
                                        info.panelVersion ?: info.panelVersionLegacy ?: "3.x.x"
                                    )
                                    ServerCompactRow(
                                        stringRes("uptime_panel"),
                                        formatUptime(info.uptime)
                                    )
                                }
                            }

                            HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

                            // РЯД 3: Столбец 5 (Сеть и трафик — Публичный IPv4 | Трафик ↑/↓ | СРЕДНЕЕ ЗА ПЕРИОД ↑/↓)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    stringRes("net_traffic"),
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(
                                        16.dp,
                                        Alignment.Start
                                    ),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // 1. Публичный IPv4
                                    ServerCompactRow(
                                        label = stringRes("public_ip"),
                                        value = info.publicIP?.ipv4 ?: connection.host
                                    )

                                    // 2. Трафик (Отправлено ↑ сверху, Получено ↓ снизу)
                                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                        Text(
                                            stringRes("traffic"),
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            style = TextStyle(
                                                platformStyle = PlatformTextStyle(
                                                    includeFontPadding = false
                                                ), lineHeight = 11.sp
                                            )
                                        )
                                        Text(
                                            "↑ ${formatBytesExact(info.netTraffic?.sent ?: 0)}",
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            style = TextStyle(
                                                platformStyle = PlatformTextStyle(
                                                    includeFontPadding = false
                                                ), lineHeight = 12.sp
                                            )
                                        )
                                        Text(
                                            "↓ ${formatBytesExact(info.netTraffic?.recv ?: 0)}",
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            style = TextStyle(
                                                platformStyle = PlatformTextStyle(
                                                    includeFontPadding = false
                                                ), lineHeight = 12.sp
                                            )
                                        )
                                    }

                                    // 3. СРЕДНЕЕ ЗА ПЕРИОД (Отправлено ↑ сверху, Получено ↓ снизу)
                                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                        Text(
                                            stringRes("avg_period"),
                                            color = TextSecondary,
                                            fontSize = 10.sp,
                                            style = TextStyle(
                                                platformStyle = PlatformTextStyle(
                                                    includeFontPadding = false
                                                ), lineHeight = 11.sp
                                            )
                                        )
                                        Text(
                                            "↑ ${formatSpeedKbTwoDecimals(info.netIO?.up ?: 0)}",
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            style = TextStyle(
                                                platformStyle = PlatformTextStyle(
                                                    includeFontPadding = false
                                                ), lineHeight = 12.sp
                                            )
                                        )
                                        Text(
                                            "↓ ${formatSpeedKbTwoDecimals(info.netIO?.down ?: 0)}",
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            style = TextStyle(
                                                platformStyle = PlatformTextStyle(
                                                    includeFontPadding = false
                                                ), lineHeight = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                modifier = Modifier.padding(bottom = 0.dp)
            ) {
                Text(stringRes("close"), color = AccentCyan, fontSize = 13.sp)
            }
        }
    )
}

@Composable
fun ServerCompactRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(
            label,
            color = TextSecondary,
            fontSize = 10.sp,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeight = 11.sp
            )
        )
        Text(
            value,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeight = 12.sp
            )
        )
    }
}

private fun formatBytesExact(bytes: Long): String {
    return when {
        bytes <= 0 -> "0 B"
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(Locale.US, "%.2f KB", bytes.toDouble() / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format(
            Locale.US,
            "%.2f MB",
            bytes.toDouble() / (1024.0 * 1024.0)
        )

        else -> String.format(Locale.US, "%.2f GB", bytes.toDouble() / (1024.0 * 1024.0 * 1024.0))
    }
}

private fun formatSpeedKbTwoDecimals(bytesPerSec: Long): String {
    val kbPerSec = bytesPerSec.toDouble() / 1024.0
    return String.format(Locale.US, "%.2f KB/s", kbPerSec)
}

private fun formatUptime(seconds: Long): String {
    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    val minutes = (seconds % 3600) / 60
    return if (days > 0) "${days}д ${hours}ч ${minutes}м" else "${hours}ч ${minutes}м"
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ConnectionDialog(
    connection: PanelConnection?,
    onSave: (PanelConnection) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(connection?.name ?: "") }
    var host by remember { mutableStateOf(connection?.host ?: "") }
    var portText by remember { mutableStateOf(connection?.port?.toString() ?: "2053") }
    var username by remember { mutableStateOf(connection?.username ?: "") }
    var password by remember { mutableStateOf(connection?.password ?: "") }
    var path by remember { mutableStateOf(connection?.path ?: "") }
    var token by remember { mutableStateOf(connection?.token ?: "") }
    var skipCertVerify by remember { mutableStateOf(connection?.skipCertVerify ?: true) }

    val isPortValid =
        portText.isNotBlank() && portText.toIntOrNull() != null && portText.toInt() in 1..65535

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .fillMaxWidth(0.94f),
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (connection != null) "Редактировать подключение" else "Новое подключение",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "SSL",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Switch(
                        checked = skipCertVerify,
                        onCheckedChange = { skipCertVerify = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentBlue,
                            checkedTrackColor = AccentBlue.copy(alpha = 0.5f),
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkCardBorder
                        ),
                        modifier = Modifier.scale(0.8f)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp, bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. Название (Опционально)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(
                            "Название (опционально)",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    placeholder = {
                        Text(
                            "Авто название: Страна + IPv4",
                            color = TextSecondary.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                    },
                    textStyle = TextStyle(fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. Хост и Порт в одной компактной строке
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = {
                            Text(
                                "Хост (IP / домен)",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        },
                        textStyle = TextStyle(fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkCardBg,
                            unfocusedContainerColor = DarkCardBg,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(2.2f)
                    )

                    OutlinedTextField(
                        value = portText,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                portText = input
                            }
                        },
                        label = {
                            Text(
                                "Порт",
                                color = if (isPortValid) TextSecondary else RedStatus,
                                fontSize = 13.sp
                            )
                        },
                        textStyle = TextStyle(fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkCardBg,
                            unfocusedContainerColor = DarkCardBg,
                            focusedLabelColor = if (isPortValid) AccentCyan else RedStatus,
                            unfocusedLabelColor = if (isPortValid) TextSecondary else RedStatus,
                            focusedBorderColor = if (isPortValid) AccentCyan else RedStatus,
                            unfocusedBorderColor = if (isPortValid) DarkCardBorder else RedStatus
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 3. URL-путь
                OutlinedTextField(
                    value = path,
                    onValueChange = { path = it },
                    label = {
                        Text(
                            "URL-путь (Web Base Path / Subfolder)",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    textStyle = TextStyle(fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. API Токен
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = {
                        Text(
                            "API Токен (опционально)",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    textStyle = TextStyle(fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. Логин и Пароль
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Логин", color = TextSecondary, fontSize = 13.sp) },
                        textStyle = TextStyle(fontSize = 14.sp),
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
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Пароль", color = TextSecondary, fontSize = 13.sp) },
                        textStyle = TextStyle(fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkCardBg,
                            unfocusedContainerColor = DarkCardBg,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = isPortValid && host.isNotBlank(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                onClick = {
                    val finalPort = portText.toIntOrNull() ?: 41667
                    val finalName = if (name.isBlank()) host else name
                    onSave(
                        PanelConnection(
                            id = connection?.id ?: UUID.randomUUID().toString(),
                            name = finalName,
                            host = host,
                            port = finalPort,
                            username = username,
                            password = password,
                            protocol = "https",
                            path = path,
                            token = token,
                            skipCertVerify = skipCertVerify,
                            isConnected = false
                        )
                    )
                }
            ) {
                Text(
                    "Сохранить",
                    color = if (isPortValid && host.isNotBlank()) AccentCyan else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) {
                Text("Отмена", color = TextSecondary, fontSize = 14.sp)
            }
        }
    )
}