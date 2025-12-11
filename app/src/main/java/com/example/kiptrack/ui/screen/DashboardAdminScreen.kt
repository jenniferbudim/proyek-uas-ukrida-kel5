package com.example.kiptrack.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.data.Cluster
import com.example.kiptrack.ui.data.University
import com.example.kiptrack.ui.theme.Purple300
import com.example.kiptrack.ui.theme.Purple50
import com.example.kiptrack.ui.theme.Purple200
import com.example.kiptrack.ui.theme.PurpleDark
import com.example.kiptrack.ui.theme.PurplePrimary
import com.example.kiptrack.ui.theme.PurpleTextDeep
import com.example.kiptrack.ui.viewmodel.DashboardAdminViewModel
import com.example.kiptrack.ui.viewmodel.DashboardAdminViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardAdminScreen(
    uid: String,
    onNavigateToListUniversitas: (String, String) -> Unit,
    onLogoutClicked: () -> Unit
) {
    val viewModel: DashboardAdminViewModel = viewModel(
        factory = DashboardAdminViewModelFactory(uid)
    )
    val state = viewModel.uiState
    val context = LocalContext.current

    // State for dialogs
    var showAddUniDialog by remember { mutableStateOf(false) }
    var showEditUniDialog by remember { mutableStateOf(false) }
    var showDeleteUniConfirmDialog by remember { mutableStateOf(false) }
    var selectedUniForEdit by remember { mutableStateOf<University?>(null) }

    var showEditClusterDialog by remember { mutableStateOf(false) }
    var selectedClusterForEdit by remember { mutableStateOf<Cluster?>(null) }

    // Toast Handler
    LaunchedEffect(state.actionSuccess, state.actionError) {
        state.actionSuccess?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.resetActionState()
        }
        state.actionError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.resetActionState()
        }
    }

    Scaffold(
        floatingActionButton = {
            if (state.selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddUniDialog = true },
                    containerColor = Purple300,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Universitas")
                }
            }
        }
    ) { paddingValues ->
        // Gradient Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Purple200)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- HEADER SECTION ---
                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.ExitToApp,
                        contentDescription = "Logout",
                        tint = Purple50,
                        modifier = Modifier
                            .size(28.dp)
                            .scale(scaleX = -1f, scaleY = 1f)
                            .align(Alignment.CenterStart)
                            .clickable { onLogoutClicked() }
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color(0xFFF3E5F5))) { append("Hello, ") }
                            withStyle(style = SpanStyle(color = Color(0xFF8E24AA), fontWeight = FontWeight.Bold)) { append(state.username); append("!") }
                        },
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                // Search Bar
                val searchPlaceholder = if (state.selectedTab == 0) "Cari Nama Universitas" else "Cari Cluster"
                AdminSearchBar(query = state.searchQuery, onQueryChange = viewModel::onSearchQueryChange, placeholder = searchPlaceholder)
                Spacer(modifier = Modifier.height(24.dp))

                // --- CONTENT CONTAINER ---
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(elevation = 10.dp, shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                        .background(Color.White)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // TAB ROW
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            AdminTabItem(title = "UNIVERSITAS", isSelected = state.selectedTab == 0) { viewModel.onTabSelected(0) }
                            AdminTabItem(title = "CLUSTER", isSelected = state.selectedTab == 1) { viewModel.onTabSelected(1) }
                        }
                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                        // LIST CONTENT
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (state.selectedTab == 0) {
                                val filteredList = state.universities.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
                                items(filteredList) { uni ->
                                    UniversityCard(
                                        uni = uni,
                                        onCardClick = { onNavigateToListUniversitas(uid, uni.id) },
                                        onEditClick = {
                                            selectedUniForEdit = uni
                                            showEditUniDialog = true
                                        }
                                    )
                                }
                            } else {
                                val filteredList = state.clusters.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
                                items(filteredList) { cluster ->
                                    ClusterCard(
                                        cluster = cluster,
                                        onEditClick = {
                                            selectedClusterForEdit = cluster
                                            showEditClusterDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    // 1. TAMBAH UNIVERSITAS
    if (showAddUniDialog) {
        AddUniversityDialog(
            onDismiss = { showAddUniDialog = false },
            onSave = { id, nama, akred, klaster ->
                viewModel.addUniversity(id, nama, akred, klaster)
                showAddUniDialog = false
            }
        )
    }

    // 2. EDIT UNIVERSITAS
    if (showEditUniDialog && selectedUniForEdit != null) {
        EditUniversityDialog(
            university = selectedUniForEdit!!,
            onDismiss = { showEditUniDialog = false },
            onSave = { accreditation, cluster ->
                viewModel.updateUniversity(selectedUniForEdit!!.id, accreditation, cluster)
                showEditUniDialog = false
            },
            onDelete = {
                showEditUniDialog = false
                showDeleteUniConfirmDialog = true
            }
        )
    }

    // 3. HAPUS UNIVERSITAS (CONFIRM PASSWORD)
    if (showDeleteUniConfirmDialog && selectedUniForEdit != null) {
        DeleteConfirmationDialog(
            universityName = selectedUniForEdit!!.name,
            onDismiss = { showDeleteUniConfirmDialog = false },
            onConfirm = { password ->
                viewModel.deleteUniversityWithAuth(selectedUniForEdit!!.id, password)
                showDeleteUniConfirmDialog = false
                selectedUniForEdit = null
            }
        )
    }

    // 4. EDIT CLUSTER
    if (showEditClusterDialog && selectedClusterForEdit != null) {
        EditClusterDialog(
            cluster = selectedClusterForEdit!!,
            onDismiss = { showEditClusterDialog = false },
            onSave = { newNominal ->
                viewModel.updateCluster(selectedClusterForEdit!!.id, newNominal)
                showEditClusterDialog = false
            }
        )
    }
}

// --- ITEM CARDS & DIALOG COMPONENTS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUniversityDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var idUniv by remember { mutableStateOf("") }
    var namaKampus by remember { mutableStateOf("") }
    var akreditasi by remember { mutableStateOf("") }
    var selectedCluster by remember { mutableStateOf("1") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Universitas", fontWeight = FontWeight.Bold, color = Purple300) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = idUniv, onValueChange = { idUniv = it }, label = { Text("ID Universitas (Unik)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = namaKampus, onValueChange = { namaKampus = it }, label = { Text("Nama Kampus") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = akreditasi, onValueChange = { akreditasi = it }, label = { Text("Akreditasi") }, modifier = Modifier.fillMaxWidth())

                // Dropdown Cluster 1-5
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedCluster, onValueChange = {}, readOnly = true, label = { Text("Wilayah Klaster") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        (1..5).forEach { num ->
                            DropdownMenuItem(text = { Text("$num") }, onClick = { selectedCluster = num.toString(); expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (idUniv.isNotBlank() && namaKampus.isNotBlank()) {
                        onSave(idUniv, namaKampus, akreditasi, selectedCluster)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Purple300)
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Purple300) } }
    )
}

@Composable
fun EditUniversityDialog(
    university: University,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    var accreditation by remember { mutableStateOf(university.accreditation) }
    var cluster by remember { mutableStateOf(university.cluster) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Universitas", fontWeight = FontWeight.Bold, color = Purple300) },
        text = {
            Column {
                Text(university.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(value = accreditation, onValueChange = { accreditation = it }, label = { Text("Akreditasi") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = cluster, onValueChange = { cluster = it }, label = { Text("Cluster (1-5)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onSave(accreditation, cluster) }, colors = ButtonDefaults.buttonColors(containerColor = Purple300)) { Text("Simpan") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) { Text("Hapus", color = Color.Red) }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) { Text("Batal", color = Purple300) }
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    universityName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hapus Universitas", color = Color.Red, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Anda akan menghapus '$universityName' secara permanen.\n(Data mahasiswa terkait tidak otomatis terhapus)")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password Admin") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(password) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), enabled = password.isNotBlank()) { Text("Hapus") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) } }
    )
}

@Composable
fun EditClusterDialog(cluster: Cluster, onDismiss: () -> Unit, onSave: (Long) -> Unit) {
    var nominalString by remember { mutableStateOf(cluster.nominal.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text("Edit Cluster", fontWeight = FontWeight.Bold, color = Purple300) },
        text = {
            Column {
                Text(cluster.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(value = nominalString, onValueChange = { nominalString = it.filter { char -> char.isDigit() } }, label = { Text("Nominal (Rp)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), prefix = { Text("Rp ") })
            }
        },
        confirmButton = { Button(onClick = { onSave(nominalString.toLongOrNull() ?: 0L) }, colors = ButtonDefaults.buttonColors(containerColor = Purple300)) { Text("Simpan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal", color = Purple300) } }
    )
}

@Composable
fun AdminSearchBar(query: String, onQueryChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    TextField(
        value = query, onValueChange = onQueryChange, placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp).shadow(4.dp, RoundedCornerShape(50)).clip(RoundedCornerShape(50)),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Purple300) },
        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, disabledContainerColor = Color.White, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
        singleLine = true, textStyle = TextStyle(color = Purple300, fontSize = 16.sp)
    )
}

@Composable
fun AdminTabItem(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = title, color = if (isSelected) Purple300 else Color.Gray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        if (isSelected) Box(modifier = Modifier.width(80.dp).height(3.dp).background(Purple300, shape = RoundedCornerShape(2.dp)))
    }
}

@Composable
fun UniversityCard(uni: University, onCardClick: () -> Unit, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3E5F5)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color(0xFFE1BEE7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🏛️", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uni.name,
                    fontWeight = FontWeight.Bold,
                    color = PurpleTextDeep,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Akreditasi: ${uni.accreditation}",
                    color = PurpleDark,
                    fontSize = 14.sp
                )
                Text(
                    text = "Cluster: ${uni.cluster}",
                    color = PurpleDark,
                    fontSize = 14.sp
                )
            }

            // Edit Button
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        spotColor = Color.Gray
                    )
                    .background(Color.White, CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = PurpleDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ClusterCard(cluster: Cluster, onEditClick: () -> Unit) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("in", "ID")) }

    // Row Utama pembungkus (Parent)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. KOTAK KONTEN (Card)
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
                .heightIn(min = 80.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Purple50
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = cluster.name,
                    fontWeight = FontWeight.Bold,
                    color = PurpleDark,
                    fontSize = 15.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Nominal: ${formatter.format(cluster.nominal)}",
                    color = PurplePrimary.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // 2. TOMBOL EDIT
        IconButton(
            onClick = onEditClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit",
                tint = PurpleDark,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}