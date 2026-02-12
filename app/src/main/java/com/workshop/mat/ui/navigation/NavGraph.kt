package com.workshop.mat.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
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
import com.workshop.mat.ui.screens.productions.ProductionsScreen
import com.workshop.mat.ui.screens.products.ProductsScreen
import com.workshop.mat.ui.screens.receipts.ReceiptsScreen
import com.workshop.mat.ui.screens.register.RegisterScreen
import com.workshop.mat.ui.screens.settings.SettingsScreen
import com.workshop.mat.ui.theme.DarkBackground
import com.workshop.mat.ui.theme.DarkSurface
import com.workshop.mat.ui.theme.Primary
import com.workshop.mat.ui.theme.TextPrimary

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val MATERIALS = "materials"
    const val PRODUCTS = "products"
    const val PRODUCTIONS = "productions"
    const val RECEIPTS = "receipts"
    const val FINISHED_PRODUCTS = "finished_products"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
}

// Screen titles for the top bar
private val screenTitles = mapOf(
    Routes.MATERIALS to "Материалы",
    Routes.PRODUCTS to "Изделия",
    Routes.PRODUCTIONS to "Производство",
    Routes.RECEIPTS to "Приходы",
    Routes.FINISHED_PRODUCTS to "Готовая продукция",
    Routes.HISTORY to "История",
    Routes.SETTINGS to "Настройки"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = hiltViewModel()
    val startDestination = if (loginViewModel.isLoggedIn()) Routes.DASHBOARD else Routes.LOGIN

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showTopBar = currentRoute in screenTitles.keys

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text(
                            text = screenTitles[currentRoute] ?: "",
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Назад",
                                tint = TextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface
                    )
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

            // Dashboard — home screen with navigation icons
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onNavigate = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Sub-screens with swipe-back
            composable(Routes.MATERIALS) {
                SwipeBackWrapper(onBack = { navController.popBackStack() }) {
                    MaterialsScreen()
                }
            }
            composable(Routes.PRODUCTS) {
                SwipeBackWrapper(onBack = { navController.popBackStack() }) {
                    ProductsScreen()
                }
            }
            composable(Routes.PRODUCTIONS) {
                SwipeBackWrapper(onBack = { navController.popBackStack() }) {
                    ProductionsScreen()
                }
            }
            composable(Routes.RECEIPTS) {
                SwipeBackWrapper(onBack = { navController.popBackStack() }) {
                    ReceiptsScreen()
                }
            }
            composable(Routes.FINISHED_PRODUCTS) {
                SwipeBackWrapper(onBack = { navController.popBackStack() }) {
                    FinishedProductsScreen()
                }
            }
            composable(Routes.HISTORY) {
                SwipeBackWrapper(onBack = { navController.popBackStack() }) {
                    HistoryScreen()
                }
            }
            composable(Routes.SETTINGS) {
                SwipeBackWrapper(onBack = { navController.popBackStack() }) {
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
}

@Composable
fun SwipeBackWrapper(
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    var totalDrag by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        if (totalDrag > 200f) {
                            onBack()
                        }
                        totalDrag = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (dragAmount > 0) {
                            totalDrag += dragAmount
                        }
                    }
                )
            }
    ) {
        content()
    }
}
