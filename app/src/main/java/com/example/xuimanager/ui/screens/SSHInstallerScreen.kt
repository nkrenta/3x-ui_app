package com.example.xuimanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import com.example.xuimanager.ui.viewmodel.SSHInstallerViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SSHInstallerScreen(
    viewModel: SSHInstallerViewModel = viewModel(),
    onAddConnection: (PanelConnection) -> Unit = {}
) {
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("22") }
    var username by remember { mutableStateOf("root") }
    var password by remember { mutableStateOf("") }
    val logs by viewModel.logs.collectAsState()
    val isInstalling by viewModel.isInstalling.collectAsState()
    val result by viewModel.installResult.collectAsState()
    val installedConnection by viewModel.installedConnection.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text("SSH Инсталлер 3x-ui", color = TextPrimary, fontSize = 20.sp)
        Text(
            "Автоматическая установка панели MHSanaei/3x-ui на VPS",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Connection Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Подключение к серверу", color = TextPrimary, fontSize = 16.sp)

                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Хост (IP адрес)", color = TextSecondary) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = DarkBackground,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Порт SSH", color = TextSecondary) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = DarkBackground,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Пользователь", color = TextSecondary) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = DarkBackground,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = TextSecondary
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль (или ключ SSH)", color = TextSecondary) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = DarkBackground,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary
                    ),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (!isInstalling && host.isNotBlank() && password.isNotBlank()) {
                                viewModel.install3xUI(
                                    host,
                                    port.toIntOrNull() ?: 22,
                                    username,
                                    password
                                )
                            }
                        },
                        enabled = !isInstalling && host.isNotBlank() && password.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isInstalling && host.isNotBlank() && password.isNotBlank()) AccentBlue else DarkCardBorder
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isInstalling) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.foundation.layout.Box(modifier = Modifier.size(16.dp)) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Установка...", color = Color.White)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Установить 3x-ui", color = Color.White)
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.clearLogs() },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCardBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "",
                                tint = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Очистить лог", color = TextSecondary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Installation info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Что будет установлено:", color = TextPrimary, fontSize = 14.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    InstallStep("✓", "Docker & Docker Compose")
                    InstallStep("✓", "3x-ui Panel (MHSanaei fork)")
                    InstallStep("✓", "Xray Core (последняя версия)")
                    InstallStep("✓", "Автоматический SSL (Let's Encrypt)")
                    InstallStep("✓", "Настройка Firewall (UFW/iptables)")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logs
        Text("Лог установки:", color = TextPrimary, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117))
        ) {
            if (logs.isEmpty() && !isInstalling) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Terminal,
                            contentDescription = "",
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Лог пуст. Запустите установку.", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    reverseLayout = true
                ) {
                    items(logs.reversed()) { log ->
                        LogLine(log)
                    }
                }
            }
        }

        // Result message
        result?.let { res ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (res.contains("успешно") || res.contains("success")) GreenStatus.copy(
                        alpha = 0.2f
                    ) else RedStatus.copy(alpha = 0.2f)
                )
            ) {
                Text(
                    res,
                    color = if (res.contains("успешно") || res.contains("success")) GreenStatus else RedStatus,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        installedConnection?.let { conn ->
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onAddConnection(conn) },
                colors = ButtonDefaults.buttonColors(containerColor = GreenStatus),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    val pathText = if (conn.path.isNotBlank()) "/${conn.path.trim('/')}" else ""
                    Text(
                        "Сохранить панель в 'Подключения' (${conn.host}:${conn.port}$pathText)",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun InstallStep(icon: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, color = GreenStatus, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
fun LogLine(log: String) {
    val isError =
        log.contains("ОШИБКА") || log.contains("ERROR") || log.contains("Error") || log.contains("failed")
    val isSuccess =
        log.contains("успешно") || log.contains("success") || log.contains("completed") || log.contains(
            "OK"
        )
    val isInfo =
        log.contains("Запуск") || log.contains("подключение") || log.contains("Starting") || log.contains(
            "Connecting"
        )

    val color = when {
        isError -> RedStatus
        isSuccess -> GreenStatus
        isInfo -> AccentCyan
        else -> TextSecondary
    }

    Text(
        text = "> $log",
        color = color,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        modifier = Modifier.fillMaxWidth(),
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
    )
}