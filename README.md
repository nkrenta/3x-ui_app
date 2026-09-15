# 🚀 3X-UI Manager (Android App)

[![Android SDK](https://img.shields.io/badge/API-26%2B%20%28Android%208.0%2B%20)-00F5A0?style=for-the-badge&logo=android&logoColor=black)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-06B6D4?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-38BDF8?style=for-the-badge&logo=jetpackcompose&logoColor=black)](https://developer.android.com/jetpack/compose)
[![Version](https://img.shields.io/badge/Version-1.2.2.1-00F5A0?style=for-the-badge)](https://github.com/nkrenta/3x-ui_app)

**3X-UI Manager** — современное мобильное Android-приложение для удобного управления, настройки и мониторинга панелей **3x-ui** (X-UI) по защищенному протоколу HTTPS / REST API. Приложение выполнено в кибер-панковском интерфейсе **Cyber-Ops / Dark Theme** на базе системы **Google Stitch AI** с поддержкой динамической генерации Reality-ключей, биометрии и автоустановки панелей по SSH.

---

## ✨ Ключевые возможности

### 🖥️ Мониторинг и управление серверами
* **Живой мониторинг Ping**: Автоматический легковесный опрос доступности серверов и задержки каждые 2 секунды с цветовой индикацией:
  * 🟢 **0–150 ms** (Отличный отклик)
  * 🟡 **150–500 ms** (Умеренная задержка)
  * 🔴 **> 500 ms** / Офлайн
* **Детальная телеметрия сервера (Read-Only)**: При нажатии на карточку сервера выводится подробная сводка:
  * **Параметры CPU**: Количество ядер, потоков, тактовая частота в МГц.
  * **Память и Диск**: Общий объем оперативной памяти (RAM) и накопителя (Disk).
  * **Службы**: Версии Xray Core и 3x-ui Панели, время непрерывной работы (Uptime).
  * **Сеть**: Публичный IPv4, общий переданный/принятый трафик и средняя скорость ввода-вывода (IO Rate).
* **Управление службами**: Быстрый перезапуск панели 3x-ui и службы Xray Core в один клик.

### 🛠️ Предустановленные шаблоны Reality
* **Наборы готовых профилей**:
  1. `Подключение №1: Reality | TCP` (VLESS)
  2. `Подключение №2: Reality | xHTTP` (VLESS)
  3. `Подключение №3: Reality | gRPC` (VLESS)
  4. `Подключение №4: Reality | TCP | trojan` (Trojan)
* **Автоматическая генерация параметров**:
  * Генерация ключевой пары X25519 (Private & Public Keys) в формате URL-safe Base64.
  * Подбор случайного свободного порта (`15000..60000`).
  * Генерация массива `shortIds` и случайного пути `spiderX`.
  * Выбор целей маскировки Target & SNI (`Samsung`, `Sony`, `NVIDIA`).

### 🛡️ Безопасность и защищённость
* **Защита входа в приложение**: Авторизация по PIN-коду и биометрии (Face ID / Отпечаток пальца).
* **Безопасная работа с сертификатами**: Поддержка пропуска проверки самоподписанных SSL-сертификатов (HTTPS).
* **Поддержка CSRF & API-токенов**: Автоматический перехват сессионных куки и токенов безопасности 3x-ui.

### 🌐 Локализация и кэширование
* **Мультиязычность**: Мгновенное переключение интерфейса между **Русским** и **English**.
* **Масштабирование шрифтов**: Градация шрифта интерфейса от **60% до 140%**.
* **Кэширование DataStore**: Автоматическое сохранение добавленных подключений на диске устройства.

---

## 🛠️ Технологический стек

* **Язык**: [Kotlin 2.0](https://kotlinlang.org)
* **UI**: [Jetpack Compose (Material 3)](https://developer.android.com/jetpack/compose), [Custom Graphics](https://developer.android.com/jetpack/compose/graphics)
* **Архитектура**: MVVM, Kotlin Coroutines, StateFlow
* **Сетевой слой**: Retrofit 2, OkHttp 4, Gson
* **SSH-клиент**: [JSch 2.28.7](https://github.com/mwiede/jsch) (Поддержка Ed25519 и RSA-SHA2)
* **Хранение данных**: AndroidX DataStore Preferences
* **Безопасность**: AndroidX Biometric API

---

## 🚀 Сборка проекта

### Требования
* Android Studio **2024.1 (Koala/Panda/Quail)** или новее
* JDK **17**
* Target SDK **35**, Min SDK **26** (Android 8.0+)

### Инструкция по сборке

1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/nkrenta/3x-ui_app.git
   cd 3x-ui_app
   ```

2. Соберите Debug APK через Gradle:
   ```bash
   ./gradlew app:assembleDebug
   ```
   Собранный файл будет доступен по пути: `app/build/outputs/apk/debug/app-debug.apk`

---

## 📱 Структура экранов

1. **`Подключенные панели 3X-UI` (`Сервера`)** — Главный экран со списком ваших серверов, пингом, телеметрией и быстрыми кнопками управления.
2. **`Пользователи`** — Управление клиентами и инбаундами панелей.
3. **`SSH Автоустановка`** — Автоматическая развертка 3x-ui панели на чистом Linux VDS/VPS сервере по SSH.
4. **`Настройки`** — Управление JSON-шаблонами, масштабом текста, языком интерфейса и биометрической защитой.

---

## 📄 Лицензия

Проект распространяется под лицензией [MIT License](LICENSE).
