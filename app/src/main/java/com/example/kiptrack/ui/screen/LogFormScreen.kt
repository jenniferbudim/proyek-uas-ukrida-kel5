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
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
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
                            val formatter = SimpleDateFormat("dd / MM / yyyy", Locale.US)
                            val formattedDate = formatter.format(Date(selectedMillis))
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
                .height(240.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Purple300, PurplePrimary)
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = "Total Saat Ini",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = NumberUtils.formatRupiah(state.totalCalculated),
                    color = Color.White,
                    fontSize = 40.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.totalSpelled,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 30.dp)
                )
            }
        }

        // --- Form Card Section ---
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-60).dp)
                    .padding(horizontal = 20.dp)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tambah Laporan",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = PurpleDark,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // 1. Tanggal Pengeluaran
                    FormLabelRef("Tanggal Pengeluaran :")
                    Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                        CustomTextFieldRef(
                            value = state.dateInput,
                            onValueChange = {},
                            placeholder = "DD / MM / YYYY",
                            readOnly = true
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                    }
                    Spacer(Modifier.height(16.dp))

                    // 2. Kategori (UPDATED TO MATCH IMAGE EXACTLY)
                    FormLabelRef("Kategori Pengeluaran :")

                    // Container for the whole dropdown block
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Purple100) // Solid Light Purple Background
                    ) {
                        // Header (Always Visible)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.category.isNullOrEmpty()) "Pilih Kategori" else state.category,
                                color = PurpleDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = PurpleDark
                            )
                        }

                        // Expanded List (Accordion style - pushes content down)
                        if (isExpanded) {
                            Divider(color = PurpleDark.copy(alpha = 0.1f), thickness = 1.dp)

                            ExpenseCategories.categories.forEachIndexed { index, category ->
                                val isSelected = state.category == category

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.onCategoryChange(category)
                                            isExpanded = false
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            // Conditional Border for Selected Item (The "Hunian" look)
                                            .border(
                                                width = if (isSelected) 1.dp else 0.dp,
                                                color = if (isSelected) PurpleDark else Color.Transparent,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(vertical = 8.dp, horizontal = 4.dp)
                                    ) {
                                        Text(
                                            text = category,
                                            color = PurpleDark,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                    }

                                    // Separator line (except for last item)
                                    if (index < ExpenseCategories.categories.lastIndex) {
                                        Divider(
                                            color = PurpleDark.copy(alpha = 0.1f),
                                            thickness = 1.dp,
                                            modifier = Modifier.align(Alignment.BottomCenter)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 3. Deskripsi
                    FormLabelRef("Deskripsi Pengeluaran :")
                    CustomTextFieldRef(
                        value = state.description,
                        onValueChange = viewModel::onDescriptionChange,
                        placeholder = "Masukkan Deskripsi Pengeluaran"
                    )
                    Spacer(Modifier.height(16.dp))

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
                                placeholder = "Contoh : 2",
                                keyboardType = KeyboardType.Number
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabelRef("Harga Satuan :")
                            CustomTextFieldRef(
                                value = state.unitPrice,
                                onValueChange = viewModel::onUnitPriceChange,
                                placeholder = "Contoh : 15000",
                                keyboardType = KeyboardType.Number
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // 6. Upload Bukti
                    FormLabelRef("Upload Bukti :")
                    Button(
                        onClick = { launcher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Purple100, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Filled.CloudUpload,
                            null,
                            tint = PurpleDark.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (state.photoBase64.isNotBlank()) "Foto Terpilih" else "Pilih Foto dari Galeri",
                            color = PurpleDark.copy(alpha = 0.4f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(Modifier.weight(1f))
                    }

                    // --- PREVIEW GAMBAR ---
                    if (state.photoBase64.isNotBlank()) {
                        Spacer(Modifier.height(16.dp))
                        val bitmap = ImageUtils.base64ToBitmap(state.photoBase64)
                        if (bitmap != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Purple100, RoundedCornerShape(8.dp))
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

                    Spacer(Modifier.height(30.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = onBackClicked,
                            colors = ButtonDefaults.buttonColors(containerColor = Purple100),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("KEMBALI", color = PurpleDark, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                        }

                        Button(
                            onClick = { viewModel.submitReport() },
                            colors = ButtonDefaults.buttonColors(containerColor = Purple200),
                            shape = RoundedCornerShape(50),
                            enabled = !state.isLoading,
                            modifier = Modifier.weight(1f),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(color = PurpleDark, modifier = Modifier.size(24.dp))
                            } else {
                                Text("SIMPAN", color = PurpleDark, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                            }
                        }
                    }
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun FormLabelRef(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontFamily = FontFamily.Serif,
        color = PurpleDark,
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
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
        placeholder = {
            Text(
                placeholder,
                color = PurpleDark.copy(alpha = 0.25f),
                fontSize = 14.sp
            )
        },
        trailingIcon = trailingIcon,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = PurpleDark,
            unfocusedTextColor = PurpleDark
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Purple100, RoundedCornerShape(8.dp))
    )
}