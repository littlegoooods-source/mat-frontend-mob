package com.workshop.mat.ui.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.workshop.mat.ui.navigation.Routes
import com.workshop.mat.ui.theme.*

data class MoreMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

private val menuItems = listOf(
    MoreMenuItem("Приходы материалов", "Учёт поступлений", Icons.Default.LocalShipping, Routes.RECEIPTS),
    MoreMenuItem("Готовая продукция", "Склад и продажи", Icons.Default.ShoppingCart, Routes.FINISHED_PRODUCTS),
    MoreMenuItem("История операций", "Журнал всех действий", Icons.Default.History, Routes.HISTORY),
    MoreMenuItem("Настройки", "Организации и профиль", Icons.Default.Settings, Routes.SETTINGS),
)

@Composable
fun MoreScreen(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Дополнительно",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Дополнительные разделы приложения",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))

        menuItems.forEach { item ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onNavigate(item.route) },
                shape = RoundedCornerShape(16.dp),
                color = DarkCard
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Primary.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextMuted
                    )
                }
            }
        }
    }
}
