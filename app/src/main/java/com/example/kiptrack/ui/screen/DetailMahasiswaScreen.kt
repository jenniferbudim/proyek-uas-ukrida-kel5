package com.example.kiptrack.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.data.CategorySummary
import com.example.kiptrack.ui.data.DetailTab
import com.example.kiptrack.ui.data.Transaction
import com.example.kiptrack.ui.theme.*
import com.example.kiptrack.ui.utils.ImageUtils
import com.example.kiptrack.ui.viewmodel.DetailMahasiswaUiState
import com.example.kiptrack.ui.viewmodel.DetailMahasiswaViewModel
import com.example.kiptrack.ui.viewmodel.DetailMahasiswaViewModelFactory
import java.text.NumberFormat
import java.util.Locale

// Helper Format Rupiah
fun formatRupiahDetail(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount).replace("Rp", "Rp ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailMahasiswaScreen(
    uid: String,
    studentUid: String,
    onBackClick: () -> Unit
) {
    val viewModel: DetailMahasiswaViewModel = viewModel(
        factory = DetailMahasiswaViewModelFactory(uid, studentUid)
    )
    val state = viewModel.uiState
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("in", "ID")) }

    var selectedTab by remember { mutableStateOf(DetailTab.Home) }

    // State Dialog Konfirmasi
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedLog by remember { mutableStateOf<Transaction?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Mahasiswa", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple200)
            )
        },
        bottomBar = {
            CustomBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Purple50)
                .padding(bottom = paddingValues.calculateBottomPadding(), top = paddingValues.calculateTopPadding())
        ) {
            // HEADER
            CommonHeader(state)

            // CONTENT AREA
            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoading && state.name == "Loading...") {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Purple300)
                    }
                } else {
                    when (selectedTab) {
                        // Tab Home (Grafik) tetap sama
                        DetailTab.Home -> TabContentCharts(state, viewModel)

                        // --- PERUBAHAN DI SINI (DITUKAR) ---

                        // 1. Tab Konfirmasi (Ikon Surat) -> Sekarang Menampilkan DETAIL TRANSAKSI
                        DetailTab.Konfirmasi -> TabContentDetail(
                            transaction = state.selectedTransaction,
                            onApprove = { viewModel.approveTransaction(it.id) },
                            onDeny = { viewModel.denyTransaction(it.id, it.amount) }
                        )

                        // 2. Tab Perincian (Ikon Mata) -> Sekarang Menampilkan LIST RIWAYAT
                        DetailTab.Perincian -> TabContentHistory(
                            transactions = state.transactionList,
                            formatter = formatter,
                            onItemClick = { trx ->
                                if (trx.status == "MENUNGGU") {
                                    // Jika status Menunggu, buka Dialog Konfirmasi
                                    selectedLog = trx
                                    showConfirmDialog = true
                                } else {
                                    // Jika status lain, buka Detail di Tab Konfirmasi (Surat)
                                    viewModel.selectTransaction(trx)
                                    selectedTab = DetailTab.Konfirmasi
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // DIALOG KONFIRMASI (FIXED: Menggunakan description dan amount)
    if (showConfirmDialog && selectedLog != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Konfirmasi Pengajuan", color = Purple300, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Apakah Anda ingin menyetujui atau menolak pengajuan ini?")
                    Spacer(modifier = Modifier.height(8.dp))
                    // --- PERBAIKAN DI SINI ---
                    Text("Deskripsi: ${selectedLog!!.description}", fontWeight = FontWeight.Medium)
                    Text("Nominal: ${formatRupiahDetail(selectedLog!!.amount)}", fontWeight = FontWeight.Medium)
                    // -------------------------
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.approveTransaction(selectedLog!!.id)
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("Setujui") }
            },
            dismissButton = {
                Row {
                    Button(
                        onClick = {
                            viewModel.denyTransaction(selectedLog!!.id, selectedLog!!.amount)
                            showConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) { Text("Tolak") }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { showConfirmDialog = false }) { Text("Batal", color = Color.Gray) }
                }
            }
        )
    }
}

// ... (SISA KODE KE BAWAH TIDAK PERLU DIUBAH) ...
// Pastikan Anda tetap mengcopy fungsi-fungsi helper di bawah ini:

@Composable
fun CommonHeader(state: DetailMahasiswaUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFD1C4E9), Color(0xFFB39DDB))
                )
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = state.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PurpleTextDeep
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    val photo = state.photoProfile
                    if (photo.isNotBlank()) {
                        val bitmap = ImageUtils.base64ToBitmap(photo)
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else { Icon(Icons.Filled.Person, null, tint = Purple300) }
                    } else { Icon(Icons.Filled.Person, null, tint = Purple300) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Saldo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PurpleTextDeep.copy(alpha = 0.7f))
                Text(
                    text = formatRupiahDetail(state.saldo),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun TabContentCharts(state: DetailMahasiswaUiState, viewModel: DetailMahasiswaViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryCard("Total Pengeluaran", formatRupiahDetail(state.totalPengeluaranAllTime), Modifier.weight(1f))
                SummaryCard("Total Pelanggaran", formatRupiahDetail(state.totalPelanggaranAllTime), Modifier.weight(1f))
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFA680C3)),
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.previousYear() }) {
                            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Prev", tint = Color.White)
                        }
                        Text(
                            text = state.selectedYear.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(onClick = { viewModel.nextYear() }) {
                            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Next", tint = Color.White)
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        if (state.graphData.any { it > 0 }) {
                            CustomLineChart(state.graphData, Modifier.fillMaxSize().padding(bottom = 20.dp))
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Data Kosong", color = Color.White.copy(0.7f))
                            }
                        }
                        Row(
                            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("J","F","M","A","M","J","J","A","S","O","N","D").forEach {
                                Text(it, fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.White.copy(0.3f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Pelanggaran Sem. Ini", color = Color.White.copy(0.8f), fontSize = 11.sp)
                            Text(formatRupiahDetail(state.pelanggaranSemesterIni), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pendapatan Sem. Depan", color = Color.White.copy(0.8f), fontSize = 11.sp, textAlign = TextAlign.End)
                            Text(formatRupiahDetail(state.estimasiPendapatanDepan), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.End)
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Rincian Kategori",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PurpleTextDeep
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.categoryData.isNotEmpty()) {
                            CustomPieChart(
                                data = state.categoryData,
                                modifier = Modifier.size(120.dp)
                            )
                        } else {
                            Box(modifier = Modifier.size(120.dp).background(Color.LightGray, CircleShape), contentAlignment = Alignment.Center) {
                                Text("No Data", fontSize = 10.sp, color = Color.White)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.categoryData.forEach { cat ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).background(cat.color, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${cat.name} (${cat.percentage.toInt()}%)",
                                        fontSize = 12.sp,
                                        color = PurpleTextDeep
                                    )
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
fun TabContentHistory(
    transactions: List<Transaction>,
    formatter: java.text.NumberFormat,
    onItemClick: (Transaction) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Riwayat Pengeluaran",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = PurpleTextDeep,
            modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
        )

        if (transactions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tidak ada riwayat transaksi.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(transactions) { trx ->
                    HistoryItemCard(trx, onClick = { onItemClick(trx) })
                }
            }
        }
    }
}

@Composable
fun TabContentDetail(
    transaction: Transaction?,
    onApprove: (Transaction) -> Unit,
    onDeny: (Transaction) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (transaction == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Pilih transaksi dari Tab Konfirmasi (Tengah) dulu.", color = Color.Gray, textAlign = TextAlign.Center)
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Konfirmasi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PurpleTextDeep, modifier = Modifier.padding(bottom = 16.dp))

                    DetailLabelValue("Tanggal", transaction.date)
                    DetailLabelValue("Deskripsi", transaction.description)
                    DetailLabelValue("Kuantitas", transaction.quantity.toString())
                    DetailLabelValue("Harga Satuan", formatRupiahDetail(transaction.unitPrice))
                    DetailLabelValue("Total", formatRupiahDetail(transaction.amount))
                    DetailLabelValue("Status Saat Ini", transaction.status)

                    Spacer(Modifier.height(8.dp))
                    Text("Bukti :", fontSize = 12.sp, color = PurpleTextDeep)
                    Spacer(Modifier.height(8.dp))

                    if (transaction.proofImage.isNotBlank()) {
                        val bitmap = ImageUtils.base64ToBitmap(transaction.proofImage)
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(Modifier.fillMaxWidth().height(100.dp).background(Color.LightGray)) { Text("Gambar rusak", Modifier.align(Alignment.Center)) }
                        }
                    } else {
                        Box(Modifier.fillMaxWidth().height(100.dp).background(Color.LightGray)) { Text("Tidak ada bukti", Modifier.align(Alignment.Center)) }
                    }

                    Spacer(Modifier.height(24.dp))

                    if (transaction.status == "MENUNGGU") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(
                                onClick = { onDeny(transaction) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                            ) { Text("Deny") }
                            Button(
                                onClick = { onApprove(transaction) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) { Text("Approve") }
                        }
                    } else {
                        Text(
                            "Status: ${transaction.status}",
                            color = if(transaction.status == "DISETUJUI") Color(0xFF00C853) else Color.Red,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD1C4E9)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PurpleTextDeep, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

@Composable
fun HistoryItemCard(trx: Transaction, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val dateParts = trx.date.split(" ")
                val month = if(dateParts.isNotEmpty()) dateParts[0] else ""
                Text(month, fontWeight = FontWeight.Bold, color = PurpleTextDeep)
                Text(trx.date, fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text(formatRupiahDetail(trx.amount), fontWeight = FontWeight.Bold, color = PurpleTextDeep)
            }
            Column(horizontalAlignment = Alignment.End) {
                val icon = when (trx.status) {
                    "DISETUJUI" -> Icons.Filled.CheckCircle
                    "DITOLAK" -> Icons.Filled.Close
                    else -> Icons.Filled.Warning
                }
                val color = when (trx.status) {
                    "DISETUJUI" -> Color(0xFF00C853)
                    "DITOLAK" -> Color.Red
                    else -> Color(0xFFFFA000)
                }
                Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(4.dp))
                Text(trx.description, fontSize = 12.sp, color = PurpleTextDeep, maxLines = 1)
            }
        }
    }
}

@Composable
fun DetailLabelValue(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 12.sp, color = PurpleTextDeep.copy(alpha = 0.7f))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = PurpleTextDeep)
    }
}

@Composable
fun CustomLineChart(data: List<Long>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return
    val maxValue = data.maxOrNull()?.takeIf { it > 0 } ?: 1L

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / 11f
        val path = Path()

        data.forEachIndexed { index, value ->
            val normalizedY = value.toFloat() / maxValue.toFloat()
            val y = height - (normalizedY * height)
            val x = index * stepX

            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(Color.White, radius = 3.dp.toPx(), center = Offset(x, y))
        }
        drawPath(path, Color.White, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun CustomPieChart(data: List<CategorySummary>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val totalPercentage = data.sumOf { it.percentage.toDouble() }.toFloat()
        var startAngle = -90f
        data.forEach { item ->
            val sweepAngle = (item.percentage / 100f) * 360f
            drawArc(color = item.color, startAngle = startAngle, sweepAngle = sweepAngle, useCenter = true)
            startAngle += sweepAngle
        }
    }
}

@Composable
fun CustomBottomNavigation(selectedTab: DetailTab, onTabSelected: (DetailTab) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.BottomCenter) {
        Surface(modifier = Modifier.fillMaxWidth().height(60.dp), color = Color(0xFF9575CD)) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onTabSelected(DetailTab.Home) }, modifier = Modifier.weight(1f)) {
                    Icon(DetailTab.Home.icon, null, tint = if (selectedTab == DetailTab.Home) Color.White else Color.White.copy(0.5f))
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onTabSelected(DetailTab.Perincian) }, modifier = Modifier.weight(1f)) {
                    Icon(DetailTab.Perincian.icon, null, tint = if (selectedTab == DetailTab.Perincian) Color.White else Color.White.copy(0.5f))
                }
            }
        }
        Box(
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 10.dp).size(64.dp)
                .clip(CircleShape).background(Color(0xFF7E57C2))
                .clickable { onTabSelected(DetailTab.Konfirmasi) }
                .border(4.dp, Purple50, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(DetailTab.Konfirmasi.icon, null, tint = Color.White)
        }
    }
}