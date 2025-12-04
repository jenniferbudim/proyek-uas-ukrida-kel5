package com.example.kiptrack.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.theme.Purple300
import com.example.kiptrack.ui.theme.Purple50
import com.example.kiptrack.ui.theme.Purple200
import com.example.kiptrack.ui.viewmodel.ListUniversitasViewModel
import com.example.kiptrack.ui.viewmodel.ListUniversitasViewModelFactory

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

    // State untuk Dialog Tambah Prodi
    var showAddDialog by remember { mutableStateOf(false) }

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
                onClick = { showAddDialog = true }, // Buka Dialog
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
                if (state.prodiList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.universityName == "Loading...") {
                                CircularProgressIndicator(color = Purple300)
                            } else {
                                Text("Belum ada data Prodi.", color = Color.Gray)
                            }
                        }
                    }
                } else {
                    items(state.prodiList.size) { index ->
                        val prodiName = state.prodiList[index]
                        val prodiId = state.prodiIds[index]

                        ProgramStudiItem(
                            name = prodiName,
                            onClick = { onNavigateToListMahasiswa(uid, prodiId) }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOG TAMBAH PRODI ---
    if (showAddDialog) {
        AddProdiDialog(
            onDismiss = { showAddDialog = false },
            onSave = { nama, jenjang, kategori ->
                viewModel.addProdi(nama, jenjang, kategori)
                showAddDialog = false
            }
        )
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
                // Input Nama
                OutlinedTextField(
                    value = namaProdi,
                    onValueChange = { namaProdi = it },
                    label = { Text("Nama Prodi") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Input Jenjang (Simple Radio/Dropdown Simulation)
                Text("Jenjang:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row {
                    listOf("D3", "S1").forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable { selectedJenjang = item }
                        ) {
                            RadioButton(
                                selected = (selectedJenjang == item),
                                onClick = { selectedJenjang = item },
                                colors = RadioButtonDefaults.colors(selectedColor = Purple300)
                            )
                            Text(text = item)
                        }
                    }
                }

                // Input Kategori
                Text("Kategori:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row {
                    listOf("non-kedokteran", "kedokteran").forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { selectedKategori = item }
                        ) {
                            RadioButton(
                                selected = (selectedKategori == item),
                                onClick = { selectedKategori = item },
                                colors = RadioButtonDefaults.colors(selectedColor = Purple300)
                            )
                            Text(text = if(item == "non-kedokteran") "Non-Kedokteran" else "Kedokteran", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (namaProdi.isNotBlank()) {
                        onSave(namaProdi, selectedJenjang, selectedKategori)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Purple300)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Purple300)
            }
        }
    )
}

@Composable
fun ProgramStudiItem(name: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Purple300)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Program Studi", fontSize = 12.sp, color = Purple300.copy(alpha = 0.5f))
        }
    }
}