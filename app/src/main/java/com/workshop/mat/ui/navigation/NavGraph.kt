package com.workshop.mat.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.workshop.mat.ui.screens.dashboard.DashboardScreen
import com.workshop.mat.ui.screens.finished.FinishedProductsScreen
import com.workshop.mat.ui.screens.history.HistoryScreen
import com.workshop.mat.ui.screens.login.LoginScreen
import com.workshop.mat.ui.screens.login.LoginViewModel
import com.workshop.mat.ui.screens.materials.MaterialsScreen
import com.workshop.mat.ui.screens.more.MoreScreen
import com.workshop.mat.ui.screens.productions.ProductionsScreen
import com.workshop.mat.ui.screens.products.ProductsScreen
import com.workshop.mat.ui.screens.receipts.ReceiptsScreen
import com.workshop.mat.ui.screens.register.RegisterScreen
import com.workshop.mat.ui.screens.settings.SettingsScreen
import com.workshop.mat.ui.theme.DarkBackground

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val MATERIALS = "materials"
    const val PRODUCTS = "products"
    const val PRODUCTIONS = "productions"
    const val MORE = "more"
    const val RECEIPTS = "receipts"
    const val FINISHED_PRODUCTS = "finished_products"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
}

private val mainRoutes = setOf(
    Routes.DASHBOARD, Routes.MATERIALS, Routes.PRODUCTS,
    Routes.PRODUCTIONS, Routes.MORE, Routes.RECEIPTS,
    Routes.FINISHED_PRODUCTS, Routes.HISTORY, Routes.SETTINGS
)

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val startDestination = if (loginViewModel.isLoggedIn()) Routes.DASHBOARD else Routes.LOGIN

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in mainRoutes

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkBackground)
        ) {
            // Auth
            composable(Routes.LOGIN) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Routes.REGISTER)
                    },
                    onLoginSuccess = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            // Main screens
            composable(Routes.DASHBOARD) { DashboardScreen() }
            composable(Routes.MATERIALS) { MaterialsScreen() }
            composable(Routes.PRODUCTS) { ProductsScreen() }
            composable(Routes.PRODUCTIONS) { ProductionsScreen() }
            composable(Routes.MORE) {
                MoreScreen(
                    onNavigate = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Routes.RECEIPTS) { ReceiptsScreen() }
            composable(Routes.FINISHED_PRODUCTS) { FinishedProductsScreen() }
            composable(Routes.HISTORY) { HistoryScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
