package com.example.kiptrack.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiptrack.ui.theme.PieRed
import com.example.kiptrack.ui.theme.Purple100
import com.example.kiptrack.ui.theme.Purple50
import com.example.kiptrack.ui.theme.PurplePrimary
import com.example.kiptrack.ui.theme.PurpleTextDeep
import com.example.kiptrack.ui.theme.SuccessGreen

@Composable
fun PerincianPengeluaranWaliScreen(
    uid: String,
    onBackToDashboard: () -> Unit
) {
    // Colors derived from the uploaded image
    val TextAmountWhite = Color.White

    // Vertical Gradient Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Purple50, PurplePrimary)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // Space for the bottom button
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Purple50) // The requested background color
                    .padding(bottom = 24.dp) // Optional: Adds padding at the bottom of the purple block
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                // --- HEADER: SALDO SAAT INI ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = Purple100),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(60.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saldo Saat Ini:",
                            color = PurpleTextDeep,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "1.000.000",
                            color = TextAmountWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- HEADER: EXPENSES & VIOLATIONS ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Total Pengeluaran
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Purple100)
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Total Pengeluaran",
                            color = PurpleTextDeep,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1.500.000",
                            color = TextAmountWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Total Pelanggaran
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp) // Matching height
                            .clip(RoundedCornerShape(12.dp))
                            .background(Purple100)
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Total Pelanggaran",
                            color = PurpleTextDeep,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "100.000",
                            color = TextAmountWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // --- TITLE: RIWAYAT PENGELUARAN ---
            Text(
                text = "Riwayat Pengeluaran",
                color = PurpleTextDeep,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, top = 20.dp)
            )

            // --- LIST OF HISTORY ITEMS ---
            // Manual items to match the screenshot exactly
            HistoryItemCard(
                month = "JAN", dateYear = "16/2025",
                amount = "Rp50.000", category = "Buku",
                isViolation = false, iconColor = SuccessGreen
            )
            HistoryItemCard(
                month = "JAN", dateYear = "16/2025",
                amount = "Rp20.000", category = "Alat Tulis",
                isViolation = true, iconColor = PieRed
            )
            HistoryItemCard(
                month = "JAN", dateYear = "16/2025",
                amount = "Rp50.000", category = "Buku",
                isViolation = false, iconColor = SuccessGreen
            )
            HistoryItemCard(
                month = "JAN", dateYear = "16/2025",
                amount = "Rp20.000", category = "Alat Tulis",
                isViolation = true, iconColor = PieRed
            )
            Spacer(modifier = Modifier.height(40.dp))
        }

        // --- BOTTOM NAVIGATION: KEMBALI KE DASHBOARD ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(Color(0xFFE1BEE7)) // Light Purple background for footer
                .clickable { onBackToDashboard() }
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = PurpleTextDeep,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "KEMBALI KE DASHBOARD",
                    color = PurpleTextDeep,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    month: String,
    dateYear: String,
    amount: String,
    category: String,
    isViolation: Boolean,
    iconColor: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Date and Amount
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = month,
                    color = Color(0xFF7B1FA2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    style = LocalTextStyle.current.copy(textDecoration = TextDecoration.Underline)
                )
                Text(
                    text = dateYear,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = amount,
                    color = Color(0xFF7B1FA2), // Purple amount
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Right Side: Icon and Category
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                Icon(
                    imageVector = if (isViolation) Icons.Default.WarningAmber else Icons.Default.CheckBox,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = category,
                    color = Color(0xFF7B1FA2),
                    fontSize = 14.sp
                )
            }
        }
    }
}