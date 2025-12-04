package com.example.kiptrack.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiptrack.ui.data.ExpenseCategories
import com.example.kiptrack.ui.theme.*
import com.example.kiptrack.ui.utils.ImageUtils
import com.example.kiptrack.ui.utils.NumberUtils
import com.example.kiptrack.ui.viewmodel.LogFormViewModel
import com.example.kiptrack.ui.viewmodel.LogFormViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogFormScreen(uid: String, onBackClicked: () -> Unit) {
    val viewModel: LogFormViewModel = viewModel(
        factory = LogFormViewModelFactory(uid)
    )
    val state = viewModel.uiState
    val context = LocalContext.current

    // --- Date Picker Logic ---
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val formatter = SimpleDateFormat("MMM dd/yyyy", Locale.US)
                            val formattedDate = formatter.format(Date(selectedMillis)).uppercase()
                            viewModel.onDateChange(formattedDate)
                        }
                        showDatePicker = false
                    }
                ) { Text("OK", color = PurpleDark) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = PurpleDark) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val base64 = ImageUtils.uriToBase64(context, it)
            if (base64 != null) {
                viewModel.onPhotoSelected(base64)
            }
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            Toast.makeText(context, "Laporan Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
            onBackClicked()
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    val scrollState = rememberScrollState()
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Purple50)
    ) {
        // --- Header Section ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(PurplePrimary),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 50.dp)
            ) {
                Text("Total Saat Ini", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = NumberUtils.formatRupiah(state.totalCalculated),
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.totalSpelled,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
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
                    .offset(y = (-40).dp)
                    .padding(horizontal = 16.dp)
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
                        color = PurpleDark,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // 1. Tanggal Pengeluaran
                    FormLabelRef("Tanggal Pengeluaran :")
                    Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                        CustomTextFieldRef(
                            value = state.dateInput,
                            onValueChange = {},
                            placeholder = "Pilih Tanggal",
                            readOnly = true,
                            trailingIcon = {
                                Icon(Icons.Default.CalendarToday, null, tint = PurpleDark)
                            }
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                    }
                    Spacer(Modifier.height(20.dp))

                    // 2. Kategori
                    FormLabelRef("Kategori Pengeluaran :")
                    ExposedDropdownMenuBox(
                        expanded = isExpanded,
                        onExpandedChange = { isExpanded = !isExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Purple100)
                                .clickable { isExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = state.category ?: "Pilih Kategori",
                                    color = if(state.category == null) PurpleDark.copy(alpha = 0.5f) else PurpleDark,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = PurpleDark
                                )
                            }
                        }

                        ExposedDropdownMenu(
                            expanded = isExpanded,
                            onDismissRequest = { isExpanded = false },
                            modifier = Modifier.background(Purple100)
                        ) {
                            ExpenseCategories.categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category, color = PurpleDark, fontSize = 14.sp) },
                                    onClick = {
                                        viewModel.onCategoryChange(category)
                                        isExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    // 3. Deskripsi
                    FormLabelRef("Deskripsi Pengeluaran :")
                    CustomTextFieldRef(
                        value = state.description,
                        onValueChange = viewModel::onDescriptionChange,
                        placeholder = "Masukkan Deskripsi"
                    )
                    Spacer(Modifier.height(20.dp))

                    // 4 & 5. Kuantitas & Harga
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabelRef("Kuantitas :")
                            CustomTextFieldRef(
                                value = state.quantity,
                                onValueChange = viewModel::onQuantityChange,
                                placeholder = "Contoh: 1",
                                keyboardType = KeyboardType.Number
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabelRef("Harga Satuan :")
                            CustomTextFieldRef(
                                value = state.unitPrice,
                                onValueChange = viewModel::onUnitPriceChange,
                                placeholder = "Contoh: 15000",
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    // 6. Upload Bukti
                    FormLabelRef("Upload Bukti :")
                    Button(
                        onClick = { launcher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = if (state.photoBase64.isNotBlank()) Purple100 else Color.White),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Filled.CloudUpload, null, tint = PurpleDark.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (state.photoBase64.isNotBlank()) "Foto Terpilih (Ganti?)" else "Pilih Foto dari Galeri",
                            color = if (state.photoBase64.isNotBlank()) PurpleDark else Color.Gray.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }

                    // --- [BARU] PREVIEW GAMBAR ---
                    if (state.photoBase64.isNotBlank()) {
                        Spacer(Modifier.height(16.dp))
                        val bitmap = ImageUtils.base64ToBitmap(state.photoBase64)
                        if (bitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp) // Tinggi Preview
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Purple100, RoundedCornerShape(12.dp))
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Preview Bukti",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(40.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = onBackClicked,
                            colors = ButtonDefaults.buttonColors(containerColor = Purple100),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f).shadow(4.dp, RoundedCornerShape(50)),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("KEMBALI", color = PurpleDark, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.submitReport() },
                            colors = ButtonDefaults.buttonColors(containerColor = Purple200),
                            shape = RoundedCornerShape(50),
                            enabled = !state.isLoading,
                            modifier = Modifier.weight(1f).shadow(4.dp, RoundedCornerShape(50)),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(color = PurpleDark, modifier = Modifier.size(24.dp))
                            } else {
                                Text("SIMPAN", color = PurpleDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

// --- Helper Composables ---

@Composable
fun FormLabelRef(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = PurpleDark,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    )
}

@Composable
fun CustomTextFieldRef(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.5f), fontSize = 14.sp) },
        trailingIcon = trailingIcon,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp))
    )
}

@Composable
fun UploadReceiptButtonRef() {
    Button(
        onClick = { /* Handle file selection */ },
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(12.dp))
    ) {
        Icon(Icons.Filled.CloudUpload, null, tint = PurpleDark.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            "Pilih Foto dari Galeri",
            color = Color.Gray.copy(alpha = 0.6f),
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        )
        Spacer(Modifier.weight(1f))
    }
}