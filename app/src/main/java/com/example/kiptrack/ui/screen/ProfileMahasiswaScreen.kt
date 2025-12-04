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
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
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

    // --- LOGIKA BARU: CROP IMAGE LAUNCHER ---
    // Menggantikan launcher galeri biasa
    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent = result.uriContent
            // Jika berhasil crop, ambil URI dan konversi ke Base64
            if (uriContent != null) {
                val base64 = ImageUtils.uriToBase64(context, uriContent)
                if (base64 != null) {
                    viewModel.updateProfilePicture(base64)
                }
            }
        } else {
            println("Image Cropping error: ${result.error}")
        }
    }

    // Fungsi helper untuk memulai crop dengan settingan BULAT
    fun startCrop() {
        val options = CropImageContractOptions(
            uri = null, // null artinya user akan diminta pilih dari galeri dulu
            cropImageOptions = CropImageOptions(
                imageSourceIncludeGallery = true,
                imageSourceIncludeCamera = true,
                // Paksa crop berbentuk OVAL (Bulat)
                cropShape = CropImageView.CropShape.OVAL,
                // Paksa rasio 1:1
                fixAspectRatio = true,
                aspectRatioX = 1,
                aspectRatioY = 1,
                // Kompresi hasil crop
                outputCompressFormat = android.graphics.Bitmap.CompressFormat.JPEG,
                outputCompressQuality = 70,
                outputRequestWidth = 500,
                outputRequestHeight = 500
            )
        )
        imageCropLauncher.launch(options)
    }

    Scaffold(
        containerColor = PurplePrimary, // Warna asli kode kamu
        topBar = {
            TopAppBar(
                title = { Text("Profile Mahasiswa", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PurplePrimary) // Warna asli
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PurplePrimary) // Warna asli
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
                    modifier = Modifier.size(84.dp)
                ) {
                    // A. Lingkaran Foto Profil
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(AvatarBackground) // Warna asli
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { startCrop() }, // <--- PANGGIL FUNGSI CROP DISINI
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.studentData.fotoProfil.isNotBlank()) {
                            val bitmap = com.example.kiptrack.ui.utils.ImageUtils.base64ToBitmap(state.studentData.fotoProfil)
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, null, tint = PurplePrimary, modifier = Modifier.size(40.dp))
                            }
                        } else {
                            Icon(Icons.Default.Person, null, tint = PurplePrimary, modifier = Modifier.size(40.dp))
                        }
                    }

                    // B. Ikon Kamera (Mengapung di Pojok Kanan Bawah)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, AvatarBackground, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Edit Foto",
                            tint = Purple300, // Warna asli
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
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary), // Warna asli
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
            color = Purple300, // Warna asli
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Purple50) // Warna asli
                .padding(vertical = 14.dp, horizontal = 16.dp)
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                color = PurpleTextDeep, // Warna asli
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}