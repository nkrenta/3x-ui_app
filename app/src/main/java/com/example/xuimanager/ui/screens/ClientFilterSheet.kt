package com.example.xuimanager.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xuimanager.ui.theme.*

enum class ClientStatusBucket { ONLINE, ACTIVE, DISABLED, DEPLETED }
enum class ClientSortOrder { TRAFFIC_DOWN, LAST_ONLINE, EMAIL_ASC, EXPIRY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFilterSheet(
    selectedStatuses: Set<ClientStatusBucket>,
    sortOrder: ClientSortOrder,
    onApply: (Set<ClientStatusBucket>, ClientSortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    var tempStatuses by remember { mutableStateOf(selectedStatuses) }
    var tempSort by remember { mutableStateOf(sortOrder) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkCardBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    Icon(Icons.Default.FilterList, contentDescription = "", tint = AccentCyan, modifier = Modifier.size(20.dp))
                    Text("Фильтры и сортировка", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = {
                    tempStatuses = emptySet()
                    tempSort = ClientSortOrder.TRAFFIC_DOWN
                }) {
                    Text("Сбросить", color = TextSecondary, fontSize = 12.sp)
                }
            }

            HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

            // Фильтр по статусу
            Text("Статус клиента:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClientStatusBucket.values().forEach { status ->
                    val isSelected = tempStatuses.contains(status)
                    val label = when (status) {
                        ClientStatusBucket.ONLINE -> "В сети"
                        ClientStatusBucket.ACTIVE -> "Активен"
                        ClientStatusBucket.DISABLED -> "Пауза"
                        ClientStatusBucket.DEPLETED -> "Исчерпан"
                    }
                    val statusColor = when (status) {
                        ClientStatusBucket.ONLINE -> GreenStatus
                        ClientStatusBucket.ACTIVE -> AccentCyan
                        ClientStatusBucket.DISABLED -> YellowStatus
                        ClientStatusBucket.DEPLETED -> RedStatus
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            tempStatuses = if (isSelected) tempStatuses - status else tempStatuses + status
                        },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = statusColor.copy(alpha = 0.25f),
                            selectedLabelColor = statusColor,
                            containerColor = SurfaceContainerLow,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

            // Сортировка
            Text("Сортировка списка:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val sortOptions = listOf(
                    ClientSortOrder.TRAFFIC_DOWN to "По объему трафика (↓)",
                    ClientSortOrder.LAST_ONLINE to "По времени последнего онлайна",
                    ClientSortOrder.EXPIRY to "По дате окончания подписки",
                    ClientSortOrder.EMAIL_ASC to "По алфавиту (A-Z)"
                )

                sortOptions.forEach { (order, title) ->
                    val isSelected = tempSort == order
                    Surface(
                        onClick = { tempSort = order },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) AccentCyan.copy(alpha = 0.15f) else SurfaceContainerLow,
                        border = BorderStroke(1.dp, if (isSelected) AccentCyan else DarkCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { tempSort = order },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentCyan)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    onApply(tempStatuses, tempSort)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = OnPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text("Применить фильтры", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}