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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronLeft
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.outlined.CheckCircle

// --- COLOR PALETTE SESUAI FIGMA ---
val FigmaBg1 = Color(0xFFDECDE9) // Posisi 0%
val FigmaBg2 = Color(0xFFD3BDE2) // Posisi 10%
val FigmaBg3 = Color(0xFFC9ADDB) // Posisi 20% & Sisa layar

// Warna Card Summary (Posisi 16% & 88% C9ADDB)
val FigmaCardColor1 = Color(0xFFF3EDF7)
val FigmaCardColor2 = Color(0xFFDECDE9)

val RedPink = Color(0xFFB14EA7)

// Warna Tambahan untuk Elemen Lain
val ColorTextDeep = Color(0xFF4A148C)       // Teks Ungu Tua
val ColorChartBg = Color(0xFF9575CD)
val ColorPieRed = Color(0xFFE53935)
val ColorPieTeal = Color(0xFF009688)
val ColorPieOrange = Color(0xFFFF9800)

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
    onBackClick: () -> Unit,
    onNavigateToProfileAdmin: (String) -> Unit
) {
    val viewModel: DetailMahasiswaViewModel = viewModel(
        factory = DetailMahasiswaViewModelFactory(uid, studentUid)
    )
    val state = viewModel.uiState
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("in", "ID")) }

    // --- STATE UTAMA ---
    var selectedTab by remember { mutableStateOf(DetailTab.Home) }

    // State untuk Modal Bottom Sheet
    var showDetailSheet by remember { mutableStateOf(false) }
    var selectedLogDetail by remember { mutableStateOf<Transaction?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // --- BRUSH BACKGROUND UTAMA ---
    // Ditaruh di Box paling luar agar full screen
    val backgroundBrush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to FigmaBg1,
            0.1f to FigmaBg2,
            0.2f to FigmaBg3,
            1.0f to FigmaBg3
        )
    )

    // CONTAINER UTAMA (Box) untuk Background Full Screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush) // Background Gradient diterapkan di sini
    ) {
        Scaffold(
            containerColor = Color.Transparent, // Scaffold dibuat transparan
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChevronLeft,
                                contentDescription = "Back",
                                tint = TextPurpleDark,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 16.dp) // Beri jarak dari pinggir kanan
                        ) {
                            // Tampilkan Nama
                            Text(
                                text = state.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp, // Ukuran disesuaikan agar muat di AppBar
                                color = ColorTextDeep
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Tampilkan Foto Profil
                            Box(
                                modifier = Modifier
                                    .size(36.dp) // Ukuran sedikit diperkecil untuk AppBar
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                val photo = state.photoProfile
                                if (photo.isNotBlank()) {
                                    val bitmap = ImageUtils.base64ToBitmap(photo)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { onNavigateToProfileAdmin(studentUid) }, // Pastikan ID benar
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Filled.Person, null, tint = FigmaBg3)
                                    }
                                } else {
                                    Icon(Icons.Filled.Person, null, tint = FigmaBg3)
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                    .padding(bottom = paddingValues.calculateBottomPadding(), top = paddingValues.calculateTopPadding())
            ) {
                // HEADER SALDO
                CommonHeader(
                    state = state,
                    uid = studentUid,
                    onNavigateToProfileAdmin = onNavigateToProfileAdmin
                )

                // CONTENT AREA
                Box(modifier = Modifier.weight(1f)) {
                    if (state.isLoading && state.name == "Loading...") {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = ColorTextDeep)
                        }
                    } else {
                        when (selectedTab) {
                            DetailTab.Home -> TabContentCharts(state, viewModel)
                            DetailTab.Konfirmasi -> TabContentHistory(
                                transactions = state.transactionList,
                                formatter = formatter,
                                onItemClick = { trx ->
                                    selectedLogDetail = trx
                                    showDetailSheet = true
                                }
                            )
                            DetailTab.Perincian -> TabContentDetail(
                                pendingList = state.transactionList.filter { it.status == "MENUNGGU" },
                                currentIndex = 0,
                                onIndexChange = { },
                                onApprove = { viewModel.approveTransaction(it.id) },
                                onDeny = { viewModel.denyTransaction(it.id, it.amount) }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- BOTTOM SHEET DETAIL ---
    if (showDetailSheet && selectedLogDetail != null) {
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Detail Riwayat", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ColorTextDeep, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(24.dp))
                if (selectedLogDetail!!.proofImage.isNotBlank()) {
                    val bitmap = ImageUtils.base64ToBitmap(selectedLogDetail!!.proofImage)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Bukti",
                            modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else { Box(Modifier.fillMaxWidth().height(150.dp).background(Color.LightGray, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text("Gambar Rusak") } }
                } else { Box(Modifier.fillMaxWidth().height(150.dp).background(Color.LightGray, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text("Tidak Ada Bukti Foto") } }
                Spacer(modifier = Modifier.height(24.dp))
                DetailLabelValue("Tanggal", selectedLogDetail!!.date)
                DetailLabelValue("Kategori", selectedLogDetail!!.category)
                DetailLabelValue("Deskripsi", selectedLogDetail!!.description)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) { DetailLabelValue("Kuantitas", selectedLogDetail!!.quantity.toString()) }
                    Column(modifier = Modifier.weight(1f)) { DetailLabelValue("Harga Satuan", formatRupiahDetail(selectedLogDetail!!.unitPrice)) }
                }
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text("Total", fontSize = 12.sp, color = Color.Gray); Text(formatRupiahDetail(selectedLogDetail!!.amount), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ColorTextDeep) }
                    val statusColor = when(selectedLogDetail!!.status) { "DISETUJUI" -> Color(0xFF4CAF50); "DITOLAK" -> Color.Red; else -> Color(0xFFFFC107) }
                    Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(50), border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)) {
                        Text(text = selectedLogDetail!!.status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// --- HEADER SECTION ---
@Composable
fun CommonHeader(
    uid: String,
    state: DetailMahasiswaUiState,
    onNavigateToProfileAdmin: (String) -> Unit
) {
    // Column header sekarang hanya untuk Saldo
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // --- BAGIAN ROW PROFIL LAMA DIHAPUS ---

        Spacer(modifier = Modifier.height(10.dp))

        // Saldo Center
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Saldo", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RedPink.copy(alpha = 0.6f))
            Text(
                text = formatRupiahDetail(state.saldo).replace("Rp", "").trim(),
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(Color.White.copy(0.5f), Offset(1f,1f), 2f)
                )
            )
        }
    }
}

// --- HOME CONTENT (CHARTS) ---
@Composable
fun TabContentCharts(state: DetailMahasiswaUiState, viewModel: DetailMahasiswaViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp), // Padding konten agar tidak mepet
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. DUA KARTU SUMMARY
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryCardFigma("Total Pengeluaran", formatRupiahDetail(state.totalPengeluaranAllTime), Modifier.weight(1f))
                SummaryCardFigma("Total Pelanggaran", formatRupiahDetail(state.totalPelanggaranAllTime), Modifier.weight(1f))
            }
        }

        // 2. MAIN CHART
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ColorChartBg.copy(alpha = 0.85f)), // Sedikit transparan biar menyatu
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
                        Row(modifier = Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.previousYear() }) { Icon(Icons.Filled.KeyboardArrowLeft, "Prev", tint = Color.White) }
                            Text(text = state.selectedYear.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                            IconButton(onClick = { viewModel.nextYear() }) { Icon(Icons.Filled.KeyboardArrowRight, "Next", tint = Color.White) }
                        }
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                            if (state.graphData.any { it > 0 }) {
                                CustomLineChartNew(state.graphData, Modifier.fillMaxSize().padding(bottom = 20.dp))
                            } else {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Data Kosong", color = Color.White.copy(0.7f)) }
                            }
                            Row(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                listOf("JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC").forEach {
                                    Text(it, fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("Pelanggaran Sem. Ini", color = Color.White.copy(0.8f), fontSize = 11.sp); Text(formatRupiahDetail(state.pelanggaranSemesterIni), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                            Column(horizontalAlignment = Alignment.End) { Text("Uang Saku Sem. Depan", color = Color.White.copy(0.8f), fontSize = 11.sp, textAlign = TextAlign.End); Text(formatRupiahDetail(state.estimasiPendapatanDepan), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.End) }
                        }
                    }
                }
            }
        }

        // 3. PIE CHART KATEGORI
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                // Background kartu dibuat putih solid
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp) // Tambahan padding bawah agar aman dari bottom nav
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Rincian Kategori", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RedPink); Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        if (state.categoryData.isNotEmpty()) CustomPieChart(data = state.categoryData, modifier = Modifier.size(120.dp))
                        else Box(modifier = Modifier.size(120.dp).background(Color.LightGray, CircleShape), contentAlignment = Alignment.Center) { Text("No Data", fontSize = 10.sp, color = Color.White) }

                        // List Legend
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.categoryData.take(4).forEachIndexed { _, cat ->
                                val color = cat.color
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(color, RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${cat.name} (${cat.percentage.toInt()}%)",
                                        fontSize = 12.sp,
                                        color = ColorTextDeep
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

// --- CARD SUMMARY DENGAN GRADIENT ---
@Composable
fun SummaryCardFigma(title: String, value: String, modifier: Modifier = Modifier) {
    // 1. Mengubah background menjadi Gradient FigmaBg1 -> FigmaBg2
    val cardBrush = Brush.verticalGradient(
        colors = listOf(FigmaCardColor1, FigmaCardColor2)
    )

    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
        // Container color dibiarkan default/transparan karena akan ditimpa oleh Box di dalamnya
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardBrush) // Gradient diterapkan di sini
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 2. Mengubah warna tulisan judul menjadi RedPink
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RedPink,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Mengubah warna nominal menjadi ColorTextDeep
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorTextDeep
                )
            }
        }
    }
}

// --- CHART GRAPHICS ---
@Composable
fun CustomLineChartNew(data: List<Long>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return
    val maxValue = data.maxOrNull()?.takeIf { it > 0 } ?: 1L
    Canvas(modifier = modifier) {
        val width = size.width; val height = size.height; val stepX = width / 11f; val path = Path()
        data.forEachIndexed { index, _ -> val x = index * stepX; drawLine(color = Color.White.copy(alpha = 0.2f), start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 1.dp.toPx()) }
        data.forEachIndexed { index, value -> val normalizedY = value.toFloat() / maxValue.toFloat(); val y = height - (normalizedY * height); val x = index * stepX; if (index == 0) path.moveTo(x, y) else path.lineTo(x, y) }
        drawPath(path, Color.White, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        data.forEachIndexed { index, value -> val normalizedY = value.toFloat() / maxValue.toFloat(); val y = height - (normalizedY * height); val x = index * stepX; drawCircle(Color.White, radius = 3.dp.toPx(), center = Offset(x, y)) }
    }
}

@Composable
fun CustomPieChart(data: List<CategorySummary>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        var startAngle = -90f

        // Iterasi melalui data, di mana setiap item (CategorySummary) sudah membawa properti 'color'
        data.forEach { item ->
            val sweepAngle = (item.percentage / 100f) * 360f

            // Mengambil warna langsung dari item.color
            val color = item.color

            // Menggambar irisan dengan warna yang telah ditentukan
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true
            )
            startAngle += sweepAngle
        }
    }
}

// --- OTHER TABS & NAV ---
@Composable
fun TabContentHistory(transactions: List<Transaction>, formatter: java.text.NumberFormat, onItemClick: (Transaction) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(
                text = "Riwayat Pengeluaran",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = RedPink,
                // Tambahkan TextAlign.Center
                textAlign = TextAlign.Center,
                modifier = Modifier
                    // Tambahkan fillMaxWidth agar teks punya ruang untuk ke tengah
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }
        if (transactions.isEmpty()) item { Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("Tidak ada riwayat.", color = Color.Gray) } }
        else items(transactions) { trx -> HistoryItemCard(trx, onClick = { onItemClick(trx) }) }
    }
}

@Composable
fun TabContentDetail(
    pendingList: List<Transaction>,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    onApprove: (Transaction) -> Unit,
    onDeny: (Transaction) -> Unit
) {
    var localIndex by remember { mutableIntStateOf(0) }

    // Logika sinkronisasi index (Tetap sama)
    LaunchedEffect(pendingList.size) {
        if (localIndex >= pendingList.size && pendingList.isNotEmpty()) {
            localIndex = pendingList.size - 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Padding luar agar tidak nempel banget ke layar HP
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (pendingList.isEmpty()) {
            // Tampilan Kosong (Tetap sama)
            Icon(Icons.Outlined.CheckCircle, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Semua transaksi aman.", color = Color.Gray)
        } else {
            val transaction = pendingList.getOrElse(localIndex) { pendingList[0] }

            // --- PERUBAHAN UI DIMULAI DISINI ---

            // 1. Card Utama (Warna Putih) - Menggunakan fillMaxWidth
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Agar tinggi menyesuaikan sisa layar tapi tidak overflow
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp), // Sudut lebih membulat sesuai referensi
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // HEADER: Judul Konfirmasi
                    Text(
                        text = "Konfirmasi",
                        fontSize = 20.sp, // Sedikit lebih besar
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB14EA7),
                        modifier = Modifier
                            .padding(top = 24.dp, bottom = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    )

                    // CONTENT AREA: Panah Kiri + Detail + Panah Kanan
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tombol Previous (Di dalam Card, sebelah kiri)
                        IconButton(
                            onClick = { if (localIndex > 0) localIndex-- },
                            enabled = localIndex > 0
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowLeft,
                                contentDescription = "Prev",
                                tint = if (localIndex > 0) ColorTextDeep else Color.LightGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // DETAIL CARD (Box Ungu Muda di dalam)
                        // Menggunakan Box/Card lagi agar mirip referensi gambar
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(vertical = 8.dp), // Jarak atas bawah dari panah
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)) // Warna Ungu Muda (FigmaCardColor1)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState()) // Scrollable jika konten panjang
                            ) {
                                // Detail Data
                                DetailItemFigmaRef("Tanggal Pengeluaran :", transaction.date)
                                DetailItemFigmaRef("Kategori Pengeluaran :", transaction.category)
                                DetailItemFigmaRef("Deskripsi Pengeluaran :", transaction.description)
                                DetailItemFigmaRef("Kuantitas :", transaction.quantity.toString())
                                DetailItemFigmaRef("Harga Satuan :", formatRupiahDetail(transaction.amount))

                                Spacer(Modifier.height(8.dp))
                                Text("Bukti :", fontSize = 12.sp, color = ColorTextDeep.copy(0.7f))
                                Spacer(Modifier.height(4.dp))

                                // Gambar Bukti
                                if (transaction.proofImage.isNotBlank()) {
                                    val bitmap = ImageUtils.base64ToBitmap(transaction.proofImage)
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Bukti",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White), // Background putih buat gambar
                                            contentScale = ContentScale.Crop // atau Fit agar full terlihat
                                        )
                                    } else {
                                        Box(Modifier.fillMaxWidth().height(100.dp).background(Color.White), contentAlignment = Alignment.Center) { Text("Gambar Error", fontSize = 10.sp) }
                                    }
                                } else {
                                    Box(Modifier.fillMaxWidth().height(100.dp).background(Color.White), contentAlignment = Alignment.Center) { Text("Tidak Ada Bukti", fontSize = 10.sp) }
                                }
                            }
                        }

                        // Tombol Next (Di dalam Card, sebelah kanan)
                        IconButton(
                            onClick = { if (localIndex < pendingList.size - 1) localIndex++ },
                            enabled = localIndex < pendingList.size - 1
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = "Next",
                                tint = if (localIndex < pendingList.size - 1) ColorTextDeep else Color.LightGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // FOOTER: Tombol Aksi (Deny / Approve)
                    // Menggunakan Text Button agar mirip referensi gambar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 20.dp), // Padding lebih besar
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tombol Deny (Teks Merah)
                        Text(
                            text = "Deny",
                            color = Color(0xFFD32F2F), // Merah
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onDeny(transaction) }
                                .padding(8.dp)
                        )

                        // Tombol Approve (Teks Hijau)
                        Text(
                            text = "Approve",
                            color = Color(0xFF388E3C), // Hijau
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onApprove(transaction) }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

// Helper Composable Baru untuk styling teks mirip gambar referensi
// (Label kecil ungu transparan, Value besar ungu tebal)
@Composable
fun DetailItemFigmaRef(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = ColorTextDeep.copy(alpha = 0.6f), // Warna Label Ungu agak pudar
            lineHeight = 14.sp
        )
        Text(
            text = value,
            fontSize = 16.sp, // Ukuran value lebih besar
            fontWeight = FontWeight.Bold, // Bold
            color = ColorTextDeep // Ungu Tua
        )
    }
}

@Composable
fun HistoryItemCard(trx: Transaction, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // BAGIAN KIRI
            Column {
                // Baris 1: Bulan (DEC)
                Text(trx.date.split(" ")[0], fontWeight = FontWeight.Bold, color = ColorTextDeep)

                // Baris 2: Tanggal & Tahun saja (07/2025)
                // Menggunakan substringAfter(" ") untuk membuang kata pertama (Bulan)
                Text(trx.date.substringAfter(" "), fontSize = 12.sp, color = Color.Gray)

                Spacer(Modifier.height(4.dp))

                // Baris 3: Harga
                Text(formatRupiahDetail(trx.amount), fontWeight = FontWeight.Bold, color = Color(0xFFB14EA7))
            }

            // BAGIAN KANAN (Status & Deskripsi)
            Column(horizontalAlignment = Alignment.End) {
                val icon = when (trx.status) {
                    "DISETUJUI" -> Icons.Outlined.CheckCircle
                    "DITOLAK" -> Icons.Filled.Cancel
                    else -> Icons.Filled.AccessTime
                }
                val color = when (trx.status) {
                    "DISETUJUI" -> Color(0xFF00C853)
                    "DITOLAK" -> Color.Red
                    else -> Color(0xFFFFA000)
                }
                Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(4.dp))
                Text(trx.description, fontSize = 12.sp, color = ColorTextDeep, maxLines = 1)
            }
        }
    }
}

@Composable
fun DetailLabelValue(label: String, value: String) { Column(modifier = Modifier.padding(vertical = 4.dp)) { Text(label, fontSize = 12.sp, color = ColorTextDeep.copy(alpha = 0.7f)); Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ColorTextDeep) } }

@Composable
fun CustomBottomNavigation(selectedTab: DetailTab, onTabSelected: (DetailTab) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.BottomCenter) {
        // Background Bar Bawah
        Surface(modifier = Modifier.fillMaxWidth().height(60.dp), color = ColorChartBg) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {

                // TOMBOL KIRI (HOME)
                IconButton(onClick = { onTabSelected(DetailTab.Home) }, modifier = Modifier.weight(1f)) {
                    Icon(
                        DetailTab.Home.icon,
                        null,
                        tint = if (selectedTab == DetailTab.Home) ColorTextDeep else FigmaBg2
                    )
                }

                Spacer(Modifier.weight(1f))

                // TOMBOL KANAN (RIWAYAT)
                IconButton(onClick = { onTabSelected(DetailTab.Konfirmasi) }, modifier = Modifier.weight(1f)) {
                    Icon(
                        DetailTab.Perincian.icon,
                        null,
                        tint = if (selectedTab == DetailTab.Konfirmasi) ColorTextDeep else FigmaBg2
                    )
                }
            }
        }

        // --- TOMBOL TENGAH (PAGE KONFIRMASI) ---
        // Cek apakah tab tengah sedang aktif
        val isConfirmationActive = selectedTab == DetailTab.Perincian

        // LOGIKA WARNA BACKGROUND:
        // Kalau Aktif -> Ungu Muda (FigmaBg2)
        // Kalau Tidak Aktif -> Ungu Tua (ColorTextDeep)
        val circleBgColor = if (isConfirmationActive) FigmaBg2 else Color (0xFF894EB1)

        // LOGIKA WARNA ICON (Harus kontras dengan background):
        // Kalau Aktif (Bg Muda) -> Icon Tua
        // Kalau Tidak Aktif (Bg Tua) -> Icon Muda
        val iconColor = if (isConfirmationActive) Color (0xFF894EB1) else FigmaBg2

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 10.dp)
                .size(70.dp)
                .clip(CircleShape)
                .background(circleBgColor) // <--- BACKGROUND BERUBAH DI SINI
                .clickable { onTabSelected(DetailTab.Perincian) }
                .border(4.dp, FigmaBg3, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                DetailTab.Konfirmasi.icon,
                null,
                tint = iconColor // <--- ICON BERUBAH DI SINI
            )
        }
    }
}