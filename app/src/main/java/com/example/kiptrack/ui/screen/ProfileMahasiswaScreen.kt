package com.example.kiptrack.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.theme.*
import com.example.kiptrack.ui.utils.ImageUtils
import com.example.kiptrack.ui.viewmodel.ProfileMahasiswaViewModel
import com.example.kiptrack.ui.viewmodel.ProfileMahasiswaViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMahasiswaScreen(uid: String, onBackClicked: () -> Unit, onLogoutClicked: () -> Unit) {
    // Init ViewModel
    val viewModel: ProfileMahasiswaViewModel = viewModel(
        factory = ProfileMahasiswaViewModelFactory(uid)
    )
    val state = viewModel.uiState
    val context = LocalContext.current

    // --- Image Picker Launcher ---
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Konversi Gambar ke Base64 lalu kirim ke ViewModel
            val base64 = ImageUtils.uriToBase64(context, it)
            if (base64 != null) {
                viewModel.updateProfilePicture(base64)
            }
        }
    }

    Scaffold(
        containerColor = PurplePrimary,
        topBar = {
            TopAppBar(
                title = { Text("Profile Mahasiswa", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PurplePrimary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PurplePrimary)
        ) {
            // --- HEADER (AVATAR & NAMA) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. CONTAINER UTAMA (Tanpa Clip, agar kamera tidak terpotong)
                Box(
                    modifier = Modifier.size(84.dp) // Sedikit lebih besar untuk ruang kamera
                ) {
                    // A. Lingkaran Foto Profil (Di-clip lingkaran di sini)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.Center) // Posisi di tengah container
                            .clip(CircleShape)
                            .background(AvatarBackground)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.studentData.fotoProfil.isNotBlank()) {
                            val bitmap = com.example.kiptrack.ui.utils.ImageUtils.base64ToBitmap(state.studentData.fotoProfil)
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, null, tint = PurplePrimary, modifier = Modifier.size(40.dp))
                            }
                        } else {
                            Icon(Icons.Default.Person, null, tint = PurplePrimary, modifier = Modifier.size(40.dp))
                        }
                    }

                    // B. Ikon Kamera (Mengapung di Pojok Kanan Bawah Container Utama)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd) // Pojok kanan bawah
                            .offset(x = (-2).dp, y = (-2).dp) // Geser sedikit agar pas
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White) // Background putih biar kontras
                            .border(1.dp, AvatarBackground, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Edit Foto",
                            tint = Purple300,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Nama & NIM
                Column {
                    Text(
                        text = if (state.isLoading) "Memuat..." else state.studentData.nama,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (state.isLoading) "..." else state.studentData.nim,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // --- DATA DETAIL ---
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp, vertical = 10.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                if (state.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Purple300) }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())
                    ) {
                        ProfileDetailFieldStandard("Universitas", state.studentData.universityName)
                        Spacer(Modifier.height(16.dp))
                        ProfileDetailFieldStandard("Program Studi", state.studentData.programStudiName)
                        Spacer(Modifier.height(16.dp))
                        ProfileDetailFieldStandard("Jenjang", state.studentData.jenjang)
                        Spacer(Modifier.height(16.dp))
                        ProfileDetailFieldStandard("Semester Saat Ini", state.studentData.semesterBerjalan)
                        Spacer(Modifier.height(16.dp))
                        ProfileDetailFieldStandard("Nama Wali", state.studentData.namaWali)

                        Spacer(Modifier.height(32.dp))
                        Button(
                            onClick = onLogoutClicked,
                            modifier = Modifier.fillMaxWidth(0.5f).height(48.dp).align(Alignment.CenterHorizontally),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Keluar", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
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
            color = Purple300, // Pastikan ini terimport dari Theme
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Purple50) // Pastikan ini terimport
                .padding(vertical = 14.dp, horizontal = 16.dp)
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                color = PurpleTextDeep, // Pastikan ini terimport
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}