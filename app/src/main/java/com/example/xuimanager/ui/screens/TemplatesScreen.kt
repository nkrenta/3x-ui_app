package com.example.xuimanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xuimanager.data.model.InboundTemplate
import com.example.xuimanager.ui.theme.AccentBlue
import com.example.xuimanager.ui.theme.DarkBackground
import com.example.xuimanager.ui.theme.DarkCardBg
import com.example.xuimanager.ui.theme.DarkCardBorder
import com.example.xuimanager.ui.theme.TextPrimary
import com.example.xuimanager.ui.theme.TextSecondary

@Composable
fun TemplatesScreen(
    onApplyTemplate: (InboundTemplate) -> Unit
) {
    // Дефолтные шаблоны
    val templates = remember {
        listOf(
            InboundTemplate(
                "1",
                "VLESS Reality (gRPC 443)",
                "vless",
                443,
                "grpc",
                "Reality-Fast",
                true,
                "dl.google.com"
            ),
            InboundTemplate(
                "2",
                "Shadowsocks 2022 (TCP)",
                "shadowsocks",
                8388,
                "tcp",
                "SS-2022",
                false
            ),
            InboundTemplate(
                "3",
                "VLESS WebSocket TLS",
                "vless",
                8443,
                "ws",
                "VLESS-WS",
                false,
                "cloudflare.com"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text("Шаблоны Входящих", color = TextPrimary, fontSize = 20.sp)
        Text("Создавайте подключение к 3x-ui в 1 клик", color = TextSecondary, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(templates) { template ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkCardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(template.name, color = TextPrimary, fontSize = 16.sp)
                            Text(
                                "${template.protocol.uppercase()} | Port: ${template.port} | Net: ${template.network}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = { onApplyTemplate(template) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                        ) {
                            Text("Создать")
                        }
                    }
                }
            }
        }
    }
}