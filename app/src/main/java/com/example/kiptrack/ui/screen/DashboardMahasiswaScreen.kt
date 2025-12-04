package com.example.kiptrack.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.data.Transaction
import com.example.kiptrack.ui.theme.Purple300
import com.example.kiptrack.ui.theme.Purple50
import com.example.kiptrack.ui.theme.Purple300
import com.example.kiptrack.ui.viewmodel.DashboardMahasiswaViewModel
import com.example.kiptrack.ui.viewmodel.DashboardMahasiswaViewModelFactory
import java.text.NumberFormat
import java.util.Locale

// Helper for formatting currency
fun formatRupiah(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount).replace("Rp", "Rp ")
}

@Composable
fun DashboardMahasiswaScreen(
    uid: String,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToLogForm: (String) -> Unit
) {
    val viewModel: DashboardMahasiswaViewModel = viewModel(
        factory = DashboardMahasiswaViewModelFactory(uid)
    )
    val state = viewModel.uiState
    val scrollState = rememberScrollState()

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Purple50)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Dashboard Mahasiswa",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple300,
                    modifier = Modifier.padding(top = 24.dp, bottom = 20.dp)
                )

                if (state.isLoading) {
                    CircularProgressIndicator(color = Purple300)
                } else {
                    HeaderSection(
                        userName = state.userName,
                        uid = uid,
                        photoProfile = state.photoProfile, // Pass dari state
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToLogForm = onNavigateToLogForm
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    SaldoChartSection(
                        currentSaldo = state.currentSaldo,
                        graphData = state.graphData,
                        selectedYear = state.selectedYear,
                        onYearChange = viewModel::changeYear
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    TransactionHistorySection(state.transactionHistory)
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    userName: String,
    uid: String,
    photoProfile: String, // <--- Parameter Baru (Wajib ada)
    onNavigateToProfile: (String) -> Unit,
    onNavigateToLogForm: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Avatar + Welcome Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // --- LOGIKA FOTO PROFIL BARU ---
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                        .clickable { onNavigateToProfile(uid) },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoProfile.isNotBlank()) {
                        // Decode Base64 ke Bitmap
                        val bitmap = com.example.kiptrack.ui.utils.ImageUtils.base64ToBitmap(photoProfile)

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile User",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            // Fallback jika decode gagal
                            Text("🎓", fontSize = 28.sp)
                        }
                    } else {
                        // Fallback jika belum ada foto
                        Text("🎓", fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Welcome Text
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Purple300.copy(alpha = 0.8f), fontSize = 14.sp)) {
                            append("Welcome, \n")
                        }
                        withStyle(style = SpanStyle(color = Purple300, fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                            append(userName)
                            append("!")
                        }
                    },
                    lineHeight = 20.sp
                )
            }

            // Right: Add Report Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onNavigateToLogForm(uid) }
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = "Tambah Laporan",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = "Add",
                    tint = Purple300,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun SaldoChartSection(
    currentSaldo: Long,
    graphData: List<Long>,
    selectedYear: Int,
    onYearChange: (Int) -> Unit
) {
    val months = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Saldo Uang Saku",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Purple300,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = formatRupiah(currentSaldo),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Purple300,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                IconButton(onClick = { onYearChange(-1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev Year", tint = Purple300)
                }
                Text(
                    text = "$selectedYear",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple300,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(onClick = { onYearChange(1) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Year", tint = Purple300)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 8.dp)
            ) {
                LineChart(data = graphData, modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                months.forEach { month ->
                    Text(
                        text = month,
                        fontSize = 9.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun LineChart(data: List<Long>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return

    val maxValue = data.maxOrNull()?.takeIf { it > 0 } ?: 1L
    val minValue = 0L

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / 11f

        val path = Path()
        var previousPoint: Offset? = null

        data.forEachIndexed { index, value ->
            val normalizedY = value.toFloat() / maxValue.toFloat()
            val y = height - (normalizedY * height * 0.8f + height * 0.1f)
            val x = index * stepX

            val currentPoint = Offset(x, y)

            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

            if (previousPoint != null) {
                drawLine(
                    color = Purple300,
                    start = previousPoint,
                    end = currentPoint,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            drawCircle(color = Purple300, radius = 3.dp.toPx(), center = currentPoint)
            drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = currentPoint)

            previousPoint = currentPoint
        }
    }
}

@Composable
fun TransactionHistorySection(transactions: List<Transaction>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                text = "Riwayat Pengeluaran",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Purple300,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (transactions.isEmpty()) {
                    Text(
                        text = "Belum ada transaksi",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(20.dp)
                    )
                } else {
                    transactions.take(3).forEach { transaction ->
                        TransactionItem(transaction)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    // Navigate to full list screen
                },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lihat Selengkapnya",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    // 1. Tentukan Ikon dan Warna berdasarkan Status
    val iconVector = when {
        transaction.isApproved -> Icons.Outlined.CheckCircle
        transaction.isRejected -> Icons.Default.Cancel // Silang Merah (Pastikan import Icons.Default.Cancel)
        else -> Icons.Default.AccessTime // Ikon Jam untuk Pending (Pastikan import Icons.Default.AccessTime)
    }

    val iconColor = when {
        transaction.isApproved -> Color(0xFF00C853) // Hijau
        transaction.isRejected -> Color(0xFFFF5252) // Merah
        else -> Color(0xFFFFA000) // Kuning/Orange (Pending)
    }

    // 2. UI Row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Purple50.copy(alpha = 0.5f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kolom Kiri: Tanggal & Nominal
        Column {
            Text(
                text = transaction.date.substringBefore("/"),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Purple300,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
            )
            Text(
                text = transaction.date.substringAfter("/"),
                fontSize = 12.sp,
                color = Purple300.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatRupiah(transaction.amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Purple300
            )
        }

        // Kolom Kanan: Deskripsi & Ikon Status
        Column(horizontalAlignment = Alignment.End) {
            Icon(
                imageVector = iconVector,
                contentDescription = transaction.status,
                tint = iconColor,
                modifier = Modifier
                    .size(28.dp)
                    .padding(bottom = 4.dp)
            )
            Text(
                text = transaction.description,
                fontSize = 14.sp,
                color = Purple300,
                fontWeight = FontWeight.Medium
            )
            // Tambahkan Teks Status kecil (Opsional)
            Text(
                text = transaction.status, // "MENUNGGU", "DISETUJUI", dll
                fontSize = 10.sp,
                color = iconColor,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun AsyncImageMock(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFFE0E0E0))
            .border(1.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("🧕", fontSize = 28.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardMahasiswaPreview() {
    DashboardMahasiswaScreen("preview_uid", onNavigateToLogForm = {}, onNavigateToProfile = {})
}