package com.ukrida.kiptrack.ui.screen

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
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
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
import com.ukrida.kiptrack.ui.data.CategorySummary
import com.ukrida.kiptrack.ui.data.DetailTab
import com.ukrida.kiptrack.ui.data.Transaction
import com.ukrida.kiptrack.ui.theme.*
import com.ukrida.kiptrack.ui.utils.ImageUtils
import com.ukrida.kiptrack.ui.viewmodel.DetailMahasiswaUiState
import com.ukrida.kiptrack.ui.viewmodel.DetailMahasiswaViewModel
import com.ukrida.kiptrack.ui.viewmodel.DetailMahasiswaViewModelFactory
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel

val FigmaBg2 = Color(0xFFD3BDE2)
val FigmaCardColor1 = Color(0xFFF3EDF7)

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
    val backgroundBrush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Purple50,
            0.1f to FigmaBg2,
            0.2f to Purple100,
            1.0f to Purple100
        )
    )

    // CONTAINER UTAMA (Box) untuk Background Full Screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
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
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Text(
                                text = state.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPurpleDark
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Tampilkan Foto Profil
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
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
                                                .clickable { onNavigateToProfileAdmin(studentUid) },
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Filled.Person, null, tint = Purple100)
                                    }
                                } else {
                                    Icon(Icons.Filled.Person, null, tint = Purple100)
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
                            CircularProgressIndicator(color = TextPurpleDark)
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
                Text("Detail Riwayat", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPurpleDark, modifier = Modifier.align(Alignment.CenterHorizontally))
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
                    Column { Text("Total", fontSize = 12.sp, color = Color.Gray); Text(formatRupiahDetail(selectedLogDetail!!.amount), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPurpleDark) }
                    val statusColor = when(selectedLogDetail!!.status) { "DISETUJUI" -> SuccessGreen; "DITOLAK" -> PieRed; else -> Warning }
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
        Spacer(modifier = Modifier.height(10.dp))

        // Saldo Center
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Saldo", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PurplePrimary.copy(alpha = 0.6f))
            Text(
                text = formatRupiahDetail(state.saldo).replace("Rp", "").trim(),
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(Color.White.copy(0.5f), Offset(1f,1f), 2f)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                colors = CardDefaults.cardColors(containerColor = Purple300.copy(alpha = 0.85f)), // Sedikit transparan biar menyatu
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
                                CustomLineChartNew(state.graphData, Modifier.fillMaxSize().padding(bottom = 45.dp))
                            } else {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Data Kosong", color = Color.White.copy(0.7f)) }
                            }
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC").forEach {
                                    Text(it, fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // --- LOGIKA SEMESTER AKHIR (UPDATED) ---
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Pelanggaran Sem. Ini", color = Color.White.copy(0.8f), fontSize = 11.sp)
                                Text(formatRupiahDetail(state.pelanggaranSemesterIni), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                // Cek Semester Akhir
                                val isFinalSemester = (state.jenjang.contains("S1", ignoreCase = true) && state.semester >= 8) ||
                                        (state.jenjang.contains("D3", ignoreCase = true) && state.semester >= 6)

                                val labelText = if (isFinalSemester) "Status" else "Uang Saku Sem. Depan"

                                // Logic Value (Bermasalah / Aman / Rupiah)
                                val valueText = if (isFinalSemester) {
                                    if (state.pelanggaranSemesterIni > 0) "Bermasalah" else "Aman"
                                } else {
                                    formatRupiahDetail(state.estimasiPendapatanDepan)
                                }

                                // Logic Warna (Merah / Hijau / Putih)
                                val valueColor = if (isFinalSemester) {
                                    if (state.pelanggaranSemesterIni > 0) PieRed else SuccessGreen
                                } else {
                                    Color.White
                                }

                                Text(labelText, color = Color.White.copy(0.8f), fontSize = 11.sp, textAlign = TextAlign.End)
                                Text(valueText, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.End)
                            }
                        }
                        // ----------------------------------------
                    }
                }
            }
        }

        // 3. PIE CHART KATEGORI
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // 1. Title
                    Text(
                        text = "Rincian Kategori",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PurplePrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. Pie Chart
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.categoryData.isNotEmpty()) {
                            CustomPieChart(data = state.categoryData, modifier = Modifier.size(140.dp))
                        } else {
                            Box(
                                modifier = Modifier.size(140.dp).background(Color.LightGray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No Data", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. Legend List
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            state.categoryData.forEachIndexed { _, cat ->
                                val color = cat.color
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(color, RoundedCornerShape(4.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "${cat.name} (${cat.percentage.toInt()}%)",
                                        fontSize = 14.sp,
                                        color = TextPurpleDark
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

// --- CARD SUMMARY DENGAN GRADIENT ---
@Composable
fun SummaryCardFigma(title: String, value: String, modifier: Modifier = Modifier) {
    val cardBrush = Brush.verticalGradient(
        colors = listOf(FigmaCardColor1, Purple50)
    )

    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardBrush)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PurplePrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPurpleDark
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
        data.forEach { item ->
            val sweepAngle = (item.percentage / 100f) * 360f
            val color = item.color
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
                color = PurplePrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
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

    LaunchedEffect(pendingList.size) {
        if (localIndex >= pendingList.size && pendingList.isNotEmpty()) {
            localIndex = pendingList.size - 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (pendingList.isEmpty()) {
            Icon(Icons.Outlined.CheckCircle, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Semua transaksi aman.", color = Color.Gray)
        } else {
            val transaction = pendingList.getOrElse(localIndex) { pendingList[0] }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Konfirmasi",
                        fontSize = 20.sp,
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
                                tint = if (localIndex > 0) TextPurpleDark else Color.LightGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // DETAIL CARD
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)) // Warna Ungu Muda (FigmaCardColor1)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Detail Data
                                DetailTambahan("Tanggal Pengeluaran :", transaction.date)
                                DetailTambahan("Kategori Pengeluaran :", transaction.category)
                                DetailTambahan("Deskripsi Pengeluaran :", transaction.description)
                                DetailTambahan("Kuantitas :", transaction.quantity.toString())
                                DetailTambahan("Harga Satuan :", formatRupiahDetail(transaction.amount))

                                Spacer(Modifier.height(8.dp))
                                Text("Bukti :", fontSize = 12.sp, color = TextPurpleDark.copy(0.7f))
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
                                                .background(Color.White),
                                            contentScale = ContentScale.Crop
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
                                tint = if (localIndex < pendingList.size - 1) TextPurpleDark else Color.LightGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // FOOTER: Tombol Aksi (Deny / Approve)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tombol Deny (Teks Merah)
                        Text(
                            text = "Deny",
                            color = PieRed,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onDeny(transaction) }
                                .padding(8.dp)
                        )

                        // Tombol Approve (Teks Hijau)
                        Text(
                            text = "Approve",
                            color = SuccessGreen, // Hijau
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

// Helper Composable
@Composable
fun DetailTambahan(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextPurpleDark.copy(alpha = 0.6f),
            lineHeight = 14.sp
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPurpleDark
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
                Text(trx.date.split(" ")[0], fontWeight = FontWeight.Bold, color = TextPurpleDark)

                // Baris 2: Tanggal & Tahun saja (07/2025)
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
                Text(trx.description, fontSize = 12.sp, color = TextPurpleDark, maxLines = 1)
            }
        }
    }
}

@Composable
fun DetailLabelValue(label: String, value: String) { Column(modifier = Modifier.padding(vertical = 4.dp)) { Text(label, fontSize = 12.sp, color = TextPurpleDark.copy(alpha = 0.7f)); Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPurpleDark) } }

@Composable
fun CustomBottomNavigation(selectedTab: DetailTab, onTabSelected: (DetailTab) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.BottomCenter) {
        // Background Bar Bawah
        Surface(modifier = Modifier.fillMaxWidth().height(60.dp), color = Purple300) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {

                // TOMBOL KIRI (HOME)
                IconButton(onClick = { onTabSelected(DetailTab.Home) }, modifier = Modifier.weight(1f)) {
                    Icon(
                        DetailTab.Home.icon,
                        null,
                        tint = if (selectedTab == DetailTab.Home) TextPurpleDark else FigmaBg2
                    )
                }

                Spacer(Modifier.weight(1f))

                // TOMBOL KANAN (RIWAYAT)
                IconButton(onClick = { onTabSelected(DetailTab.Konfirmasi) }, modifier = Modifier.weight(1f)) {
                    Icon(
                        DetailTab.Perincian.icon,
                        null,
                        tint = if (selectedTab == DetailTab.Konfirmasi) TextPurpleDark else FigmaBg2
                    )
                }
            }
        }

        // --- TOMBOL TENGAH (PAGE KONFIRMASI) ---
        // Cek apakah tab tengah sedang aktif
        val isConfirmationActive = selectedTab == DetailTab.Perincian

        // LOGIKA WARNA BACKGROUND:
        // Kalau Aktif -> Ungu Muda (FigmaBg2)
        // Kalau Tidak Aktif -> Ungu Tua (TextPurpleDark)
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
                .background(circleBgColor)
                .clickable { onTabSelected(DetailTab.Perincian) }
                .border(4.dp, Purple100, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                DetailTab.Konfirmasi.icon,
                null,
                tint = iconColor
            )
        }
    }
}