package com.example.kiptrack.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.theme.*
import com.example.kiptrack.ui.utils.ImageUtils
import com.example.kiptrack.ui.viewmodel.DropdownItem
import com.example.kiptrack.ui.viewmodel.ProfileAdminViewModel
import com.example.kiptrack.ui.viewmodel.ProfileAdminViewModelFactory

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

    Scaffold(
        containerColor = PurplePrimary,
        topBar = {
            TopAppBar(
                title = { Text("Edit Profil Mahasiswa", color = Color.White, fontWeight = FontWeight.Bold) },
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
            // --- HEADER (Read Only Avatar) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AvatarBackground)
                        .border(2.dp, Color.White, CircleShape),
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
                        } else { Icon(Icons.Filled.Person, null, tint = PurplePrimary, modifier = Modifier.size(40.dp)) }
                    } else { Icon(Icons.Filled.Person, null, tint = PurplePrimary, modifier = Modifier.size(40.dp)) }
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column {
                    Text(text = if (state.isLoading) "Memuat..." else state.nama, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = if (state.isLoading) "..." else state.nim, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }

            // --- FORM EDIT ---
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
                        ProfileEditableField("Nama Lengkap", state.nama, viewModel::onNameChange)
                        Spacer(Modifier.height(16.dp))
                        ProfileEditableField("Nomor Induk Mahasiswa", state.nim, viewModel::onNimChange, KeyboardType.Number)
                        Spacer(Modifier.height(16.dp))

                        // Dropdowns
                        SearchableDropdownField("Universitas", state.selectedUnivName, state.universityOptions, { viewModel.onUniversitySelected(it.id, it.name) }, "Cari Universitas...")
                        Spacer(Modifier.height(16.dp))
                        SearchableDropdownField("Program Studi", state.selectedProdiName, state.prodiOptions, { viewModel.onProdiSelected(it.id, it.name) }, if (state.selectedUnivId.isEmpty()) "Pilih Univ Dulu" else "Cari Prodi...", state.selectedUnivId.isNotEmpty())
                        Spacer(Modifier.height(16.dp))

                        ProfileEditableField("Jenjang", state.jenjang, viewModel::onJenjangChange)
                        Spacer(Modifier.height(16.dp))

                        // --- SEMESTER DROPDOWN (LOGIKA BARU) ---
                        SemesterDropdownField(
                            currentSemester = state.currentDbSemester, // Semester Asli
                            selectedSemester = state.semesterBerjalan, // Yang dipilih di UI
                            maxSemester = state.maxSemester,
                            onSemesterSelected = viewModel::onSemesterChange
                        )

                        Spacer(Modifier.height(16.dp))
                        ProfileEditableField("Nama Wali", state.namaWali, viewModel::onWaliChange)

                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = { viewModel.saveChanges() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            shape = RoundedCornerShape(50),
                            enabled = !state.isSaving
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("SIMPAN PERUBAHAN", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- KOMPONEN BARU: SEMESTER DROPDOWN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterDropdownField(
    currentSemester: Int,
    selectedSemester: String,
    maxSemester: Int,
    onSemesterSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Generate list semester: Mulai dari semester saat ini hingga Max
    // Contoh: Jika sekarang sem 5, Max 8 -> List: [5, 6, 7, 8]
    // Kita izinkan admin memilih semester sekarang (tidak berubah) atau naik
    val semesterOptions = (currentSemester..maxSemester).map { it.toString() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Semester Saat Ini",
            fontSize = 13.sp,
            color = Purple300,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedSemester,
                onValueChange = {},
                readOnly = true, // Tidak boleh ketik manual
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = PurpleTextDeep, fontWeight = FontWeight.SemiBold),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Purple50,
                    unfocusedContainerColor = Purple50,
                    disabledContainerColor = Purple50,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().menuAnchor().shadow(2.dp, RoundedCornerShape(12.dp))
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                semesterOptions.forEach { sem ->
                    DropdownMenuItem(
                        text = { Text(sem, color = PurpleTextDeep) },
                        onClick = {
                            onSemesterSelected(sem)
                            expanded = false
                        }
                    )
                }
            }
        }

        if (selectedSemester.toIntOrNull() ?: 0 > currentSemester) {
            Text(
                text = "⚠️ Menaikkan semester akan mereset pelanggaran & menambah saldo.",
                fontSize = 11.sp,
                color = Color(0xFFFFA000), // Warning Orange
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}


// ... (SearchableDropdownField & ProfileEditableField SAMA SEPERTI SEBELUMNYA) ...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableDropdownField(
    label: String,
    currentValue: String,
    options: List<DropdownItem>,
    onItemSelected: (DropdownItem) -> Unit,
    placeholder: String = "Pilih...",
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var displayedText by remember { mutableStateOf(currentValue) }
    LaunchedEffect(currentValue) { if (!expanded) displayedText = currentValue }
    val filteredOptions = if (expanded) { options.filter { it.name.contains(searchQuery, ignoreCase = true) } } else { options }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 13.sp, color = Purple300, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if(enabled) expanded = !expanded }) {
            TextField(
                value = if (expanded) searchQuery else displayedText,
                onValueChange = { newText -> searchQuery = newText; if (!expanded) expanded = true },
                readOnly = false, enabled = enabled, placeholder = { Text(placeholder, color = Color.Gray) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = PurpleTextDeep, fontWeight = FontWeight.SemiBold),
                colors = TextFieldDefaults.colors(focusedContainerColor = Purple50, unfocusedContainerColor = Purple50, disabledContainerColor = Color(0xFFF5F5F5), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().menuAnchor().shadow(2.dp, RoundedCornerShape(12.dp))
            )
            if (expanded) {
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false; searchQuery = "" }, modifier = Modifier.background(Color.White)) {
                    if (filteredOptions.isNotEmpty()) {
                        filteredOptions.forEach { item -> DropdownMenuItem(text = { Text(item.name, color = PurpleTextDeep) }, onClick = { onItemSelected(item); displayedText = item.name; expanded = false; searchQuery = "" }) }
                    } else { DropdownMenuItem(text = { Text("Tidak ditemukan", color = Color.Gray) }, onClick = { }, enabled = false) }
                }
            }
        }
    }
}

@Composable
fun ProfileEditableField(label: String, value: String, onValueChange: (String) -> Unit, keyboardType: KeyboardType = KeyboardType.Text) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 13.sp, color = Purple300, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        TextField(value = value, onValueChange = onValueChange, textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = PurpleTextDeep, fontWeight = FontWeight.SemiBold), keyboardOptions = KeyboardOptions(keyboardType = keyboardType), modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = TextFieldDefaults.colors(focusedContainerColor = Purple50, unfocusedContainerColor = Purple50, disabledContainerColor = Purple50, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), singleLine = true)
    }
}