package com.example.kiptrack.ui.screen

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.data.Cluster
import com.example.kiptrack.ui.data.University
import com.example.kiptrack.ui.theme.Purple300
import com.example.kiptrack.ui.theme.Purple50
import com.example.kiptrack.ui.theme.Purple200
import com.example.kiptrack.ui.theme.Purple300
import com.example.kiptrack.ui.viewmodel.DashboardAdminViewModel
import com.example.kiptrack.ui.viewmodel.DashboardAdminViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardAdminScreen(
    uid: String,
    // UPDATED: Callback now accepts (UID, UniversityName)
    onNavigateToListUniversitas: (String, String) -> Unit,
    onLogoutClicked: () -> Unit
) {
    val viewModel: DashboardAdminViewModel = viewModel(
        factory = DashboardAdminViewModelFactory(uid)
    )
    val state = viewModel.uiState

    // State for dialogs
    var showEditUniDialog by remember { mutableStateOf(false) }
    var selectedUniForEdit by remember { mutableStateOf<University?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var showEditClusterDialog by remember { mutableStateOf(false) }
    var selectedClusterForEdit by remember { mutableStateOf<Cluster?>(null) }

    // Gradient Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Purple200, Color(0xFFE1BEE7))
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER SECTION ---
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ExitToApp,
                    contentDescription = "Logout",
                    tint = Purple200,
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.CenterStart)
                        .clickable { onLogoutClicked() }
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Purple200.copy(alpha = 0.6f))) { append("Hello, ") }
                        withStyle(style = SpanStyle(color = Color(0xFF8E24AA), fontWeight = FontWeight.Bold)) { append(state.username); append("!") }
                    },
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Search Bar
            val searchPlaceholder = if (state.selectedTab == 0) "Cari Nama Universitas" else "Cari Cluster"
            AdminSearchBar(
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                placeholder = searchPlaceholder
            )
            Spacer(modifier = Modifier.height(24.dp))

            // --- WHITE CONTENT CONTAINER ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(Color.White)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // TAB ROW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AdminTabItem(title = "UNIVERSITAS", isSelected = state.selectedTab == 0) { viewModel.onTabSelected(0) }
                        AdminTabItem(title = "CLUSTER", isSelected = state.selectedTab == 1) { viewModel.onTabSelected(1) }
                    }
                    Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                    // LIST CONTENT
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (state.selectedTab == 0) {
                            val filteredList = state.universities.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
                            items(filteredList) { uni ->
                                UniversityCard(
                                    uni = uni,
                                    // UPDATED: Pass UID and uni.name to the navigation callback
                                    onCardClick = { onNavigateToListUniversitas(uid, uni.name) },
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
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "More", tint = Purple300, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS (Same as previous) ---
    if (showEditUniDialog && selectedUniForEdit != null) {
        EditUniversityDialog(
            university = selectedUniForEdit!!,
            onDismiss = { showEditUniDialog = false },
            onSave = { accreditation, cluster -> showEditUniDialog = false },
            onDelete = { showEditUniDialog = false; showDeleteConfirmDialog = true }
        )
    }

    if (showDeleteConfirmDialog && selectedUniForEdit != null) {
        DeleteConfirmationDialog(
            universityName = selectedUniForEdit!!.name,
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = { showDeleteConfirmDialog = false; selectedUniForEdit = null }
        )
    }

    if (showEditClusterDialog && selectedClusterForEdit != null) {
        EditClusterDialog(
            cluster = selectedClusterForEdit!!,
            onDismiss = { showEditClusterDialog = false },
            onSave = { showEditClusterDialog = false }
        )
    }
}

// ... (Rest of components: AdminSearchBar, AdminTabItem, UniversityCard, etc. remain unchanged) ...
@Composable
fun AdminSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(4.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50)),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = Purple300) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true,
        textStyle = TextStyle(color = Purple300, fontSize = 16.sp)
    )
}

@Composable
fun AdminTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) Purple300 else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(3.dp)
                    .background(Purple300, shape = RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun UniversityCard(
    uni: University,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Purple50.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color.LightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🏛️", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = uni.name, fontWeight = FontWeight.Bold, color = Purple300, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Akreditasi: ${uni.accreditation}", color = Purple300, fontSize = 14.sp)
                Text(text = "Cluster: ${uni.cluster}", color = Purple300, fontSize = 14.sp)
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit University", tint = Purple300)
            }
        }
    }
}

@Composable
fun ClusterCard(
    cluster: Cluster,
    onEditClick: () -> Unit
) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("in", "ID")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Purple50.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Cluster ${cluster.name}", fontWeight = FontWeight.Bold, color = Purple300, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Bantuan: ${formatter.format(cluster.nominal)}", color = Purple300, fontSize = 14.sp)
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Cluster", tint = Purple300)
            }
        }
    }
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
                OutlinedTextField(
                    value = accreditation,
                    onValueChange = { accreditation = it },
                    label = { Text("Akreditasi") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = cluster,
                    onValueChange = { cluster = it },
                    label = { Text("Cluster") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(accreditation, cluster) },
                colors = ButtonDefaults.buttonColors(containerColor = Purple300)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Hapus", color = Color.Red)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Batal", color = Purple300)
                }
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    universityName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hapus Universitas", fontWeight = FontWeight.Bold, color = Purple300) },
        text = { Text("Apakah Anda yakin ingin menghapus $universityName?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Ya, Hapus")
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
fun EditClusterDialog(
    cluster: Cluster,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var nominalString by remember { mutableStateOf(cluster.nominal.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Cluster", fontWeight = FontWeight.Bold, color = Purple300) },
        text = {
            Column {
                Text(cluster.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(
                    value = nominalString,
                    onValueChange = { nominalString = it.filter { char -> char.isDigit() } },
                    label = { Text("Nominal Bantuan (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("Rp ") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nominalString.toLongOrNull() ?: 0L) },
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