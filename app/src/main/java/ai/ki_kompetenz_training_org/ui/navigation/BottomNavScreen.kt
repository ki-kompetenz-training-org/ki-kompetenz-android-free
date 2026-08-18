package ai.ki_kompetenz_training_org.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import ai.ki_kompetenz_training_org.R

data class BottomTab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
)

val BOTTOM_TABS = listOf(
    BottomTab(Routes.HOME, R.string.home_title, Icons.Default.Home),
    BottomTab(Routes.MINIGAMES, R.string.games_title, Icons.Default.SportsEsports),
    BottomTab(Routes.LESSONS, R.string.lessons_title, Icons.Default.MenuBook),
    BottomTab(Routes.GAMIFICATION, R.string.profile_title, Icons.Default.Person),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavScreen() {
    val navController = androidx.navigation.compose.rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Show bottom bar only on tab destinations, hide on sub-screens (Quiz, Lesson detail, etc.)
    val isTabDestination = BOTTOM_TABS.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }

    Scaffold(
        bottomBar = {
            if (isTabDestination) {
                NavigationBar {
                    BOTTOM_TABS.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) },
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        KiKompetenzNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
