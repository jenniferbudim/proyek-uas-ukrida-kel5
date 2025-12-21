package com.ukrida.kiptrack.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ukrida.kiptrack.ui.theme.Purple300
import com.ukrida.kiptrack.ui.theme.Purple50
import com.ukrida.kiptrack.ui.utils.ImageUtils
import com.ukrida.kiptrack.ui.viewmodel.ListMahasiswaViewModel
import com.ukrida.kiptrack.ui.viewmodel.ListMahasiswaViewModelFactory

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

    // State Dialog Hapus
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedStudentId by remember { mutableStateOf("") }
    var selectedStudentName by remember { mutableStateOf("") }

    LaunchedEffect(uiState.deleteSuccess, uiState.deleteError) {
        uiState.deleteSuccess?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            showDeleteDialog = false
            viewModel.resetDeleteState()
        }
        uiState.deleteError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.resetDeleteState()
        }
    }

    Scaffold(
        containerColor = Purple50,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Purple50)
                    .padding(top = 16.dp, bottom = 0.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(imageVector = Icons.Outlined.ChevronLeft, contentDescription = "Back", tint = TextPurpleDark, modifier = Modifier.size(32.dp))
                    }
                    Text(text = "Daftar Mahasiswa", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPurpleDark, modifier = Modifier.align(Alignment.Center))
                }
                Text(text = uiState.prodiName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFB14EA7).copy(alpha = 0.8f), modifier = Modifier.fillMaxWidth().padding(end = 24.dp, top = 8.dp, bottom = 4.dp), textAlign = TextAlign.End)
                Divider(color = TextPurpleDark.copy(alpha = 0.5f), thickness = 2.dp, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = TextPurpleDark,
                contentColor = Color.White,
                shape = RoundedCornerShape(topStart = 30.dp, bottomStart = 30.dp, bottomEnd = 0.dp, topEnd = 30.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Tambah", fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Purple300) }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.mahasiswaList) { mhs ->
                        MahasiswaListItem(
                            name = mhs.nama,
                            nim = mhs.nim,
                            fotoUrl = null,
                            onClick = { onNavigateToDetailMahasiswa(uid, mhs.uid) },
                            onDeleteClick = { selectedStudentId = mhs.uid; selectedStudentName = mhs.nama; showDeleteDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteStudentDialog(studentName = selectedStudentName, onDismiss = { showDeleteDialog = false }, onConfirm = { password -> viewModel.deleteStudentWithAuth(selectedStudentId, password) })
    }

    if (formState.step == 1) {
        Dialog(onDismissRequest = { viewModel.closeAddDialog() }) {
            AddStudentFormStep1(
                jenjang = formState.autoJenjang,
                initialNama = formState.mhsNama,
                initialEmail = formState.mhsEmail,
                initialPass = formState.mhsPassword,
                initialNim = formState.mhsNim,
                initialSem = formState.mhsSemester,
                initialFoto = formState.mhsFoto,
                onNext = { nama, email, pass, nim, sem, foto ->
                    viewModel.updateMhsInput(nama, email, pass, nim, sem, foto)
                    viewModel.goToWaliForm()
                },
                onCancel = { viewModel.closeAddDialog() },
                errorMsg = formState.error
            )
        }
    }

    if (formState.step == 2) {
        Dialog(onDismissRequest = { }) {
            AddParentFormStep2(
                isLoading = formState.isLoading,
                initialNama = formState.waliNama,
                initialId = formState.waliId,
                initialEmail = formState.waliEmail,
                initialPass = formState.waliPassword,
                onSubmit = { nama, email, pass, idW ->
                    viewModel.updateWaliInput(nama, email, pass, idW)
                    viewModel.submitAllData()
                },

                onBack = { nama, email, pass, idW ->

                    viewModel.updateWaliInput(nama, email, pass, idW)
                    viewModel.openAddDialog()
                },
                errorMsg = formState.error
            )
        }
    }
}

@Composable
fun MahasiswaListItem(name: String, nim: String, fotoUrl: String? = null, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardColor), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(55.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                Text(text = name.first().toString(), color = TextPurpleDark, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPurpleDark)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = nim, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            IconButton(onClick = onDeleteClick) { Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = TextPurpleDark.copy(alpha = 0.5f)) }
        }
    }
}

@Composable
fun DeleteStudentDialog(studentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var password by remember { mutableStateOf("") }
    AlertDialog(
        containerColor = Color.White, onDismissRequest = onDismiss,
        title = { Text("Hapus Mahasiswa", color = Color.Red, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Hapus data '$studentName' permanen?", color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, visualTransformation = PasswordVisualTransformation(), placeholder = { Text("Password Admin") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = { onConfirm(password) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), enabled = password.isNotBlank()) { Text("Hapus", color = Color.White) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

@Composable
fun AddStudentFormStep1(
    jenjang: String,
    initialNama: String,
    initialEmail: String,
    initialPass: String,
    initialNim: String,
    initialSem: String,
    initialFoto: String,
    onNext: (String, String, String, String, String, String) -> Unit,
    onCancel: () -> Unit,
    errorMsg: String?
) {
    var nama by remember { mutableStateOf(initialNama) }
    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf(initialPass) }
    var nim by remember { mutableStateOf(initialNim) }
    var semester by remember { mutableStateOf(if (initialSem.isBlank()) "1" else initialSem) }
    var photoBase64 by remember { mutableStateOf(initialFoto) }

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
            Text("Tambah Mahasiswa (1/2)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPurpleDark)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Login") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password Login") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = nim, onValueChange = { nim = it }, label = { Text("NIM") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(8.dp))
            Text("Semester Berjalan:", fontSize = 14.sp, color = TextPurpleDark)
            SemesterDropdown(maxSem = maxSem, current = semester, onSelected = { semester = it })

            Spacer(Modifier.height(16.dp))
            Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = CardColor)) {
                Icon(Icons.Default.CloudUpload, null, tint = TextPurpleDark)
                Spacer(Modifier.width(8.dp))
                Text(if(photoBase64.isEmpty()) "Upload Foto Profil" else "Foto Terpilih", color = TextPurpleDark)
            }

            if (errorMsg != null) Text(errorMsg, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onCancel) { Text("Batal", color = Color.Gray) }
                Button(onClick = { onNext(nama, email, password, nim, semester, photoBase64) }, colors = ButtonDefaults.buttonColors(containerColor = TextPurpleDark)) { Text("Lanjut", color = Color.White) }
            }
        }
    }
}

@Composable
fun AddParentFormStep2(
    isLoading: Boolean,
    initialNama: String,
    initialId: String,
    initialEmail: String,
    initialPass: String,
    onSubmit: (String, String, String, String) -> Unit,

    onBack: (String, String, String, String) -> Unit,
    errorMsg: String?
) {
    var nama by remember { mutableStateOf(initialNama) }
    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf(initialPass) }
    var idWali by remember { mutableStateOf(initialId) }

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Data Orang Tua/Wali (2/2)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPurpleDark)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Wali") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = idWali, onValueChange = { idWali = it }, label = { Text("ID Wali (Untuk Login)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Wali (Aktif)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password Login") }, modifier = Modifier.fillMaxWidth())

            if (errorMsg != null) Text(errorMsg, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {

                TextButton(
                    onClick = { onBack(nama, email, password, idWali) },
                    enabled = !isLoading
                ) { Text("Kembali", color = Color.Gray) }

                Button(
                    onClick = { onSubmit(nama, email, password, idWali) },
                    colors = ButtonDefaults.buttonColors(containerColor = TextPurpleDark),
                    enabled = !isLoading
                ) {
                    if(isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) else Text("Simpan Semua", color = Color.White)
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
        OutlinedTextField(value = current, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { label -> DropdownMenuItem(text = { Text(label) }, onClick = { onSelected(label); expanded = false }) }
        }
    }
}

val CardColor = Color(0xFFDECDE9)
val TextPurpleDark = Color(0xFF894EB1)