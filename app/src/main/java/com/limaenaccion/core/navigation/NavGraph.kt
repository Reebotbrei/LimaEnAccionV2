package com.limaenaccion.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.limaenaccion.feature_alerts.presentation.IncidentDetailScreen
import com.limaenaccion.feature_auth.presentation.ForgotPasswordScreen
import com.limaenaccion.feature_auth.presentation.LoginScreen
import com.limaenaccion.feature_auth.presentation.NewPasswordScreen
import com.limaenaccion.feature_auth.presentation.RegisterStep1Screen
import com.limaenaccion.feature_auth.presentation.RegisterStep2Screen
import com.limaenaccion.feature_auth.presentation.RegisterStep3Screen
import com.limaenaccion.feature_auth.presentation.RegisterViewModel
import com.limaenaccion.feature_auth.presentation.SmsVerificationScreen
import com.limaenaccion.feature_emergency.presentation.EmergencyScreen
import com.limaenaccion.feature_map.presentation.LocationPermissionScreen
import com.limaenaccion.feature_onboarding.presentation.SplashScreen
import com.limaenaccion.feature_profile.presentation.AddAuxiliaryContactScreen
import com.limaenaccion.feature_sos.presentation.SosActivationScreen
import com.limaenaccion.feature_sos.presentation.SosActiveScreen

import com.limaenaccion.feature_reports.presentation.CameraSimulationScreen
import com.limaenaccion.feature_reports.presentation.PhotoPreviewScreen
import com.limaenaccion.feature_reports.presentation.ReportDetailsScreen
import com.limaenaccion.feature_reports.presentation.ReportSuccessScreen
import com.limaenaccion.feature_reports.presentation.ReportTypeScreen
import com.limaenaccion.feature_reports.presentation.ReportViewModel

import com.limaenaccion.feature_profile.presentation.ChangeProfilePhotoScreen
import com.limaenaccion.feature_profile.presentation.EditPersonalDataScreen
import com.limaenaccion.feature_profile.presentation.ManageContactsScreen
import com.limaenaccion.feature_profile.presentation.ReportHistoryScreen
import com.limaenaccion.feature_profile.presentation.UpdateAddressScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToEmergency = {
                    navController.navigate(Screen.Emergency.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Emergency.route) {
            EmergencyScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onAddAuxiliaryContact = { navController.navigate(Screen.AddAuxiliaryContact.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.LocationPermission.route) },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) },
                onRegisterClick = { navController.navigate(Screen.RegisterGraph.route) }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onCodeVerified = { navController.navigate(Screen.NewPassword.route) }
            )
        }

        composable(Screen.NewPassword.route) {
            NewPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onPasswordSaved = { navController.popBackStack(Screen.Login.route, false) }
            )
        }

        navigation(startDestination = Screen.RegisterStep1.route, route = Screen.RegisterGraph.route) {
            composable(Screen.RegisterStep1.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.RegisterGraph.route) }
                val registerViewModel: RegisterViewModel = viewModel(parentEntry)
                RegisterStep1Screen(
                    viewModel = registerViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(Screen.RegisterStep2.route) },
                    onLoginClick = { navController.popBackStack(Screen.Login.route, false) }
                )
            }
            composable(Screen.RegisterStep2.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.RegisterGraph.route) }
                val registerViewModel: RegisterViewModel = viewModel(parentEntry)
                RegisterStep2Screen(
                    viewModel = registerViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(Screen.RegisterStep3.route) }
                )
            }
            composable(Screen.RegisterStep3.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.RegisterGraph.route) }
                val registerViewModel: RegisterViewModel = viewModel(parentEntry)
                RegisterStep3Screen(
                    viewModel = registerViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onFinish = { navController.navigate(Screen.RegisterVerification.route) }
                )
            }
            composable(Screen.RegisterVerification.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.RegisterGraph.route) }
                val registerViewModel: RegisterViewModel = viewModel(parentEntry)
                val registerState by registerViewModel.uiState.collectAsState()
                SmsVerificationScreen(
                    phoneNumber = registerState.phoneNumber,
                    onNavigateBack = { navController.popBackStack() },
                    onVerified = {
                        navController.navigate(Screen.LocationPermission.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Screen.LocationPermission.route) {
            LocationPermissionScreen(
                onPermissionGranted = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Emergency.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AddAuxiliaryContact.route) {
            AddAuxiliaryContactScreen(
                onContactSaved = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onSosClick = { navController.navigate(Screen.SosActivation.route) },
                onNavigateToIncidentDetail = { id -> navController.navigate(Screen.IncidentDetail.createRoute(id)) },
                onReportClick = { navController.navigate(Screen.ReportGraph.route) },
                onEditPersonalData = { navController.navigate(Screen.EditPersonalData.route) },
                onChangePhoto = { navController.navigate(Screen.ChangeProfilePhoto.route) },
                onUpdateAddress = { navController.navigate(Screen.UpdateAddress.route) },
                onReportHistory = { navController.navigate(Screen.ReportHistory.route) },
                onManageContacts = { navController.navigate(Screen.ManageContacts.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        // Limpia el backstack
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SosActivation.route) {
            SosActivationScreen(
                onActivated = {
                    navController.navigate(Screen.SosActive.route) {
                        popUpTo(Screen.SosActivation.route) { inclusive = true }
                    }
                },
                onCancelled = { navController.popBackStack() }
            )
        }

        composable(Screen.SosActive.route) {
            SosActiveScreen(
                onNavigateToEmergencyDirectory = { navController.navigate(Screen.Emergency.route) },
                onDeactivated = { navController.popBackStack(Screen.Main.route, false) }
            )
        }

        composable(
            route = Screen.IncidentDetail.route,
            arguments = listOf(navArgument("incidentId") { type = NavType.StringType })
        ) {
            IncidentDetailScreen(onNavigateBack = { navController.popBackStack() })
        }
        //Grafo anidado de Reportes
        navigation(startDestination = Screen.ReportTypeSelection.route, route = Screen.ReportGraph.route) {

            composable(Screen.ReportTypeSelection.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.ReportGraph.route) }
                val reportViewModel: ReportViewModel = viewModel(parentEntry)
                ReportTypeScreen(
                    viewModel = reportViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(Screen.ReportCamera.route) }
                )
            }

            composable(Screen.ReportCamera.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.ReportGraph.route) }
                val reportViewModel: ReportViewModel = viewModel(parentEntry)
                CameraSimulationScreen(
                    viewModel = reportViewModel,
                    onClose = { navController.popBackStack(Screen.Main.route, false) },
                    onPhotoCaptured = { navController.navigate(Screen.ReportPhotoPreview.route) }
                )
            }

            composable(Screen.ReportPhotoPreview.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.ReportGraph.route) }
                val reportViewModel: ReportViewModel = viewModel(parentEntry)
                PhotoPreviewScreen(
                    viewModel = reportViewModel,
                    onNavigateBack = { navController.popBackStack(Screen.Main.route, false) },
                    onRetake = { navController.popBackStack(Screen.ReportCamera.route, false) },
                    onAccept = { navController.navigate(Screen.ReportDetails.route) }
                )
            }

            composable(Screen.ReportDetails.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.ReportGraph.route) }
                val reportViewModel: ReportViewModel = viewModel(parentEntry)
                ReportDetailsScreen(
                    viewModel = reportViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onPublished = { navController.navigate(Screen.ReportSuccess.route) }
                )
            }

            composable(Screen.ReportSuccess.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry(Screen.ReportGraph.route) }
                val reportViewModel: ReportViewModel = viewModel(parentEntry)
                ReportSuccessScreen(
                    viewModel = reportViewModel,
                    onBackToHome = {
                        // Limpia el grafo de Reportes
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.EditPersonalData.route) {
                EditPersonalDataScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.ChangeProfilePhoto.route) {
                ChangeProfilePhotoScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.UpdateAddress.route) {
                UpdateAddressScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.ReportHistory.route) {
                ReportHistoryScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.ManageContacts.route) {
                ManageContactsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onAddContact = { navController.navigate(Screen.AddAuxiliaryContact.route) }
                )
            }
        }
    }
}