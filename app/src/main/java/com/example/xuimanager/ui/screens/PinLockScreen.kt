package com.example.xuimanager.ui.screens

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.xuimanager.ui.theme.AccentCyan
import com.example.xuimanager.ui.theme.DarkBackground
import com.example.xuimanager.ui.theme.DarkCardBg
import com.example.xuimanager.ui.theme.DarkCardBorder
import com.example.xuimanager.ui.theme.RedStatus
import com.example.xuimanager.ui.theme.TextPrimary
import com.example.xuimanager.ui.theme.TextSecondary

@Composable
fun PinLockScreen(
    targetPin: String,
    isBiometricEnabled: Boolean,
    onUnlocked: () -> Unit
) {
    var inputPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val triggerBiometrics = {
        if (isBiometricEnabled) {
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
                                onUnlocked()
                            }

                            override fun onAuthenticationError(
                                errorCode: Int,
                                errString: CharSequence
                            ) {
                                errorMessage = errString.toString()
                            }
                        })

                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("3x-ui Manager")
                        .setSubtitle("Разблокировка с помощью биометрии")
                        .setNegativeButtonText("Ввести PIN")
                        .build()

                    prompt.authenticate(promptInfo)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (isBiometricEnabled) {
            triggerBiometrics()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(DarkCardBg, CircleShape)
                    .border(1.dp, AccentCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "",
                    tint = AccentCyan,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Приложение заблокировано",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                errorMessage ?: "Введите PIN-код для входа",
                color = if (errorMessage != null) RedStatus else TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (i in 0 until 4) {
                    val isFilled = i < inputPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(if (isFilled) AccentCyan else DarkCardBg, CircleShape)
                            .border(1.dp, if (isFilled) AccentCyan else TextSecondary, CircleShape)
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("bio", "0", "del")
            )

            keys.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    row.forEach { key ->
                        KeypadButton(
                            key = key,
                            isBiometricEnabled = isBiometricEnabled,
                            onClick = {
                                when (key) {
                                    "del" -> {
                                        if (inputPin.isNotEmpty()) {
                                            inputPin = inputPin.dropLast(1)
                                            errorMessage = null
                                        }
                                    }

                                    "bio" -> triggerBiometrics()
                                    else -> {
                                        if (inputPin.length < 4) {
                                            val newPin = inputPin + key
                                            inputPin = newPin
                                            errorMessage = null
                                            if (newPin.length == 4) {
                                                if (newPin == targetPin) {
                                                    onUnlocked()
                                                } else {
                                                    errorMessage = "Неверный PIN-код"
                                                    inputPin = ""
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun KeypadButton(
    key: String,
    isBiometricEnabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        color = DarkCardBg,
        border = BorderStroke(1.dp, DarkCardBorder)
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (key) {
                "del" -> Icon(
                    Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )

                "bio" -> {
                    if (isBiometricEnabled) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = "",
                            tint = AccentCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                else -> Text(
                    key,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}