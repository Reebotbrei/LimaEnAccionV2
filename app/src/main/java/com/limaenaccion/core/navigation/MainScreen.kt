package com.limaenaccion.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.limaenaccion.feature_alerts.presentation.AlertsListScreen
import com.limaenaccion.feature_emergency.presentation.EmergencyScreen
import com.limaenaccion.feature_map.presentation.DashboardScreen
import com.limaenaccion.feature_profile.presentation.ProfileScreen
import com.limaenaccion.feature_settings.presentation.SettingsScreen

import androidx.compose.foundation.layout.padding
private data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem("tab_home", "Inicio", Icons.Filled.Home),
    BottomNavItem("tab_alerts", "Alertas", Icons.Filled.Notifications),
    BottomNavItem("tab_directory", "Directorio", Icons.Filled.Shield),
    BottomNavItem("tab_profile", "Perfil", Icons.Filled.Person),
    BottomNavItem("tab_settings", "Ajustes", Icons.Filled.Settings)
)

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
                        label = { Text(item.label) }
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
                EmergencyScreen(onNavigateToLogin = { }, onAddAuxiliaryContact = onManageContacts)
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
                SettingsScreen(onLogoutConfirmed = onLogout)
            }
        }
    }
}