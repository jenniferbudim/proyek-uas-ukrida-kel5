package com.example.kiptrack.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
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

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Purple50)
    ) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PurplePrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- HEADER ---
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logout Icon (Left)
                    Icon(
                        imageVector = Icons.Outlined.ExitToApp,
                        contentDescription = "Logout",
                        tint = PurplePrimary,
                        modifier = Modifier
                            .size(28.dp)
                            .scale(scaleX = -1f, scaleY = 1f)
                            .align(Alignment.CenterVertically)
                            .clickable { onLogoutClicked() }
                    )

                    // Centered Text
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Selamat Datang, ${state.username}!",
                            fontSize = 16.sp,
                            color = PurpleTextDeep,
                            fontWeight = FontWeight.Medium,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = state.studentName,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurpleDark,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif
                        )
                    }
                    Box(modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- SALDO CARD ---
                SaldoCard(
                    title = "Saldo Saat Ini:",
                    value = "Rp ${formatter.format(state.currentBalance)}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- SUMMARY CARDS ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniSummaryCard(
                        "Total Pengeluaran",
                        "Rp ${formatter.format(state.totalExpenditure)}",
                        Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    MiniSummaryCard(
                        "Total Pelanggaran",
                        "Rp ${formatter.format(state.totalViolations)}",
                        Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- CHART SECTION  ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(
                            horizontal = 24.dp,
                            vertical = 32.dp
                        )
                ) {
                    // 1. Year Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.previousYear() }) {
                            Icon(Icons.Default.ChevronLeft, "Prev", tint = PurpleDark, modifier = Modifier.size(32.dp))
                        }
                        Text(
                            text = state.selectedYear.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurpleTextDeep
                        )
                        IconButton(onClick = { viewModel.nextYear() }) {
                            Icon(Icons.Default.ChevronRight, "Next", tint = PurpleDark, modifier = Modifier.size(32.dp))
                        }
                    }

                    // 2. Chart Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .background(Purple50, RoundedCornerShape(12.dp))
                            .border(1.dp, Purple200.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        MonthlyLineChart(
                            monthlyData = state.monthlyExpenditure,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- CATEGORY PIE CHART ---
                Text(
                    "Rincian Kategori",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. CHART (First Row)
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.categorySummary.isNotEmpty()) {
                            PieChart(state.categorySummary, Modifier.size(160.dp))
                        } else {
                            Box(
                                modifier = Modifier.size(160.dp)
                                    .background(Color.LightGray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No Data", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. LEGEND (Second Row)
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            state.categorySummary.forEach { category ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(
                                                category.color,
                                                shape = RoundedCornerShape(3.dp)
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = "${category.name} (${category.percentage.toInt()}%)",
                                        fontSize = 14.sp,
                                        color = PurpleTextDeep,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // --- BOTTOM BUTTON ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                        .background(Purple200)
                        .clickable { onNavigateToHistory(uid) }
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "LIHAT RIWAYAT",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurpleTextDeep,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "View",
                            tint = PurpleTextDeep,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- REFINED COMPONENTS ---
@Composable
fun SaldoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Purple100),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
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
                fontWeight = FontWeight.Bold,
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .height(90.dp)
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
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = PurpleTextDeep,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
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
    val lineColor = PurplePrimary
    val areaColor = lineColor.copy(alpha = 0.1f)
    val pointColor = Color.White
    val labelColor = PurpleTextDeep
    val gridColor = Purple300.copy(alpha = 0.6f)
    val density = LocalDensity.current

    Canvas(modifier = modifier) {
        val dataPoints = monthlyData.map { it.amount }
        if (dataPoints.isEmpty()) return@Canvas

        val maxAmount = dataPoints.maxOrNull() ?: 1L
        val minAmount = dataPoints.minOrNull() ?: 0L
        val range = (maxAmount - minAmount).toFloat().coerceAtLeast(1f)

        // Padding config
        val paddingHorizontal = 24.dp.toPx()
        val paddingTop = 24.dp.toPx()
        val paddingBottom = 32.dp.toPx()

        val chartHeight = size.height - paddingTop - paddingBottom
        val chartWidth = size.width - 2 * paddingHorizontal
        val stepX = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)

        val points = dataPoints.mapIndexed { index, amount ->
            val x = paddingHorizontal + index * stepX
            val normalizedAmount = (amount - minAmount).toFloat() / range
            val y = paddingTop + (chartHeight - (normalizedAmount * chartHeight))
            Offset(x, y)
        }

        // 1. Vertical Grid Lines
        points.forEach { point ->
            drawLine(
                color = gridColor,
                start = Offset(point.x, paddingTop),
                end = Offset(point.x, size.height - paddingBottom),
                strokeWidth = 2f
            )
        }

        // 2. Horizontal Base Line (Dotted style option or solid)
        drawLine(
            color = gridColor,
            start = Offset(paddingHorizontal, size.height - paddingBottom),
            end = Offset(size.width - paddingHorizontal, size.height - paddingBottom),
            strokeWidth = 2f
        )

        val midY = paddingTop + (chartHeight / 2)
        drawLine(
            color = gridColor,
            start = Offset(paddingHorizontal, midY),
            end = Offset(size.width - paddingHorizontal, midY),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        // 3. Draw Area (Gradient fill)
        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points.first().x, size.height - paddingBottom)
                points.forEach { p -> lineTo(p.x, p.y) }
                lineTo(points.last().x, size.height - paddingBottom)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(areaColor, Color.Transparent),
                    startY = paddingTop,
                    endY = size.height - paddingBottom
                )
            )
        }

        // 4. Draw Line
        if (points.size > 1) {
            drawPath(
                path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                },
                color = lineColor,
                style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // 5. Draw Points & Labels
        points.forEachIndexed { index, point ->
            drawCircle(color = lineColor, center = point, radius = 6f)
            drawCircle(color = pointColor, center = point, radius = 3f)

            with(density) {
                drawContext.canvas.nativeCanvas.apply {
                    val textPaint = android.graphics.Paint().apply {
                        color = labelColor.toArgb()
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                    }
                    val monthLabel = monthlyData[index].month.take(3).uppercase()

                    // Draw Month Label
                    drawText(monthLabel, point.x, size.height - 8f, textPaint)
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