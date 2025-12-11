package com.ukrida.kiptrack.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChevronLeft
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ukrida.kiptrack.ui.utils.ImageUtils
import com.ukrida.kiptrack.ui.viewmodel.DropdownItem
import com.ukrida.kiptrack.ui.viewmodel.ProfileAdminViewModel
import com.ukrida.kiptrack.ui.viewmodel.ProfileAdminViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilMahasiswaAdminScreen(
    uid: String,
    onBackClicked: () -> Unit
) {
    val viewModel: ProfileAdminViewModel = viewModel(
        factory = ProfileAdminViewModelFactory(uid)
    )
    val state = viewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            Toast.makeText(context, "Data Berhasil Diupdate!", Toast.LENGTH_SHORT).show()
            viewModel.resetSuccess()
        }
    }

    // Container Utama
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientMiddle1, GradientMiddle2, GradientEnd)
                )
            )
    ) {
        // --- 1. WAVE BACKGROUND  ---
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .align(Alignment.TopCenter)
        ) {
            val width = size.width
            val height = size.height

            val path1 = Path().apply {
                moveTo(0f, 0f)
                lineTo(0f, height * 0.6f)
                quadraticBezierTo(
                    width * 0.5f, height * 1.0f,
                    width, height * 0.5f
                )
                lineTo(width, 0f)
                close()
            }
            drawPath(path1, Color.White.copy(alpha = 0.15f))

            val path2 = Path().apply {
                moveTo(0f, 0f)
                lineTo(0f, height * 0.4f)
                quadraticBezierTo(
                    width * 0.4f, height * 0.7f,
                    width, height * 0.3f
                )
                lineTo(width, 0f)
                close()
            }
            drawPath(path2, Color.White.copy(alpha = 0.1f))
        }


        // --- 2. Custom Top Bar  ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp, start = 8.dp, end = 24.dp, bottom = 10.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClicked, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = "Back",
                    tint = Color(0xFF4C005F),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Edit Profil Mahasiswa",
                color = NameColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // --- 3. Content Scrollable ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Box Wrapper untuk Avatar + Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 30.dp)
            ) {
                // LAYER A: KARTU PUTIH
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                    ) {
                        Spacer(Modifier.height(50.dp))

                        // --- INPUT FIELDS ---
                        ProfileReadOnlyField("Nomor Induk Mahasiswa", state.nim)
                        Spacer(Modifier.height(16.dp))

                        SearchableDropdownField("Universitas", state.selectedUnivName, state.universityOptions, { viewModel.onUniversitySelected(it.id, it.name) }, "Cari Universitas...")
                        Spacer(Modifier.height(16.dp))

                        SearchableDropdownField("Program Studi", state.selectedProdiName, state.prodiOptions, { viewModel.onProdiSelected(it.id, it.name) }, if (state.selectedUnivId.isEmpty()) "Pilih Univ Dulu" else "Cari Prodi...", state.selectedUnivId.isNotEmpty())
                        Spacer(Modifier.height(16.dp))

                        ProfileEditableField("Jenjang yang akan Ditempuh", state.jenjang, viewModel::onJenjangChange)
                        Spacer(Modifier.height(16.dp))

                        SemesterDropdownField(
                            currentSemester = state.currentDbSemester,
                            selectedSemester = state.semesterBerjalan,
                            maxSemester = state.maxSemester,
                            onSemesterSelected = viewModel::onSemesterChange
                        )
                        Spacer(Modifier.height(16.dp))

                        ProfileEditableField("Nama Orang Tua/Wali", state.namaWali, viewModel::onWaliChange)

                        Spacer(Modifier.height(32.dp))

                        // --- TOMBOL SIMPAN ---
                        Button(
                            onClick = { viewModel.saveChanges() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(6.dp, RoundedCornerShape(50)),
                            colors = ButtonDefaults.buttonColors(containerColor = FieldValueColor),
                            shape = RoundedCornerShape(50),
                            enabled = !state.isSaving
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("SIMPAN", color = GradientStart, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // LAYER B: AVATAR & NAMA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(4.dp, GradientEnd.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.photoProfile.isNotBlank()) {
                            val bitmap = ImageUtils.base64ToBitmap(state.photoProfile)
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Filled.Person, null, tint = GradientEnd, modifier = Modifier.size(50.dp))
                            }
                        } else {
                            Icon(Icons.Filled.Person, null, tint = GradientEnd, modifier = Modifier.size(50.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Nama
                    Text(
                        text = if (state.isLoading) "Memuat..." else state.nama,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = NameColor,
                        modifier = Modifier.padding(top = 70.dp, start = 8.dp)
                    )
                }
            }
        }
    }
}

// --- KOMPONEN INPUT (Tidak Berubah) ---

@Composable
fun ProfileReadOnlyField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 13.sp, color = LabelColor, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(FieldBackground, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = value, fontSize = 16.sp, color = FieldValueColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditableField(label: String, value: String, onValueChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 13.sp, color = LabelColor, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = FieldValueColor, fontWeight = FontWeight.SemiBold),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FieldBackground,
                unfocusedContainerColor = FieldBackground,
                disabledContainerColor = FieldBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterDropdownField(currentSemester: Int, selectedSemester: String, maxSemester: Int, onSemesterSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val semesterOptions = (currentSemester..maxSemester).map { "Semester $it" }
    val semesterSelectionList = (currentSemester..maxSemester).map { it.toString() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Semester Saat Ini", fontSize = 13.sp, color = LabelColor, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            TextField(
                value = selectedSemester, onValueChange = {}, readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = FieldValueColor, fontWeight = FontWeight.SemiBold),
                colors = TextFieldDefaults.colors(focusedContainerColor = FieldBackground, unfocusedContainerColor = FieldBackground, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
                semesterSelectionList.forEach { sem ->
                    DropdownMenuItem(text = { Text("Semester $sem", color = FieldValueColor) }, onClick = { onSemesterSelected(sem); expanded = false })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableDropdownField(label: String, currentValue: String, options: List<DropdownItem>, onItemSelected: (DropdownItem) -> Unit, placeholder: String, enabled: Boolean = true) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var displayedText by remember { mutableStateOf(currentValue) }
    LaunchedEffect(currentValue) { if (!expanded) displayedText = currentValue }
    val filteredOptions = if (expanded) options.filter { it.name.contains(searchQuery, ignoreCase = true) } else options

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 13.sp, color = LabelColor, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if(enabled) expanded = !expanded }) {
            TextField(
                value = if (expanded) searchQuery else displayedText,
                onValueChange = { searchQuery = it; if (!expanded) expanded = true },
                readOnly = false, enabled = enabled, placeholder = { Text(placeholder, color = Color.Gray) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = FieldValueColor, fontWeight = FontWeight.SemiBold),
                colors = TextFieldDefaults.colors(focusedContainerColor = FieldBackground, unfocusedContainerColor = FieldBackground, disabledContainerColor = FieldBackground, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            if (expanded) {
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false; searchQuery = "" }, modifier = Modifier.background(Color.White)) {
                    if (filteredOptions.isNotEmpty()) {
                        filteredOptions.forEach { item -> DropdownMenuItem(text = { Text(item.name, color = FieldValueColor) }, onClick = { onItemSelected(item); displayedText = item.name; expanded = false; searchQuery = "" }) }
                    } else { DropdownMenuItem(text = { Text("Tidak ditemukan", color = Color.Gray) }, onClick = { }, enabled = false) }
                }
            }
        }
    }
}

val GradientStart = Color(0xFFDECDE9)
val GradientMiddle1 = Color(0xFFC9ADDB)
val GradientMiddle2 = Color(0xFFB48ECD)
val GradientEnd = Color(0xFFB14EA7)

// Warna UI
val LabelColor = Color(0xFFB14EA7)
val NameColor = Color(0xFFB14EA7)
val FieldValueColor = Color(0xFF894EB1)
val FieldBackground = Color(0xFFF7F4F9)