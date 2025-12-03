package com.example.kiptrack.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiptrack.ui.data.UserMahasiswa
import com.example.kiptrack.ui.theme.RefAvatarBg
import com.example.kiptrack.ui.theme.RefAvatarIcon
import com.example.kiptrack.ui.theme.RefHeaderPurple
import com.example.kiptrack.ui.theme.RefInputBackground
import com.example.kiptrack.ui.theme.RefLabelColor
import com.example.kiptrack.ui.theme.RefTextPurple

@Composable
fun ProfileAvatarFixed(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(80.dp) // Slightly smaller to fit the fixed header nicely
            .clip(CircleShape)
            .background(RefAvatarBg)
            .border(2.dp, Color.White, CircleShape), // Added white border for contrast
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Avatar",
            tint = RefAvatarIcon,
            modifier = Modifier.size(50.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMahasiswaScreen(uid: String, onBackClicked: () -> Unit, onLogoutClicked: () -> Unit) {
    // --- Mock Data ---
    val studentData = remember(uid) {
        UserMahasiswa(
            uid = uid,
            nim = "202300259",
            nama = "Blessy Jeniffer",
            university = "Universitas Kristen Krida Wacana",
            programStudi = "Informatika",
            jenjang = "Sarjana (S1)",
            semesterBerjalan = "Semester 6",
            namaWali = "Ayu Perry"
        )
    }

    Scaffold(
        containerColor = RefHeaderPurple,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile Mahasiswa",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RefHeaderPurple)
            )
        }
    ) { paddingValues ->

        // Main Container (Purple Background)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(RefHeaderPurple)
        ) {

            // --- FIXED HEADER SECTION ---
            // Profile Picture and Name side-by-side, fixed in place.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatarFixed()

                Spacer(modifier = Modifier.width(20.dp))

                // Name text (White to contrast with purple header)
                Text(
                    text = studentData.nama,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // --- SCROLLABLE FIELDS SECTION ---
            // White card with purple peaking out (margin)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes up all remaining space
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp), // Purple peaks out here
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()), // Only this part scrolls
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    ProfileDetailFieldStandard(label = "Nomor Induk Mahasiswa", value = studentData.nim)
                    Spacer(Modifier.height(16.dp))

                    ProfileDetailFieldStandard(label = "Universitas", value = studentData.university)
                    Spacer(Modifier.height(16.dp))

                    ProfileDetailFieldStandard(label = "Program Studi", value = studentData.programStudi)
                    Spacer(Modifier.height(16.dp))

                    ProfileDetailFieldStandard(label = "Jenjang", value = studentData.jenjang)
                    Spacer(Modifier.height(16.dp))

                    ProfileDetailFieldStandard(label = "Semester Saat Ini", value = studentData.semesterBerjalan)
                    Spacer(Modifier.height(16.dp))

                    ProfileDetailFieldStandard(label = "Nama Wali", value = studentData.namaWali)

                    Spacer(Modifier.height(32.dp))

                    // Logout Button
                    // Less wide as requested (50% of card width)
                    Button(
                        onClick = onLogoutClicked,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RefHeaderPurple),
                        shape = RoundedCornerShape(50) // Pill shape
                    ) {
                        Text("Keluar", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileDetailFieldStandard(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = RefLabelColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(RefInputBackground)
                .padding(vertical = 14.dp, horizontal = 16.dp)
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                color = RefTextPurple,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}