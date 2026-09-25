package com.example.xuimanager.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.xuimanager.ui.theme.*
import com.example.xuimanager.ui.viewmodel.ConnectionsViewModel
import com.example.xuimanager.ui.viewmodel.UsersViewModel

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

    var activeSelectedConnection by remember { mutableStateOf<PanelConnection?>(selectedConnection ?: connections.firstOrNull()) }
    var showServerMenu by remember { mutableStateOf(false) }
    var showAddClientDialog by remember { mutableStateOf(false) }

    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topContentPadding = statusBarTopPadding + 68.dp
    val bottomContentPadding = navBarBottomPadding + 96.dp

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
                            val activeCount = clients.count { it.enable }
                            Text("$activeCount активны", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("По трафику ▼", color = TextSecondary, fontSize = 11.sp)
                }
            }

            // Список клиентов
            items(clients) { client ->
                ClientCardItem(
                    client = client,
                    viewModel = viewModel,
                    context = context
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
                                .background(AccentCyan.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "${clients.size}",
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(80.dp))

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

        // ДИАЛОГ ДОБАВЛЕНИЯ ПОЛЬЗОВАТЕЛЯ (POST /panel/api/clients/add)
        if (showAddClientDialog && activeSelectedConnection != null) {
            AddClientDialog(
                connection = activeSelectedConnection!!,
                onConfirm = { clientItem, inboundIds ->
                    viewModel.addClientApi(context, clientItem, inboundIds) { success, errorMsg ->
                        if (success) {
                            Toast.makeText(context, "Пользователь ${clientItem.email} успешно создан!", Toast.LENGTH_SHORT).show()
                            showAddClientDialog = false
                        } else {
                            Toast.makeText(context, "Ошибка: ${errorMsg ?: "Не удалось создать"}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onDismiss = { showAddClientDialog = false }
            )
        }
    }
}

@Composable
private fun ClientCardItem(
    client: ApiClient,
    viewModel: UsersViewModel,
    context: Context
) {
    var showOptionsMenu by remember { mutableStateOf(false) }
    var viewingLinks by remember { mutableStateOf<List<String>?>(null) }
    var isLoadingLinks by remember { mutableStateOf(false) }

    val clientEmail = client.email ?: "Client"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainerHigh, RoundedCornerShape(12.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceContainerHighest, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        clientEmail.take(2).uppercase(),
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            clientEmail,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (client.enable) GreenStatus.copy(alpha = 0.2f) else RedStatus.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                if (client.enable) "Активен" else "Выключен",
                                color = if (client.enable) GreenStatus else RedStatus,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    SurfaceContainerLowest,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("VLESS Reality", color = PrimaryFixedDim, fontSize = 10.sp)
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    SurfaceContainerLowest,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                client.flow?.ifBlank { "xHTTP" } ?: "xHTTP",
                                color = SecondaryFixedDim,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // 4 КНОПКИ ДЕЙСТВИЙ (QR, ССЫЛКА, ПАУЗА/ВКЛ, ОПЦИИ)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    isLoadingLinks = true
                    viewModel.getClientLinks(context, clientEmail) { links ->
                        isLoadingLinks = false
                        if (links.isNotEmpty()) {
                            viewingLinks = links
                        } else {
                            Toast.makeText(context, "Ссылки не найдены", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isLoadingLinks,
                modifier = Modifier.weight(1f).height(36.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceContainerLow,
                    contentColor = Primary
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.QrCode, contentDescription = "", modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("QR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    isLoadingLinks = true
                    viewModel.getClientLinks(context, clientEmail) { links ->
                        isLoadingLinks = false
                        if (links.isNotEmpty()) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("VLESS Link", links.first()))
                            Toast.makeText(context, "Ссылка скопирована в буфер обмена", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Ссылки не найдены", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = !isLoadingLinks,
                modifier = Modifier.weight(1f).height(36.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceContainerLow,
                    contentColor = PrimaryFixed
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Link, contentDescription = "", modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("ССЫЛКА", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    viewModel.toggleClientEnabled(context, clientEmail, !client.enable)
                    Toast.makeText(context, if (client.enable) "Клиент отключен" else "Клиент включен", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f).height(36.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceContainerLow,
                    contentColor = if (client.enable) YellowStatus else GreenStatus
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    if (client.enable) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "",
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(if (client.enable) "ПАУЗА" else "ВКЛ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { showOptionsMenu = true },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceContainerLow,
                        contentColor = OnSurfaceVariant
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "", modifier = Modifier.size(15.dp))
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
        AlertDialog(
            onDismissRequest = { viewingLinks = null },
            title = { Text("Ссылка подключения: $clientEmail", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    links.forEach { link ->
                        Surface(
                            color = SurfaceContainerLow,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Link", link))
                                Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
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
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
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