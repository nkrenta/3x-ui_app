package com.example.xuimanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xuimanager.data.api.model.ApiClient
import com.example.xuimanager.data.api.model.ClientSettingsItem
import com.example.xuimanager.ui.theme.AccentBlue
import com.example.xuimanager.ui.theme.AccentCyan
import com.example.xuimanager.ui.theme.DarkBackground
import com.example.xuimanager.ui.theme.DarkCardBg
import com.example.xuimanager.ui.theme.DarkCardBorder
import com.example.xuimanager.ui.theme.GreenStatus
import com.example.xuimanager.ui.theme.RedStatus
import com.example.xuimanager.ui.theme.TextPrimary
import com.example.xuimanager.ui.theme.TextSecondary
import com.example.xuimanager.ui.viewmodel.UsersViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun UsersScreen(viewModel: UsersViewModel = viewModel()) {
    val clients by viewModel.clients.collectAsState()
    val inbounds by viewModel.inbounds.collectAsState()
    val selectedInboundId by viewModel.selectedInboundId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var editingClient by remember { mutableStateOf<ApiClient?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // Header with inbound selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Клиенты", color = TextPrimary, fontSize = 20.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (inbounds.size > 1) {
                    OutlinedTextField(
                        value = inbounds.find { it.id == selectedInboundId }?.remark ?: "",
                        onValueChange = {},
                        label = { Text("Входящий", color = TextSecondary) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = DarkCardBg,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary
                        ),
                        readOnly = true,
                        modifier = Modifier.width(200.dp)
                    )
                    MenuAnchor(
                        inbounds = inbounds,
                        selectedInboundId = selectedInboundId,
                        onSelect = { id ->
                            viewModel.loadClients(context, id)
                        })
                }
                IconButton(onClick = { viewModel.loadInbounds(context) }, enabled = !isLoading) {
                    if (isLoading) {
                        androidx.compose.foundation.layout.Box(modifier = Modifier.size(24.dp)) {
                            CircularProgressIndicator(
                                color = AccentCyan,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Обновить",
                            tint = TextSecondary
                        )
                    }
                }
                FloatingActionButton(
                    onClick = { editingClient = null; showAddDialog = true },
                    containerColor = AccentBlue,
                    modifier = Modifier
                        .width(48.dp)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить клиента")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Сначала выберите панель во вкладке 'Подключения'",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        error?.let { err ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RedStatus.copy(alpha = 0.2f))
            ) {
                Text(err, color = RedStatus, modifier = Modifier.padding(16.dp))
            }
        }

        if (clients.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "",
                        tint = TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Нет клиентов", color = TextPrimary, fontSize = 18.sp)
                    Text(
                        "Добавьте первого клиента кнопкой +",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(clients) { client ->
                    ClientCard(
                        client = client,
                        onEdit = { editingClient = client; showAddDialog = true },
                        onDelete = { viewModel.deleteClient(context, client.id!!) },
                        onToggle = { viewModel.toggleClient(context, client) },
                        onShowQR = { /* TODO: show QR code dialog */ },
                        onResetTraffic = { viewModel.resetClientTraffic(context, client.id!!) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ClientDialog(
            client = editingClient,
            inboundId = selectedInboundId ?: 0,
            onSave = { newClient ->
                viewModel.addClient(context, newClient)
                showAddDialog = false
                editingClient = null
            },
            onDismiss = { showAddDialog = false; editingClient = null }
        )
    }
}

@Composable
fun MenuAnchor(
    inbounds: List<com.example.xuimanager.data.api.model.Inbound>,
    selectedInboundId: Int?,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        inbounds.forEach { inbound ->
            DropdownMenuItem(
                onClick = {
                    onSelect(inbound.id)
                    expanded = false
                },
                text = { Text(inbound.remark ?: "Inbound #${inbound.id}", color = TextPrimary) }
            )
        }
    }
    TextButton(onClick = { expanded = !expanded }) {
        Text("Выбрать", color = AccentCyan)
    }
}

@Composable
fun ClientCard(
    client: ApiClient,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit,
    onShowQR: () -> Unit,
    onResetTraffic: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "",
                            tint = AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(client.email ?: "Unknown", color = TextPrimary, fontSize = 16.sp)
                    }
                    Text(
                        "ID: ${client.id?.take(8)}... | ${client.flow ?: "none"} | Inbound: ${client.inboundId}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onShowQR) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = "QR код",
                            tint = TextSecondary
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Редактировать",
                            tint = TextSecondary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = RedStatus)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ClientStatItem(label = "Скачано", value = formatBytes(client.up))
                ClientStatItem(label = "Загружено", value = formatBytes(client.down))
                ClientStatItem(
                    label = "Лимит",
                    value = if (client.total > 0) formatBytes(client.total) else "∞"
                )
                ClientStatItem(
                    label = "Срок",
                    value = if (client.expiryTime > 0) formatDate(client.expiryTime) else "Бессрочно"
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClientStatusChip(enable = client.enable, onToggle = onToggle)
                TextButton(onClick = onResetTraffic) {
                    Text("Сбросить трафик", color = AccentCyan, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun RowScope.ClientStatItem(label: String, value: String) {
    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 10.sp)
        Text(
            value,
            color = TextPrimary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ClientStatusChip(enable: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(
                if (enable) GreenStatus.copy(alpha = 0.2f) else RedStatus.copy(alpha = 0.2f),
                RoundedCornerShape(12.dp)
            )
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            if (enable) GreenStatus else RedStatus,
                            RoundedCornerShape(3.dp)
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (enable) "Активен" else "Отключен",
                    color = if (enable) GreenStatus else RedStatus,
                    fontSize = 11.sp
                )
            }
            Switch(
                checked = enable,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentBlue,
                    checkedTrackColor = AccentBlue.copy(alpha = 0.5f),
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = DarkCardBorder
                )
            )
        }
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
    var id by remember { mutableStateOf(client?.id ?: java.util.UUID.randomUUID().toString()) }
    var flow by remember { mutableStateOf(client?.flow ?: "xtls-rprx-vision") }
    var totalGb by remember { mutableStateOf((client?.total ?: 0L) / 1024 / 1024 / 1024) }
    var expiryTime by remember { mutableStateOf(client?.expiryTime ?: 0L) }
    var enable by remember { mutableStateOf(client?.enable ?: true) }
    var limitIp by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (client != null) "Редактировать клиента" else "Новый клиент",
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(340.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email / Имя", color = TextSecondary) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = DarkCardBg,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary
                    ),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = flow,
                        onValueChange = { flow = it },
                        label = { Text("Flow", color = TextSecondary) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = DarkCardBg,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = limitIp.toString(),
                        onValueChange = { limitIp = it.toIntOrNull() ?: 0 },
                        label = { Text("Лимит IP", color = TextSecondary) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = DarkCardBg,
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
                    label = { Text("Лимит трафика (ГБ, 0 = безлимит)", color = TextSecondary) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = DarkCardBg,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = expiryTime.toString(),
                    onValueChange = { expiryTime = it.toLongOrNull() ?: 0 },
                    label = {
                        Text(
                            "Срок действия (timestamp ms, 0 = бессрочно)",
                            color = TextSecondary
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = DarkCardBg,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Активен", color = TextPrimary)
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
            TextButton(onClick = {
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
            }) {
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
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${(bytes / 1024).toString()} KB"
        bytes < 1024 * 1024 * 1024 -> "${(bytes / 1024 / 1024).toString()} MB"
        else -> "${(bytes / 1024 / 1024 / 1024).toString()} GB"
    }
}

private fun formatDate(timestampMs: Long): String {
    val date = java.util.Date(timestampMs)
    return java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(date)
}