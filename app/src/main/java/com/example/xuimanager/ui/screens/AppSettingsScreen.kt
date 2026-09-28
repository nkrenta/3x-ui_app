package com.example.xuimanager.ui.screens

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.xuimanager.ui.theme.OnPrimary
import com.example.xuimanager.ui.theme.OnSurfaceVariant
import com.example.xuimanager.ui.theme.OutlineVariant
import com.example.xuimanager.ui.theme.PrimaryContainer
import com.example.xuimanager.ui.theme.RedStatus
import com.example.xuimanager.ui.theme.SurfaceContainerLow
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
    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()
    val isPinEnabled by settingsViewModel.isPinEnabled.collectAsState()

    val connections by connectionsViewModel.connections.collectAsState()
    val templates by templatesViewModel.templates.collectAsState()
    val isApplying by templatesViewModel.isApplying.collectAsState()
    val applyResult by templatesViewModel.applyResult.collectAsState()

    val context = LocalContext.current
    var showPinDialog by remember { mutableStateOf(false) }
    var showJsonImportDialog by remember { mutableStateOf(false) }
    var selectedTemplateForTarget by remember { mutableStateOf<JsonTemplateItem?>(null) }
    var editingTemplate by remember { mutableStateOf<JsonTemplateItem?>(null) }
    var isTemplatesExpanded by remember { mutableStateOf(false) }

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
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // БЛОК 1: Шаблоны 3x-ui (Сворачиваемый)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isTemplatesExpanded = !isTemplatesExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (isTemplatesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "",
                            tint = AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(stringRes("templates"), color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Импорт, редактирование и применение шаблонов",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
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
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Импорт JSON", color = DarkBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // СВОРАЧИВАЕМАЯ СЕКЦИЯ С ШАБЛОНАМИ
                AnimatedVisibility(
                    visible = isTemplatesExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

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
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Нет шаблонов", color = TextPrimary, fontSize = 13.sp)
                                    Text(
                                        "Импортируйте JSON файл шаблона (например TCP_Reality.json)",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        } else {
                            templates.forEach { template ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp)),
                                    colors = CardDefaults.cardColors(containerColor = DarkBackground)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(template.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                "${template.protocol.uppercase()} • ${template.network.uppercase()}",
                                                color = TextSecondary,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = { editingTemplate = template },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Редактировать",
                                                    tint = AccentCyan,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

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
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("Создать", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
        }

        // БЛОК 2: Масштаб шрифта (Градация от 60% до 140%)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringRes("font_size"), color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${(fontSizeScale * 100).toInt()}%",
                        color = AccentCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = fontSizeScale.coerceIn(0.6f, 1.4f),
                    onValueChange = { settingsViewModel.updateFontSizeScale(it) },
                    valueRange = 0.6f..1.4f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentCyan,
                        activeTrackColor = AccentBlue,
                        inactiveTrackColor = DarkCardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 0.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Мелкий (60%)", color = TextSecondary, fontSize = 12.sp)
                    Text("Стандарт (100%)", color = TextSecondary, fontSize = 12.sp)
                    Text("Крупный (140%)", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }

        // БЛОК: Язык приложения (Слева) и Стиль шрифта (Справа) в одной строке
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Язык приложения (Слева)
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                shape = RoundedCornerShape(10.dp)
            ) {
                var showLanguageMenu by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        stringRes("app_language"),
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showLanguageMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCardBorder),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    selectedLanguage,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false },
                            modifier = Modifier.background(DarkCardBg)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Русский", color = TextPrimary, fontSize = 13.sp) },
                                onClick = {
                                    settingsViewModel.updateSelectedLanguage("Русский")
                                    showLanguageMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("English", color = TextPrimary, fontSize = 13.sp) },
                                onClick = {
                                    settingsViewModel.updateSelectedLanguage("English")
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // 2. Стиль шрифта (Справа)
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                shape = RoundedCornerShape(10.dp)
            ) {
                var showFontMenu by remember { mutableStateOf(false) }
                val currentFontFamily by settingsViewModel.appFontFamily.collectAsState()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Стиль шрифта",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showFontMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCardBorder),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    currentFontFamily,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showFontMenu,
                            onDismissRequest = { showFontMenu = false },
                            modifier = Modifier.background(DarkCardBg)
                        ) {
                            val fontOptions = listOf(
                                "System",
                                "Condensed",
                                "Medium",
                                "Light",
                                "Serif",
                                "Monospace",
                                "Default"
                            )
                            fontOptions.forEach { fontName ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            fontName,
                                            color = TextPrimary,
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        settingsViewModel.updateAppFontFamily(fontName)
                                        showFontMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // БЛОК 4: Защита и Безопасность (PIN & Биометрия)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringRes("app_security"), color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)

                // 1. ПИН-код
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Защита PIN-кодом", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(
                            if (isPinEnabled) "ПИН-код активирован" else "Отключено",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = isPinEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                showPinDialog = true
                            } else {
                                settingsViewModel.updatePinEnabled(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentBlue,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkCardBorder
                        )
                    )
                }

                if (isPinEnabled) {
                    Button(
                        onClick = { showPinDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Изменить PIN-код", color = TextPrimary, fontSize = 11.sp)
                    }
                }

                HorizontalDivider(color = DarkCardBorder, thickness = 0.5.dp)

                // 2. Биометрия (Отпечаток / Face ID)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Вход по отпечатку / Face ID", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(
                            "Использовать системную биометрию",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                val fragmentActivity =
                                    context as? FragmentActivity
                                if (fragmentActivity != null) {
                                    BiometricPromptHelper.authenticate(
                                        activity = fragmentActivity,
                                        title = "Подтвердите биометрию",
                                        subtitle = "Для включения входа по отпечатку пальца",
                                        onSuccess = {
                                            settingsViewModel.updateBiometricEnabled(true)
                                            Toast.makeText(
                                                context,
                                                "Биометрия успешно подключена",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { err ->
                                            settingsViewModel.updateBiometricEnabled(false)
                                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    settingsViewModel.updateBiometricEnabled(true)
                                }
                            } else {
                                settingsViewModel.updateBiometricEnabled(false)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentBlue,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkCardBorder
                        )
                    )
                }
            }
        }
    }

    if (showPinDialog) {
        PinSetDialog(
            onConfirm = { newPin ->
                settingsViewModel.savePinCode(newPin)
                settingsViewModel.updatePinEnabled(true)
                showPinDialog = false
            },
            onDismiss = { showPinDialog = false }
        )
    }

    if (showJsonImportDialog) {
        JsonImportDialog(
            onImport = { jsonStr ->
                val success = templatesViewModel.importJsonTemplate(jsonStr)
                if (success) {
                    Toast.makeText(context, "Шаблон успешно импортирован", Toast.LENGTH_SHORT).show()
                    showJsonImportDialog = false
                } else {
                    Toast.makeText(context, "Ошибка разбора JSON шаблона", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showJsonImportDialog = false }
        )
    }

    editingTemplate?.let { template ->
        EditTemplateDialog(
            template = template,
            onSave = { appName, panelRemark, protocol, port, network ->
                templatesViewModel.updateTemplate(template.id, appName, panelRemark, protocol, port, network)
                Toast.makeText(context, "Шаблон '$appName' обновлен!", Toast.LENGTH_SHORT).show()
                editingTemplate = null
            },
            onDismiss = { editingTemplate = null }
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
fun EditTemplateDialog(
    template: JsonTemplateItem,
    onSave: (String, String, String, Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var appName by remember { mutableStateOf(template.name) }
    var panelRemark by remember { mutableStateOf(template.inbound.remark ?: template.name) }
    var protocol by remember { mutableStateOf(template.protocol) }
    var network by remember { mutableStateOf(template.network) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Редактирование шаблона",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("Название шаблона в приложении", color = OnSurfaceVariant, fontSize = 11.sp) },
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

                OutlinedTextField(
                    value = panelRemark,
                    onValueChange = { panelRemark = it },
                    label = { Text("Название Inbound в панели 3x-ui", color = OnSurfaceVariant, fontSize = 11.sp) },
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
                        value = protocol,
                        onValueChange = { protocol = it },
                        label = { Text("Протокол", color = OnSurfaceVariant, fontSize = 11.sp) },
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

                    OutlinedTextField(
                        value = network,
                        onValueChange = { network = it },
                        label = {
                            Text(
                                "Сеть / Поток",
                                color = OnSurfaceVariant,
                                fontSize = 11.sp
                            )
                        },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        appName.trim(),
                        panelRemark.trim(),
                        protocol.trim(),
                        template.port,
                        network.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer, contentColor = OnPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Сохранить изменения", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
fun TargetSelectDialog(
    connections: List<PanelConnection>,
    onConfirmTarget: (RealityTarget, PanelConnection) -> Unit,
    onDismiss: () -> Unit
) {
    val targets = RealityTargets.targets
    var selectedTarget by remember { mutableStateOf(targets[0]) }
    var selectedConnection by remember {
        mutableStateOf(connections.firstOrNull() ?: PanelConnection("", "", ""))
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
                                        fontSize = 10.sp,
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
                Text("Отправить в панель", color = Color.White, fontSize = 12.sp)
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
fun PinSetDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Установка PIN-кода", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Введите цифровой PIN-код для входа в приложение:", color = TextSecondary, fontSize = 12.sp)
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (pin.isNotBlank()) onConfirm(pin) },
                enabled = pin.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Сохранить PIN", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = TextSecondary) }
        }
    )
}

@Composable
fun JsonImportDialog(
    onImport: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var jsonText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Импорт JSON шаблона", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Вставьте сырой JSON объект шаблона подключения:", color = TextSecondary, fontSize = 12.sp)
                OutlinedTextField(
                    value = jsonText,
                    onValueChange = { jsonText = it },
                    minLines = 5,
                    maxLines = 10,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (jsonText.isNotBlank()) onImport(jsonText) },
                enabled = jsonText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = DarkBackground),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Импортировать", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = TextSecondary) }
        }
    )
}

object BiometricPromptHelper {
    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Отмена")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}