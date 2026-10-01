package com.example.xuimanager.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xuimanager.data.api.model.ApiClient
import com.example.xuimanager.data.api.model.ClientHwid
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.data.repository.PanelRepository
import com.example.xuimanager.ui.qr.QrCodeGenerator
import com.example.xuimanager.ui.theme.*
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientShareSheet(
    client: ApiClient,
    links: List<String>,
    connection: PanelConnection,
    subUrl: String? = null,
    onDismiss: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onIpLog: () -> Unit = {}
) {
    var confirmDelete by remember { mutableStateOf(false) }
    var showIpLogDialog by remember { mutableStateOf(false) }
    var showHwidDialog by remember { mutableStateOf(false) }
    var happLink by remember { mutableStateOf<String?>(null) }
    var happLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { PanelRepository() }
    val email = client.email ?: "Client"

    // Определение публичного URL подписки, если он сформирован
    val effectiveSubUrl = subUrl ?: if (client.getEffectiveSubId().isNotBlank()) {
        "${connection.protocol}://${connection.host}:${connection.port}/sub/${client.getEffectiveSubId()}"
    } else {
        links.firstOrNull() ?: ""
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkCardBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                email,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // --- 1. Секция: Подписка (Subscription) --------------------------------
            ExpandableSection(
                title = "Подписка",
                subtitle = "Авто-обновляемая ссылка для всех конфигураций",
                initiallyExpanded = true
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (effectiveSubUrl.isNotBlank()) {
                        QrAndLink(content = effectiveSubUrl, context = context)

                        // Статус глазами подписчика
                        SubStatusRow(client = client)

                        // Зашифрованная Happ ссылка
                        HappLinkBlock(
                            link = happLink,
                            loading = happLoading,
                            onGenerate = {
                                happLoading = true
                                coroutineScope.launch {
                                    repository.getHappLink(context, connection, client.id?.toString()?.toIntOrNull() ?: 0)
                                        .onSuccess {
                                            happLink = it
                                            happLoading = false
                                        }
                                        .onFailure {
                                            happLoading = false
                                            Toast.makeText(context, "Панель не поддерживает Happ ссылки", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            },
                            context = context
                        )
                    } else {
                        Text(
                            "Нет URL подписки. Убедитесь, что на сервере включен модуль Sub.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // --- 2. Секция: Подключения (Connections) -----------------------------
            ExpandableSection(
                title = "Подключения" + if (links.isNotEmpty()) " (${links.size})" else "",
                subtitle = "Индивидуальные ссылки серверов",
                initiallyExpanded = false
            ) {
                if (links.isEmpty()) {
                    Text("Нет подключений для данного клиента", color = TextSecondary, fontSize = 12.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        links.forEachIndexed { index, link ->
                            ConnectionItem(
                                label = connectionLabel(link, index),
                                link = link,
                                context = context
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

            // Кнопки управления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onEdit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = OnPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Изменить", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { confirmDelete = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedStatus),
                    border = BorderStroke(1.dp, RedStatus.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Удалить", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showHwidDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("HWID Устройства", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        showIpLogDialog = true
                        onIpLog()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("IP лог", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Закрыть", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Удалить пользователя?", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = { Text("$email будет отвязан от всех подключений панели.", color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                    onDismiss()
                }) { Text("Удалить", color = RedStatus, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Отмена", color = TextSecondary) }
            }
        )
    }

    if (showIpLogDialog) {
        IpLogDialog(
            email = email,
            connection = connection,
            repository = repository,
            onDismiss = { showIpLogDialog = false }
        )
    }

    if (showHwidDialog) {
        HwidManageDialog(
            email = email,
            connection = connection,
            repository = repository,
            onDismiss = { showHwidDialog = false }
        )
    }
}

/** Преобразование URI подключения в понятную метку: SCHEME · #Remark */
private fun connectionLabel(link: String, index: Int): String {
    val scheme = link.substringBefore("://", "").uppercase().ifBlank { "LINK" }
    val remark = link.substringAfter("#", "").substringBefore("\n").trim()
    val decoded = runCatching { java.net.URLDecoder.decode(remark, "UTF-8") }.getOrDefault(remark)
    return if (decoded.isNotBlank()) "$scheme · $decoded" else "$scheme · #${index + 1}"
}

@Composable
private fun ExpandableSection(
    title: String,
    subtitle: String,
    initiallyExpanded: Boolean,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = TextSecondary, fontSize = 11.sp)
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = AccentCyan
                )
            }
            AnimatedVisibility(visible = expanded) {
                Box(modifier = Modifier.padding(top = 10.dp)) { content() }
            }
        }
    }
}

@Composable
private fun ConnectionItem(
    label: String,
    link: String,
    context: Context
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    label,
                    color = PrimaryContainer,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
            AnimatedVisibility(visible = expanded) {
                Box(modifier = Modifier.padding(top = 8.dp)) {
                    QrAndLink(content = link, context = context)
                }
            }
        }
    }
}

@Composable
private fun QrAndLink(content: String, context: Context) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        QrCard(content = content)
        LinkRow(content = content, context = context)
    }
}

@Composable
private fun LinkRow(content: String, context: Context) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            content,
            color = AccentCyan,
            fontSize = 11.sp,
            fontFamily = GeistMonoFontFamily,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("Link", content))
            Toast.makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT).show()
        }) {
            Icon(Icons.Outlined.ContentCopy, contentDescription = "", tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
        IconButton(onClick = {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Поделиться"))
        }) {
            Icon(Icons.Outlined.Share, contentDescription = "", tint = PrimaryContainer, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun QrCard(content: String) {
    val bitmap = remember(content) { runCatching { QrCodeGenerator.generateQrBitmap(content, 512) }.getOrNull() }
    if (bitmap == null) {
        Text("Ссылка слишком длинная для QR-кода", color = RedStatus, fontSize = 11.sp)
        return
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "QR Code",
            modifier = Modifier.size(200.dp)
        )
    }
}

@Composable
private fun HappLinkBlock(
    link: String?,
    loading: Boolean,
    onGenerate: () -> Unit,
    context: Context
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)
        Text("Зашифрованная Happ ссылка", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        when {
            loading -> Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(20.dp))
            }
            link != null -> {
                QrAndLink(content = link, context = context)
                Text(
                    "Ссылка открывается только клиентом Happ, но содержит подписку.",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
                TextButton(onClick = onGenerate) { Text("Перегенерировать", color = AccentCyan, fontSize = 11.sp) }
            }
            else -> {
                OutlinedButton(
                    onClick = onGenerate,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f))
                ) {
                    Text("Создать зашифрованную Happ ссылку", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun SubStatusRow(client: ApiClient) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("Статус подписки:", color = TextSecondary, fontSize = 11.sp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(if (client.enable) GreenStatus else RedStatus, CircleShape)
            )
            Text(
                if (client.enable) "Активна" else "Отключена",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        val total = client.getTotalTrafficLimit()
        val totalStr = if (total > 0) formatBytesLocal(total) else "∞"
        val usedStr = formatBytesLocal(client.getUpTraffic() + client.getDownTraffic())
        Text("Использовано: $usedStr из $totalStr", color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun IpLogDialog(
    email: String,
    connection: PanelConnection,
    repository: PanelRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var ips by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repository.getClientIps(context, connection, email).onSuccess {
            ips = it
            isLoading = false
        }.onFailure { isLoading = false }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("IP лог: $email", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(24.dp))
                }
            } else if (ips.isEmpty()) {
                Text("История подключений пуста", color = TextSecondary, fontSize = 12.sp)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ips.forEach { ip ->
                        Text("• $ip", color = AccentCyan, fontSize = 12.sp, fontFamily = GeistMonoFontFamily)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть", color = AccentCyan) }
        }
    )
}

@Composable
private fun HwidManageDialog(
    email: String,
    connection: PanelConnection,
    repository: PanelRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var hwids by remember { mutableStateOf<List<ClientHwid>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repository.listClientHwids(context, connection, email).onSuccess {
            hwids = it
            isLoading = false
        }.onFailure { isLoading = false }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("HWID устройства: $email", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(24.dp))
                }
            } else if (hwids.isEmpty()) {
                Text("Нет зарегистрированных устройств", color = TextSecondary, fontSize = 12.sp)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    hwids.forEach { hwid ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceContainerLow, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(hwid.hwid, color = TextPrimary, fontSize = 11.sp, fontFamily = GeistMonoFontFamily, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                hwid.ip?.let { Text("IP: $it", color = TextSecondary, fontSize = 10.sp) }
                            }
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    repository.deleteClientHwid(context, connection, email, hwid.id)
                                    hwids = hwids.filter { it.id != hwid.id }
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "", tint = RedStatus, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть", color = AccentCyan) }
        }
    )
}