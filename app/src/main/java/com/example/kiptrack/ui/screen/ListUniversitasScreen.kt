package com.example.kiptrack.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.theme.Purple200
import com.example.kiptrack.ui.theme.Purple300
import com.example.kiptrack.ui.theme.Purple50
import com.example.kiptrack.ui.theme.PurpleTextDeep
import com.example.kiptrack.ui.viewmodel.ListUniversitasViewModel
import com.example.kiptrack.ui.viewmodel.ListUniversitasViewModelFactory
import com.example.kiptrack.ui.viewmodel.ProdiItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListUniversitasScreen(
    uid: String,
    universityId: String,
    onNavigateToListMahasiswa: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel: ListUniversitasViewModel = viewModel(
        factory = ListUniversitasViewModelFactory(universityId)
    )
    val state = viewModel.uiState
    val context = LocalContext.current

    // Dialog States
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedProdi by remember { mutableStateOf<ProdiItem?>(null) }

    // Toast Handler
    LaunchedEffect(state.actionSuccess, state.error) {
        state.actionSuccess?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            showAddDialog = false
            showEditDialog = false
            showDeleteDialog = false
            viewModel.resetActionState()
        }
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.universityName,
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
                onClick = { showAddDialog = true },
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (state.prodiItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.isLoading) CircularProgressIndicator(color = Purple300)
                            else Text("Belum ada data Prodi.", color = Color.Gray)
                        }
                    }
                } else {
                    items(state.prodiItems) { prodi ->
                        ProgramStudiItem(
                            prodi = prodi,
                            onClick = { onNavigateToListMahasiswa(uid, prodi.id) },
                            onEditClick = {
                                selectedProdi = prodi
                                showEditDialog = true
                            },
                            onDeleteClick = {
                                selectedProdi = prodi
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    if (showAddDialog) {
        AddProdiDialog(
            onDismiss = { showAddDialog = false },
            onSave = { nama, jenjang, kat -> viewModel.addProdi(nama, jenjang, kat) }
        )
    }

    if (showEditDialog && selectedProdi != null) {
        EditProdiDialog(
            prodi = selectedProdi!!,
            onDismiss = { showEditDialog = false },
            onSave = { nama, jenjang -> viewModel.updateProdi(selectedProdi!!.id, nama, jenjang) }
        )
    }

    if (showDeleteDialog && selectedProdi != null) {
        DeleteConfirmDialog(
            prodiName = selectedProdi!!.nama,
            onDismiss = { showDeleteDialog = false },
            onConfirm = { pass -> viewModel.deleteProdi(selectedProdi!!.id, pass) }
        )
    }
}

// --- ITEM & DIALOG COMPONENTS ---

@Composable
fun ProgramStudiItem(
    prodi: ProdiItem,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(prodi.nama, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Purple300)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Jenjang: ${prodi.jenjang}", fontSize = 12.sp, color = Purple300.copy(alpha = 0.5f))
            }

            // Tombol Edit & Delete
            Row {
                IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, null, tint = Color(0xFFFFA000), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun AddProdiDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var namaProdi by remember { mutableStateOf("") }
    var selectedJenjang by remember { mutableStateOf("S1") }
    var selectedKategori by remember { mutableStateOf("non-kedokteran") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Program Studi", fontWeight = FontWeight.Bold, color = Purple300) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = namaProdi, onValueChange = { namaProdi = it }, label = { Text("Nama Prodi") }, modifier = Modifier.fillMaxWidth())
                Text("Jenjang:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row {
                    listOf("D3", "S1").forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp).clickable { selectedJenjang = item }) {
                            RadioButton(selected = (selectedJenjang == item), onClick = { selectedJenjang = item }, colors = RadioButtonDefaults.colors(selectedColor = Purple300))
                            Text(text = item)
                        }
                    }
                }
                Text("Kategori:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row {
                    listOf("non-kedokteran", "kedokteran").forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp).clickable { selectedKategori = item }) {
                            RadioButton(selected = (selectedKategori == item), onClick = { selectedKategori = item }, colors = RadioButtonDefaults.colors(selectedColor = Purple300))
                            Text(text = if(item == "non-kedokteran") "Non-Kedokteran" else "Kedokteran", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { if (namaProdi.isNotBlank()) onSave(namaProdi, selectedJenjang, selectedKategori) }, colors = ButtonDefaults.buttonColors(containerColor = Purple300)) { Text("Simpan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Purple300) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProdiDialog(
    prodi: ProdiItem,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var nama by remember { mutableStateOf(prodi.nama) }
    var jenjang by remember { mutableStateOf(prodi.jenjang) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Program Studi", color = PurpleTextDeep, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Prodi") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = jenjang, onValueChange = {}, readOnly = true, label = { Text("Jenjang") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("S1", "D3").forEach { item ->
                            DropdownMenuItem(text = { Text(item) }, onClick = { jenjang = item; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(nama, jenjang) }, colors = ButtonDefaults.buttonColors(containerColor = Purple300)) { Text("Simpan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

@Composable
fun DeleteConfirmDialog(
    prodiName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hapus Prodi", color = Color.Red, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Apakah Anda yakin ingin menghapus '$prodiName' secara permanen?")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it }, label = { Text("Password Admin") },
                    visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
        },
        confirmButton = { Button(onClick = { onConfirm(password) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), enabled = password.isNotBlank()) { Text("Hapus") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}