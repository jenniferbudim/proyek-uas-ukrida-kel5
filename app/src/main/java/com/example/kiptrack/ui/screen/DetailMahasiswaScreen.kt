package com.example.kiptrack.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiptrack.R // Make sure to import your R file for images if needed
import com.example.kiptrack.ui.data.DetailTab
import com.example.kiptrack.ui.theme.PieGreen
import com.example.kiptrack.ui.theme.PieOrange
import com.example.kiptrack.ui.theme.PieRed
import com.example.kiptrack.ui.theme.PurpleDark
import com.example.kiptrack.ui.theme.PurpleLightBg
import com.example.kiptrack.ui.theme.TextPurple

@Composable
fun DetailMahasiswaScreen(
    mahasiswaName: String = "Blessy Jeniffer",
    uid: String,
    onBackClick: () -> Unit
) {
    // Default tab is Mail (Konfirmasi) based on the first image provided
    var selectedTab by remember { mutableStateOf(DetailTab.Konfirmasi) }

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
                .background(PurpleLightBg)
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // --- HEADER SECTION (Common to all tabs) ---
            CommonHeader(mahasiswaName, onBackClick)

            // --- TAB CONTENT ---
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    DetailTab.Konfirmasi -> TabContentCharts() // Mail Icon -> Charts
                    DetailTab.Home -> TabContentHistory() // Home Icon -> List
                    DetailTab.Perincian -> TabContentDetail() // Eye Icon -> Single Detail
                }
            }
        }
    }
}

// --- HEADER COMPONENT ---
@Composable
fun CommonHeader(name: String, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Height for the purple header area
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFD1C4E9), Color(0xFFB39DDB))
                )
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Back Button & Name & Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = PurpleDark
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPurple
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Profile Placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray) // Replace with Image
                ) {
                    // Place an Image composable here for the avatar
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White, modifier = Modifier.align(Alignment.Center))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Saldo Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Saldo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPurple.copy(alpha = 0.7f)
                )
                Text(
                    text = "1.000.000",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.1f),
                            blurRadius = 4f
                        )
                    )
                )
            }
        }
    }
}

// --- TAB 1: KONFIRMASI (Mail Icon) -> CHARTS (Image 1) ---
@Composable
fun TabContentCharts() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Summary Cards Row
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryCard(
                    title = "Total Pengeluaran",
                    value = "1.500.000",
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Total Pelanggaran",
                    value = "100.000",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 2. Line Chart Container
        item {
            Card(
                shape = RoundedCornerShape(0.dp), // The screenshot has a boxy look for the chart bg
                colors = CardDefaults.cardColors(containerColor = Color(0xFFA680C3)), // Darker purple for chart bg
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Navigation Arrows
                    Icon(
                        Icons.Outlined.ChevronLeft, "Prev", tint = Color.White,
                        modifier = Modifier.align(Alignment.CenterStart).padding(4.dp)
                    )
                    Icon(
                        Icons.Outlined.ChevronRight, "Next", tint = Color.White,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(4.dp)
                    )

                    // Custom Line Chart Drawing
                    CustomLineChart(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp, vertical = 24.dp)
                    )

                    // Month Labels at bottom
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = 8.dp, start = 32.dp, end = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT").forEach {
                            Text(text = it, fontSize = 8.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // 3. Pie Chart Section
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Rincian Kategori",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPurple,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Pie Chart Graphic
                    CustomPieChart(size = 120.dp)

                    // Legend
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegendItem(color = PieRed, label = "Makanan & Minuman")
                        LegendItem(color = PieGreen, label = "Transportasi")
                        LegendItem(color = PieOrange, label = "Sandang")
                    }
                }
            }
        }
    }
}

// --- TAB 2: HOME (Home Icon) -> LIST/HISTORY (Image 2) ---
@Composable
fun TabContentHistory() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Riwayat Pengeluaran",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextPurple,
            modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
        )

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(4) { index ->
                HistoryItemCard(index)
            }
        }
    }
}

// --- TAB 3: PERINCIAN (Eye Icon) -> DETAIL/APPROVAL (Image 3) ---
@Composable
fun TabContentDetail() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // White Card container with rounded corners
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Konfirmasi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPurple,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Detail Fields
                DetailLabelValue("Tanggal Pengeluaran :", "08/08/2025")
                DetailLabelValue("Kategori Pengeluaran :", "Hunian")
                DetailLabelValue("Deskripsi Pengeluaran :", "Uang Sewa Kos")
                DetailLabelValue("Kuantitas :", "1")
                DetailLabelValue("Harga Satuan :", "5.000.000")

                Spacer(modifier = Modifier.height(8.dp))
                Text("Bukti :", fontSize = 12.sp, color = TextPurple)
                Spacer(modifier = Modifier.height(8.dp))

                // Placeholder for Proof Image (BCA)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Image Proof Placeholder", color = Color.Gray, fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons (Deny / Approve)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {}) {
                        Text("Deny", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    TextButton(onClick = {}) {
                        Text("Approve", color = Color(0xFF00C853), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// --- HELPER COMPONENTS ---

@Composable
fun CustomBottomNavigation(
    selectedTab: DetailTab,
    onTabSelected: (DetailTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp), // Taller to accommodate floating button
        contentAlignment = Alignment.BottomCenter
    ) {
        // Purple Bar Background
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            color = Color(0xFF9575CD), // Bottom bar purple
            // Optional: rounded top corners if desired
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Icon (Home)
                IconButton(
                    onClick = { onTabSelected(DetailTab.Home) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        DetailTab.Home.icon,
                        contentDescription = "Home",
                        tint = if (selectedTab == DetailTab.Home) Color.White else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f)) // Space for center button

                // Right Icon (Perincian/Eye)
                IconButton(
                    onClick = { onTabSelected(DetailTab.Perincian) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        DetailTab.Perincian.icon,
                        contentDescription = "Detail",
                        tint = if (selectedTab == DetailTab.Perincian) Color.White else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Floating Center Button (Mail)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 10.dp) // Push it down slightly into the bar
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF7E57C2)) // Slightly darker purple circle
                .clickable { onTabSelected(DetailTab.Konfirmasi) }
                .border(4.dp, PurpleLightBg, CircleShape), // White/Bg border
            contentAlignment = Alignment.Center
        ) {
            Icon(
                DetailTab.Konfirmasi.icon,
                contentDescription = "Mail",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD1C4E9)), // Light purple card
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPurple, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

@Composable
fun HistoryItemCard(index: Int) {
    val isEven = index % 2 == 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("JAN", fontWeight = FontWeight.Bold, color = TextPurple)
                Text("16/2025", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Rp${if(isEven) "50.000" else "20.000"}", fontWeight = FontWeight.Bold, color = TextPurple)
            }

            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    imageVector = if(isEven) Icons.Outlined.CheckBox else Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = if(isEven) Color(0xFF00C853) else Color.Red,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(if(isEven) "Buku" else "Alat Tulis", fontSize = 12.sp, color = TextPurple)
            }
        }
    }
}

@Composable
fun DetailLabelValue(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 12.sp, color = TextPurple.copy(alpha = 0.7f))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPurple)
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 12.sp, color = PieRed) // Using red for text based on image look
    }
}

// --- VISUALIZATIONS (Simple Canvas Implementations) ---

@Composable
fun CustomLineChart(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        // Mock points
        val points = listOf(0.7f, 0.6f, 0.4f, 0.45f, 0.1f, 0.8f, 0.5f, 0.7f, 0.2f, 0.6f)
        val stepX = width / (points.size - 1)

        // Draw vertical grid lines
        for (i in points.indices) {
            drawLine(
                color = Color.White.copy(alpha = 0.2f),
                start = Offset(i * stepX, 0f),
                end = Offset(i * stepX, height),
                strokeWidth = 1f
            )
        }

        // Draw Line Path
        val path = Path()
        points.forEachIndexed { index, value ->
            val x = index * stepX
            val y = value * height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw Dots
        points.forEachIndexed { index, value ->
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(index * stepX, value * height)
            )
        }
    }
}

@Composable
fun CustomPieChart(size: androidx.compose.ui.unit.Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val radius = size.toPx() / 2
        val center = Offset(size.toPx() / 2, size.toPx() / 2)

        // Slice 1 (Red - Big)
        drawArc(
            color = PieRed,
            startAngle = 0f,
            sweepAngle = 200f,
            useCenter = true,
            size = Size(size.toPx(), size.toPx())
        )
        // Slice 2 (Teal)
        drawArc(
            color = PieGreen,
            startAngle = 200f,
            sweepAngle = 90f,
            useCenter = true,
            size = Size(size.toPx(), size.toPx())
        )
        // Slice 3 (Orange)
        drawArc(
            color = PieOrange,
            startAngle = 290f,
            sweepAngle = 70f,
            useCenter = true,
            size = Size(size.toPx(), size.toPx())
        )
    }
}