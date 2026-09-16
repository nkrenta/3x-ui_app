package com.example.xuimanager.ui.screens

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.xuimanager.data.model.PanelConnection
import com.example.xuimanager.data.model.RealityTarget
import com.example.xuimanager.data.model.RealityTargets
import com.example.xuimanager.ui.theme.AccentBlue
import com.example.xuimanager.ui.theme.AccentCyan
import com.example.xuimanager.ui.theme.DarkBackground
import com.example.xuimanager.ui.theme.DarkCardBg
import com.example.xuimanager.ui.theme.DarkCardBorder
import com.example.xuimanager.ui.theme.GreenStatus
import com.example.xuimanager.ui.theme.RedStatus
import com.example.xuimanager.ui.theme.TextPrimary
import com.example.xuimanager.ui.theme.TextSecondary
import com.example.xuimanager.ui.theme.stringRes
import com.example.xuimanager.ui.viewmodel.ConnectionsViewModel
import com.example.xuimanager.ui.viewmodel.JsonTemplateItem
import com.example.xuimanager.ui.viewmodel.SettingsViewModel
import com.example.xuimanager.ui.viewmodel.TemplatesViewModel

@Composable
fun AppSettingsScreen(
    connectionsViewModel: ConnectionsViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    templatesViewModel: TemplatesViewModel = viewModel()
) {
    val fontSizeScale by settingsViewModel.fontSizeScale.collectAsState()
    val selectedLanguage by settingsViewModel.selectedLanguage.collectAsState()
    val alertCpuThreshold by settingsViewModel.alertCpuThreshold.collectAsState()
    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()
    val pinCode by settingsViewModel.pinCode.collectAsState()
    val isPinEnabled by settingsViewModel.isPinEnabled.collectAsState()

    val connections by connectionsViewModel.connections.collectAsState()
    val templates by templatesViewModel.templates.collectAsState()
    val isApplying by templatesViewModel.isApplying.collectAsState()
    val applyResult by templatesViewModel.applyResult.collectAsState()

    val context = LocalContext.current
    var showPinDialog by remember { mutableStateOf(false) }
    var showJsonImportDialog by remember { mutableStateOf(false) }
    var selectedTemplateForTarget by remember { mutableStateOf<JsonTemplateItem?>(null) }

    applyResult?.let { result ->
        LaunchedEffect(result) {
            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringRes("app_settings"), color = TextPrimary, fontSize = 20.sp)

        // БЛОК 1: Шаблоны Входящих (Импорт JSON TCP_Reality.json)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringRes("templates"), color = TextPrimary, fontSize = 16.sp)
                        Text(
                            "Импорт и применение JSON шаблонов 3x-ui",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = { showJsonImportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Upload,
                            contentDescription = "",
                            tint = DarkBackground,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Импорт JSON", color = DarkBackground, fontSize = 12.sp)
                    }
                }

                if (templates.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Extension,
                                contentDescription = "",
                                tint = TextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Нет шаблонов", color = TextPrimary, fontSize = 14.sp)
                            Text(
                                "Импортируйте JSON файл шаблона (например TCP_Reality.json)",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        templates.forEach { template ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(containerColor = DarkBackground)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(template.name, color = TextPrimary, fontSize = 14.sp)
                                        Text(
                                            "${template.protocol.uppercase()} | ${template.network.uppercase()} | Reality",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Button(
                                            onClick = {
                                                if (connections.isEmpty()) {
                                                    Toast.makeText(
                                                        context,
                                                        "Сначала добавьте подключение к серверу",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    selectedTemplateForTarget = template
                                                }
                                            },
                                            enabled = !isApplying && connections.isNotEmpty(),
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                            contentPadding = PaddingValues(
                                                horizontal = 8.dp,
                                                vertical = 2.dp
                                            ),
                                            modifier = Modifier.height(28.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Создать", color = Color.White, fontSize = 11.sp)
                                        }
                                        IconButton(
                                            onClick = { templatesViewModel.deleteTemplate(template.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "",
                                                tint = RedStatus,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // БЛОК 2: Масштаб шрифта (Градация от 60% до 140%)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringRes("font_size"), color = TextPrimary, fontSize = 15.sp)
                Slider(
                    value = fontSizeScale,
                    onValueChange = { settingsViewModel.updateFontSizeScale(it) },
                    valueRange = 0.6f..1.4f,
                    steps = 7,
                    modifier = Modifier.height(28.dp)
                )
                Text(
                    "${stringRes("current_scale")}: ${(fontSizeScale * 100).toInt()}%",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // БЛОК 3: Выбор языка приложения
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringRes("app_language"), color = TextPrimary, fontSize = 15.sp)
                    Text(stringRes("select_language"), color = TextSecondary, fontSize = 11.sp)
                }
                TextButton(
                    onClick = {
                        val newLang = if (selectedLanguage == "Русский") "English" else "Русский"
                        settingsViewModel.updateSelectedLanguage(newLang)
                        Toast.makeText(context, "Language: $newLang", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(selectedLanguage, color = AccentCyan, fontSize = 14.sp)
                }
            }
        }

        // БЛОК 4: Защита приложения (Единый консолидированный блок: PIN + Биометрия)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringRes("app_security"), color = TextPrimary, fontSize = 15.sp)

                // 1. PIN-код
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringRes("pin_security"), color = TextPrimary, fontSize = 13.sp)
                        Text(stringRes("pin_security_sub"), color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isPinEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (pinCode.isBlank()) {
                                    showPinDialog = true
                                } else {
                                    settingsViewModel.updatePinEnabled(true)
                                }
                            } else {
                                settingsViewModel.updatePinEnabled(false)
                            }
                        }
                    )
                }

                if (isPinEnabled || pinCode.isNotBlank()) {
                    TextButton(
                        onClick = { showPinDialog = true },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "",
                            tint = AccentCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (pinCode.isBlank()) stringRes("set_pin") else stringRes("change_pin"),
                            color = AccentCyan,
                            fontSize = 12.sp
                        )
                    }
                }

                HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

                // 2. Биометрия (Тумблер внутри блока защиты PIN)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringRes("biometric_security"), color = TextPrimary, fontSize = 13.sp)
                        Text(stringRes("biometric_sub"), color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                val biometricManager = BiometricManager.from(context)
                                val canAuth = biometricManager.canAuthenticate(
                                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
                                )
                                if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
                                    val activity = context as? FragmentActivity
                                    if (activity != null) {
                                        val executor = ContextCompat.getMainExecutor(context)
                                        val prompt = BiometricPrompt(
                                            activity,
                                            executor,
                                            object : BiometricPrompt.AuthenticationCallback() {
                                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                    settingsViewModel.updateBiometricEnabled(true)
                                                    Toast.makeText(
                                                        context,
                                                        "Биометрия включена",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }

                                                override fun onAuthenticationError(
                                                    errorCode: Int,
                                                    errString: CharSequence
                                                ) {
                                                    settingsViewModel.updateBiometricEnabled(false)
                                                    Toast.makeText(
                                                        context,
                                                        "Ошибка биометрии: $errString",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            })

                                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                            .setTitle("Защита 3x-ui Manager")
                                            .setSubtitle("Подтвердите биометрию для активации")
                                            .setNegativeButtonText("Отмена")
                                            .build()

                                        prompt.authenticate(promptInfo)
                                    } else {
                                        settingsViewModel.updateBiometricEnabled(true)
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Биометрия недоступна на устройстве",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    settingsViewModel.updateBiometricEnabled(false)
                                }
                            } else {
                                settingsViewModel.updateBiometricEnabled(false)
                                Toast.makeText(
                                    context,
                                    "Биометрическая защита отключена",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }
        }

        // БЛОК 5: Уведомления и Alerts (Настройка порогов)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringRes("push_alerts"), color = TextPrimary, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${stringRes("cpu_alert")}: ${alertCpuThreshold.toInt()}%",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Slider(
                    value = alertCpuThreshold,
                    onValueChange = { settingsViewModel.updateAlertCpuThreshold(it) },
                    valueRange = 50f..98f,
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        // Информация о версии
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("3X-UI v1.2.2", color = TextSecondary, fontSize = 11.sp)
        }
    }

    if (showPinDialog) {
        PinSetDialog(
            onSavePin = { newPin ->
                settingsViewModel.savePinCode(newPin)
                showPinDialog = false
                Toast.makeText(context, "PIN-код сохранён", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showPinDialog = false }
        )
    }

    if (showJsonImportDialog) {
        JsonImportDialog(
            onImportJson = { jsonStr ->
                val success = templatesViewModel.importJsonTemplate(jsonStr)
                if (success) {
                    Toast.makeText(context, "JSON шаблон импортирован!", Toast.LENGTH_SHORT).show()
                    showJsonImportDialog = false
                } else {
                    Toast.makeText(context, "Ошибка разбора JSON файла", Toast.LENGTH_LONG).show()
                }
            },
            onDismiss = { showJsonImportDialog = false }
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
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TargetSelectDialog(
    connections: List<PanelConnection>,
    onConfirmTarget: (RealityTarget, PanelConnection) -> Unit,
    onDismiss: () -> Unit
) {
    val targets = RealityTargets.targets
    var selectedTarget by remember { mutableStateOf(targets[0]) }
    var selectedConnection by remember {
        mutableStateOf(
            connections.firstOrNull() ?: PanelConnection("", "", "")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выбор цели и сервера", color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Выбор сервера для установки
                Text(
                    "1. Выберите сервер 3x-ui:",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    connections.forEach { conn ->
                        val isConnSelected = selectedConnection.id == conn.id
                        Card(
                            onClick = { selectedConnection = conn },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isConnSelected) AccentBlue else DarkCardBorder,
                                    RoundedCornerShape(6.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isConnSelected) AccentBlue.copy(
                                    alpha = 0.15f
                                ) else DarkCardBg
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isConnSelected,
                                    onClick = { selectedConnection = conn },
                                    colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(conn.name, color = TextPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }

                HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

                // 2. Выбор маскировки Reality
                Text(
                    "2. Выберите маскировку Target & SNI:",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                targets.forEach { target ->
                    val isTargetSelected = selectedTarget.id == target.id
                    Card(
                        onClick = { selectedTarget = target },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isTargetSelected) AccentCyan else DarkCardBorder,
                                RoundedCornerShape(6.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTargetSelected) AccentCyan.copy(
                                alpha = 0.15f
                            ) else DarkCardBg
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isTargetSelected,
                                    onClick = { selectedTarget = target },
                                    colors = RadioButtonDefaults.colors(selectedColor = AccentCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        target.name,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "SNI адресов в списке: ${target.serverNames.size}",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    "✓ Сниффинг отключен. Порт, ключи X25519 (Private/Public), shortIds и spiderX генерируются автоматически.",
                    color = GreenStatus,
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmTarget(selectedTarget, selectedConnection) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Отправить в панель", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextSecondary)
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PinSetDialog(
    onSavePin: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(1) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (step == 1) "Придумайте PIN-код (4 цифры)" else "Повторите PIN-код",
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = if (step == 1) newPin else confirmPin,
                    onValueChange = { input ->
                        if (input.length <= 4 && input.all { it.isDigit() }) {
                            if (step == 1) newPin = input else confirmPin = input
                            errorMsg = null
                        }
                    },
                    label = {
                        Text(
                            if (step == 1) "PIN-код" else "Подтверждение PIN",
                            color = TextSecondary
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary
                    ),
                    singleLine = true
                )

                errorMsg?.let { err ->
                    Text(err, color = RedStatus, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (step == 1) {
                        if (newPin.length == 4) {
                            step = 2
                        } else {
                            errorMsg = "PIN должен состоять из 4 цифр"
                        }
                    } else {
                        if (confirmPin == newPin) {
                            onSavePin(newPin)
                        } else {
                            errorMsg = "PIN-коды не совпадают"
                            confirmPin = ""
                        }
                    }
                }
            ) {
                Text(if (step == 1) "Далее" else "Сохранить", color = AccentCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextSecondary)
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun JsonImportDialog(
    onImportJson: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var jsonText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Импорт JSON шаблона", color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Вставьте JSON содержимое файла шаблона (например TCP_Reality.json):",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = jsonText,
                    onValueChange = {
                        jsonText = it
                        errorMsg = null
                    },
                    placeholder = {
                        Text(
                            "{\n  \"remark\": \"TCP|Reality\",\n  \"port\": 21717,\n  ...\n}",
                            color = TextSecondary.copy(alpha = 0.4f),
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkCardBg,
                        unfocusedContainerColor = DarkCardBg,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                errorMsg?.let { err ->
                    Text(err, color = RedStatus, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (jsonText.isNotBlank()) {
                        onImportJson(jsonText)
                    } else {
                        errorMsg = "Введите корректный JSON"
                    }
                }
            ) {
                Text("Импортировать", color = AccentCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextSecondary)
            }
        }
    )
}