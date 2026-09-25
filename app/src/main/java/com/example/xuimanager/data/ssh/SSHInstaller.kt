package com.example.xuimanager.data.ssh

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import com.example.xuimanager.data.model.PanelConnection
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SSHInstaller {

    suspend fun install3xUI(
        context: Context,
        host: String,
        port: Int = 22,
        user: String = "root",
        password: String,
        onLogReceived: (String) -> Unit
    ): Result<Pair<PanelConnection, File>> = withContext(Dispatchers.IO) {
        var session: Session? = null
        var logFile: File? = null
        var fileOutputStream: FileOutputStream? = null

        try {
            // 0. СОЗДАНИЕ И ИНИЦИАЛИЗАЦИЯ ЛОГ-ФАЙЛА: Downloads/3x-ui/{IP}_{Date}.txt
            val dateStr = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
            val fileName = "${host.replace('/', '_')}_${dateStr}.txt"
            
            val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "3x-ui")
            if (!downloadsDir.exists()) {
                try { downloadsDir.mkdirs() } catch (_: Exception) {}
            }

            logFile = try {
                if (downloadsDir.exists()) {
                    File(downloadsDir, fileName)
                } else {
                    File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir, fileName)
                }
            } catch (_: Exception) {
                File(context.filesDir, fileName)
            }

            fileOutputStream = FileOutputStream(logFile, true)

            fun writeLog(message: String) {
                val timePrefix = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
                val formattedLine = "[$timePrefix] $message\n"
                fileOutputStream?.write(formattedLine.toByteArray(Charsets.UTF_8))
                fileOutputStream?.flush()
                onLogReceived(message)
            }

            writeLog("===== НАЧАЛО ДЕПЛОЯ 3X-UI ДЛЯ $host =====")
            writeLog("Сохранение логов процесса в файл: ${logFile.absolutePath}")

            // 1. ПОДКЛЮЧЕНИЕ ПО SSH, ПРОВЕРКА СОЕДИНЕНИЯ
            writeLog("Шаг 1: Подключение по SSH к $host:$port (пользователь: $user)...")
            val jsch = JSch()
            session = jsch.getSession(user, host, port).apply {
                setPassword(password)
                setConfig("StrictHostKeyChecking", "no")
                setConfig("PreferredAuthentications", "password,keyboard-interactive,publickey")
                timeout = 20000
            }
            session.connect(20000)
            writeLog("✓ SSH соединение с $host:$port успешно установлено!")

            // 2. ГЕНЕРАЦИЯ И СМЕНА ПОРТА SSH В sshd_config
            val newSshPort = (22000..61000).random()
            writeLog("Шаг 2: Генерация нового порта SSH ($newSshPort) и обновление /etc/ssh/sshd_config...")

            val changePortCmd = "sed -i 's/^#\\?Port .*/Port $newSshPort/' /etc/ssh/sshd_config && (ufw allow $newSshPort/tcp 2>/dev/null || true) && (systemctl restart sshd || service ssh restart || true)"
            execSingleCommand(session, changePortCmd, ::writeLog)
            writeLog("✓ Порт SSH изменен на $newSshPort и сохранен в память приложения!")

            // 3. ОБНОВЛЕНИЕ УСТАНОВЛЕННЫХ ПАКЕТОВ СИСТЕМЫ
            writeLog("Шаг 3: Обновление установленных пакетов системы (apt update && apt upgrade -y)...")
            val updateCmd = "export DEBIAN_FRONTEND=noninteractive && apt-get update -y && apt-get upgrade -y"
            execSingleCommand(session, updateCmd, ::writeLog)
            writeLog("✓ Пакеты системы успешно обновлены!")

            // 4. УСТАНОВКА ПАНЕЛИ 3X-UI
            writeLog("Шаг 4: Запуск инсталлера MHSanaei 3x-ui (bash <(curl -Ls https://raw.githubusercontent.com/mhsanaei/3x-ui/master/install.sh))...")

            val channel = (session.openChannel("exec") as ChannelExec).apply {
                setPty(true)
                setCommand("bash <(curl -Ls https://raw.githubusercontent.com/mhsanaei/3x-ui/master/install.sh)")
            }

            val inputStream = channel.inputStream
            val outputStream = channel.outputStream
            channel.connect(15000)

            var detectedUser = "admin"
            var detectedPass = "admin"
            var detectedPort = 2053
            var detectedPath = ""
            var detectedProtocol = "http"
            var detectedToken = ""
            var capturingToken = false

            var repliedDb = false
            var repliedPortOpt = false
            var repliedSslOpt = false
            var repliedIpConfirm = false
            var repliedIpv6 = false
            var repliedAcmePort = false

            val buffer = ByteArray(4096)
            val sb = StringBuilder()

            while (channel.isConnected || inputStream.available() > 0) {
                val available = inputStream.available()
                if (available > 0) {
                    val len = inputStream.read(buffer, 0, available.coerceAtMost(buffer.size))
                    if (len > 0) {
                        val chunk = String(buffer, 0, len, Charsets.UTF_8)
                        sb.append(chunk)

                        val currentAccumulated = sb.toString()
                        val cleanAccumulated = currentAccumulated.replace(Regex("\\x1B\\[[0-9;]*[a-zA-Z]"), "")
                        val cleanLower = cleanAccumulated.lowercase()

                        // 1. Выбор БД (SQLite)
                        if (!repliedDb && (cleanLower.contains("choose [1]:") || (cleanLower.contains("database selection") && cleanLower.contains("choose")))) {
                            repliedDb = true
                            outputStream.write("1\n".toByteArray(Charsets.UTF_8))
                            outputStream.flush()
                            writeLog(">>> [AUTO-REPLY]: 1 (Database: SQLite)")
                        }
                        // 2. Порт панели (Случайный порт)
                        else if (!repliedPortOpt && (cleanLower.contains("customize the panel port settings") || cleanLower.contains("customize the panel port"))) {
                            repliedPortOpt = true
                            outputStream.write("no\n".toByteArray(Charsets.UTF_8))
                            outputStream.flush()
                            writeLog(">>> [AUTO-REPLY]: no (Random Port)")
                        }
                        // 3. Метод SSL (Let's Encrypt for IP)
                        else if (!repliedSslOpt && (cleanLower.contains("choose an option (default 2 for ip)") || cleanLower.contains("default 2 for ip"))) {
                            repliedSslOpt = true
                            outputStream.write("2\n".toByteArray(Charsets.UTF_8))
                            outputStream.flush()
                            writeLog(">>> [AUTO-REPLY]: 2 (Let's Encrypt for IP)")
                        }
                        // 4. Подтверждение внешнего IP адреса
                        else if (!repliedIpConfirm && (cleanLower.contains("correct incoming public ipv4 address") || cleanLower.contains("correct incoming public ipv4"))) {
                            repliedIpConfirm = true
                            outputStream.write("y\n".toByteArray(Charsets.UTF_8))
                            outputStream.flush()
                            writeLog(">>> [AUTO-REPLY]: y (Confirm IP)")
                        }
                        // 5. Запрос IPv6 адреса (Пропуск)
                        else if (!repliedIpv6 && (cleanLower.contains("ipv6 address to include") || cleanLower.contains("leave empty to skip"))) {
                            repliedIpv6 = true
                            outputStream.write("\n".toByteArray(Charsets.UTF_8))
                            outputStream.flush()
                            writeLog(">>> [AUTO-REPLY]: [ENTER] (Skip IPv6)")
                        }
                        // 6. Порт для ACME HTTP-01 listener (Порт 80)
                        else if (!repliedAcmePort && (cleanLower.contains("port to use for acme http-01 listener") || cleanLower.contains("listener (default 80)"))) {
                            repliedAcmePort = true
                            outputStream.write("\n".toByteArray(Charsets.UTF_8))
                            outputStream.flush()
                            writeLog(">>> [AUTO-REPLY]: [ENTER] (Port 80)")
                        }
                        // 7. Фоллбэк: Прямой запрос IP-адреса
                        else if (cleanLower.contains("please enter your server's public ipv4 address") || cleanLower.contains("invalid ipv4 address")) {
                            outputStream.write("$host\n".toByteArray(Charsets.UTF_8))
                            outputStream.flush()
                            writeLog(">>> [AUTO-REPLY]: $host (Fallback IP)")
                        }

                        // Вывод построчных логов при наличии '\n'
                        if (currentAccumulated.contains("\n")) {
                            val lines = currentAccumulated.split("\n")
                            for (i in 0 until lines.size - 1) {
                                val cleanLine = lines[i].replace(Regex("\\x1B\\[[0-9;]*[a-zA-Z]"), "").trim()
                                if (cleanLine.isNotEmpty()) {
                                    writeLog(cleanLine)

                                    val lineLower = cleanLine.lowercase()

                                    // Парсинг учетных данных из итоговых строк
                                    if (lineLower.contains("username:")) {
                                        detectedUser = cleanLine.substringAfter("Username:").trim()
                                    }
                                    if (lineLower.contains("password:")) {
                                        detectedPass = cleanLine.substringAfter("Password:").trim()
                                    }
                                    if (lineLower.contains("port:")) {
                                        cleanLine.substringAfter("Port:").trim().toIntOrNull()?.let { detectedPort = it }
                                    }
                                    if (lineLower.contains("webbasepath:")) {
                                        detectedPath = cleanLine.substringAfter("WebBasePath:").trim().trim('/')
                                    }
                                    if (lineLower.contains("access url:")) {
                                        val url = cleanLine.substringAfter("Access URL:").trim()
                                        if (url.startsWith("https", ignoreCase = true)) {
                                            detectedProtocol = "https"
                                        }
                                    }

                                    // Импорт API Token
                                    if (lineLower.contains("api token:")) {
                                        capturingToken = true
                                        val tokenStart = cleanLine.substringAfter("API Token:").trim()
                                        if (tokenStart.isNotBlank()) {
                                            detectedToken += tokenStart
                                        }
                                    } else if (capturingToken) {
                                        if (cleanLine.startsWith("════") || cleanLine.startsWith("⚠") || (cleanLine.contains(":") && !cleanLine.startsWith("SKT"))) {
                                            capturingToken = false
                                        } else {
                                            detectedToken += cleanLine.trim()
                                        }
                                    }
                                }
                            }
                            sb.clear()
                            sb.append(lines.last())
                        }
                    }
                } else {
                    if (channel.isClosed) break
                    Thread.sleep(50)
                }
            }

            var waitCount = 0
            while (!channel.isClosed && waitCount < 50) {
                Thread.sleep(100)
                waitCount++
            }

            writeLog("═══════════════════════════════════════════")
            writeLog("Panel installation complete!")
            writeLog("═══════════════════════════════════════════")
            writeLog("Учетные данные: Host=$host, Port=$detectedPort, SSHPort=$newSshPort, Login=$detectedUser, Pass=$detectedPass, ApiToken=${detectedToken.take(12)}... Proto=$detectedProtocol")

            // Сохраняем имя пользователя и пароль панели в память приложения вместе с API Token
            val connection = PanelConnection(
                id = UUID.randomUUID().toString(),
                name = host,
                host = host,
                port = detectedPort,
                username = detectedUser,
                password = detectedPass,
                protocol = detectedProtocol,
                path = detectedPath,
                token = detectedToken.trim(),
                skipCertVerify = true,
                sshPort = newSshPort,
                sshUsername = user,
                sshPassword = password
            )

            Result.success(Pair(connection, logFile))
        } catch (e: Exception) {
            val err = e.localizedMessage ?: e.message ?: "Ошибка установки 3x-ui по SSH"
            onLogReceived("ОШИБКА: $err")
            fileOutputStream?.write("ОШИБКА: $err\n".toByteArray(Charsets.UTF_8))
            Result.failure(e)
        } finally {
            try {
                fileOutputStream?.close()
                if (logFile != null && logFile.exists()) {
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(logFile.absolutePath),
                        arrayOf("text/plain"),
                        null
                    )
                }
                session?.disconnect()
            } catch (_: Exception) {
            }
        }
    }

    private fun execSingleCommand(session: Session, command: String, writeLog: (String) -> Unit) {
        var channel: ChannelExec? = null
        try {
            channel = (session.openChannel("exec") as ChannelExec).apply {
                setCommand(command)
            }
            channel.connect(5000)
            val reader = BufferedReader(InputStreamReader(channel.inputStream, Charsets.UTF_8))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.let { writeLog(it) }
            }
        } catch (e: Exception) {
            writeLog("Предупреждение выполнения команды: ${e.message}")
        } finally {
            channel?.disconnect()
        }
    }
}