package com.example.kiptrack.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiptrack.ui.theme.Purple300
import com.example.kiptrack.ui.theme.Purple50
import com.example.kiptrack.ui.theme.Purple200

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListUniversitasScreen(
    uid: String,
    universityName: String,
    onNavigateToListMahasiswa: (String, String) -> Unit, // New callback for next screen
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = universityName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 2,
                        lineHeight = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Purple200,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Handle Add Program Studi */ },
                containerColor = Purple300,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Program Studi")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Purple50)
                .padding(paddingValues)
        ) {
            // Mock Data mimicking "Program Studi"
            val studyPrograms = listOf(
                "Informatika", "Sistem Informasi", "Teknik Industri", "Teknik Elektro",
                "Teknik Sipil", "Manajemen", "Akuntansi", "Sastra Inggris",
                "Keperawatan", "Kedokteran"
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(studyPrograms.size) { index ->
                    val prodiName = studyPrograms[index]
                    ProgramStudiItem(
                        name = prodiName,
                        // Trigger navigation, passing UID and Program Studi Name
                        onClick = { onNavigateToListMahasiswa(uid, prodiName) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProgramStudiItem(name: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // Use the new onClick handler
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Purple300
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Non-Kedokteran",
                fontSize = 12.sp,
                color = Purple300.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sarjana (S1)",
                fontSize = 14.sp,
                color = Purple300,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}