package com.example.kiptrack.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiptrack.ui.theme.DeepPurple
import com.example.kiptrack.ui.theme.LightPurple
import com.example.kiptrack.ui.theme.MediumPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListMahasiswaProdiScreen(
    uid: String,
    programStudiName: String,
    onNavigateToDetailMahasiswa: (String, String) -> Unit, // NEW: Callback to navigate to student detail
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = programStudiName,
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
                    containerColor = MediumPurple,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            // Mock Data for students
            val mockStudents = listOf(
                "Naufal Ramadhan", "Adinda Putri", "Rizky Firmansyah",
                "Siti Aisyah", "Budi Santoso", "Dewi Lestari", "Cahyo Nugroho"
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mockStudents) { studentName ->
                    MahasiswaListItem(
                        name = studentName,
                        prodi = programStudiName,
                        // Trigger navigation, passing admin UID and student name
                        onClick = { onNavigateToDetailMahasiswa(uid, studentName) }
                    )
                }
            }
        }
    }
}

@Composable
fun MahasiswaListItem(name: String, prodi: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightPurple.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // Use the new onClick handler
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder Avatar/Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MediumPurple),
                contentAlignment = Alignment.Center
            ) {
                // Display the first letter of the name
                Text(
                    text = name.first().toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DeepPurple
                )
                Text(
                    text = "NIM: 20210${(100..999).random()}", // Mock NIM
                    fontSize = 12.sp,
                    color = DeepPurple.copy(alpha = 0.7f)
                )
                Text(
                    text = "Prodi: $prodi",
                    fontSize = 12.sp,
                    color = DeepPurple.copy(alpha = 0.5f)
                )
            }
        }
    }
}