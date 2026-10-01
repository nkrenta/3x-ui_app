package com.example.xuimanager.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xuimanager.data.api.model.ApiClient
import com.example.xuimanager.data.api.model.ApiClientItem
import com.example.xuimanager.data.api.model.Inbound
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.data.repository.PanelRepository
import com.example.xuimanager.ui.theme.*
import java.util.UUID

@Composable
fun ClientEditorDialog(
    connection: PanelConnection,
    existingClient: ApiClient? = null,
    onConfirm: (ApiClientItem, List<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { PanelRepository() }

    var email by remember { mutableStateOf(existingClient?.email ?: "") }
    var totalGbStr by remember { mutableStateOf(if ((existingClient?.totalGB ?: 0L) > 0) "${existingClient!!.totalGB / (1024 * 1024 * 1024)}" else "") }
    var daysStr by remember { mutableStateOf("") }
    var limitIpStr by remember { mutableStateOf(if ((existingClient?.limitIp ?: 0) > 0) "${existingClient!!.limitIp}" else "") }
    var limitHwidStr by remember { mutableStateOf(if ((existingClient?.limitHwid ?: 0) > 0) "${existingClient!!.limitHwid}" else "") }
    var enable by remember { mutableStateOf(existingClient?.enable ?: true) }

    var availableInbounds by remember { mutableStateOf<List<Inbound>>(emptyList()) }
    val selectedInboundIds = remember { mutableStateListOf<Int>() }
    var isLoadingInbounds by remember { mutableStateOf(true) }

    LaunchedEffect(connection) {
        isLoadingInbounds = true
        repository.getInboundsList(context, connection).onSuccess { list ->
            availableInbounds = list
            if (existingClient?.inboundIds != null && existingClient.inboundIds.isNotEmpty()) {
                selectedInboundIds.addAll(existingClient.inboundIds)
            } else if (list.isNotEmpty()) {
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
                if (existingClient == null) "Добавление клиента" else "Редактирование клиента",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Email / Имя
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email / Логин пользователя", color = OnSurfaceVariant, fontSize = 11.sp) },
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

                // Активен (Enable Switch)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Активен", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = enable,
                        onCheckedChange = { enable = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GreenStatus,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkCardBorder
                        )
                    )
                }

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Лимит IP
                    OutlinedTextField(
                        value = limitIpStr,
                        onValueChange = { limitIpStr = it },
                        label = { Text("Лимит IP (0 = ∞)", color = OnSurfaceVariant, fontSize = 11.sp) },
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

                    // Лимит HWID
                    OutlinedTextField(
                        value = limitHwidStr,
                        onValueChange = { limitHwidStr = it },
                        label = { Text("Лимит HWID (0 = ∞)", color = OnSurfaceVariant, fontSize = 11.sp) },
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
                            .heightIn(max = 160.dp)
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
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
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
                        limitIp = limitIpStr.toIntOrNull() ?: 0,
                        limitHwid = limitHwidStr.toIntOrNull() ?: 0,
                        enable = enable
                    )

                    onConfirm(clientItem, selectedInboundIds.toList())
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = OnPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (existingClient == null) "Создать клиента" else "Сохранить", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextSecondary)
            }
        }
    )
}