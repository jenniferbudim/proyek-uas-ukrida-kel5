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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.West
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.data.CategorySummary
import com.example.kiptrack.ui.data.MonthlyData
import com.example.kiptrack.ui.theme.Purple300
import com.example.kiptrack.ui.theme.Purple50
import com.example.kiptrack.ui.viewmodel.DashboardWaliViewModel
import com.example.kiptrack.ui.viewmodel.DashboardWaliViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardWaliScreen(uid: String) {
    val viewModel: DashboardWaliViewModel = viewModel(
        factory = DashboardWaliViewModelFactory(uid)
    )
    val state = viewModel.uiState
    val scrollState = rememberScrollState()

    // Indonesian Rupiah formatter, removing .00
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    // Base color from reference image's background
    val WaliBackground = Color(0xFFE5CCF2) // A light, soft lavender/purple

    // Gradient Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaliBackground)
    ) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Purple300)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- HEADER SECTION ---
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    // Back/Logout Icon (as per image, looks like a back/exit icon)
                    Icon(
                        imageVector = Icons.Default.West,
                        contentDescription = "Back",
                        tint = Purple300,
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.CenterStart)
                            .clickable { /* Handle Back/Logout */ }
                    )

                    // Welcome Text
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Welcome, Wali!",
                            fontSize = 16.sp,
                            color = Purple300.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // User Name (Prominent)
                        Text(
                            text = state.username,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8E24AA) // Vibrant Purple
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // --- SALDO SAAT INI CARD ---
                SaldoCard(
                    title = "Saldo Saat Ini:",
                    value = formatter.format(state.currentBalance),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- TOTAL PENGELUARAN & PELANGGARAN CARDS ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Total Pengeluaran
                    MiniSummaryCard(
                        title = "Total Pengeluaran",
                        value = formatter.format(state.totalExpenditure),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    // Total Pelanggaran
                    MiniSummaryCard(
                        title = "Total Pelanggaran",
                        value = formatter.format(state.totalViolations),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- MONTHLY CHART CONTAINER ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(WaliBackground.copy(alpha = 0.8f)) // Match surrounding background
                        .padding(vertical = 16.dp)
                ) {
                    MonthlyLineChart(
                        monthlyData = state.monthlyExpenditure,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 30.dp)
                    )

                    // Chart Navigation Arrows
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Previous",
                        tint = Purple300.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.CenterStart)
                            .clickable {}
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next",
                        tint = Purple300.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.CenterEnd)
                            .clickable {}
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- CATEGORY SUMMARY ---
                Text(
                    text = "Rincian Kategori",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple300,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pie Chart
                    PieChart(
                        data = state.categorySummary,
                        modifier = Modifier.size(150.dp)
                    )

                    Spacer(modifier = Modifier.width(30.dp))

                    // Legend
                    Column(modifier = Modifier.weight(1f)) {
                        state.categorySummary.forEach { category ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(category.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = category.name,
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // --- LIHAT RIWAYAT BUTTON ---
                Button(
                    onClick = { /* Handle View History Click */ },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1C4E9)), // Lighter purple for button
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(60.dp)
                        .shadow(4.dp, RoundedCornerShape(50), ambientColor = Color.Black.copy(alpha = 0.2f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "LIHAT RIWAYAT",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF6A1B9A) // Darker text color
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "View",
                            tint = Color(0xFF6A1B9A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD1C4E9)), // Light purple color
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
                color = Color(0xFF6A1B9A) // Dark text
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF6A1B9A)
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFE6F7)), // Very light purple
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
                color = Color(0xFF6A1B9A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
        }
    }
}

@Composable
fun MonthlyLineChart(monthlyData: List<MonthlyData>, modifier: Modifier = Modifier) {
    val lineColor = Color(0xFF8E24AA) // Vibrant Purple for line
    val areaColor = lineColor.copy(alpha = 0.2f) // Light shade for area
    val pointColor = Color.White
    val labelColor = Color(0xFF6A1B9A) // Darker purple for labels
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
            // Y is normalized between padding top and bottom
            val y = size.height - paddingVertical - normalizedAmount * (size.height - 2 * paddingVertical)
            Offset(x, y)
        }

        // 1. Draw area gradient
        if (points.size > 1) {
            val path = Path().apply {
                // Start at bottom left of the first point
                moveTo(points.first().x, size.height - paddingVertical)

                // Draw line through all points
                points.forEach { p -> lineTo(p.x, p.y) }

                // Close the path to the bottom right of the last point
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
            // Draw point circle (Line color)
            drawCircle(
                color = lineColor,
                center = point,
                radius = 6f
            )
            // Draw inner circle (White)
            drawCircle(
                color = pointColor,
                center = point,
                radius = 3f
            )

            // Draw X-axis labels (Month)
            with(density) {
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        monthlyData[index].month,
                        point.x,
                        size.height - 5.dp.toPx(), // Position just above the bottom edge
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
        var startAngle = -90f // Start from the top
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