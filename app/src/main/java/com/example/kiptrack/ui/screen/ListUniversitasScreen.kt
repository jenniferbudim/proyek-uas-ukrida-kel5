package com.example.kiptrack.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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

    // --- State UI ---
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedProdi by remember { mutableStateOf<ProdiItem?>(null) }

    // --- SEARCH LOGIC  ---
    var searchQuery by remember { mutableStateOf("") }

    // Filter list berdasarkan input search secara real-time
    val filteredProdiItems = remember(searchQuery, state.prodiItems) {
        if (searchQuery.isBlank()) {
            state.prodiItems
        } else {
            state.prodiItems.filter { item ->
                item.nama.contains(searchQuery, ignoreCase = true) ||
                        item.jenjang.contains(searchQuery, ignoreCase = true)
            }
        }
    }

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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MainPurple,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 90.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Program Studi")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(GradColor1, GradColor2, GradColor3, GradColor4)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                // --- 1. SEARCH BAR ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 10.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari Program Studi", color = Color.Gray.copy(alpha = 0.6f)) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MainPurple)
                        },
                        shape = RoundedCornerShape(50),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(4.dp, RoundedCornerShape(50))
                    )
                }

                // --- 2. HEADER UNIVERSITAS (Icon + Nama) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = "Logo Kampus",
                        tint = DarkPurple,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = state.universityName.ifEmpty { "Nama Universitas" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White,
                        fontFamily = FontFamily.Serif,
                        modifier = Modifier.weight(1f),
                        lineHeight = 26.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Divider(
                    color = Color.White.copy(alpha = 0.5f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                // --- 3. LIST CONTENT (GRID) ---
                Box(modifier = Modifier.weight(1f)) {
                    if (state.isLoading && filteredProdiItems.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    } else if (filteredProdiItems.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            val message = if (searchQuery.isNotEmpty()) "Tidak ditemukan: '$searchQuery'" else "Belum ada data Prodi."
                            Text(message, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp, start = 16.dp, end = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredProdiItems) { prodi ->
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

                // --- 4. FOOTER (TOMBOL KEMBALI) ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Divider(
                        color = Color.White.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = LightButtonBg),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .width(200.dp)
                            .height(50.dp)
                            .shadow(6.dp, RoundedCornerShape(50))
                    ) {
                        Text(
                            text = "KEMBALI",
                            color = DarkPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
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

@Composable
fun ProgramStudiItem(
    prodi: ProdiItem,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardPurpleBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // --- TEXT CONTENT AREA ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Nama Prodi
                Text(
                    text = prodi.nama,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPurple,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Serif,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Kategori
                val labelKategori = if (prodi.kategori.equals("kedokteran", ignoreCase = true)) {
                    "Kedokteran"
                } else {
                    "Non-Kedokteran"
                }

                Text(
                    text = labelKategori,
                    fontSize = 12.sp,
                    color = DarkPurple.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Jenjang
                Text(
                    text = "Sarjana (${prodi.jenjang})",
                    fontSize = 14.sp,
                    color = TextPink,
                    fontWeight = FontWeight.Bold
                )
            }

            // --- BUTTONS AREA ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Edit
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(elevation = 2.dp, shape = CircleShape)
                        .background(Color.White, CircleShape)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = MainPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Tombol Delete
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(elevation = 2.dp, shape = CircleShape)
                        .background(Color.White, CircleShape)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// --- DIALOG COMPONENTS ---

@Composable
fun AddProdiDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var namaProdi by remember { mutableStateOf("") }
    var selectedJenjang by remember { mutableStateOf("S1") }
    var selectedKategori by remember { mutableStateOf("non-kedokteran") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Program Studi", fontWeight = FontWeight.Bold, color = MainPurple) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = namaProdi,
                    onValueChange = { namaProdi = it },
                    label = { Text("Nama Prodi") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Jenjang:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = DarkPurple)
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
                                colors = RadioButtonDefaults.colors(selectedColor = MainPurple)
                            )
                            Text(text = item)
                        }
                    }
                }
                Text("Kategori:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = DarkPurple)
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
                                colors = RadioButtonDefaults.colors(selectedColor = MainPurple)
                            )
                            Text(
                                text = if(item == "non-kedokteran") "Non-Kedokteran" else "Kedokteran",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (namaProdi.isNotBlank()) onSave(namaProdi, selectedJenjang, selectedKategori) },
                colors = ButtonDefaults.buttonColors(containerColor = MainPurple)
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = MainPurple) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProdiDialog(prodi: ProdiItem, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var nama by remember { mutableStateOf(prodi.nama) }
    var jenjang by remember { mutableStateOf(prodi.jenjang) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Program Studi", color = MainPurple, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Prodi") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = jenjang,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jenjang") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("S1", "D3").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    jenjang = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nama, jenjang) },
                colors = ButtonDefaults.buttonColors(containerColor = MainPurple)
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
        }
    )
}

@Composable
fun DeleteConfirmDialog(prodiName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hapus Prodi", color = Color.Red, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Hapus '$prodiName' permanen?")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password Admin") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(password) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                enabled = password.isNotBlank()
            ) { Text("Hapus") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
        }
    )
}

// --- Custom Colors ---
private val MainPurple = Color(0xFF9C27B0)
private val DarkPurple = Color(0xFF4A148C)
private val TextPurple = Color(0xFF6A1B9A)
private val TextPink = Color(0xFFD81B60)
private val CardPurpleBg = Color(0xFFF3E5F5)
private val LightButtonBg = Color(0xFFE1BEE7)

// Warna Gradient Sesuai Request
private val GradColor1 = Color(0xFFDECDE9)
private val GradColor2 = Color(0xFFC9ADDB)
private val GradColor3 = Color(0xFFB48ECD)
private val GradColor4 = Color(0xFFB14EA7)