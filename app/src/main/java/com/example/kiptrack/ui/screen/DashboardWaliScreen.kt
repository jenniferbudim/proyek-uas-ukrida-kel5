package com.example.kiptrack.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.data.CategorySummary
import com.example.kiptrack.ui.data.MonthlyData
import com.example.kiptrack.ui.theme.*
import com.example.kiptrack.ui.viewmodel.DashboardWaliViewModel
import com.example.kiptrack.ui.viewmodel.DashboardWaliViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardWaliScreen(
    uid: String,
    onNavigateToHistory: (String) -> Unit,
    onLogoutClicked: () -> Unit
) {
    val viewModel: DashboardWaliViewModel = viewModel(
        factory = DashboardWaliViewModelFactory(uid)
    )
    val state = viewModel.uiState
    val scrollState = rememberScrollState()

    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Box(modifier = Modifier.fillMaxSize().background(Purple50)) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PurplePrimary)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- HEADER ---
                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = PurpleTextDeep,
                        modifier = Modifier.size(28.dp).align(Alignment.CenterStart).clickable { onLogoutClicked() }
                    )
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Selamat Datang, ${state.username}!", fontSize = 16.sp, color = PurpleDark, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(state.studentName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                SaldoCard("Saldo Saat Ini:", "Rp ${formatter.format(state.currentBalance)}", Modifier.fillMaxWidth().padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    MiniSummaryCard("Total Pengeluaran", "Rp ${formatter.format(state.totalExpenditure)}", Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(16.dp))
                    MiniSummaryCard("Total Pelanggaran", "Rp ${formatter.format(state.totalViolations)}", Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- CHART WITH YEAR SELECTOR ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(280.dp) // Sedikit lebih tinggi untuk header tahun
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBg.copy(alpha = 0.5f))
                        .padding(vertical = 16.dp)
                ) {
                    Column {
                        // Year Selector Header
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ChevronLeft, "Prev", tint = PurpleDark, modifier = Modifier.size(32.dp).clickable { viewModel.previousYear() })
                            Text(state.selectedYear.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PurpleTextDeep)
                            Icon(Icons.Default.ChevronRight, "Next", tint = PurpleDark, modifier = Modifier.size(32.dp).clickable { viewModel.nextYear() })
                        }

                        // Chart
                        MonthlyLineChart(
                            monthlyData = state.monthlyExpenditure,
                            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- CATEGORY ---
                Text("Rincian Kategori", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PurpleDark, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    PieChart(state.categorySummary, Modifier.size(150.dp))
                    Spacer(modifier = Modifier.width(30.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        state.categorySummary.forEach { category ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                Box(modifier = Modifier.size(12.dp).background(category.color))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${category.name} (${category.percentage.toInt()}%)", fontSize = 14.sp, color = PurpleTextDeep)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)).background(Purple200).clickable { onNavigateToHistory(uid) }.padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("LIHAT RIWAYAT", fontWeight = FontWeight.ExtraBold, color = PurpleTextDeep)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Visibility, contentDescription = "View", tint = PurpleTextDeep, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

// --- Custom Composable Functions for UI Matching ---

@Composable
fun SaldoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        // Using Purple100 (Light Purple) for the main card
        colors = CardDefaults.cardColors(containerColor = Purple100),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .height(70.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PurpleTextDeep
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PurpleTextDeep
            )
        }
    }
}

@Composable
fun MiniSummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        // Using CardBg (White) to pop against the Purple50 background
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .height(80.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PurpleTextDeep
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PurpleTextDeep
            )
        }
    }
}

@Composable
fun MonthlyLineChart(monthlyData: List<MonthlyData>, modifier: Modifier = Modifier) {
    val lineColor = PurplePrimary // Main Brand Color for the line
    val areaColor = lineColor.copy(alpha = 0.2f)
    val pointColor = Color.White
    val labelColor = PurpleTextDeep // Readable labels
    val density = LocalDensity.current

    Canvas(modifier = modifier) {
        val dataPoints = monthlyData.map { it.amount }
        if (dataPoints.isEmpty()) return@Canvas

        val maxAmount = dataPoints.maxOrNull() ?: 1L
        val minAmount = dataPoints.minOrNull() ?: 0L
        val range = (maxAmount - minAmount).toFloat().coerceAtLeast(1f)

        val paddingHorizontal = 10.dp.toPx()
        val paddingVertical = 20.dp.toPx()
        val stepX = (size.width - 2 * paddingHorizontal) / (dataPoints.size - 1).coerceAtLeast(1)

        val points = dataPoints.mapIndexed { index, amount ->
            val x = paddingHorizontal + index * stepX
            val normalizedAmount = (amount - minAmount).toFloat() / range
            val y = size.height - paddingVertical - normalizedAmount * (size.height - 2 * paddingVertical)
            Offset(x, y)
        }

        // 1. Draw area gradient
        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points.first().x, size.height - paddingVertical)
                points.forEach { p -> lineTo(p.x, p.y) }
                lineTo(points.last().x, size.height - paddingVertical)
                close()
            }

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(areaColor, Color.Transparent),
                    startY = 0f,
                    endY = size.height
                )
            )
        }

        // 2. Draw the line
        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }

        // 3. Draw points and labels
        points.forEachIndexed { index, point ->
            drawCircle(
                color = lineColor,
                center = point,
                radius = 6f
            )
            drawCircle(
                color = pointColor,
                center = point,
                radius = 3f
            )

            with(density) {
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        monthlyData[index].month,
                        point.x,
                        size.height - 5.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = labelColor.toArgb()
                            textSize = 12.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PieChart(data: List<CategorySummary>, modifier: Modifier = Modifier) {
    val totalPercentage = data.sumOf { it.percentage.toDouble() }.toFloat()
    if (totalPercentage == 0f) return

    Canvas(modifier = modifier) {
        var startAngle = -90f
        data.forEach { item ->
            val sweepAngle = item.percentage / totalPercentage * 360f
            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                style = Fill
            )
            startAngle += sweepAngle
        }
    }
}