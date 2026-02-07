package com.workshop.mat.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.workshop.mat.ui.theme.*

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : BottomNavItem("dashboard", "Главная", Icons.Default.Home)
    data object Materials : BottomNavItem("materials", "Материалы", Icons.Default.Inventory2)
    data object Products : BottomNavItem("products", "Изделия", Icons.Default.Category)
    data object Productions : BottomNavItem("productions", "Производство", Icons.Default.Factory)
    data object More : BottomNavItem("more", "Ещё", Icons.Default.MoreHoriz)
}

val bottomNavItems = listOf(
    BottomNavItem.Dashboard,
    BottomNavItem.Materials,
    BottomNavItem.Products,
    BottomNavItem.Productions,
    BottomNavItem.More
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = DarkSurface,
        contentColor = TextPrimary
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                selected = selected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = Primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}
