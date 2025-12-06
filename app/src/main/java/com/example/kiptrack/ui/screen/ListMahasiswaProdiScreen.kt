package com.example.kiptrack.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.theme.Purple200
import com.example.kiptrack.ui.theme.Purple300
import com.example.kiptrack.ui.theme.Purple50
import com.example.kiptrack.ui.utils.ImageUtils
import com.example.kiptrack.ui.viewmodel.ListMahasiswaViewModel
import com.example.kiptrack.ui.viewmodel.ListMahasiswaViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListMahasiswaProdiScreen(
    uid: String,
    universityId: String,
    prodiId: String,
    onNavigateToDetailMahasiswa: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: ListMahasiswaViewModel = viewModel(
        factory = ListMahasiswaViewModelFactory(universityId, prodiId)
    )
    val uiState = viewModel.uiState
    val formState = viewModel.formState
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = uiState.prodiName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                onClick = { viewModel.openAddDialog() },
                containerColor = Purple300,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Mahasiswa")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple300)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.mahasiswaList) { mhs ->
                        MahasiswaListItem(
                            name = mhs.nama,
                            nim = mhs.nim,
                            onClick = { onNavigateToDetailMahasiswa(uid, mhs.uid) }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOG STEP 1: MAHASISWA ---
    if (formState.step == 1) {
        Dialog(onDismissRequest = { viewModel.closeAddDialog() }) {
            AddStudentFormStep1(
                jenjang = formState.autoJenjang,
                onNext = { nama, email, pass, nim, sem, foto ->
                    viewModel.updateMhsInput(nama, email, pass, nim, sem, foto)
                    viewModel.goToWaliForm()
                },
                onCancel = { viewModel.closeAddDialog() },
                errorMsg = formState.error
            )
        }
    }

    // --- DIALOG STEP 2: WALI & SUBMIT ---
    if (formState.step == 2) {
        Dialog(onDismissRequest = { /* Prevent dismiss while loading */ }) {
            AddParentFormStep2(
                isLoading = formState.isLoading,
                onSubmit = { nama, email, pass, idW ->
                    viewModel.updateWaliInput(nama, email, pass, idW)
                    viewModel.submitAllData()
                },
                onBack = { viewModel.openAddDialog() },
                errorMsg = formState.error
            )
        }
    }
}

@Composable
fun AddStudentFormStep1(
    jenjang: String,
    onNext: (String, String, String, String, String, String) -> Unit,
    onCancel: () -> Unit,
    errorMsg: String?
) {
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nim by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("1") }
    var photoBase64 by remember { mutableStateOf("") }

    val maxSem = if(jenjang.contains("D3")) 6 else 8
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val base64 = ImageUtils.uriToBase64(context, it)
            if (base64 != null) photoBase64 = base64
        }
    }

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Tambah Mahasiswa (1/2)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Purple300)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Login") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password Login") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = nim, onValueChange = { nim = it }, label = { Text("NIM") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(8.dp))
            Text("Semester Berjalan:", fontSize = 14.sp, color = Purple300)
            SemesterDropdown(maxSem = maxSem, current = semester, onSelected = { semester = it })

            Spacer(Modifier.height(16.dp))
            Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Purple50)) {
                Icon(Icons.Default.CloudUpload, null, tint = Purple300)
                Spacer(Modifier.width(8.dp))
                Text(if(photoBase64.isEmpty()) "Upload Foto Profil" else "Foto Terpilih", color = Purple300)
            }

            if (errorMsg != null) {
                Text(errorMsg, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onCancel) { Text("Batal", color = Color.Gray) }
                Button(onClick = { onNext(nama, email, password, nim, semester, photoBase64) }, colors = ButtonDefaults.buttonColors(containerColor = Purple300)) { Text("Lanjut") }
            }
        }
    }
}

@Composable
fun AddParentFormStep2(
    isLoading: Boolean,
    onSubmit: (String, String, String, String) -> Unit,
    onBack: () -> Unit,
    errorMsg: String?
) {
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var idWali by remember { mutableStateOf("") }

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Data Orang Tua/Wali (2/2)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Purple300)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Wali") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = idWali, onValueChange = { idWali = it }, label = { Text("ID Wali (Untuk Login)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Wali (Aktif)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password Login") }, modifier = Modifier.fillMaxWidth())

            if (errorMsg != null) {
                Text(errorMsg, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onBack, enabled = !isLoading) { Text("Kembali", color = Color.Gray) }
                Button(
                    onClick = { onSubmit(nama, email, password, idWali) },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple300),
                    enabled = !isLoading
                ) {
                    if(isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("Simpan Semua")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterDropdown(maxSem: Int, current: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = (1..maxSem).map { it.toString() }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = current, onValueChange = {}, readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { label ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onSelected(label); expanded = false })
            }
        }
    }
}

// ... (MahasiswaListItem SAMA SEPERTI SEBELUMNYA)
@Composable
fun MahasiswaListItem(name: String, nim: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Purple50.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Purple200),
                contentAlignment = Alignment.Center
            ) {
                Text(name.first().toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Purple300)
                Text("NIM: $nim", fontSize = 12.sp, color = Purple300.copy(alpha = 0.7f))
            }
        }
    }
}