package com.example.xuimanager.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.ui.theme.*
import com.example.xuimanager.ui.viewmodel.DeploymentStepItem
import com.example.xuimanager.ui.viewmodel.SSHInstallerViewModel
import com.example.xuimanager.ui.viewmodel.StepStatus

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
    val steps by viewModel.steps.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val installedConnection by viewModel.installedConnection.collectAsState()

    val context = LocalContext.current
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topContentPadding = statusBarTopPadding + 68.dp
    val bottomContentPadding = navBarBottomPadding + 96.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // 1. КОНТЕНТ ЭКРАНА (Скроллится под парящей шапкой)
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
            // ФОРМА ПОДКЛЮЧЕНИЯ К VPS
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = PrimaryContainer,
                            spotColor = PrimaryContainer
                        )
                        .border(
                            border = BorderStroke(1.5.dp, PrimaryContainer.copy(alpha = 0.65f)),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(PrimaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Terminal,
                                    contentDescription = "",
                                    tint = PrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    "SSH Деплой 3X-UI",
                                    color = Primary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Автоматическая установка MHSanaei на чистый VPS",
                                    color = OnSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        OutlinedTextField(
                            value = host,
                            onValueChange = { host = it },
                            label = { Text("Хост / IP адрес сервера", color = OnSurfaceVariant, fontSize = 11.sp) },
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
                            OutlinedTextField(
                                value = port,
                                onValueChange = { port = it },
                                label = { Text("Порт SSH", color = OnSurfaceVariant, fontSize = 11.sp) },
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

                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Пользователь", color = OnSurfaceVariant, fontSize = 11.sp) },
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
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Пароль SSH (root)", color = OnSurfaceVariant, fontSize = 11.sp) },
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
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (!isInstalling && host.isNotBlank() && password.isNotBlank()) {
                                        viewModel.install3xUI(
                                            context,
                                            host.trim(),
                                            port.toIntOrNull() ?: 22,
                                            username.trim(),
                                            password.trim()
                                        )
                                    } else {
                                        Toast.makeText(context, "Заполните IP и пароль", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isInstalling && host.isNotBlank() && password.isNotBlank(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryContainer,
                                    contentColor = OnPrimary,
                                    disabledContainerColor = SurfaceContainerHighest,
                                    disabledContentColor = OnSurfaceVariant
                                ),
                                modifier = Modifier
                                    .weight(1.6f)
                                    .height(38.dp)
                            ) {
                                if (isInstalling) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            color = OnPrimary,
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Установка...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Запустить деплой", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Button(
                                onClick = { viewModel.clearLogs() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SurfaceContainerHighest,
                                    contentColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Очистить", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // РЕЗУЛЬТАТ ДЕПЛОЯ И КНОПКА ДОБАВЛЕНИЯ
            installedConnection?.let { conn ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, TertiaryFixedDim.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = TertiaryContainer.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "", tint = TertiaryFixedDim, modifier = Modifier.size(20.dp))
                                Text("3x-ui успешно установлен!", color = TertiaryFixedDim, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            val creds by viewModel.detectedCredentials.collectAsState()
                            val displayUser = creds?.first?.ifBlank { "admin" } ?: "admin"
                            val displayPass = creds?.second?.ifBlank { "admin" } ?: "admin"

                            Text(
                                "Сервер: ${conn.host}:${conn.port} | SSH Порт: ${conn.sshPort ?: 22}\nЛогин: $displayUser | Пароль: $displayPass",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontFamily = GeistMonoFontFamily
                            )
                            viewModel.logFilePath.collectAsState().value?.let { path ->
                                Text(
                                    "Лог файл сохранен: $path",
                                    color = TertiaryFixedDim,
                                    fontSize = 11.sp,
                                    fontFamily = GeistMonoFontFamily
                                )
                            }
                            Button(
                                onClick = { onAddConnection(conn) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = OnPrimary),
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "", modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Сохранить и подключиться", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // БЛОК "ПРОГРЕСС РАЗВЕРТЫВАНИЯ" (100% В ТОЧНОСТИ КАК НА СКРИНШОТЕ 2)
            item {
                DeploymentProgressCard(
                    progress = progress,
                    steps = steps,
                    isInstalling = isInstalling
                )
            }

            // ЛОГ УСТАНОВКИ В РЕАЛЬНОМ ВРЕМЕНИ (STITCH TERMINAL BOX)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Консольный лог деплоя:", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceContainerLow)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(modifier = Modifier.size(10.dp).background(RedStatus, CircleShape))
                                    Box(modifier = Modifier.size(10.dp).background(YellowStatus, CircleShape))
                                    Box(modifier = Modifier.size(10.dp).background(GreenStatus, CircleShape))
                                }
                                Text(
                                    "bash - root@${host.ifBlank { "vps" }}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = GeistMonoFontFamily
                                )
                            }

                            if (logs.isEmpty() && !isInstalling) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Terminal,
                                            contentDescription = "",
                                            tint = Outline,
                                            modifier = Modifier.size(44.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Лог пуст. Запустите деплой выше.", color = TextSecondary, fontSize = 12.sp)
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    reverseLayout = true
                                ) {
                                    items(logs.reversed()) { logLine ->
                                        val lineLower = logLine.lowercase()
                                        val lineColor = when {
                                            lineLower.contains("error") || lineLower.contains("fail") || lineLower.contains("ошибка") -> RedStatus
                                            lineLower.contains("success") || lineLower.contains("успешно") || lineLower.contains("done") -> TertiaryFixedDim
                                            lineLower.contains("install") || lineLower.contains("running") -> PrimaryContainer
                                            else -> TextPrimary
                                        }

                                        Text(
                                            logLine,
                                            color = lineColor,
                                            fontSize = 11.sp,
                                            fontFamily = GeistMonoFontFamily,
                                            maxLines = 4,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. НЕПОДВИЖНАЯ ПЛАВАЮЩАЯ ШАПКА ВВЕРХУ
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
                        stringRes("ssh"),
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(PrimaryContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("MHSanaei 3x-ui", color = PrimaryContainer, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeploymentProgressCard(
    progress: Float,
    steps: List<DeploymentStepItem>,
    isInstalling: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val activeBorderColor = if (isInstalling) PrimaryContainer.copy(alpha = pulseAlpha) else OutlineVariant.copy(alpha = 0.3f)
    val activeShadowColor = if (isInstalling) PrimaryContainer else Color.Black
    val activeElevation = if (isInstalling) 14.dp else 0.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = activeElevation,
                shape = RoundedCornerShape(18.dp),
                ambientColor = activeShadowColor,
                spotColor = activeShadowColor
            )
            .border(1.5.dp, activeBorderColor, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ШАПКА КАРТОЧКИ С ФИОЛЕТОВОЙ ИКОНКОЙ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFF282561), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "Прогресс развертывания",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            "Выполнение сценария оркестрации",
                            color = Color(0xFF9CA3AF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    "${(animatedProgress * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }

            // ШКАЛА ПРОГРЕССА
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = PrimaryContainer,
                trackColor = SurfaceContainerLowest
            )

            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.25f), thickness = 0.5.dp)

            // СПИСОК ШАГОВ
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                steps.forEach { step ->
                    DeploymentStepRow(step = step)
                }
            }
        }
    }
}

@Composable
private fun DeploymentStepRow(step: DeploymentStepItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300, easing = FastOutSlowInEasing)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Иконка состояния шага
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            when (step.status) {
                StepStatus.COMPLETED -> {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(Color(0xFF0F382B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "",
                            tint = TertiaryFixedDim,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                StepStatus.IN_PROGRESS -> {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .shadow(8.dp, CircleShape, spotColor = PrimaryContainer)
                            .background(Color(0xFF133E43), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryContainer,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                StepStatus.FAILED -> {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(RedStatus.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "",
                            tint = RedStatus,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                StepStatus.PENDING -> {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .border(2.dp, Color(0xFF374151), CircleShape)
                    )
                }
            }
        }

        // Заголовок шага + бейдж Active + Телеметрия
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    step.title,
                    color = when (step.status) {
                        StepStatus.PENDING -> Color(0xFF6B7280)
                        StepStatus.IN_PROGRESS -> Color.White
                        StepStatus.FAILED -> RedStatus
                        StepStatus.COMPLETED -> Color.White
                    },
                    fontSize = 14.sp,
                    fontWeight = if (step.status == StepStatus.PENDING) FontWeight.Normal else FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )

                if (step.status == StepStatus.IN_PROGRESS) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF133E43), RoundedCornerShape(6.dp))
                            .border(0.5.dp, PrimaryContainer.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "Active",
                            color = PrimaryFixed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = GeistMonoFontFamily
                        )
                    }
                }
            }

            Text(
                step.description,
                color = if (step.status == StepStatus.PENDING) Color(0xFF4B5563) else Color(0xFF9CA3AF),
                fontSize = 12.sp,
                fontFamily = GeistMonoFontFamily
            )
        }
    }
}