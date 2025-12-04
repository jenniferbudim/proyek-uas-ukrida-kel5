package com.example.kiptrack.ui.nav

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
import com.example.kiptrack.ui.screen.DetailMahasiswaScreen
import com.example.kiptrack.ui.screen.ListMahasiswaProdiScreen
import com.example.kiptrack.ui.screen.ListUniversitasScreen
import com.example.kiptrack.ui.screen.LogFormScreen
import com.example.kiptrack.ui.screen.LoginScreen
import com.example.kiptrack.ui.screen.ProfileMahasiswaScreen

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

        // --- 3. WALI FLOW ---
        composable(
            route = "dashboard_wali/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            DashboardWaliScreen(uid = uid)
        }

        // --- 4. ADMIN FLOW (PERBAIKAN UTAMA DI SINI) ---

        // A. Dashboard Admin
        composable(
            route = "dashboard_admin/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            DashboardAdminScreen(
                uid = uid,
                // Mengirim ID Universitas (cth: "univ_ukrida") ke layar berikutnya
                onNavigateToListUniversitas = { adminUid, universityId ->
                    navController.navigate("universitas_list/$adminUid/$universityId")
                },
                onLogoutClicked = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }

        // B. List Universitas (Menampilkan Daftar Prodi)
        // Menerima universityId agar bisa fetch sub-collection prodi
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
                // Mengirim UnivID dan ProdiID untuk filter mahasiswa nanti
                onNavigateToListMahasiswa = { currentUid, prodiId ->
                    navController.navigate("mahasiswa_list/$currentUid/$uniId/$prodiId")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // C. List Mahasiswa (Berdasarkan Prodi & Universitas)
        // Menerima UniversityID & ProdiID untuk Query ke collection 'users'
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
                universityId = uniId, // Parameter yang sebelumnya merah/missing
                prodiId = prodiId,    // Parameter yang sebelumnya merah/missing
                onNavigateToDetailMahasiswa = { currentUid, studentUid ->
                    navController.navigate("mahasiswa_detail/$currentUid/$studentUid")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // D. Detail Mahasiswa (Charts & History)
        // Menerima studentUid (UID Mahasiswa target)
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
                uid = uid, // UID Admin
                studentUid = studentUid, // UID Mahasiswa yang dilihat
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}