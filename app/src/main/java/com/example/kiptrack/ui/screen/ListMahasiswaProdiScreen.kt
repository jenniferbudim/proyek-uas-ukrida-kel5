package com.example.kiptrack.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.theme.Purple300
import com.example.kiptrack.ui.theme.Purple50
import com.example.kiptrack.ui.theme.Purple200
import com.example.kiptrack.ui.viewmodel.ListMahasiswaViewModel
import com.example.kiptrack.ui.viewmodel.ListMahasiswaViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListMahasiswaProdiScreen(
    uid: String,
    universityId: String, // <-- PERBAIKAN: Terima UnivID
    prodiId: String,      // <-- PERBAIKAN: Terima ProdiID
    onNavigateToDetailMahasiswa: (String, String) -> Unit, // (UID, StudentUID)
    onBackClick: () -> Unit
) {
    val viewModel: ListMahasiswaViewModel = viewModel(
        factory = ListMahasiswaViewModelFactory(universityId, prodiId)
    )
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(state.prodiName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple300)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.mahasiswaList) { mhs ->
                        MahasiswaListItem(
                            name = mhs.nama,
                            nim = mhs.nim,
                            // Kirim UID Mahasiswa Asli
                            onClick = { onNavigateToDetailMahasiswa(uid, mhs.uid) }
                        )
                    }
                }
            }
        }
    }
}

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