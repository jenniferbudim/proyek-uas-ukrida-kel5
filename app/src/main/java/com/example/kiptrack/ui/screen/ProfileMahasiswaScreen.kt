package com.example.kiptrack.ui.screen

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.kiptrack.ui.theme.*
import com.example.kiptrack.ui.utils.ImageUtils
import com.example.kiptrack.ui.viewmodel.ProfileMahasiswaViewModel
import com.example.kiptrack.ui.viewmodel.ProfileMahasiswaViewModelFactory

@Composable
fun ProfileMahasiswaScreen(uid: String, onBackClicked: () -> Unit, onLogoutClicked: () -> Unit) {
    // Init ViewModel
    val viewModel: ProfileMahasiswaViewModel = viewModel(
        factory = ProfileMahasiswaViewModelFactory(uid)
    )
    val state = viewModel.uiState
    val context = LocalContext.current

    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent = result.uriContent
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

    fun startCrop() {
        val options = CropImageContractOptions(
            uri = null,
            cropImageOptions = CropImageOptions(
                imageSourceIncludeGallery = true,
                imageSourceIncludeCamera = true,
                cropShape = CropImageView.CropShape.OVAL,
                fixAspectRatio = true,
                aspectRatioX = 1,
                aspectRatioY = 1,
                outputCompressFormat = Bitmap.CompressFormat.JPEG,
                outputCompressQuality = 70,
                outputRequestWidth = 500,
                outputRequestHeight = 500
            )
        )
        imageCropLauncher.launch(options)
    }

    // --- UI CONTENT ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Purple100, PurplePrimary)
                )
            )
    ) {
        // 1. Wavy Background Shape
        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height * 0.5f)
                quadraticBezierTo(
                    size.width * 0.5f, size.height * 1.0f,
                    0f, size.height * 0.3f
                )
                close()
            }
            drawPath(path = path, color = Purple50)
        }

        // 2. Custom Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, start = 16.dp, end = 24.dp)
                .zIndex(2f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PurpleTextDeep,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "Profil Mahasiswa",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Purple300
            )
        }

        // 3. Main Content Wrapper
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 90.dp, bottom = 40.dp, start = 20.dp, end = 20.dp),
            contentAlignment = Alignment.TopCenter
        ) {

            // --- MAIN CARD  ---
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 50.dp, bottom = 25.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    if (state.isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PurplePrimary)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 60.dp, start = 20.dp, end = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Name
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.studentData.nama,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Purple200.copy(alpha = 0.3f), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(16.dp))

                            // Scrollable Fields
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                ProfileDetailFieldRefStyle("Nomor Induk Mahasiswa", state.studentData.nim)
                                Spacer(modifier = Modifier.height(12.dp))
                                ProfileDetailFieldRefStyle("Universitas", state.studentData.universityName)
                                Spacer(modifier = Modifier.height(12.dp))
                                ProfileDetailFieldRefStyle("Program Studi", state.studentData.programStudiName)
                                Spacer(modifier = Modifier.height(12.dp))
                                ProfileDetailFieldRefStyle("Jenjang yang akan Ditempuh", state.studentData.jenjang)
                                Spacer(modifier = Modifier.height(12.dp))
                                ProfileDetailFieldRefStyle("Semester Saat Ini", state.studentData.semesterBerjalan)
                                Spacer(modifier = Modifier.height(12.dp))
                                ProfileDetailFieldRefStyle("Nama Orang Tua/Wali", state.studentData.namaWali)
                            }

                            Spacer(modifier = Modifier.height(64.dp))
                        }
                    }
                }

                // --- FLOATING BUTTON ---
                Button(
                    onClick = onLogoutClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = Purple200),
                    shape = RoundedCornerShape(15.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(50.dp)
                        .width(150.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = PurpleTextDeep,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Keluar",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // --- AVATAR (Floating on Top) ---
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .zIndex(1f)
                    .clip(CircleShape)
                    .background(Purple300)
                    .clickable { startCrop() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(AvatarBackground)
                ) {
                    if (state.studentData.fotoProfil.isNotBlank()) {
                        val bitmap = ImageUtils.base64ToBitmap(state.studentData.fotoProfil)
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.fillMaxSize().padding(10.dp))
                        }
                    } else {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.fillMaxSize().padding(10.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Purple300, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Edit",
                        tint = PurplePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileDetailFieldRefStyle(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Purple300,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Text(
                text = value,
                fontSize = 15.sp,
                color = PurpleTextDeep,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}