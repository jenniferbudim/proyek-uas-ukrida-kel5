package com.example.kiptrack.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.data.Transaction
import com.example.kiptrack.ui.theme.*
import com.example.kiptrack.ui.utils.ImageUtils
import com.example.kiptrack.ui.viewmodel.RiwayatWaliViewModel
import com.example.kiptrack.ui.viewmodel.RiwayatWaliViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PerincianPengeluaranWaliScreen(
    uid: String,
    onBackToDashboard: () -> Unit
) {
    val viewModel: RiwayatWaliViewModel = viewModel(
        factory = RiwayatWaliViewModelFactory(uid)
    )
    val state = viewModel.uiState

    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }

    // State untuk Pop-up Detail
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(Purple50, PurplePrimary)))
    ) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- HEADER AREA (Saldo & Statistik) ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Purple50)
                        .padding(bottom = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    // Saldo Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Purple200),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(60.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Saldo Saat Ini:",
                                color = PurpleTextDeep,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = formatter.format(state.currentBalance),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Total Pengeluaran
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Purple200)
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Total Pengeluaran",
                                color = PurpleTextDeep,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatter.format(state.totalExpenditure),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Total Pelanggaran
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Purple200)
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Total Pelanggaran",
                                color = PurpleTextDeep,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatter.format(state.totalViolationsAmount),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // --- LIST RIWAYAT HEADER ---
                Text(
                    text = "Riwayat Pengeluaran",
                    color = PurpleTextDeep,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 20.dp)
                )

                Spacer(modifier = Modifier.padding(4.dp))

                // --- LIST RIWAYAT ---
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.transactionList) { trx ->
                        val dateParts = trx.date.split(" ")
                        val month = if(dateParts.isNotEmpty()) dateParts[0] else ""
                        val year = if(dateParts.size > 1) dateParts[1] else ""

                        // --- ICON LOGIC ---
                        val (icon, color) = when (trx.status) {
                            "DISETUJUI" -> Pair(Icons.Outlined.CheckCircle, SuccessGreen)
                            "DITOLAK" -> Pair(Icons.Filled.Cancel, PieRed)
                            else -> Pair(Icons.Filled.AccessTime, Warning)
                        }

                        HistoryItemCard(
                            month = month.uppercase(),
                            dateYear = year,
                            amount = "Rp${formatter.format(trx.amount)}",
                            category = trx.category,
                            icon = icon,
                            iconColor = color,
                            onClick = {
                                selectedTransaction = trx
                                showDetailDialog = true
                            }
                        )
                    }
                }
            }
        }

        // --- BOTTOM NAVIGATION ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(Purple100)
                .clickable { onBackToDashboard() }
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = PurpleTextDeep,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "KEMBALI KE DASHBOARD",
                    color = PurpleTextDeep,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }

    // --- POP-UP DETAIL TRANSAKSI ---
    if (showDetailDialog && selectedTransaction != null) {
        val trx = selectedTransaction!!
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            containerColor = Color.White,
            title = { Text("Detail Transaksi", color = PurpleTextDeep, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (trx.proofImage.isNotBlank()) {
                        val bitmap = ImageUtils.base64ToBitmap(trx.proofImage)
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Bukti",
                                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(Modifier.fillMaxWidth().height(150.dp).background(Color.LightGray)) { Text("Gambar rusak", Modifier.align(Alignment.Center)) }
                        }
                    } else {
                        Box(Modifier.fillMaxWidth().height(150.dp).background(Color.LightGray)) { Text("Tidak ada bukti", Modifier.align(Alignment.Center)) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    WaliDetailRow("Tanggal", trx.date)
                    WaliDetailRow("Kategori", trx.category)
                    WaliDetailRow("Deskripsi", trx.description)
                    WaliDetailRow("Total", "Rp ${formatter.format(trx.amount)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    val statusColor = when(trx.status) { "DISETUJUI" -> SuccessGreen; "DITOLAK" -> PieRed; else -> Warning }
                    Text("Status: ${trx.status}", color = statusColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.align(Alignment.End))
                }
            },
            confirmButton = {
                Button(onClick = { showDetailDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Purple200)) {
                    Text("Tutup")
                }
            }
        )
    }
}

// Helper Text Row for Dialog
@Composable
fun WaliDetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 12.sp, color = PurpleTextDeep.copy(alpha = 0.7f))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PurpleTextDeep)
    }
}

// --- CARD COMPONENT ---
@Composable
fun HistoryItemCard(
    month: String,
    dateYear: String,
    amount: String,
    category: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT COLUMN: Month, Date, Amount
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Column {
                    // Month (Underlined)
                    Text(
                        text = month,
                        color = PurpleTextDeep,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        style = LocalTextStyle.current.copy(textDecoration = TextDecoration.Underline)
                    )
                    // Date Year
                    Text(
                        text = dateYear,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                // Amount (Bottom Left)
                Text(
                    text = amount,
                    color = PurpleTextDeep,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // RIGHT COLUMN: Icon, Category
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                // Icon (Top Right)
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )

                // Category (Bottom Right)
                Text(
                    text = category,
                    color = PurpleTextDeep,
                    fontSize = 14.sp
                )
            }
        }
    }
}