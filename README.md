# 🚀 3X-UI Manager (Android App)

[![Min SDK](https://img.shields.io/badge/Min_SDK-26%2B-00F5A0?style=for-the-badge&logo=android&logoColor=black)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-06B6D4?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-38BDF8?style=for-the-badge&logo=jetpackcompose&logoColor=black)](https://developer.android.com/jetpack/compose)
[![Version](https://img.shields.io/badge/Version-1.3.2.2-00F5A0?style=for-the-badge)](https://github.com/nkrenta/3x-ui_app)

**3X-UI Manager** — современное мобильное Android-приложение для удобного управления, настройки и
мониторинга панелей **3x-ui** (MHSanaei / X-UI) по защищенному протоколу HTTPS / REST API. Приложение выполнено
в стилистике панели 3x-ui в стиле Glassmorphism с поддержкой динамической генерации Reality-ключей, массового управления пользователями, биометрии и автоустановки панелей по SSH.

---

## ✨ Ключевые возможности

### 🖥️ 1. Мониторинг и управление серверами (`Dashboard`)
* **Живой мониторинг Ping**: Автоматический легковесный опрос задержки серверов с цветовыми индикаторами.
* **Сводка ресурсов**: Точные шкалы нагрузки CPU Load, RAM и NVMe диска.
* **Быстрое меню реквизитов**: Копирование URL панели, логина, пароля и API Token в 1 клик.
* **Управление службами**: Перезапуск Xray Core и службы панели 3x-ui прямо с карточки сервера.

### 🔗 2. Шаблоны и подключения (`Inbounds & Templates`)
* **Готовые Reality-шаблоны**: Выдвижная лента шаблонов (`Reality TCP`, `xHTTP`, `gRPC`, `Trojan`).
* **Автоматическая генерация параметров**: Ключи X25519, подбор портов, shortIds, spiderX и маскировка Target/SNI.
* **Авто-префикс флага страны**: Автоматическое добавление эмодзи флага страны сервера в начало названия Inbound.

### 👥 3. Управление пользователями (`Users`)
* **Полный редактор клиентов**: Настройка лимитов ГБ, срока в днях, лимитов IP/HWID и привязка к Inbounds.
* **Режим массового выбора (Multi-Select)**: Выделение нескольких клиентов для массового ВКЛ/ВЫКЛ, сброса трафика и удаления.
* **Онлайн-статус и даты**: Зеленая точка активности, расчет оставшегося срока и последнего входа в сеть.
* **QR-коды и ссылки**: Генерация QR-кодов на базе ZXing, просмотр логов IP-адресов, зарегистрированных HWID и экспорт в JSON.

### 🛠️ 4. SSH Деплой и Оркестрация (`SSH Installer`)
* **Автоматическая развертка**: Установка MHSanaei 3x-ui на чистый VPS по SSH в 1 клик.
* **Авто-ответ в PTY-потоке**: Автоматическое прохождение всех 8 интерактивных промптов инсталлятора.
* **Сворачиваемая карточка ввода**: Авто-сворачивание формы ввода после старта установки для удобного просмотра логов.
* **Сохранение отчетов**: Автоматическое сохранение логов в папку `Downloads/3x-ui/`.

### ⚙️ 5. Глобальные настройки (`Settings`)
* **Глобальные параметры подписок 3x-ui**: Редактирование `remarkTemplate`, `subTitle`, `subSupportUrl`, `subShowIdentityOnAllLinks` и правил Happ.
* **Выбор из 7 системных шрифтов**: `System`, `Condensed`, `Medium`, `Light`, `Serif`, `Monospace`, `Default`.
* **Масштабирование текста (60%–140%)**: Динамическое изменение размера шрифтов без раздвигания системных отступов.
* **Безопасность**: Защита входа по PIN-коду и системной биометрии (Fingerprint / Face ID).

---

## 📚 Подробная документация

Подробное техническое описание архитектуры, структуры файлов, всех функций REST API и полное руководство пользователя доступны в файле **[`ALL.md`](ALL.md)**.

---

## 🛠️ Технологический стек

* **Язык**: [Kotlin 2.0](https://kotlinlang.org)
* **UI**: [Jetpack Compose (Material 3)](https://developer.android.com/jetpack/compose)
* **Архитектура**: MVVM, Coroutines, StateFlow
* **Сетевой слой**: Retrofit 2, OkHttp 4, Gson
* **QR-Коды**: ZXing Core 3.5.3
* **SSH-клиент**: [JSch 2.28.7](https://github.com/mwiede/jsch) (Ed25519 & RSA-SHA2)
* **Хранилище**: AndroidX DataStore Preferences
* **Безопасность**: AndroidX Biometric API

---

## 🚀 Сборка проекта

### Требования
* Android Studio **2024.1+**
* JDK **17**
* Target SDK **35**, Min SDK **26** (Android 8.0+)

```bash
git clone https://github.com/nkrenta/3x-ui_app.git
cd 3x-ui_app
./gradlew app:assembleDebug
```

---

## 📄 Лицензия

Проект распространяется под лицензией [MIT License](LICENSE).
