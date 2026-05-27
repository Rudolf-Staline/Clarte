package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        NavigationTabItem("home", "Accueil", Icons.Outlined.Home, Icons.Filled.Home),
        NavigationTabItem("choose_mode", "Écrire", Icons.Outlined.Edit, Icons.Filled.Edit),
        NavigationTabItem("history", "Historique", Icons.Outlined.History, Icons.Filled.History),
        NavigationTabItem("trends", "Tendances", Icons.Outlined.TrendingUp, Icons.Filled.TrendingUp),
        NavigationTabItem("settings", "Paramètres", Icons.Outlined.Settings, Icons.Filled.Settings)
    )

    NavigationBar {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { if (!isSelected) onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = { Text(text = item.title) },
                modifier = Modifier.testTag("nav_item_${item.route}")
            )
        }
    }
}

data class NavigationTabItem(
    val route: String,
    val title: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
)
