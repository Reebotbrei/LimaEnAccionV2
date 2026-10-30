package com.limaenaccion.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.limaenaccion.core.util.LocalAppStrings
import com.limaenaccion.feature_alerts.presentation.AlertsListScreen
import com.limaenaccion.feature_emergency.presentation.EmergencyScreen
import com.limaenaccion.feature_map.presentation.DashboardScreen
import com.limaenaccion.feature_profile.presentation.ProfileScreen
import com.limaenaccion.feature_settings.presentation.SettingsScreen

private data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

@Composable
fun MainScreen(
    onSosClick: () -> Unit,
    onNavigateToIncidentDetail: (String) -> Unit,
    onReportClick: () -> Unit,
    onEditPersonalData: () -> Unit,
    onChangePhoto: () -> Unit,
    onUpdateAddress: () -> Unit,
    onReportHistory: () -> Unit,
    onManageContacts: () -> Unit,
    onLogout: () -> Unit
) {
    val innerNavController = rememberNavController()
    val strings = LocalAppStrings.current
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    // Intercepta la flecha atrás del celular en la pantalla principal para evitar salir abruptamente
    BackHandler(enabled = true) {
        showLogoutConfirmDialog = true
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text(strings.logoutDialogTitle) },
            text = { Text(strings.logoutDialogText) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.logoutConfirmBtn)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text(strings.cancelBtn)
                }
            }
        )
    }

    val bottomNavItems = listOf(
        BottomNavItem("tab_home", strings.navHome, Icons.Filled.Home),
        BottomNavItem("tab_alerts", strings.navAlerts, Icons.Filled.Notifications),
        BottomNavItem("tab_directory", strings.navDirectory, Icons.Filled.Shield),
        BottomNavItem("tab_profile", strings.navProfile, Icons.Filled.Person),
        BottomNavItem("tab_settings", strings.navSettings, Icons.Filled.Settings)
    )

    Scaffold(
        bottomBar = {
            val currentEntry by innerNavController.currentBackStackEntryAsState()
            val currentRoute = currentEntry?.destination?.route
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            innerNavController.navigate(item.route) {
                                popUpTo(innerNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(navController = innerNavController, startDestination = "tab_home", modifier = Modifier.padding(paddingValues)) {
            composable("tab_home") {
                DashboardScreen(onSosClick = onSosClick, onNavigateToIncidentDetail = onNavigateToIncidentDetail, onReportClick = onReportClick)
            }
            composable("tab_alerts") {
                AlertsListScreen(onIncidentClick = onNavigateToIncidentDetail)
            }
            composable("tab_directory") {
                EmergencyScreen(showBottomBar = false, onNavigateToLogin = { }, onAddAuxiliaryContact = onManageContacts)
            }
            composable("tab_profile") {
                ProfileScreen(
                    onEditPersonalData = onEditPersonalData,
                    onChangePhoto = onChangePhoto,
                    onUpdateAddress = onUpdateAddress,
                    onReportHistory = onReportHistory,
                    onManageContacts = onManageContacts
                )
            }
            composable("tab_settings") {
                SettingsScreen(
                    onLogoutConfirmed = onLogout,
                    onManageContacts = onManageContacts
                )
            }
        }
    }
}
