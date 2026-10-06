package com.example.xuimanager.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xuimanager.data.api.model.ApiClient
import com.example.xuimanager.data.api.model.ApiClientItem
import com.example.xuimanager.data.api.model.Inbound
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.data.repository.PanelRepository
import com.example.xuimanager.ui.theme.AccentCyan
import com.example.xuimanager.ui.theme.DarkBackground
import com.example.xuimanager.ui.theme.DarkCardBg
import com.example.xuimanager.ui.theme.DarkCardBorder
import com.example.xuimanager.ui.theme.GeistMonoFontFamily
import com.example.xuimanager.ui.theme.GreenStatus
import com.example.xuimanager.ui.theme.OnPrimary
import com.example.xuimanager.ui.theme.OnSurfaceVariant
import com.example.xuimanager.ui.theme.OutlineVariant
import com.example.xuimanager.ui.theme.Primary
import com.example.xuimanager.ui.theme.PrimaryContainer
import com.example.xuimanager.ui.theme.PrimaryFixed
import com.example.xuimanager.ui.theme.RedStatus
import com.example.xuimanager.ui.theme.SurfaceContainerHigh
import com.example.xuimanager.ui.theme.SurfaceContainerLow
import com.example.xuimanager.ui.theme.TextPrimary
import com.example.xuimanager.ui.theme.TextSecondary
import com.example.xuimanager.ui.theme.YellowStatus
import com.example.xuimanager.ui.theme.stringRes
import com.example.xuimanager.ui.viewmodel.ConnectionsViewModel
import com.example.xuimanager.ui.viewmodel.UsersViewModel
import com.google.gson.GsonBuilder
import java.util.Locale
import java.util.concurrent.TimeUnit

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

private fun formatExpiry(expiryTime: Long): String {
    if (expiryTime <= 0) return "Безлимит"
    val now = System.currentTimeMillis()
    val diff = expiryTime - now
    if (diff <= 0) return "Истёк"
    
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return if (days > 0) "через $days дн." else "сегодня"
}

private fun formatLastOnline(lastOnline: Long): String {
    if (lastOnline <= 0) return "Никогда"
    val diff = System.currentTimeMillis() - lastOnline
    
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    if (minutes < 3) return "Онлайн"
    if (minutes < 60) return "$minutes мин. назад"
    
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    if (hours < 24) return "$hours ч. назад"
    
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return "$days дн. назад"
}

private fun isOnline(lastOnline: Long): Boolean {
    if (lastOnline <= 0) return false
    return (System.currentTimeMillis() - lastOnline) < TimeUnit.MINUTES.toMillis(3)
}

@Composable
fun UsersScreen(
    viewModel: UsersViewModel = viewModel(),
    connectionsViewModel: ConnectionsViewModel = viewModel(),
    selectedConnection: PanelConnection? = null
) {
    val clients by viewModel.clients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val connections by connectionsViewModel.connections.collectAsState()
    val context = LocalContext.current

    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedClients by viewModel.selectedClients.collectAsState()

    var activeSelectedConnection by remember { mutableStateOf<PanelConnection?>(selectedConnection ?: connections.firstOrNull()) }
    var showServerMenu by remember { mutableStateOf(false) }
    var showAddClientDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedStatuses by remember { mutableStateOf<Set<ClientStatusBucket>>(emptySet()) }
    var sortOrder by remember { mutableStateOf(ClientSortOrder.TRAFFIC_DOWN) }

    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topContentPadding = statusBarTopPadding + 68.dp
    val bottomContentPadding = navBarBottomPadding + 96.dp

    BackHandler(enabled = isSelectionMode) {
        viewModel.exitSelection()
    }

    LaunchedEffect(Unit) {
        connectionsViewModel.initPersistence(context)
    }

    LaunchedEffect(connections) {
        if (activeSelectedConnection == null && connections.isNotEmpty()) {
            activeSelectedConnection = connections.first()
        }
    }

    LaunchedEffect(activeSelectedConnection) {
        activeSelectedConnection?.let { conn ->
            viewModel.setConnection(conn, context)
        }
    }

    val filteredClients = remember(clients, searchQuery, selectedStatuses, sortOrder) {
        val now = System.currentTimeMillis()
        clients.filter { client ->
            val matchesQuery = searchQuery.isBlank() ||
                (client.email?.contains(searchQuery, ignoreCase = true) == true) ||
                (client.id?.toString()?.contains(searchQuery, ignoreCase = true) == true)

            val matchesStatus = if (selectedStatuses.isEmpty()) true else {
                val isOnline = isOnline(client.traffic?.lastOnline ?: 0L)
                val isDepleted = (client.getEffectiveExpiryTime() in 1..now) ||
                        (client.getTotalTrafficLimit() in 1..(client.getUpTraffic() + client.getDownTraffic()))

                selectedStatuses.any { status ->
                    when (status) {
                        ClientStatusBucket.ONLINE -> isOnline
                        ClientStatusBucket.ACTIVE -> client.enable && !isDepleted
                        ClientStatusBucket.DISABLED -> !client.enable
                        ClientStatusBucket.DEPLETED -> isDepleted
                    }
                }
            }

            matchesQuery && matchesStatus
        }.let { list ->
            when (sortOrder) {
                ClientSortOrder.TRAFFIC_DOWN -> list.sortedByDescending { it.getUpTraffic() + it.getDownTraffic() }
                ClientSortOrder.LAST_ONLINE -> list.sortedByDescending { it.traffic?.lastOnline ?: 0L }
                ClientSortOrder.EMAIL_ASC -> list.sortedBy { it.email ?: "" }
                ClientSortOrder.EXPIRY -> list.sortedBy { if (it.getEffectiveExpiryTime() <= 0) Long.MAX_VALUE else it.getEffectiveExpiryTime() }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // 1. КОНТЕНТ ЭКРАНА (Скроллится под парящей стеклянной шапкой)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 14.dp,
                end = 14.dp,
                top = topContentPadding,
                bottom = bottomContentPadding
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Поиск пользователей
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Поиск по email или ID...", color = TextSecondary, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow,
                        focusedBorderColor = PrimaryContainer,
                        unfocusedBorderColor = OutlineVariant.copy(alpha = 0.4f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                )
            }

            // Заголовок списка пользователей и быстрая фильтрация
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Список пользователей",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    SurfaceContainerHigh,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            val activeCount = filteredClients.count { it.enable }
                            Text("$activeCount активны", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        "Фильтры / Сортировка ▼",
                        color = AccentCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showFilterSheet = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Список клиентов
            items(filteredClients) { client ->
                val isSelected = selectedClients.contains(client.email)
                ClientCardItem(
                    client = client,
                    viewModel = viewModel,
                    context = context,
                    activeSelectedConnection = activeSelectedConnection,
                    isSelected = isSelected,
                    isSelectionMode = isSelectionMode,
                    onClick = {
                        if (isSelectionMode) {
                            client.email?.let { viewModel.toggleSelection(it) }
                        }
                    },
                    onLongClick = {
                        if (!isSelectionMode) {
                            client.email?.let { viewModel.toggleSelection(it) }
                        }
                    }
                )
            }

            // Главная кнопка "+ Новое подключение"
            item {
                Button(
                    onClick = { showAddClientDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryContainer,
                        contentColor = OnPrimary
                    )
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
                                Icons.Default.PersonAdd,
                                contentDescription = "",
                                modifier = Modifier.size(22.dp)
                            )
                            Text("+ Новое подключение", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    OnPrimary.copy(alpha = 0.2f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "KEY-GEN",
                                color = OnPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. НЕПОДВИЖНАЯ ПЛАВАЮЩАЯ ШАПКА ВВЕРХУ (Fixed Floating Top Glass Bar)
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
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Левый блок: Заголовок "Клиенты" + Счётчик + Селектор серверов прямо правее!
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            stringRes("users"),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Box(
                            modifier = Modifier
                                .background(
                                    AccentCyan.copy(alpha = 0.15f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "${clients.size}",
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(35.dp))

                        // Селектор серверов (прямо правее от надписи "Клиенты")
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
                                        activeSelectedConnection?.name ?: "Выберите сервер",
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
                                            activeSelectedConnection = conn
                                            showServerMenu = false
                                            viewModel.setConnection(conn, context)
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Cloud,
                                                contentDescription = "",
                                                tint = if (activeSelectedConnection?.id == conn.id) AccentCyan else TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Правая секция: Только кнопка "Обновить" (28.dp x 28.dp)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = !isLoading) {
                                activeSelectedConnection?.let { viewModel.loadAllClients(context) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
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
        }

        // БАР МАССОВОГО УПРАВЛЕНИЯ КЛИЕНТАМИ (ПЛАВАЮЩИЙ СНИЗУ)
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = DarkCardBg,
                shadowElevation = 24.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                border = BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Выбрано: ${selectedClients.size}",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { viewModel.selectAllVisible(clients) }) {
                                Text("Выбрать все", color = AccentCyan)
                            }
                            TextButton(onClick = { viewModel.exitSelection() }) {
                                Text("Отмена", color = TextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { viewModel.bulkEnableSelected(context) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "", tint = GreenStatus)
                                Text("Вкл", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                        IconButton(onClick = { viewModel.bulkDisableSelected(context) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Pause, contentDescription = "", tint = YellowStatus)
                                Text("Пауза", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                        IconButton(onClick = { viewModel.bulkResetTrafficSelected(context) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Refresh, contentDescription = "", tint = AccentCyan)
                                Text("Сброс", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                        IconButton(onClick = { viewModel.bulkDeleteSelected(context) }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Delete, contentDescription = "", tint = RedStatus)
                                Text("Удалить", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // ШИТ ФИЛЬТРОВ И СОРТИРОВКИ
        if (showFilterSheet) {
            ClientFilterSheet(
                selectedStatuses = selectedStatuses,
                sortOrder = sortOrder,
                onApply = { statuses, order ->
                    selectedStatuses = statuses
                    sortOrder = order
                },
                onDismiss = { showFilterSheet = false }
            )
        }

        // ДИАЛОГ СОЗДАНИЯ / РЕДАКТИРОВАНИЯ ПОЛЬЗОВАТЕЛЯ (POST /panel/api/clients/add)
        if (showAddClientDialog && activeSelectedConnection != null) {
            ClientEditorDialog(
                connection = activeSelectedConnection!!,
                onConfirm = { clientItem, inboundIds ->
                    viewModel.addClientApi(context, clientItem, inboundIds) { success, errorMsg ->
                        if (success) {
                            Toast.makeText(context, "Пользователь ${clientItem.email} успешно сохранён!", Toast.LENGTH_SHORT).show()
                            showAddClientDialog = false
                        } else {
                            Toast.makeText(context, "Ошибка: ${errorMsg ?: "Не удалось сохранить"}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onDismiss = { showAddClientDialog = false }
            )
        }
    }
}

@Composable
fun ExportJsonDialog(
    client: ApiClient,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val jsonString = remember(client) {
        GsonBuilder().setPrettyPrinting().create().toJson(client)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("JSON конфигурация: ${client.email ?: "Client"}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = SurfaceContainerLow,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            jsonString,
                            color = AccentCyan,
                            fontSize = 11.sp,
                            fontFamily = GeistMonoFontFamily
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Client JSON", jsonString))
                    Toast.makeText(context, "JSON скопирован в буфер обмена", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = OnPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Скопировать JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = TextSecondary)
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClientCardItem(
    client: ApiClient,
    viewModel: UsersViewModel,
    context: Context,
    activeSelectedConnection: PanelConnection? = null,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showJsonDialog by remember { mutableStateOf(false) }
    var viewingSubUrl by remember { mutableStateOf<String?>(null) }
    var viewingLinks by remember { mutableStateOf<List<String>?>(null) }
    var isLoadingLinks by remember { mutableStateOf(false) }

    val clientEmail = client.email ?: "Client"

    val bgColor = when {
        isSelected -> PrimaryContainer.copy(alpha = 0.2f)
        else -> SurfaceContainerHigh
    }

    val borderColor = when {
        isSelected -> PrimaryContainer
        else -> DarkCardBorder
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            // Строка 1: Онлайн точка + Email + Свитч ВКЛ/ВЫКЛ (или Чекбокс в режиме выбора)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        colors = CheckboxDefaults.colors(checkedColor = AccentCyan),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }

                val online = isOnline(client.traffic?.lastOnline ?: 0L)
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (online) Color(0xFF34C759) else TextSecondary.copy(alpha = 0.4f),
                            CircleShape
                        )
                )

                Text(
                    text = clientEmail,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp)
                )

                if (!isSelectionMode) {
                    Switch(
                        checked = client.enable,
                        onCheckedChange = {
                            viewModel.toggleClientEnabled(context, clientEmail, !client.enable)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GreenStatus,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkCardBorder
                        ),
                        modifier = Modifier
                            .scale(0.7f)
                            .requiredHeight(24.dp)
                    )
                }
            }

            // Строка 2: Трафик ↑ ↓ из Лимит
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "↑ ${formatBytesLocal(client.getUpTraffic())}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
                Text(
                    "↓ ${formatBytesLocal(client.getDownTraffic())}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
                if (client.getTotalTrafficLimit() > 0) {
                    Text(
                        "из ${formatBytesLocal(client.getTotalTrafficLimit())}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                    )
                }
            }

            // Строка 3: Истекает
            Text(
                "Истекает: ${formatExpiry(client.getEffectiveExpiryTime())}",
                color = TextSecondary,
                fontSize = 11.sp,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
            )

            // Строка 4: Был в сети
            Text(
                "Был в сети: ${formatLastOnline(client.traffic?.lastOnline ?: 0L)}",
                color = TextSecondary,
                fontSize = 11.sp,
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
            )

            Spacer(Modifier.height(2.dp))

            // Кнопки действий (QR, Ссылка, Опции)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        isLoadingLinks = true
                        viewModel.getClientSubscriptionUrl(context, client) { subUrl ->
                            viewModel.getClientLinks(context, clientEmail) { links ->
                                isLoadingLinks = false
                                viewingSubUrl = subUrl
                                viewingLinks =
                                    links.ifEmpty { if (!subUrl.isNullOrBlank()) listOf(subUrl) else emptyList() }
                            }
                        }
                    },
                    enabled = !isLoadingLinks,
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLow, contentColor = Primary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = "", modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("QR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        isLoadingLinks = true
                        viewModel.getClientSubscriptionUrl(context, client) { subUrl ->
                            if (!subUrl.isNullOrBlank()) {
                                isLoadingLinks = false
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText(
                                        "Subscription Link",
                                        subUrl
                                    )
                                )
                                Toast.makeText(
                                    context,
                                    "Ссылка подписки скопирована",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                viewModel.getClientLinks(context, clientEmail) { links ->
                                    isLoadingLinks = false
                                    if (links.isNotEmpty()) {
                                        val clipboard =
                                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(
                                            ClipData.newPlainText(
                                                "Link",
                                                links.first()
                                            )
                                        )
                                        Toast.makeText(
                                            context,
                                            "Ссылка скопирована в буфер обмена",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Ссылки не найдены",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoadingLinks,
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLow, contentColor = PrimaryFixed),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Link, contentDescription = "", modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("ССЫЛКА", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { showOptionsMenu = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerLow, contentColor = OnSurfaceVariant),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "", modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("ОПЦИИ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false },
                        modifier = Modifier
                            .background(DarkCardBg)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
                    ) {
                    DropdownMenuItem(
                        text = { Text("📄 Экспорт JSON", color = TextPrimary, fontSize = 12.sp) },
                        onClick = {
                            showOptionsMenu = false
                            showJsonDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🔄 Сбросить трафик", color = TextPrimary, fontSize = 12.sp) },
                        onClick = {
                            showOptionsMenu = false
                            viewModel.resetClientTrafficByEmail(context, clientEmail) {
                                Toast.makeText(context, "Трафик сброшен", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("📱 Очистить HWID", color = TextPrimary, fontSize = 12.sp) },
                        onClick = {
                            showOptionsMenu = false
                            viewModel.clearClientHwidsByEmail(context, clientEmail) {
                                Toast.makeText(context, "Устройства HWID очищены", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🗑️ Удалить клиента", color = RedStatus, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        onClick = {
                            showOptionsMenu = false
                            viewModel.deleteClientByEmail(context, clientEmail) {
                                Toast.makeText(context, "Клиент $clientEmail удален", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }

    viewingLinks?.let { links ->
        if (activeSelectedConnection != null) {
            ClientShareSheet(
                client = client,
                links = links,
                connection = activeSelectedConnection,
                subUrl = viewingSubUrl,
                onDismiss = {
                    viewingLinks = null
                    viewingSubUrl = null
                },
                onDelete = {
                    viewingLinks = null
                    viewingSubUrl = null
                    viewModel.deleteClientByEmail(context, clientEmail) {
                        Toast.makeText(context, "Пользователь $clientEmail удалён", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { viewingLinks = null },
                title = { Text("Ссылка подключения: $clientEmail", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        links.forEach { link ->
                            Surface(
                                color = SurfaceContainerLow,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val clipboard =
                                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(
                                            ClipData.newPlainText(
                                                "Link",
                                                link
                                            )
                                        )
                                        Toast.makeText(
                                            context,
                                            "Ссылка скопирована",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            ) {
                                Text(
                                    link,
                                    color = AccentCyan,
                                    fontSize = 11.sp,
                                    fontFamily = GeistMonoFontFamily,
                                    modifier = Modifier.padding(8.dp),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewingLinks = null }) {
                        Text("Закрыть", color = AccentCyan)
                    }
                }
            )
        }
    }

    if (showJsonDialog) {
        ExportJsonDialog(
            client = client,
            onDismiss = { showJsonDialog = false }
        )
    }
}

@Composable
fun AddClientDialog(
    connection: PanelConnection,
    onConfirm: (ApiClientItem, List<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { PanelRepository() }

    var email by remember { mutableStateOf("") }
    var totalGbStr by remember { mutableStateOf("") }
    var daysStr by remember { mutableStateOf("") }

    var availableInbounds by remember { mutableStateOf<List<Inbound>>(emptyList()) }
    val selectedInboundIds = remember { mutableStateListOf<Int>() }
    var isLoadingInbounds by remember { mutableStateOf(true) }

    LaunchedEffect(connection) {
        isLoadingInbounds = true
        repository.getInboundsList(context, connection).onSuccess { list ->
            availableInbounds = list
            if (list.isNotEmpty()) {
                selectedInboundIds.add(list.first().id)
            }
            isLoadingInbounds = false
        }.onFailure {
            isLoadingInbounds = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Добавление нового пользователя",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Email / Логин
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email / Имя пользователя", color = OnSurfaceVariant, fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow,
                        focusedBorderColor = PrimaryContainer,
                        unfocusedBorderColor = OutlineVariant.copy(alpha = 0.4f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Трафик GB
                    OutlinedTextField(
                        value = totalGbStr,
                        onValueChange = { totalGbStr = it },
                        label = { Text("Лимит GB (0 = ∞)", color = OnSurfaceVariant, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerLow,
                            unfocusedContainerColor = SurfaceContainerLow,
                            focusedBorderColor = PrimaryContainer,
                            unfocusedBorderColor = OutlineVariant.copy(alpha = 0.4f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    // Срок в днях
                    OutlinedTextField(
                        value = daysStr,
                        onValueChange = { daysStr = it },
                        label = { Text("Дней (0 = ∞)", color = OnSurfaceVariant, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerLow,
                            unfocusedContainerColor = SurfaceContainerLow,
                            focusedBorderColor = PrimaryContainer,
                            unfocusedBorderColor = OutlineVariant.copy(alpha = 0.4f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

                // Выбор привязываемых подключений (inboundIds)
                Text(
                    "Привязка к подключениям (inboundIds):",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                if (isLoadingInbounds) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                } else if (availableInbounds.isEmpty()) {
                    Text("На сервере нет входящих подключений", color = RedStatus, fontSize = 11.sp)
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                    ) {
                        availableInbounds.forEach { inbound ->
                            val isSelected = selectedInboundIds.contains(inbound.id)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) AccentCyan.copy(alpha = 0.15f) else SurfaceContainerLow,
                                border = BorderStroke(1.dp, if (isSelected) AccentCyan else OutlineVariant.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) {
                                            selectedInboundIds.remove(inbound.id)
                                        } else {
                                            selectedInboundIds.add(inbound.id)
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedInboundIds.add(inbound.id)
                                            else selectedInboundIds.remove(inbound.id)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = AccentCyan)
                                    )
                                    val remarkStr = inbound.remark?.takeIf { it.isNotBlank() } ?: inbound.getSecurityType().uppercase()
                                    Text(
                                        "#${inbound.id} $remarkStr",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isBlank()) {
                        Toast.makeText(context, "Введите Email пользователя", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedInboundIds.isEmpty()) {
                        Toast.makeText(context, "Выберите хотя бы одно подключение", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val gbVal = totalGbStr.toLongOrNull() ?: 0L
                    val totalBytes = gbVal * 1024 * 1024 * 1024L

                    val daysVal = daysStr.toLongOrNull() ?: 0L
                    val expiryMs = if (daysVal > 0) System.currentTimeMillis() + (daysVal * 86400000L) else 0L

                    val clientItem = ApiClientItem(
                        email = email.trim(),
                        totalGB = totalBytes,
                        expiryTime = expiryMs,
                        tgId = 0,
                        limitIp = 0,
                        limitHwid = 0,
                        enable = true
                    )

                    onConfirm(clientItem, selectedInboundIds.toList())
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = OnPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Создать пользователя", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextSecondary)
            }
        }
    )
}
}