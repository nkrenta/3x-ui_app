package com.example.xuimanager

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.xuimanager.ui.screens.*
import com.example.xuimanager.ui.theme.*
import com.example.xuimanager.ui.viewmodel.*

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val fontSizeScale by settingsViewModel.fontSizeScale.collectAsState()
            val selectedLanguage by settingsViewModel.selectedLanguage.collectAsState()
            val isPinEnabled by settingsViewModel.isPinEnabled.collectAsState()
            val pinCode by settingsViewModel.pinCode.collectAsState()
            val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()

            var isUnlocked by remember { mutableStateOf(false) }
            val appLang = if (selectedLanguage == "English") AppLanguage.EN else AppLanguage.RU

            CompositionLocalProvider(LocalAppLanguage provides appLang) {
                ThreeXUITheme(fontSizeScale = fontSizeScale) {
                    val isLockedNeeded = (isPinEnabled || isBiometricEnabled) && (pinCode.isNotBlank() || isBiometricEnabled)
                    if (isLockedNeeded && !isUnlocked) {
                        PinLockScreen(
                            targetPin = pinCode,
                            isBiometricEnabled = isBiometricEnabled,
                            onUnlocked = { isUnlocked = true }
                        )
                    } else {
                        MainAppStructure(settingsViewModel = settingsViewModel)
                    }
                }
            }
        }
    }
}

private data class NavigationItem(
    val route: String,
    val labelKey: String,
    val icon: ImageVector
)

@Composable
fun MainAppStructure(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val connectionsViewModel: ConnectionsViewModel = viewModel()
    val inboundsViewModel: InboundsViewModel = viewModel()
    val usersViewModel: UsersViewModel = viewModel()
    val sshInstallerViewModel: SSHInstallerViewModel = viewModel()
    val templatesViewModel: TemplatesViewModel = viewModel()

    val navItems = listOf(
        NavigationItem("dashboard", "dashboard", Icons.Default.Dns),
        NavigationItem("inbounds", "inbounds", Icons.Default.CellTower),
        NavigationItem("users", "users", Icons.Default.People),
        NavigationItem("ssh", "ssh", Icons.Default.Terminal),
        NavigationItem("app_settings", "settings", Icons.Default.Settings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(28.dp),
                            clip = false,
                            ambientColor = Color.Black,
                            spotColor = Color.Black
                        ),
                    color = DarkCardBg.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        navItems.forEach { item ->
                            val selected = currentRoute == item.route
                            val label = stringRes(item.labelKey)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .pointerInput(item.route) {
                                        detectTapGestures(
                                            onTap = {
                                                if (currentRoute != item.route) {
                                                    navController.navigate(item.route) {
                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                    }
                                                }
                                            },
                                            onLongPress = {
                                                Toast.makeText(context, label, Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight(0.88f)
                                        .fillMaxWidth(0.88f)
                                        .background(
                                            if (selected) AccentBlue.copy(alpha = 0.28f) else Color.Transparent,
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .border(
                                            if (selected) 1.dp else 0.dp,
                                            if (selected) AccentCyan.copy(alpha = 0.5f) else Color.Transparent,
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .padding(vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        if (selected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(AccentCyan, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                        }

                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = label,
                                            tint = if (selected) AccentCyan else TextSecondary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 6 } },
            exitTransition = { fadeOut(tween(220)) + slideOutHorizontally(tween(220)) { -it / 6 } },
            popEnterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { -it / 6 } },
            popExitTransition = { fadeOut(tween(220)) + slideOutHorizontally(tween(220)) { it / 6 } }
        ) {
            composable("dashboard") {
                DashboardScreen(connectionsViewModel = connectionsViewModel)
            }
            composable("inbounds") {
                InboundsScreen(
                    inboundsViewModel = inboundsViewModel,
                    connectionsViewModel = connectionsViewModel,
                    usersViewModel = usersViewModel,
                    onNavigateToUsers = {
                        navController.navigate("users") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("users") {
                UsersScreen(
                    viewModel = usersViewModel,
                    connectionsViewModel = connectionsViewModel
                )
            }
            composable("ssh") {
                SSHInstallerScreen(
                    viewModel = sshInstallerViewModel,
                    onAddConnection = { newConn ->
                        connectionsViewModel.addConnection(newConn)
                        navController.navigate("app_settings") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("app_settings") {
                AppSettingsScreen(
                    connectionsViewModel = connectionsViewModel,
                    settingsViewModel = settingsViewModel,
                    templatesViewModel = templatesViewModel
                )
            }
        }
    }
}