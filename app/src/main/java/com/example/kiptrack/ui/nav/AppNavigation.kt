package com.example.kiptrack.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kiptrack.ui.data.UserRole
import com.example.kiptrack.ui.screen.*

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login", modifier = modifier) {

        // --- 1. LOGIN ---
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

        // --- 2. MAHASISWA FLOW ---
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
            route = "profile_mahasiswa/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            ProfileMahasiswaScreen(
                uid = uid,
                onBackClicked = { navController.popBackStack() },
                onLogoutClicked = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = true }
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

        // --- 3. WALI FLOW ---
        composable(
            route = "dashboard_wali/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            DashboardWaliScreen(
                uid = uid,
                onNavigateToHistory = { currentUid ->
                    navController.navigate("history_wali/$currentUid")
                },
                onLogoutClicked = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "history_wali/{uid}",
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            PerincianPengeluaranWaliScreen(
                uid = uid,
                onBackToDashboard = { navController.popBackStack() }
            )
        }


        // --- 4. ADMIN FLOW ---

        // A. Dashboard Admin
        composable(
            route = "dashboard_admin/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            DashboardAdminScreen(
                uid = uid,
                onNavigateToListUniversitas = { adminUid, universityId ->
                    navController.navigate("universitas_list/$adminUid/$universityId")
                },
                onLogoutClicked = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        // B. List Universitas
        composable(
            route = "universitas_list/{uid}/{universityId}",
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType },
                navArgument("universityId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            val uniId = backStackEntry.arguments?.getString("universityId") ?: ""

            ListUniversitasScreen(
                uid = uid,
                universityId = uniId,
                onNavigateToListMahasiswa = { currentUid, prodiId ->
                    navController.navigate("mahasiswa_list/$currentUid/$uniId/$prodiId")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // C. List Mahasiswa
        composable(
            route = "mahasiswa_list/{uid}/{universityId}/{prodiId}",
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType },
                navArgument("universityId") { type = NavType.StringType },
                navArgument("prodiId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            val uniId = backStackEntry.arguments?.getString("universityId") ?: ""
            val prodiId = backStackEntry.arguments?.getString("prodiId") ?: ""

            ListMahasiswaProdiScreen(
                uid = uid,
                universityId = uniId,
                prodiId = prodiId,
                onNavigateToDetailMahasiswa = { currentUid, studentUid ->
                    navController.navigate("mahasiswa_detail/$currentUid/$studentUid")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // D. Detail Mahasiswa (Admin View)
        composable(
            route = "mahasiswa_detail/{uid}/{studentUid}",
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType },
                navArgument("studentUid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            val studentUid = backStackEntry.arguments?.getString("studentUid") ?: ""

            DetailMahasiswaScreen(
                uid = uid,
                studentUid = studentUid,
                onBackClick = { navController.popBackStack() },
                onNavigateToProfileAdmin = { targetStudentUid ->
                    navController.navigate("profile_mahasiswa_admin/$targetStudentUid")
                }
            )
        }

        // E. Profile Mahasiswa (Admin View)
        composable(
            route = "profile_mahasiswa_admin/{studentUid}",
            arguments = listOf(
                navArgument("studentUid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val studentUid = backStackEntry.arguments?.getString("studentUid") ?: ""
            ProfilMahasiswaAdminScreen(
                uid = studentUid,
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}