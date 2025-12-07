package com.example.kiptrack.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.data.Transaction
import com.example.kiptrack.ui.theme.PieOrange
import com.example.kiptrack.ui.theme.PieRed
import com.example.kiptrack.ui.theme.Purple100
import com.example.kiptrack.ui.theme.Purple300
import com.example.kiptrack.ui.theme.Purple50
import com.example.kiptrack.ui.theme.PurpleDark
import com.example.kiptrack.ui.theme.PurplePrimary
import com.example.kiptrack.ui.utils.ImageUtils
import com.example.kiptrack.ui.viewmodel.DashboardMahasiswaViewModel
import com.example.kiptrack.ui.viewmodel.DashboardMahasiswaViewModelFactory
import java.text.NumberFormat
import java.util.Locale

// Format rupiah
fun formatRupiah(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount).replace("Rp", "Rp ")
}

@OptIn(ExperimentalMaterial3Api::class)
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

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Purple100)
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
                    color = PurplePrimary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 20.dp)
                )

                if (state.isLoading) {
                    CircularProgressIndicator(color = Purple300)
                } else {
                    HeaderSection(
                        userName = state.userName,
                        uid = uid,
                        photoProfile = state.photoProfile,
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToLogForm = onNavigateToLogForm
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    SaldoChartSection(
                        currentSaldo = state.currentSaldo,
                        totalViolations = state.totalViolations,
                        nextSemesterAllowance = state.nextSemesterAllowance,
                        graphData = state.graphData,
                        selectedYear = state.selectedYear,
                        onYearChange = viewModel::changeYear
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Callback onSeeAllClick
                    TransactionHistorySection(
                        transactions = state.transactionHistory,
                        onSeeAllClick = { showBottomSheet = true }
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }

            // Riwayat Pengeluaran lengkap
            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState,
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        // Header Bottom Sheet
                        Text(
                            text = "Semua Riwayat Pengeluaran",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Purple300,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        // LazyColumn untuk Scroll List Penuh
                        LazyColumn(
                            modifier = Modifier.fillMaxHeight(0.85f), // Tinggi Sheet 85% layar
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            if (state.transactionHistory.isEmpty()) {
                                item {
                                    Text("Belum ada data.", color = Color.Gray, modifier = Modifier.padding(20.dp))
                                }
                            } else {
                                // Tampilkan SEMUA data tanpa batasan
                                items(state.transactionHistory) { transaction ->
                                    TransactionItem(transaction)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    userName: String,
    uid: String,
    photoProfile: String,
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
            // Profile & Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                        .clickable { onNavigateToProfile(uid) },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoProfile.isNotBlank()) {
                        val bitmap = ImageUtils.base64ToBitmap(photoProfile)
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile User",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("🎓", fontSize = 28.sp)
                        }
                    } else {
                        Text("🎓", fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

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

            // Add Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PurplePrimary)
                    .clickable { onNavigateToLogForm(uid) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Report",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SaldoChartSection(
    currentSaldo: Long,
    totalViolations: Long,
    nextSemesterAllowance: Long,
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

            // Saldo Title & Amount
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Saldo Uang Saku",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Purple300
                )
                Text(
                    text = formatRupiah(currentSaldo),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleDark,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Total Pelanggaran",
                        fontSize = 11.sp,
                        color = PieOrange.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatRupiah(totalViolations),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PieRed
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Sem. Depan",
                        fontSize = 11.sp,
                        color = Purple300,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatRupiah(nextSemesterAllowance),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurpleDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Year Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                IconButton(onClick = { onYearChange(-1) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev", tint = PurpleDark)
                }
                Text(
                    text = "$selectedYear",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleDark,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(onClick = { onYearChange(1) }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next", tint = PurpleDark)
                }
            }

            // Graph Area - INCREASED HEIGHT FOR DRAMA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp) // Changed from 150.dp to 250.dp
                    .clip(RoundedCornerShape(12.dp))
                    .background(Purple100)
                    .padding(vertical = 12.dp, horizontal = 0.dp)
            ) {
                LineChart(data = graphData, modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Month Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                months.forEach { month ->
                    Text(
                        text = month,
                        fontSize = 9.sp,
                        color = Purple300,
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

    // Define Purple200 locally if not in theme
    val Purple200 = Color(0xFFCE93D8)

    val maxValue = data.maxOrNull()?.takeIf { it > 0 } ?: 1L

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val totalPoints = 12 // Assumes 12 months
        val stepX = width / (totalPoints - 1)

        // 1. Draw Vertical Grid Lines (Purple200)
        for (i in 0 until totalPoints) {
            val x = i * stepX
            drawLine(
                color = Purple200,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 2. Draw Graph Line & Points
        val path = Path()
        var previousPoint: Offset? = null

        data.forEachIndexed { index, value ->
            if (index >= totalPoints) return@forEachIndexed

            val normalizedY = value.toFloat() / maxValue.toFloat()

            // UPDATED MATH: Uses 85% of height for the wave (vs 70% before)
            // This makes the spikes taller and valleys deeper visually
            val y = height - (normalizedY * height * 0.85f + height * 0.075f)

            val x = index * stepX
            val currentPoint = Offset(x, y)

            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

            if (previousPoint != null) {
                drawLine(
                    color = PurpleDark,
                    start = previousPoint!!,
                    end = currentPoint,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draw Dots
            drawCircle(color = PurplePrimary, radius = 3.dp.toPx(), center = currentPoint)

            previousPoint = currentPoint
        }
    }
}

@Composable
fun TransactionHistorySection(
    transactions: List<Transaction>,
    onSeeAllClick: () -> Unit
) {
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
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(20.dp)
                    )
                } else {
                    transactions.take(3).forEach { transaction ->
                        TransactionItem(transaction)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSeeAllClick() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lihat Selengkapnya",
                    fontSize = 12.sp,
                    color = Purple100
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Purple100,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val iconVector = when {
        transaction.isApproved -> Icons.Outlined.CheckCircle
        transaction.isRejected -> Icons.Filled.Cancel
        else -> Icons.Filled.AccessTime
    }

    val iconColor = when {
        transaction.isApproved -> Color(0xFF00C853)
        transaction.isRejected -> Color(0xFFFF5252)
        else -> Color(0xFFFFA000)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Purple50.copy(alpha = 0.5f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
            Text(
                text = transaction.status,
                fontSize = 10.sp,
                color = iconColor,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardMahasiswaPreview() {
    DashboardMahasiswaScreen("preview_uid", onNavigateToLogForm = {}, onNavigateToProfile = {})
}