package com.example.xuimanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xuimanager.data.api.model.Inbound
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.ui.theme.AccentCyan
import com.example.xuimanager.ui.theme.DarkBackground
import com.example.xuimanager.ui.theme.DarkCardBg
import com.example.xuimanager.ui.theme.DarkCardBorder
import com.example.xuimanager.ui.theme.GeistFontFamily
import com.example.xuimanager.ui.theme.GeistMonoFontFamily
import com.example.xuimanager.ui.theme.GreenStatus
import com.example.xuimanager.ui.theme.RedStatus
import com.example.xuimanager.ui.theme.TextPrimary
import com.example.xuimanager.ui.theme.TextSecondary
import com.example.xuimanager.ui.theme.YellowStatus
import com.example.xuimanager.ui.theme.stringRes
import com.example.xuimanager.ui.viewmodel.ConnectionsViewModel
import com.example.xuimanager.ui.viewmodel.InboundsViewModel
import com.example.xuimanager.ui.viewmodel.UsersViewModel
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun InboundsScreen(
    inboundsViewModel: InboundsViewModel = viewModel(),
    connectionsViewModel: ConnectionsViewModel = viewModel(),
    usersViewModel: UsersViewModel = viewModel(),
    onNavigateToUsers: () -> Unit = {}
) {
    val inbounds by inboundsViewModel.inbounds.collectAsState()
    val isLoading by inboundsViewModel.isLoading.collectAsState()
    val error by inboundsViewModel.error.collectAsState()

    val connections by connectionsViewModel.connections.collectAsState()
    val context = LocalContext.current

    var selectedConnection by remember { mutableStateOf<PanelConnection?>(connections.firstOrNull()) }
    var showServerMenu by remember { mutableStateOf(false) }
    var viewingInboundDetails by remember { mutableStateOf<Inbound?>(null) }

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
            inboundsViewModel.setConnection(conn, context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. ВЕРХНИЙ БАР: Заголовок "Подключения" + Селектор серверов (без окантовки) + Кнопка Обновить
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringRes("inbounds"),
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Селектор серверов (без окантовки, цвет фона совпадает с фоном приложения)
                Box {
                    Card(
                        onClick = { showServerMenu = true },
                        colors = CardDefaults.cardColors(containerColor = DarkBackground),
                        border = null,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Cloud,
                                contentDescription = "",
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                selectedConnection?.name ?: "Выберите сервер",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
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
                                    Icon(
                                        Icons.Default.Cloud,
                                        contentDescription = "",
                                        tint = if (selectedConnection?.id == conn.id) AccentCyan else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                // Кнопка Обновить
                IconButton(
                    onClick = {
                        selectedConnection?.let { inboundsViewModel.loadInbounds(context) }
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
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Обновить",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        error?.let { err ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RedStatus.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "",
                        tint = RedStatus,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(err, color = RedStatus, fontSize = 12.sp)
                }
            }
        }

        // 2. Список подключений (Inbound Cards)
        if (inbounds.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CellTower,
                        contentDescription = "",
                        tint = TextSecondary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Нет входящих подключений",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Создайте первое подключение из вкладки 'Настройки' -> 'Шаблоны'",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(inbounds) { inbound ->
                    InboundCard(
                        inbound = inbound,
                        onClick = { viewingInboundDetails = inbound }
                    )
                }
            }
        }
    }

    viewingInboundDetails?.let { inbound ->
        InboundDetailsDialog(
            inbound = inbound,
            onSelectClient = { clientId ->
                usersViewModel.highlightClient(clientId)
                viewingInboundDetails = null
                onNavigateToUsers()
            },
            onDismiss = { viewingInboundDetails = null }
        )
    }
}

@Composable
fun InboundCard(
    inbound: Inbound,
    onClick: () -> Unit = {}
) {
    val secRaw = inbound.getSecurityType().uppercase()
    val secLabel = if (secRaw.isBlank() || secRaw == "NONE") "-" else secRaw
    val secBgColor = when (secLabel) {
        "REALITY" -> GreenStatus.copy(alpha = 0.2f)
        "TLS" -> YellowStatus.copy(alpha = 0.2f)
        else -> DarkCardBorder
    }
    val secTextColor = when (secLabel) {
        "REALITY" -> GreenStatus
        "TLS" -> YellowStatus
        else -> TextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // РЯД 1: Безопасность + Название + Статус
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Бейдж безопасности (REALITY - зеленый, TLS - желтый)
                    Box(
                        modifier = Modifier
                            .background(secBgColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            secLabel,
                            color = secTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        inbound.remark ?: "Inbound #${inbound.id}",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Бейдж статуса
                Box(
                    modifier = Modifier
                        .background(
                            if (inbound.enable) GreenStatus.copy(alpha = 0.15f) else RedStatus.copy(
                                alpha = 0.15f
                            ),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (inbound.enable) GreenStatus else RedStatus,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (inbound.enable) stringRes("active") else stringRes("disabled"),
                            color = if (inbound.enable) GreenStatus else RedStatus,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                        )
                    }
                }
            }

            HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

            // РЯД 2: Поток, Протокол, Порт, Клиентов
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InboundCompactParam(
                    "Поток",
                    inbound.getNetworkType().uppercase(),
                    Modifier.weight(1f)
                )
                InboundCompactParam(
                    "Протокол",
                    inbound.protocol?.uppercase() ?: "VLESS",
                    Modifier.weight(1f)
                )
                InboundCompactParam("Порт", "${inbound.port}", Modifier.weight(1f), isMono = true)
                InboundCompactParam(
                    "Клиентов",
                    "${inbound.clientStats?.size ?: 0}",
                    Modifier.weight(1f),
                    isMono = true
                )
            }

            HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

            // РЯД 3: Маскировка Target
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Маскировка (Target)",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                    Text(
                        inbound.getRealityTarget(),
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun InboundDetailsDialog(
    inbound: Inbound,
    onSelectClient: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val clientsList = inbound.getClientsListFromSettings()

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .fillMaxWidth(0.94f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CellTower,
                    contentDescription = "",
                    tint = AccentCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Информация о подключении",
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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    inbound.remark ?: "Inbound #${inbound.id}",
                    color = AccentCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                InboundDetailRow("ID подключения (id)", "${inbound.id}")
                InboundDetailRow("Порядковый номер (subSortIndex)", "${inbound.subSortIndex}")
                InboundDetailRow("Порт (port)", "${inbound.port}")
                InboundDetailRow("Протокол (protocol)", inbound.protocol?.uppercase() ?: "VLESS")
                InboundDetailRow(
                    "Статус доступа (enable)",
                    if (inbound.enable) "Активен (true)" else "Отключен (false)"
                )

                HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

                // 2. КЛИЕНТЫ (В виде интерактивных карточек)
                Text(
                    "Зарегистрированные клиенты (${clientsList.size})",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                if (clientsList.isEmpty()) {
                    Text(
                        "0 клиентов привязано к подключению",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        clientsList.forEach { client ->
                            Card(
                                onClick = { onSelectClient(client.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(containerColor = DarkBackground)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = "",
                                            tint = AccentCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                client.email,
                                                color = TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (client.id.isNotBlank()) {
                                                Text(
                                                    "ID: ${client.id.take(12)}...",
                                                    color = TextSecondary,
                                                    fontSize = 9.sp,
                                                    fontFamily = GeistMonoFontFamily
                                                )
                                            }
                                        }
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = "Перейти",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

                // 3. НАСТРОЙКИ ПОТОКА (В виде структурированных текстовых параметров)
                Text(
                    "Настройки потока (streamSettings)",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                InboundDetailRow("Тип сети (Network)", inbound.getNetworkType().uppercase())
                InboundDetailRow("Безопасность (Security)", inbound.getSecurityType().uppercase())
                InboundDetailRow("Маскировка (Target)", inbound.getRealityTarget())
                if (inbound.getRealityPublicKey().isNotBlank()) {
                    InboundDetailRow("Public Key", inbound.getRealityPublicKey())
                }
                if (inbound.getRealityPrivateKey().isNotBlank()) {
                    InboundDetailRow("Private Key", inbound.getRealityPrivateKey())
                }
                InboundDetailRow("Short IDs", inbound.getRealityShortIds())
                InboundDetailRow("SpiderX", inbound.getRealitySpiderX())
                InboundDetailRow("Fingerprint", inbound.getRealityFingerprint())

                HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

                // 4. СНИФФИНГ (Включен / Выключен)
                Text(
                    "Параметры сниффинга (sniffing)",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                InboundDetailRow(
                    "Статус сниффинга",
                    if (inbound.isSniffingEnabled()) "🟢 Включен (true)" else "🔴 Выключен (false)"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringRes("close"), color = AccentCyan, fontSize = 13.sp)
            }
        }
    )
}

@Composable
fun InboundDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = TextSecondary,
            fontSize = 10.sp,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            value,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = GeistMonoFontFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
        )
    }
}

@Composable
fun InboundCompactParam(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isMono: Boolean = false
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            label,
            color = TextSecondary,
            fontSize = 10.sp,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
        )
        Text(
            value,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = if (isMono) GeistMonoFontFamily else GeistFontFamily,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
        )
    }
}

private fun formatBytes(bytes: Long): String {
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