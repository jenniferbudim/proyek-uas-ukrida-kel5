package com.example.kiptrack.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.data.Transaction
import com.example.kiptrack.ui.theme.PieOrange
import com.example.kiptrack.ui.theme.PieRed
import com.example.kiptrack.ui.theme.Purple100
import com.example.kiptrack.ui.theme.Purple200
import com.example.kiptrack.ui.theme.Purple300
import com.example.kiptrack.ui.theme.Purple50
import com.example.kiptrack.ui.theme.PurpleDark
import com.example.kiptrack.ui.theme.PurplePrimary
import com.example.kiptrack.ui.theme.PurpleTextDeep
import com.example.kiptrack.ui.theme.SuccessGreen
import com.example.kiptrack.ui.theme.Warning
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

    // --- STATE POPUP DETAIL ---
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

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

                    // Pass onClick to Section
                    TransactionHistorySection(
                        transactions = state.transactionHistory,
                        onSeeAllClick = { showBottomSheet = true },
                        onItemClick = { trx ->
                            selectedTransaction = trx
                            showDetailDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }

            // --- BOTTOM SHEET LIST ---
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
                        Text(
                            text = "Semua Riwayat Pengeluaran",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Purple300,
                            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally)
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxHeight(0.85f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            if (state.transactionHistory.isEmpty()) {
                                item { Text("Belum ada data.", color = Color.Gray, modifier = Modifier.padding(20.dp)) }
                            } else {
                                items(state.transactionHistory) { transaction ->
                                    TransactionItem(
                                        transaction = transaction,
                                        onClick = {
                                            selectedTransaction = transaction
                                            showDetailDialog = true
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

    // POPUP DETAIL TRANSAKSI
    if (showDetailDialog && selectedTransaction != null) {
        val trx = selectedTransaction!!
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            containerColor = Purple50,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            title = {
                Text("Detail Transaksi", color = PurpleTextDeep, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    // Gambar
                    if (trx.proofImage.isNotBlank()) {
                        val bitmap = ImageUtils.base64ToBitmap(trx.proofImage)
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Bukti",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(Modifier.fillMaxWidth().height(150.dp).background(Color.LightGray)) { Text("Gambar rusak", Modifier.align(Alignment.Center)) }
                        }
                    } else {
                        Box(Modifier.fillMaxWidth().height(150.dp).background(Color.LightGray)) { Text("Tidak ada bukti", Modifier.align(Alignment.Center)) }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detail Teks
                    DetailItemLabel("Tanggal", trx.date)
                    DetailItemLabel("Kategori", trx.category)
                    DetailItemLabel("Deskripsi", trx.description)
                    DetailItemLabel("Harga Satuan", formatRupiah(trx.unitPrice))
                    DetailItemLabel("Kuantitas", trx.quantity.toString())
                    DetailItemLabel("Total", formatRupiah(trx.amount))

                    Spacer(modifier = Modifier.height(8.dp))

                    val statusColor = when(trx.status) {
                        "DISETUJUI" -> SuccessGreen ; "DITOLAK" -> PieRed; else -> Warning
                    }
                    Text("Status: ${trx.status}", color = statusColor, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
                }
            },
            confirmButton = {
                Button(onClick = { showDetailDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Purple200)) {
                    Text("Tutup", color = PurpleDark)
                }
            }
        )
    }
}

@Composable
fun DetailItemLabel(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 12.sp, color = PurpleTextDeep.copy(alpha = 0.7f))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PurpleTextDeep)
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFFE0E0E0)).clickable { onNavigateToProfile(uid) },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoProfile.isNotBlank()) {
                        val bitmap = ImageUtils.base64ToBitmap(photoProfile)
                        if (bitmap != null) { Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Profile", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                        else { Text("🎓", fontSize = 28.sp) }
                    } else { Text("🎓", fontSize = 28.sp) }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Purple300.copy(alpha = 0.8f), fontSize = 14.sp)) { append("Welcome, \n") }
                        withStyle(style = SpanStyle(color = Purple300, fontWeight = FontWeight.Bold, fontSize = 18.sp)) { append("$userName!") }
                    },
                    lineHeight = 20.sp
                )
            }
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(PurplePrimary).clickable { onNavigateToLogForm(uid) },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(24.dp)) }
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
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text(text = "Saldo Uang Saku", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Purple300)
                Text(text = formatRupiah(currentSaldo), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = PurpleDark, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(text = "Total Pelanggaran", fontSize = 11.sp, color = PieOrange.copy(alpha = 0.8f), fontWeight = FontWeight.SemiBold)
                    Text(text = formatRupiah(totalViolations), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PieRed)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Sem. Depan", fontSize = 11.sp, color = Purple300, fontWeight = FontWeight.SemiBold)
                    Text(text = formatRupiah(nextSemesterAllowance), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PurpleDark)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                IconButton(onClick = { onYearChange(-1) }, modifier = Modifier.size(24.dp)) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Prev", tint = PurpleDark) }
                Text(text = "$selectedYear", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PurpleDark, modifier = Modifier.padding(horizontal = 16.dp))
                IconButton(onClick = { onYearChange(1) }, modifier = Modifier.size(24.dp)) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next", tint = PurpleDark) }
            }
            Box(modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp)).background(Purple100).padding(vertical = 12.dp)) {
                LineChart(data = graphData, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                months.forEach { month -> Text(text = month, fontSize = 9.sp, color = Purple300, fontWeight = FontWeight.Medium) }
            }
        }
    }
}

@Composable
fun LineChart(data: List<Long>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return
    val Purple200 = Color(0xFFCE93D8)
    val maxValue = data.maxOrNull()?.takeIf { it > 0 } ?: 1L
    Canvas(modifier = modifier) {
        val width = size.width; val height = size.height; val totalPoints = 12; val stepX = width / (totalPoints - 1)
        for (i in 0 until totalPoints) { val x = i * stepX; drawLine(color = Purple200, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 1.dp.toPx()) }
        val path = Path(); var previousPoint: Offset? = null
        data.forEachIndexed { index, value ->
            if (index >= totalPoints) return@forEachIndexed
            val normalizedY = value.toFloat() / maxValue.toFloat()
            val y = height - (normalizedY * height * 0.85f + height * 0.075f)
            val x = index * stepX; val currentPoint = Offset(x, y)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            if (previousPoint != null) { drawLine(color = PurpleDark, start = previousPoint!!, end = currentPoint, strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round) }
            drawCircle(color = PurplePrimary, radius = 3.dp.toPx(), center = currentPoint)
            previousPoint = currentPoint
        }
    }
}

// TransactionHistorySection
@Composable
fun TransactionHistorySection(
    transactions: List<Transaction>,
    onSeeAllClick: () -> Unit,
    onItemClick: (Transaction) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(text = "Riwayat Pengeluaran", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Purple300, modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
                if (transactions.isEmpty()) {
                    Text(text = "Belum ada transaksi", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally).padding(20.dp))
                } else {
                    transactions.take(3).forEach { transaction ->
                        TransactionItem(transaction, onClick = { onItemClick(transaction) })
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth().clickable { onSeeAllClick() }, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Lihat Selengkapnya", fontSize = 12.sp, color = Purple100)
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Purple100, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// TransactionItem
@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: (() -> Unit)? = null
) {
    val iconVector = when {
        transaction.isApproved -> Icons.Outlined.CheckCircle
        transaction.isRejected -> Icons.Filled.Cancel
        else -> Icons.Filled.AccessTime
    }
    val iconColor = when {
        transaction.isApproved -> SuccessGreen
        transaction.isRejected -> PieRed
        else -> Warning
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Purple50.copy(alpha = 0.5f))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = transaction.date.substringBefore("/"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Purple300, textDecoration = TextDecoration.Underline)
            Text(text = transaction.date.substringAfter("/"), fontSize = 12.sp, color = Purple300.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = formatRupiah(transaction.amount), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Purple300)
        }
        Column(horizontalAlignment = Alignment.End) {
            Icon(imageVector = iconVector, contentDescription = transaction.status, tint = iconColor, modifier = Modifier.size(28.dp).padding(bottom = 4.dp))
            Text(text = transaction.description, fontSize = 14.sp, color = Purple300, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(text = transaction.status, fontSize = 10.sp, color = iconColor, fontWeight = FontWeight.Light)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardMahasiswaPreview() {
    DashboardMahasiswaScreen("preview_uid", onNavigateToLogForm = {}, onNavigateToProfile = {})
}