package com.example.xuimanager.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xuimanager.data.api.model.PanelInfo
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.ui.theme.*
import com.example.xuimanager.ui.viewmodel.ConnectionsViewModel

@Composable
fun DashboardScreen(
    connectionsViewModel: ConnectionsViewModel = viewModel(),
    selectedConnection: PanelConnection? = null
) {
    val connections by connectionsViewModel.connections.collectAsState()
    val context = LocalContext.current

    var editingConnection by remember { mutableStateOf<PanelConnection?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        connectionsViewModel.testAllConnections(context)
        connectionsViewModel.startAutoPingLoop(context)
    }

    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topContentPadding = statusBarTopPadding + 68.dp
    val bottomContentPadding = navBarBottomPadding + 96.dp

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        // 1. СПИСОК СЕРВЕРОВ
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 14.dp, 
                end = 14.dp, 
                top = topContentPadding, 
                bottom = bottomContentPadding
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(connections) { node ->
                ServerNodeCard(
                    connection = node,
                    isSelected = node.id == selectedConnection?.id,
                    serverStats = connectionsViewModel.serverStats.collectAsState().value,
                    onEditServer = {
                        editingConnection = node
                        showAddDialog = true
                    },
                    onDeleteServer = {
                        connectionsViewModel.deleteConnection(node.id, context)
                        Toast.makeText(context, "Сервер удален", Toast.LENGTH_SHORT).show()
                    },
                    onRestartXray = {
                        connectionsViewModel.restartXrayService(context, node) { _, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onRestartPanel = {
                        connectionsViewModel.restartPanel(context, node) { _, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onConsoleClick = {
                        Toast.makeText(context, "Консоль ${node.name}", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Кнопка Добавить сервер
            item {
                Button(
                    onClick = { editingConnection = null; showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryContainer,
                        contentColor = OnPrimary
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Добавить сервер", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. НЕПОДВИЖНАЯ ПЛАВАЮЩАЯ ШАПКА ВВЕРХУ (Без выпадающего меню)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(20.dp),
                        clip = false,
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    ),
                color = DarkCardBg.copy(alpha = 0.92f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringRes("dashboard"),
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Кнопка Обновить (28.dp x 28.dp)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                connectionsViewModel.testAllConnections(context)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Обновить",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
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
                        connectionsViewModel.updateConnection(newConn, context)
                    } else {
                        connectionsViewModel.addConnection(newConn, context)
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
    }
}

@Composable
fun ServerNodeCard(
    connection: PanelConnection,
    isSelected: Boolean,
    serverStats: Map<String, PanelInfo>,
    onEditServer: () -> Unit,
    onDeleteServer: () -> Unit,
    onRestartXray: () -> Unit,
    onRestartPanel: () -> Unit,
    onConsoleClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(isSelected) }
    var showCopyMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    fun copyText(label: String, text: String) {
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "$label скопирован", Toast.LENGTH_SHORT).show()
    }

    val info = serverStats[connection.id]
    val cpuValue = info?.cpu ?: 0.0
    val cpuPercent =
        if (cpuValue > 0.0 && cpuValue <= 1.0) (cpuValue * 100).toInt() else cpuValue.toInt()
    val cpuFormatted = "$cpuPercent%"

    val memTotal = info?.mem?.total ?: 1L
    val memCurrent = info?.mem?.current ?: 0L
    val memPercent =
        if (memTotal > 0) ((memCurrent.toDouble() / memTotal.toDouble()) * 100).toFloat() else 0f

    val diskTotal = info?.disk?.total ?: 1L
    val diskCurrent = info?.disk?.current ?: 0L
    val diskPercent =
        if (diskTotal > 0) ((diskCurrent.toDouble() / diskTotal.toDouble()) * 100).toFloat() else 0f

    // Удаляем флаг эмодзи из начала наименования для заголовка
    val cleanTitle = connection.name
        .replace(Regex("^[\\uD83C\\uDDE6-\\uD83C\\uDDFF]{2}\\s*"), "")
        .trim()

    val fullPath = connection.path.trim().trim('/')
    val formattedPath = if (fullPath.isNotBlank()) "/$fullPath" else ""
    val fullUrl = "${connection.protocol}://${connection.host}:${connection.port}$formattedPath"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainer)
            .clickable { isExpanded = !isExpanded }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(SurfaceContainerHigh, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val flag = if (connection.name.startsWith("🇫🇮")) "🇫🇮"
                    else if (connection.name.startsWith("🇩🇪")) "🇩🇪"
                    else if (connection.name.startsWith("🇺🇸")) "🇺🇸"
                    else if (connection.name.startsWith("🇳🇱")) "🇳🇱"
                    else "☁️"
                    Text(flag, fontSize = 18.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        cleanTitle,
                        color = Primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "${connection.host}:${connection.port}",
                            color = OnSurfaceVariant,
                            fontSize = 12.sp
                        )

                        Box {
                            IconButton(
                                onClick = {
                                    copyText("Адрес панели", fullUrl)
                                    showCopyMenu = true
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Копировать адрес",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showCopyMenu,
                                onDismissRequest = { showCopyMenu = false },
                                modifier = Modifier
                                    .background(DarkCardBg)
                                    .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
                                    .padding(0.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .width(230.dp)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        "Реквизиты панели 3X-UI",
                                        color = PrimaryContainer,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

                                    CredentialCopyRow(
                                        label = "URL Панели",
                                        value = fullUrl,
                                        onCopy = { copyText("URL Панели", fullUrl) }
                                    )

                                    val loginVal = connection.username.ifBlank { "—" }
                                    CredentialCopyRow(
                                        label = "Логин",
                                        value = loginVal,
                                        onCopy = { copyText("Логин", loginVal) }
                                    )

                                    val passVal = connection.password.ifBlank { "—" }
                                    CredentialCopyRow(
                                        label = "Пароль",
                                        value = passVal,
                                        onCopy = { copyText("Пароль", passVal) }
                                    )

                                    if (connection.token.isNotBlank()) {
                                        CredentialCopyRow(
                                            label = "API Token",
                                            value = connection.token,
                                            onCopy = { copyText("API Token", connection.token) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .background(
                        TertiaryContainer.copy(alpha = 0.15f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                if (connection.isConnected) TertiaryContainer else RedStatus,
                                CircleShape
                            )
                    )
                    Text(
                        "${connection.pingMs ?: "--"} ms",
                        color = if (connection.isConnected) TertiaryFixedDim else RedStatus,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Version Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainerLow, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = "",
                            tint = PrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "Xray Core ${info?.xray?.version ?: connection.xrayVersion ?: "v1.8.x"}",
                            color = OnSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                    Text("•", color = OutlineVariant)
                    Text(
                        "3x-ui v${info?.panelVersion ?: "2.4.x"}",
                        color = OnSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                // Resource Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(SurfaceContainerLow, RoundedCornerShape(8.dp))
                            .padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            cpuFormatted,
                            color = OnSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("CPU Load", color = OnSurfaceVariant, fontSize = 10.sp)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1.5f)
                            .background(SurfaceContainerLow, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("RAM", color = OnSurfaceVariant, fontSize = 10.sp)
                            Text(
                                "${memPercent.toInt()}%",
                                color = Primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { memPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .height(4.dp),
                            color = PrimaryContainer,
                            trackColor = SurfaceContainerHighest
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1.5f)
                            .background(SurfaceContainerLow, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("NVMe", color = OnSurfaceVariant, fontSize = 10.sp)
                            Text(
                                "${diskPercent.toInt()}%",
                                color = SecondaryFixedDim,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { diskPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .height(4.dp),
                            color = SecondaryFixedDim,
                            trackColor = SurfaceContainerHighest
                        )
                    }
                }

                // 5 КНОПОК БЕЗ ТЕКСТА
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ServerActionButton(
                        icon = Icons.Default.RestartAlt,
                        label = "Рестарт Xray",
                        containerColor = SurfaceContainerHigh,
                        contentColor = PrimaryContainer,
                        onClick = onRestartXray,
                        modifier = Modifier.weight(1f)
                    )

                    ServerActionButton(
                        icon = Icons.Default.Sync,
                        label = "Перезапуск панели",
                        containerColor = SurfaceContainerHigh,
                        contentColor = SecondaryFixedDim,
                        onClick = onRestartPanel,
                        modifier = Modifier.weight(1f)
                    )

                    ServerActionButton(
                        icon = Icons.Default.Terminal,
                        label = "Консоль",
                        containerColor = SecondaryContainer,
                        contentColor = OnSecondary,
                        onClick = onConsoleClick,
                        modifier = Modifier.weight(1f)
                    )

                    ServerActionButton(
                        icon = Icons.Default.Edit,
                        label = "Редактировать сервер",
                        containerColor = SurfaceContainerHigh,
                        contentColor = AccentCyan,
                        onClick = onEditServer,
                        modifier = Modifier.weight(1f)
                    )

                    ServerActionButton(
                        icon = Icons.Default.Delete,
                        label = "Удалить сервер",
                        containerColor = RedStatus.copy(alpha = 0.2f),
                        contentColor = RedStatus,
                        onClick = onDeleteServer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CredentialCopyRow(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextSecondary, fontSize = 10.sp)
            Text(
                value,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = GeistMonoFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        IconButton(
            onClick = onCopy,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = "Скопировать $label",
                tint = AccentCyan,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun ServerActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .pointerInput(label) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
    }
}