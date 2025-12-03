package com.example.kiptrack.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.data.Cluster
import com.example.kiptrack.ui.data.University
import com.example.kiptrack.ui.theme.DeepPurple
import com.example.kiptrack.ui.theme.LightPurple
import com.example.kiptrack.ui.theme.MediumPurple
import com.example.kiptrack.ui.viewmodel.DashboardAdminViewModel
import com.example.kiptrack.ui.viewmodel.DashboardAdminViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardAdminScreen(uid: String) {
    val viewModel: DashboardAdminViewModel = viewModel(
        factory = DashboardAdminViewModelFactory(uid)
    )
    val state = viewModel.uiState

    // Gradient Background for the top part
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MediumPurple, Color(0xFFE1BEE7))
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER SECTION ---
            Spacer(modifier = Modifier.height(20.dp))

            // Top Row: Back Icon and "Hello, Admin!"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Logout/Back Icon
                Icon(
                    imageVector = Icons.Outlined.ExitToApp,
                    contentDescription = "Logout",
                    tint = DeepPurple,
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.CenterStart)
                        .clickable { /* Handle Logout */ }
                )

                // Hello Text
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = DeepPurple.copy(alpha = 0.6f))) {
                            append("Hello, ")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFF8E24AA), fontWeight = FontWeight.Bold)) {
                            append(state.username)
                            append("!")
                        }
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
                        AdminTabItem(
                            title = "UNIVERSITAS",
                            isSelected = state.selectedTab == 0,
                            onClick = { viewModel.onTabSelected(0) }
                        )
                        AdminTabItem(
                            title = "CLUSTER",
                            isSelected = state.selectedTab == 1,
                            onClick = { viewModel.onTabSelected(1) }
                        )
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
                            val filteredList = state.universities.filter {
                                it.name.contains(state.searchQuery, ignoreCase = true)
                            }
                            items(filteredList) { uni ->
                                UniversityCard(uni)
                            }
                        } else {
                            val filteredList = state.clusters.filter {
                                it.name.contains(state.searchQuery, ignoreCase = true)
                            }
                            items(filteredList) { cluster ->
                                ClusterCard(cluster)
                            }
                        }

                        // Bottom spacer + Arrow
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "More",
                                    tint = DeepPurple,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholder,
                color = Color.Gray,
                fontSize = 14.sp
            )
        },
        textStyle = TextStyle(
            color = DeepPurple,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MediumPurple
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            cursorColor = DeepPurple,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(50),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            // REMOVED fixed .height(50.dp) to prevent text clipping
            .shadow(4.dp, RoundedCornerShape(50))
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
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) DeepPurple else Color.LightGray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(2.dp)
                    .background(Color(0xFF8E24AA))
            )
        }
    }
}

@Composable
fun UniversityCard(uni: University) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightPurple.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
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
                Text(
                    text = uni.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DeepPurple
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Akreditasi: ${uni.accreditation}",
                    fontSize = 12.sp,
                    color = DeepPurple.copy(alpha = 0.6f)
                )
                Text(
                    text = "Cluster: ${uni.cluster}",
                    fontSize = 12.sp,
                    color = DeepPurple.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = DeepPurple,
                modifier = Modifier.size(24.dp).padding(4.dp).border(1.dp, DeepPurple, RoundedCornerShape(4.dp)).padding(2.dp)
            )
        }
    }
}

@Composable
fun ClusterCard(cluster: Cluster) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightPurple.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cluster.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DeepPurple
                )
                Spacer(modifier = Modifier.height(4.dp))

                val formattedNominal = NumberFormat.getNumberInstance(Locale("id", "ID")).format(cluster.nominal)
                Text(
                    text = "Nominal: $formattedNominal",
                    fontSize = 12.sp,
                    color = DeepPurple.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = DeepPurple,
                modifier = Modifier.size(24.dp).padding(4.dp).border(1.dp, DeepPurple, RoundedCornerShape(4.dp)).padding(2.dp)
            )
        }
    }
}