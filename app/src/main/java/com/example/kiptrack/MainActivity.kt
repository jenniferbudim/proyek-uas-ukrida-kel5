package com.example.kiptrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kiptrack.ui.screen.DashboardAdminScreen
import com.example.kiptrack.ui.screen.DashboardMahasiswaScreen
import com.example.kiptrack.ui.screen.DashboardWaliScreen
import com.example.kiptrack.ui.screen.LoginScreen
import com.example.kiptrack.ui.theme.KIPTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KIPTrackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    KipTrackAppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun KipTrackAppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        // 1. Login Screen
        composable("login") {
            LoginScreen(
                onNavigateToMahasiswa = { uid ->
                    navController.navigate("dashboard_mahasiswa/$uid") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToWali = { uid ->
                    navController.navigate("dashboard_wali/$uid") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToAdmin = { uid ->
                    navController.navigate("dashboard_admin/$uid") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // 2. Dashboard Mahasiswa (imported from DashboardMahasiswaScreen.kt)
        composable(
            route = "dashboard_mahasiswa/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            DashboardMahasiswaScreen(uid = uid)
        }

        // 3. Dashboard Wali (imported from DashboardWaliScreen.kt)
        composable(
            route = "dashboard_wali/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            DashboardWaliScreen(uid = uid)
        }

        // 4. Dashboard Admin (imported from DashboardAdminScreen.kt)
        composable(
            route = "dashboard_admin/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            DashboardAdminScreen(uid = uid)
        }
    }
}