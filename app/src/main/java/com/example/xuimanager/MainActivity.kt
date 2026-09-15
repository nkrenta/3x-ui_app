package com.example.xuimanager

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.xuimanager.ui.screens.AppSettingsScreen
import com.example.xuimanager.ui.screens.DashboardScreen
import com.example.xuimanager.ui.screens.PinLockScreen
import com.example.xuimanager.ui.screens.SSHInstallerScreen
import com.example.xuimanager.ui.screens.UsersScreen
import com.example.xuimanager.ui.theme.AppLanguage
import com.example.xuimanager.ui.theme.DarkBackground
import com.example.xuimanager.ui.theme.LocalAppLanguage
import com.example.xuimanager.ui.theme.ThreeXUITheme
import com.example.xuimanager.ui.theme.stringRes
import com.example.xuimanager.ui.viewmodel.ConnectionsViewModel
import com.example.xuimanager.ui.viewmodel.SSHInstallerViewModel
import com.example.xuimanager.ui.viewmodel.SettingsViewModel
import com.example.xuimanager.ui.viewmodel.TemplatesViewModel
import com.example.xuimanager.ui.viewmodel.UsersViewModel

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
                    val isLockedNeeded =
                        (isPinEnabled || isBiometricEnabled) && (pinCode.isNotBlank() || isBiometricEnabled)
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

    val connectionsViewModel: ConnectionsViewModel = viewModel()
    val usersViewModel: UsersViewModel = viewModel()
    val sshInstallerViewModel: SSHInstallerViewModel = viewModel()
    val templatesViewModel: TemplatesViewModel = viewModel()

    val navItems = listOf(
        NavigationItem("dashboard", "dashboard", Icons.Default.Dns),
        NavigationItem("users", "users", Icons.Default.People),
        NavigationItem("ssh", "ssh", Icons.Default.Terminal),
        NavigationItem("app_settings", "settings", Icons.Default.Settings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                navItems.forEach { item ->
                    val selected = currentRoute == item.route
                    val label = stringRes(item.labelKey)
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = selected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("dashboard") {
                DashboardScreen(connectionsViewModel = connectionsViewModel)
            }
            composable("users") {
                UsersScreen(viewModel = usersViewModel)
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