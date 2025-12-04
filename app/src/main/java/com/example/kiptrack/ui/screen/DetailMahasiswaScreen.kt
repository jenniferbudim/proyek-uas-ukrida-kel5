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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount).replace("Rp", "Rp ")
}

@Composable
fun DetailMahasiswaScreen(
    uid: String, // Admin UID
    studentUid: String, // Target Student UID
    onBackClick: () -> Unit
) {
    val viewModel: DetailMahasiswaViewModel = viewModel(
        factory = DetailMahasiswaViewModelFactory(uid, studentUid)
    )
    val state = viewModel.uiState

    // Tab Logic: Default Home (Charts)
    var selectedTab by remember { mutableStateOf(DetailTab.Home) }

    Scaffold(
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
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // --- HEADER ---
            CommonHeader(state, onBackClick)

            // --- CONTENT ---
            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PurplePrimary)
                    }
                } else {
                    when (selectedTab) {
                        // Image 1: Charts & Summary
                        DetailTab.Home -> TabContentCharts(state)

                        // Image 2: List History
                        DetailTab.Konfirmasi -> TabContentHistory(
                            transactions = state.transactionList,
                            onItemClick = { trx ->
                                viewModel.selectTransaction(trx)
                                selectedTab = DetailTab.Perincian // Pindah ke tab detail
                            }
                        )

                        // Image 3: Detail & Approval
                        DetailTab.Perincian -> TabContentDetail(
                            transaction = state.selectedTransaction,
                            onApprove = { viewModel.approveTransaction(it.id) },
                            onDeny = { viewModel.denyTransaction(it.id, it.amount) }
                        )
                    }
                }
            }
        }
    }
}

// --- HEADER COMPONENT ---
@Composable
fun CommonHeader(state: DetailMahasiswaUiState, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFD1C4E9), Color(0xFFB39DDB))
                )
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PurpleDark)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = state.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PurpleTextDeep
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Avatar
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.photoProfile.isNotBlank()) {
                        val bitmap = ImageUtils.base64ToBitmap(state.photoProfile)
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Filled.Person, null, tint = Purple300)
                        }
                    } else {
                        Icon(Icons.Filled.Person, null, tint = Purple300)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Saldo
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Saldo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PurpleTextDeep.copy(alpha = 0.7f))
                Text(
                    text = formatRupiahDetail(state.saldo),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// --- TAB 1: DASHBOARD / CHARTS ---
@Composable
fun TabContentCharts(state: DetailMahasiswaUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Cards
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryCard("Total Pengeluaran", formatRupiahDetail(state.totalPengeluaran), Modifier.weight(1f))
                SummaryCard("Total Pelanggaran", formatRupiahDetail(state.totalPelanggaran), Modifier.weight(1f))
            }
        }

        // Line Chart
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFA680C3)),
                modifier = Modifier.fillMaxWidth().height(200.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    CustomLineChart(state.graphData, Modifier.fillMaxSize().padding(bottom = 20.dp))
                    // Labels Bulan
                    Row(
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC").forEach {
                            Text(it, fontSize = 8.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 2: LIST HISTORY (MAIL ICON) ---
@Composable
fun TabContentHistory(
    transactions: List<Transaction>,
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

// --- TAB 3: DETAIL KONFIRMASI (EYE ICON) ---
@Composable
fun TabContentDetail(
    transaction: Transaction?,
    onApprove: (Transaction) -> Unit,
    onDeny: (Transaction) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (transaction == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Pilih transaksi dari list (ikon surat) terlebih dahulu.", color = Color.Gray, textAlign = TextAlign.Center)
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

                    // Bukti Image
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

                    // Buttons
                    if (transaction.status == "MENUNGGU") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { onDeny(transaction) }) {
                                Text("Deny", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            TextButton(onClick = { onApprove(transaction) }) {
                                Text("Approve", color = Color(0xFF00C853), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    } else {
                        Text(
                            "Status: ${transaction.status}",
                            color = if(transaction.isApproved) Color(0xFF00C853) else Color.Red,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

// --- HELPER ITEMS ---

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
                Text(trx.date.split(" ")[0], fontWeight = FontWeight.Bold, color = PurpleTextDeep) // Month (JAN)
                Text(trx.date, fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text(formatRupiahDetail(trx.amount), fontWeight = FontWeight.Bold, color = PurpleTextDeep)
            }
            Column(horizontalAlignment = Alignment.End) {
                val icon = when {
                    trx.isApproved -> Icons.Outlined.CheckBox
                    trx.isRejected -> Icons.Outlined.Cancel
                    else -> Icons.Outlined.Warning
                }
                val color = when {
                    trx.isApproved -> Color(0xFF00C853)
                    trx.isRejected -> Color.Red
                    else -> Color(0xFFFFA000)
                }
                Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(4.dp))
                Text(trx.description, fontSize = 12.sp, color = PurpleTextDeep)
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