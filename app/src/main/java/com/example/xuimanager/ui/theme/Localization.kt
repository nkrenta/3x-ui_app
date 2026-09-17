package com.example.xuimanager.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage { RU, EN }

val LocalAppLanguage = compositionLocalOf { AppLanguage.RU }

object Strings {
    fun get(key: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> enStrings[key] ?: ruStrings[key] ?: key
        AppLanguage.RU -> ruStrings[key] ?: key
    }

    private val ruStrings = mapOf(
        "app_settings" to "Настройки приложения",
        "connections_title" to "Подключения к 3x-ui",
        "connections_sub" to "Управление серверами и панелями",
        "add" to "Добавить",
        "no_connections" to "Нет подключенных панелей",
        "no_connections_sub" to "Нажмите 'Добавить' для подключения к 3x-ui",
        "font_size" to "Размер шрифта интерфейса",
        "current_scale" to "Текущий масштаб",
        "app_language" to "Язык приложения",
        "select_language" to "Выберите язык интерфейса",
        "app_security" to "Защита приложения",
        "pin_security" to "Защита PIN-кодом",
        "pin_security_sub" to "Блокировка приложения при входе",
        "biometric_security" to "Защита биометрией",
        "biometric_sub" to "Вход по Face ID / Отпечатку пальца",
        "set_pin" to "Установить PIN-код",
        "change_pin" to "Изменить PIN-код",
        "push_alerts" to "Настройка Push-уведомлений (Alerts)",
        "cpu_alert" to "Уведомлять при превышении ЦП",
        "dashboard" to "Сервера",
        "servers" to "Сервера",
        "connected_panels_title" to "Подключенные панели 3X-UI",
        "servers_subtitle" to "Управление и мониторинг серверов 3x-ui",
        "total_servers" to "Всего серверов",
        "online" to "Онлайн",
        "offline" to "Офлайн",
        "no_servers" to "Нет подключенных серверов",
        "no_servers_sub" to "Нажмите 'Добавить' для подключения панели 3x-ui",
        "refresh_all" to "Обновить все",
        "server_info" to "Информация о сервере",
        "test_connection" to "Проверить подключение",
        "restart_panel" to "Перезапуск панели",
        "restart_xray" to "Перезапуск Xray",
        "edit_connection" to "Редактировать подключение",
        "delete_connection" to "Удалить подключение",
        "cpu_params" to "1. Параметры CPU",
        "cores_threads" to "Ядра / Потоки",
        "cpu_frequency" to "Частота CPU",
        "ram_disk" to "2. Память и Диск",
        "ram_memory" to "Память (RAM)",
        "disk_storage" to "Накопитель (Disk)",
        "xray_title" to "3. Xray Core",
        "xray_version" to "Версия Xray",
        "uptime_xray" to "Время работы (Xray)",
        "panel_title" to "4. Панель 3x-ui",
        "panel_version" to "Версия панели",
        "uptime_panel" to "Время работы (Панель)",
        "net_traffic" to "5. Сеть и трафик",
        "public_ip" to "Публичный IPv4",
        "traffic" to "Трафик",
        "avg_period" to "СРЕДНЕЕ ЗА ПЕРИОД",
        "close" to "Закрыть",
        "users" to "Клиенты",
        "inbounds" to "Подключения",
        "templates" to "Шаблоны",
        "ssh" to "SSH",
        "settings" to "Настройки"
    )

    private val enStrings = mapOf(
        "app_settings" to "App Settings",
        "connections_title" to "3x-ui Connections",
        "connections_sub" to "Manage servers and panels",
        "add" to "Add",
        "no_connections" to "No connected panels",
        "no_connections_sub" to "Click 'Add' to connect to 3x-ui",
        "font_size" to "Interface Font Size",
        "current_scale" to "Current scale",
        "app_language" to "App Language",
        "select_language" to "Select interface language",
        "app_security" to "App Security",
        "pin_security" to "PIN Code Protection",
        "pin_security_sub" to "Lock application on launch",
        "biometric_security" to "Biometric Protection",
        "biometric_sub" to "Sign in with Face ID / Fingerprint",
        "set_pin" to "Set PIN Code",
        "change_pin" to "Change PIN Code",
        "push_alerts" to "Push Notifications (Alerts)",
        "cpu_alert" to "Notify when CPU exceeds",
        "dashboard" to "Servers",
        "servers" to "Servers",
        "connected_panels_title" to "Connected 3X-UI Panels",
        "servers_subtitle" to "Manage and monitor 3x-ui servers",
        "total_servers" to "Total Servers",
        "online" to "Online",
        "offline" to "Offline",
        "no_servers" to "No connected servers",
        "no_servers_sub" to "Click 'Add' to connect to 3x-ui panel",
        "refresh_all" to "Refresh All",
        "server_info" to "Server Information",
        "test_connection" to "Test Connection",
        "restart_panel" to "Restart Panel",
        "restart_xray" to "Restart Xray",
        "edit_connection" to "Edit Connection",
        "delete_connection" to "Delete Connection",
        "cpu_params" to "1. CPU Parameters",
        "cores_threads" to "Cores / Threads",
        "cpu_frequency" to "CPU Frequency",
        "ram_disk" to "2. RAM & Storage",
        "ram_memory" to "RAM Memory",
        "disk_storage" to "Disk Storage",
        "xray_title" to "3. Xray Core",
        "xray_version" to "Xray Version",
        "uptime_xray" to "Uptime (Xray)",
        "panel_title" to "4. 3x-ui Panel",
        "panel_version" to "Panel Version",
        "uptime_panel" to "Uptime (Panel)",
        "net_traffic" to "5. Network & Traffic",
        "public_ip" to "Public IPv4",
        "traffic" to "Traffic",
        "avg_period" to "AVG IO RATE",
        "close" to "Close",
        "users" to "Clients",
        "inbounds" to "Connections",
        "templates" to "Templates",
        "ssh" to "SSH",
        "settings" to "Settings"
    )
}

@Composable
fun stringRes(key: String): String {
    val lang = LocalAppLanguage.current
    return Strings.get(key, lang)
}