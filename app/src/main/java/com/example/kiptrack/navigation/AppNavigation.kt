package com.example.kiptrack.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kiptrack.ui.model.UserRole
import com.example.kiptrack.ui.screen.DashboardAdminScreen
import com.example.kiptrack.ui.screen.DashboardMahasiswaScreen
import com.example.kiptrack.ui.screen.DashboardWaliScreen
import com.example.kiptrack.ui.screen.LogFormScreen
import com.example.kiptrack.ui.screen.LoginScreen
import com.example.kiptrack.ui.screen.ProfileMahasiswaScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login", modifier = modifier) {
        composable("login") {
            LoginScreen(onLoginSuccess = { role, uid ->
                val route = when (role) {
                    UserRole.MAHASISWA -> "dashboard_mahasiswa/$uid"
                    UserRole.WALI -> "dashboard_wali/$uid"
                    UserRole.ADMIN -> "dashboard_admin/$uid"
                }
                navController.navigate(route) {
                    popUpTo("login") { inclusive = true }
                }
            })
        }

        composable(
            route = "dashboard_mahasiswa/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            DashboardMahasiswaScreen(
                uid = uid,
                onNavigateToProfile = { uidToPass ->
                    navController.navigate("profile_mahasiswa/$uidToPass")
                },
                onNavigateToLogForm = { uidToPass ->
                    navController.navigate("log_form/$uidToPass")
                }
            )
        }

        composable(
            route = "dashboard_wali/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            DashboardWaliScreen(uid = uid)
        }

        composable(
            route = "dashboard_admin/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            DashboardAdminScreen(uid = uid)
        }

        composable(
            route = "profile_mahasiswa/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            ProfileMahasiswaScreen(
                uid = uid,
                onBackClicked = { navController.popBackStack() },
                onLogoutClicked = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "log_form/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            LogFormScreen(
                uid = uid,
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}
