package com.example.kiptrack.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiptrack.ui.data.ExpenseCategories
import com.example.kiptrack.ui.theme.RefBgLightPurple
import com.example.kiptrack.ui.theme.RefButtonBack
import com.example.kiptrack.ui.theme.RefButtonSave
import com.example.kiptrack.ui.theme.RefDropdownBg
import com.example.kiptrack.ui.theme.RefHeaderPurple
import com.example.kiptrack.ui.theme.RefTextPurple

/**
 * Screen for students to log a new expense report.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogFormScreen(uid: String, onBackClicked: () -> Unit) {
    // State for form fields
    var dateInput by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }

    // Dropdown State
    var selectedCategory by remember { mutableStateOf<String?>(null) } // Start empty or null
    var isExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RefBgLightPurple)
    ) {
        // --- Top Header Section ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp) // Taller to accommodate the card overlap
                .background(RefHeaderPurple),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 50.dp)
            ) {
                Text(
                    "Total Saat Ini",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Rp 1.283.940",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Satu Juta Dua Ratus Delapan Puluh Tiga\nSembilan Ratus Empat Puluh Rupiah",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- Form Card Section ---
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp) // Overlap the header
                    .padding(horizontal = 16.dp)
                    // We constrain height so it doesn't go off screen, allowing inner scroll
                    .fillMaxHeight(0.95f),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tambah Laporan",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = RefTextPurple,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // 1. Tanggal Pengeluaran
                    FormLabelRef("Tanggal Pengeluaran :")
                    CustomTextFieldRef(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        placeholder = "DD / MM / YYYY",
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(Modifier.height(20.dp))

                    // 2. Kategori Pengeluaran (EXPOSED DROPDOWN MENU)
                    // Matches Reference: Purple background, drops from field
                    FormLabelRef("Kategori Pengeluaran :")

                    ExposedDropdownMenuBox(
                        expanded = isExpanded,
                        onExpandedChange = { isExpanded = !isExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // The Trigger Field
                        Box(
                            modifier = Modifier
                                .menuAnchor() // This binds the dropdown location
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(RefDropdownBg) // Purple background from reference
                                .clickable { isExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedCategory ?: "Pilih Kategori", // Placeholder logic
                                    color = if(selectedCategory == null) RefTextPurple.copy(alpha = 0.5f) else RefTextPurple,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = RefTextPurple
                                )
                            }
                        }

                        // The Dropdown List
                        ExposedDropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false },
                            modifier = Modifier
                                .background(RefDropdownBg) // Match background color
                                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                        ) {
                            ExpenseCategories.categories.forEachIndexed { index, category ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = category,
                                            color = RefTextPurple,
                                            fontSize = 14.sp
                                        )
                                    },
                                    onClick = {
                                        selectedCategory = category
                                        isExpanded = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                    modifier = Modifier.background(RefDropdownBg)
                                )
                                // Divider between items (except last)
                                if (index < ExpenseCategories.categories.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = RefTextPurple.copy(alpha = 0.2f),
                                        thickness = 1.dp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    // 3. Deskripsi Pengeluaran
                    FormLabelRef("Deskripsi Pengeluaran :")
                    CustomTextFieldRef(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Masukkan Deskripsi Pengeluaran"
                    )
                    Spacer(Modifier.height(20.dp))

                    // 4 & 5. Kuantitas & Harga Satuan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabelRef("Kuantitas :")
                            CustomTextFieldRef(
                                value = quantity,
                                onValueChange = { quantity = it },
                                placeholder = "Contoh: 2",
                                keyboardType = KeyboardType.Number
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormLabelRef("Harga Satuan :")
                            CustomTextFieldRef(
                                value = unitPrice,
                                onValueChange = { unitPrice = it },
                                placeholder = "Contoh: 15000",
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    // 6. Upload Bukti
                    FormLabelRef("Upload Bukti :")
                    UploadReceiptButtonRef()
                    Spacer(Modifier.height(40.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = onBackClicked,
                            colors = ButtonDefaults.buttonColors(containerColor = RefButtonBack),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .shadow(4.dp, RoundedCornerShape(50)),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("KEMBALI", color = RefTextPurple, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { /* Save */ },
                            colors = ButtonDefaults.buttonColors(containerColor = RefButtonSave),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .shadow(4.dp, RoundedCornerShape(50)),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("SIMPAN", color = RefTextPurple, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

// --- Helper Composables for the "Reference" Style ---

@Composable
fun FormLabelRef(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = RefTextPurple,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

/**
 * Text Field styled to match Reference 3 (White box, no border, soft shadow)
 */
@Composable
fun CustomTextFieldRef(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    // We use a BasicTextField inside a decorated Box to get the exact look
    // avoiding the forced styling of OutlinedTextField

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                placeholder,
                color = Color.Gray.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent, // Hide line
            unfocusedIndicatorColor = Color.Transparent, // Hide line
            disabledIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
    )
}

@Composable
fun UploadReceiptButtonRef() {
    Button(
        onClick = { /* Handle file selection */ },
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
    ) {
        Icon(
            imageVector = Icons.Filled.CloudUpload,
            contentDescription = "Upload",
            tint = RefTextPurple.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            "Pilih Foto dari Galeri",
            color = Color.Gray.copy(alpha = 0.6f),
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        )
        Spacer(Modifier.weight(1f)) // Push text to left
    }
}