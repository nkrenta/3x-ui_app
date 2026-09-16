package com.example.xuimanager.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xuimanager.data.model.PanelConnection
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DashboardScreen(
    connectionsViewModel: ConnectionsViewModel = viewModel()
) {
    val connections by connectionsViewModel.connections.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var editingConnection by remember { mutableStateOf<PanelConnection?>(null) }
    var viewingServerInfo by remember { mutableStateOf<PanelConnection?>(null) }
    var testConnectionId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        connectionsViewModel.testAllConnections(context)
        connectionsViewModel.startAutoPingLoop(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. ВЕРХНИЙ БАР: "Подключенные панели 3X-UI" + Кнопка "Добавить" + Кнопка "Обновить все"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringRes("connected_panels_title"),
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    stringRes("servers_subtitle"),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { connectionsViewModel.testAllConnections(context) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = stringRes("refresh_all"),
                        tint = AccentCyan
                    )
                }

                Button(
                    onClick = { editingConnection = null; showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringRes("add"), color = Color.White, fontSize = 12.sp)
                }
            }
        }

        // 2. Сводка активности серверов (Summary Bar)
        val totalServers = connections.size
        val onlineServers = connections.count { it.isConnected }
        val offlineServers = totalServers - onlineServers

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ServerSummaryItem(
                stringRes("total_servers"),
                "$totalServers",
                Icons.Default.Dns,
                AccentBlue,
                Modifier.weight(1f)
            )
            ServerSummaryItem(
                stringRes("online"),
                "$onlineServers",
                Icons.Default.CheckCircle,
                GreenStatus,
                Modifier.weight(1f)
            )
            ServerSummaryItem(
                stringRes("offline"),
                "$offlineServers",
                Icons.Default.Error,
                if (offlineServers > 0) RedStatus else TextSecondary,
                Modifier.weight(1f)
            )
        }

        // 3. Список серверов (Server Cards)
        if (connections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Dns,
                        contentDescription = "",
                        tint = TextSecondary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringRes("no_servers"),
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(stringRes("no_servers_sub"), color = TextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                connections.forEach { connection ->
                    StitchServerCard(
                        connection = connection,
                        isTesting = testConnectionId == connection.id,
                        onTest = {
                            testConnectionId = connection.id
                            connectionsViewModel.testConnection(context, connection) {
                                testConnectionId = null
                            }
                        },
                        onRestartPanel = {
                            connectionsViewModel.restartPanel(context, connection) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onRestartXray = {
                            connectionsViewModel.restartXrayService(context, connection) { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onEdit = {
                            editingConnection = connection
                            showAddDialog = true
                        },
                        onDelete = {
                            connectionsViewModel.deleteConnection(connection.id)
                        },
                        onClick = {
                            viewingServerInfo = connection
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ConnectionDialog(
            connection = editingConnection,
            onSave = { newConn ->
                if (editingConnection != null) {
                    connectionsViewModel.updateConnection(newConn)
                } else {
                    connectionsViewModel.addConnection(newConn)
                }
                showAddDialog = false
                editingConnection = null
            },
            onDismiss = {
                showAddDialog = false
                editingConnection = null
            }
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
fun ServerSummaryItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(52.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, DarkCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = "", tint = color, modifier = Modifier.size(18.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically)
            ) {
                Text(
                    title,
                    color = TextSecondary,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeight = 10.sp
                    )
                )
                Text(
                    value,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}

@Composable
fun PingBadge(pingMs: Int?) {
    val pingColor = when {
        pingMs == null -> TextSecondary
        pingMs in 0..150 -> GreenStatus
        pingMs in 151..500 -> Color(0xFFEAB308)
        else -> RedStatus
    }

    val pingText = if (pingMs != null) "$pingMs ms" else "--- ms"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "Пинг",
            tint = pingColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            pingText,
            color = pingColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StitchServerCard(
    connection: PanelConnection,
    isTesting: Boolean,
    onTest: () -> Unit,
    onRestartPanel: () -> Unit,
    onRestartXray: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Верхний уровень: Иконка облака + Имя сервера (без URL) + Статус онлайн
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Cloud,
                        contentDescription = "",
                        tint = AccentCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        connection.name,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                ConnectionStatusChip(isConnected = connection.isConnected)
            }

            HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

            // Нижний уровень: Индикатор пинга + Кнопки управления (Инфо удалено)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Индикатор Пинга с цветовой кодировкой (0-150 зелёный, 150-500 жёлтый, 500+ красный)
                PingBadge(pingMs = connection.pingMs)

                // Ряд кнопок управления (без кнопки Инфо)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButtonWithTooltip(
                        onClick = onTest,
                        tooltipText = stringRes("test_connection"),
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
                        tooltipText = stringRes("restart_panel"),
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
                        tooltipText = stringRes("restart_xray"),
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
                        tooltipText = stringRes("edit_connection"),
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
                        tooltipText = stringRes("delete_connection"),
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