package com.example.xuimanager.data.ssh

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class SSHInstaller {

    suspend fun install3xUI(
        host: String,
        port: Int = 22,
        user: String = "root",
        password: String,
        onLogReceived: (String) -> Unit
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        var session: Session? = null
        var channel: ChannelExec? = null
        try {
            onLogReceived("Подключение по SSH к $host:$port...")

            val jsch = JSch()
            session = jsch.getSession(user, host, port).apply {
                setPassword(password)
                setConfig("StrictHostKeyChecking", "no")
                setConfig("PreferredAuthentications", "password,keyboard-interactive,publickey")
                timeout = 15000
            }
            session.connect(15000)

            onLogReceived("✓ SSH соединение успешно установлено!")
            onLogReceived("Запуск скрипта установки MHSanaei 3x-ui...")

            channel = (session.openChannel("exec") as ChannelExec).apply {
                setPty(true)
                setCommand("printf 'y\\n' | bash <(curl -Ls https://raw.githubusercontent.com/mhsanaei/3x-ui/master/install.sh)")
            }

            val inputStream = channel.inputStream
            channel.connect(10000)

            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.let { cleanLine ->
                    val sanitized = cleanLine.replace(Regex("\\x1B\\[[0-9;]*[a-zA-Z]"), "").trim()
                    if (sanitized.isNotEmpty()) {
                        onLogReceived(sanitized)
                    }
                }
            }

            var waitCount = 0
            while (!channel.isClosed && waitCount < 50) {
                Thread.sleep(100)
                waitCount++
            }

            val exitStatus = channel.exitStatus
            onLogReceived("Код завершения выполнения: $exitStatus")

            if (exitStatus == 0 || exitStatus == -1) {
                onLogReceived("✓ Установка 3x-ui завершена!")
                Result.success(true)
            } else {
                onLogReceived("ОШИБКА: Скрипт установки вернул код $exitStatus")
                Result.failure(Exception("Install script failed with exit code $exitStatus"))
            }
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: e.message ?: "Неизвестная ошибка SSH"
            onLogReceived("ОШИБКА SSH: $errorMsg")
            Result.failure(e)
        } finally {
            try {
                channel?.disconnect()
                session?.disconnect()
            } catch (_: Exception) {
            }
        }
    }
}