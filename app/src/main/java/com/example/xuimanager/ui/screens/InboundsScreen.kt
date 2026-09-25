package com.example.xuimanager.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xuimanager.data.api.model.Inbound
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.ui.theme.*
import com.example.xuimanager.ui.viewmodel.ConnectionsViewModel
import com.example.xuimanager.ui.viewmodel.InboundsViewModel
import com.example.xuimanager.ui.viewmodel.JsonTemplateItem
import com.example.xuimanager.ui.viewmodel.TemplatesViewModel
import com.example.xuimanager.ui.viewmodel.UsersViewModel
import java.util.Locale

private fun formatBytesLocal(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(
        Locale.US,
        "%.1f %s",
        bytes / Math.pow(1024.0, digitGroups.toDouble()),
        units[digitGroups.coerceAtMost(units.size - 1)]
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun InboundsScreen(
    inboundsViewModel: InboundsViewModel = viewModel(),
    connectionsViewModel: ConnectionsViewModel = viewModel(),
    templatesViewModel: TemplatesViewModel = viewModel(),
    usersViewModel: UsersViewModel = viewModel(),
    onNavigateToUsers: () -> Unit = {}
) {
    val inbounds by inboundsViewModel.inbounds.collectAsState()
    val isLoading by inboundsViewModel.isLoading.collectAsState()
    val error by inboundsViewModel.error.collectAsState()

    val templates by templatesViewModel.templates.collectAsState()
    val isApplying by templatesViewModel.isApplying.collectAsState()
    val applyResult by templatesViewModel.applyResult.collectAsState()

    val connections by connectionsViewModel.connections.collectAsState()
    val context = LocalContext.current

    var selectedConnection by remember { mutableStateOf<PanelConnection?>(connections.firstOrNull()) }
    var showServerMenu by remember { mutableStateOf(false) }
    var showTemplatesMenu by remember { mutableStateOf(false) }
    var viewingInboundDetails by remember { mutableStateOf<Inbound?>(null) }
    var deletingInbound by remember { mutableStateOf<Inbound?>(null) }

    var editingConnection by remember { mutableStateOf<PanelConnection?>(null) }
    var showEditServerDialog by remember { mutableStateOf(false) }
    var selectedTemplateForTarget by remember { mutableStateOf<JsonTemplateItem?>(null) }

    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topContentPadding = statusBarTopPadding + 110.dp
    val bottomContentPadding = navBarBottomPadding + 80.dp

    LaunchedEffect(Unit) {
        connectionsViewModel.initPersistence(context)
    }

    LaunchedEffect(applyResult) {
        applyResult?.let { res ->
            Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
            selectedConnection?.let { inboundsViewModel.loadInbounds(context) }
        }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (inbounds.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topContentPadding, bottom = bottomContentPadding),
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
                        "Нажмите 'Шаблоны подключений' под шапкой для быстрого создания",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = topContentPadding,
                    bottom = bottomContentPadding
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                error?.let { err ->
                    item {
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
                }

                items(inbounds) { inbound ->
                    InboundCard(
                        inbound = inbound,
                        onToggle = {
                            inboundsViewModel.toggleInbound(context, inbound)
                        },
                        onDeleteInbound = {
                            deletingInbound = inbound
                        },
                        onClick = { viewingInboundDetails = inbound }
                    )
                }
            }
        }

        // 2. НЕПОДВИЖНАЯ ПЛАВАЮЩАЯ ШАПКА ВВЕРХУ + ПОЛНОШИРОКАЯ КАПСУЛА ШАБЛОНОВ ПОД НЕЙ
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // ФИКСИРОВАННАЯ ВЕРХНЯЯ СТЕКЛЯННАЯ КАПСУЛА (52.dp)
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
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Заголовок вкладки
                    Text(
                        stringRes("inbounds"),
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Селектор серверов & Кнопка обновления
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Селектор серверов (прозрачный)
                        Box {
                            Card(
                                onClick = { showServerMenu = true },
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                border = null,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 0.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Cloud,
                                        contentDescription = "",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        selectedConnection?.name ?: "Выберите сервер",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(1.dp))
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
                                        text = {
                                            Text(
                                                conn.name,
                                                color = TextPrimary,
                                                fontSize = 13.sp
                                            )
                                        },
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
                            modifier = Modifier.size(20.dp)
                        ) {
                            if (isLoading) {
                                Box(modifier = Modifier.size(16.dp)) {
                                    CircularProgressIndicator(
                                        color = AccentCyan,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Обновить",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ПОЛНОШИРОКАЯ КАПСУЛА ШАБЛОНОВ СТРОГО ВО ВСЮ ДЛИНУ ШАПКИ
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(16.dp),
                        clip = false,
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showTemplatesMenu = !showTemplatesMenu },
                color = DarkCardBg.copy(alpha = 0.92f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CellTower,
                                contentDescription = "",
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "Шаблоны подключений",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Icon(
                            if (showTemplatesMenu) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Развернуть шаблоны",
                            tint = AccentCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // ВЫДВИЖНОЙ БЛОК С КАРТОЧКАМИ ШАБЛОНОВ
                    AnimatedVisibility(
                        visible = showTemplatesMenu,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Быстрое создание из шаблонов",
                                    color = PrimaryContainer,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isApplying) {
                                    CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                }
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(templates) { tmpl ->
                                    TemplateQuickChip(
                                        template = tmpl,
                                        isApplying = isApplying,
                                        onCreate = {
                                            selectedTemplateForTarget = tmpl
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditServerDialog && editingConnection != null) {
        ConnectionDialog(
            connection = editingConnection,
            onSave = { newConn ->
                connectionsViewModel.updateConnection(newConn, context)
                showEditServerDialog = false
                editingConnection = null
            },
            onDismiss = {
                showEditServerDialog = false
                editingConnection = null
            }
        )
    }

    selectedTemplateForTarget?.let { template ->
        TargetSelectDialog(
            connections = connections,
            onConfirmTarget = { target, targetConnection ->
                templatesViewModel.applyRealityTemplateWithTarget(
                    context,
                    targetConnection,
                    template,
                    target
                )
                selectedTemplateForTarget = null
            },
            onDismiss = { selectedTemplateForTarget = null }
        )
    }

    deletingInbound?.let { inbound ->
        AlertDialog(
            onDismissRequest = { deletingInbound = null },
            title = {
                Text(
                    "Удаление подключения",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Вы действительно хотите удалить подключение #${inbound.id} (${inbound.remark ?: inbound.getSecurityType()}) с сервера 3x-ui?",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        inboundsViewModel.deleteInbound(context, inbound) { success ->
                            if (success) {
                                Toast.makeText(context, "Подключение #${inbound.id} удалено", Toast.LENGTH_SHORT).show()
                            }
                            deletingInbound = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedStatus),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Удалить", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingInbound = null }) {
                    Text("Отмена", color = TextSecondary)
                }
            }
        )
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
private fun TemplateQuickChip(
    template: JsonTemplateItem,
    isApplying: Boolean,
    onCreate: () -> Unit
) {
    val cleanName = template.name.replace(Regex("""^Подключение\s*№\d+:\s*"""), "").trim()

    Surface(
        color = SurfaceContainerHigh,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.width(150.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CellTower,
                        contentDescription = "",
                        tint = AccentCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        cleanName,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                }

                Text(
                    "${template.protocol.uppercase()} • ${template.network.uppercase()}",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = GeistMonoFontFamily,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
            }

            Button(
                onClick = onCreate,
                enabled = !isApplying,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryContainer,
                    contentColor = OnPrimary
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.fillMaxWidth().height(26.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "", modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(2.dp))
                Text("Создать", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InboundDetailsDialog(
    inbound: Inbound,
    onSelectClient: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        inbound.remark ?: "Inbound #${inbound.id}",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "ID: #${inbound.id} • Tag: ${inbound.tag ?: "default"}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            if (inbound.enable) GreenStatus.copy(alpha = 0.2f) else RedStatus.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (inbound.enable) "Активен" else "Выключен",
                        color = if (inbound.enable) GreenStatus else RedStatus,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Основные параметры
                Surface(
                    color = SurfaceContainerLow,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DetailParamRow("Протокол", inbound.protocol?.uppercase() ?: "VLESS")
                        DetailParamRow("Порт", "${inbound.port}")
                        DetailParamRow("Поток / Сеть", inbound.getNetworkType().uppercase())
                        DetailParamRow("Безопасность", inbound.getSecurityType().uppercase())
                        DetailParamRow("Маскировка (Target)", inbound.getRealityTarget())

                        val pubKey = inbound.getRealityPublicKey()
                        if (pubKey.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Public Key", color = TextSecondary, fontSize = 11.sp)
                                    Text(
                                        pubKey,
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = GeistMonoFontFamily,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Public Key", pubKey))
                                        Toast.makeText(context, "Public Key скопирован", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "", tint = AccentCyan, modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        val shortIds = inbound.getRealityShortIds()
                        if (shortIds != "—") {
                            DetailParamRow("Short IDs", shortIds)
                        }
                    }
                }

                // Статистика Трафика
                Surface(
                    color = SurfaceContainerLow,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Отгрузка (Up)", color = TextSecondary, fontSize = 10.sp)
                            Text(
                                "▲ ${formatBytesLocal(inbound.up)}",
                                color = GreenStatus,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Загрузка (Down)", color = TextSecondary, fontSize = 10.sp)
                            Text(
                                "▼ ${formatBytesLocal(inbound.down)}",
                                color = AccentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Лимит", color = TextSecondary, fontSize = 10.sp)
                            Text(
                                if (inbound.total > 0) formatBytesLocal(inbound.total) else "∞",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Список клиентов
                if (!inbound.clientStats.isNullOrEmpty()) {
                    Text(
                        "Клиенты (${inbound.clientStats.size}):",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        inbound.clientStats.forEach { client ->
                            val emailStr = client.email ?: "client"
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SurfaceContainerHigh,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectClient(emailStr) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            emailStr,
                                            color = AccentCyan,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "▲ ${formatBytesLocal(client.up)} | ▼ ${
                                                formatBytesLocal(
                                                    client.down
                                                )
                                            }", color = TextSecondary, fontSize = 10.sp
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = "",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = AccentCyan, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun DetailParamRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Text(
            value,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun InboundCard(
    inbound: Inbound,
    onToggle: () -> Unit = {},
    onDeleteInbound: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val isEnable = inbound.enable
    val secRaw = inbound.getSecurityType().uppercase()
    val secDisplay = when {
        secRaw == "REALITY" -> "REALITY"
        secRaw == "TLS" -> "TLS"
        else -> "NONE"
    }

    val (secTextColor, secBgColor) = when (secDisplay) {
        "REALITY" -> Pair(
            if (isEnable) PrimaryFixed else TextSecondary,
            SurfaceContainerHighest
        )

        "TLS" -> Pair(
            if (isEnable) YellowStatus else YellowStatus.copy(alpha = 0.6f),
            YellowStatus.copy(alpha = 0.18f)
        )

        else -> Pair(
            if (isEnable) RedStatus else RedStatus.copy(alpha = 0.6f),
            RedStatus.copy(alpha = 0.18f)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isEnable) PrimaryContainer.copy(alpha = 0.3f) else DarkCardBorder,
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(animationSpec = tween(300, easing = FastOutSlowInEasing)),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // ШАПКА: Точка + Заголовок + Бейдж + Подзаголовок + Кнопка удаления + Переключатель + Меню
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    if (isEnable) TertiaryFixedDim else RedStatus,
                                    CircleShape
                                )
                        )

                        val remarkStr = inbound.remark?.takeIf { it.isNotBlank() } ?: secDisplay
                        val networkStr = inbound.getNetworkType().uppercase()
                        val cardTitleText = "#${inbound.id} $remarkStr"

                        Text(
                            cardTitleText,
                            color = Primary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .background(secBgColor, RoundedCornerShape(12.dp))
                                .border(
                                    0.5.dp,
                                    secTextColor.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                secDisplay,
                                color = secTextColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.WifiTethering,
                            contentDescription = "",
                            tint = if (isEnable) TertiaryFixedDim else TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            if (isEnable) "Active • $secDisplay Inbound Gateway" else "Disabled • Gateway Offline",
                            color = OnSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Кнопка удаления инбаунда с сервера
                    IconButton(
                        onClick = onDeleteInbound,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить подключение",
                            tint = RedStatus,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Switch(
                        checked = isEnable,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GreenStatus,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkCardBorder
                        ),
                        modifier = Modifier.scale(0.75f)
                    )

                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Детали",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // РАЗВОРАЧИВАЕМЫЙ БЛОК
            AnimatedVisibility(
                visible = isEnable,
                enter = expandVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeIn(tween(250)),
                exit = shrinkVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(200))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceContainerLowest.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ПОТОК", color = Outline, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                inbound.getNetworkType().uppercase(),
                                color = Primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(30.dp)
                                .background(OutlineVariant.copy(alpha = 0.3f))
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ПРОТОКОЛ", color = Outline, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                inbound.protocol?.uppercase() ?: "VLESS",
                                color = PrimaryFixed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(30.dp)
                                .background(OutlineVariant.copy(alpha = 0.3f))
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ПОРТ", color = Outline, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${inbound.port}",
                                color = Primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(30.dp)
                                .background(OutlineVariant.copy(alpha = 0.3f))
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("КЛИЕНТОВ", color = Outline, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${inbound.clientStats?.size ?: 0}",
                                color = TertiaryFixedDim,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceContainerLowest.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .border(1.dp, OutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = "",
                                tint = PrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Маскировка (Target):",
                                color = OnSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                inbound.getRealityTarget(),
                                color = OnSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Target", inbound.getRealityTarget()))
                                Toast.makeText(context, "Target скопирован", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Скопировать Target",
                                tint = Outline,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}