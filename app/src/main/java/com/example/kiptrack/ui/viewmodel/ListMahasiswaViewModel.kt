package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MahasiswaItem(val uid: String, val nama: String, val nim: String)

// State untuk Form Tambah
data class AddStudentUiState(
    val isLoading: Boolean = false,
    val step: Int = 0, // 0=Idle, 1=Form Mhs, 2=Form Wali
    val error: String? = null,

    // Auto-Filled Data
    val autoJenjang: String = "",
    val autoClusterNominal: Long = 0L,

    // Input Mahasiswa
    val mhsNama: String = "",
    val mhsEmail: String = "",
    val mhsPassword: String = "",
    val mhsNim: String = "",
    val mhsSemester: String = "1",
    val mhsFoto: String = "",

    // Input Wali
    val waliNama: String = "",
    val waliEmail: String = "",
    val waliPassword: String = "",
    val waliId: String = ""
)

class ListMahasiswaViewModel(private val universityId: String, private val prodiId: String) : ViewModel() {

    // State List (Lama)
    var uiState by mutableStateOf(ListMahasiswaUiState())
        private set

    // State Form (Baru)
    var formState by mutableStateOf(AddStudentUiState())
        private set

    private val db = FirebaseFirestore.getInstance()

    init {
        fetchData()
    }

    private fun fetchData() {
        // 1. Ambil Nama Prodi
        db.collection("universitas").document(universityId)
            .collection("prodi").document(prodiId).get()
            .addOnSuccessListener {
                val name = it.getString("nama") ?: "Prodi"
                val jenjang = it.getString("jenjang") ?: "S1"

                uiState = uiState.copy(prodiName = name)
                formState = formState.copy(autoJenjang = jenjang)

                // Setelah dapat prodi, kita cari nominal cluster
                fetchClusterInfo(jenjang)
            }

        // 2. Ambil List Mahasiswa
        loadStudents()
    }

    private fun loadStudents() {
        db.collection("users")
            .whereEqualTo("role", "mahasiswa")
            .whereEqualTo("id_universitas", universityId)
            .whereEqualTo("id_prodi", prodiId)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    MahasiswaItem(
                        uid = doc.id,
                        nama = doc.getString("nama") ?: "Tanpa Nama",
                        nim = doc.getString("nim") ?: "-"
                    )
                }
                uiState = uiState.copy(mahasiswaList = list, isLoading = false)
            }
    }

    // Ambil Data Cluster untuk Saldo Awal
    private fun fetchClusterInfo(jenjang: String) = viewModelScope.launch {
        try {
            val docUniv = db.collection("universitas").document(universityId).get().await()
            val clusterVal = docUniv.get("wilayah_klaster")?.toString() ?: "1"

            // Cek kategori prodi dari ID atau nama
            val isKedokteran = prodiId.contains("kedokteran", ignoreCase = true) && !prodiId.contains("non", ignoreCase = true)
            val configId = if (isKedokteran) "aturan_biaya_hidup_kedokteran" else "aturan_biaya_hidup_non-kedokteran"

            val docConfig = db.collection("konfigurasi").document(configId).get().await()
            val nominal = docConfig.getLong("klaster_$clusterVal") ?: 0L

            formState = formState.copy(autoClusterNominal = nominal)
        } catch (e: Exception) {
            println("Gagal ambil cluster: ${e.message}")
        }
    }

    // --- FORM HANDLERS ---
    fun openAddDialog() { formState = formState.copy(step = 1, error = null) }
    fun closeAddDialog() { formState = AddStudentUiState() } // Reset

    fun updateMhsInput(nama: String, email: String, pass: String, nim: String, sem: String, foto: String) {
        formState = formState.copy(mhsNama = nama, mhsEmail = email, mhsPassword = pass, mhsNim = nim, mhsSemester = sem, mhsFoto = foto)
    }

    fun goToWaliForm() {
        if (formState.mhsEmail.isBlank() || formState.mhsPassword.isBlank()) {
            formState = formState.copy(error = "Email & Password Mahasiswa Wajib Diisi")
            return
        }
        formState = formState.copy(step = 2, error = null)
    }

    fun updateWaliInput(nama: String, email: String, pass: String, idWali: String) {
        formState = formState.copy(waliNama = nama, waliEmail = email, waliPassword = pass, waliId = idWali)
    }

    // --- CORE LOGIC: CREATE 2 USERS ---
    fun submitAllData() = viewModelScope.launch {
        formState = formState.copy(isLoading = true, error = null)

        // Kita butuh membuat user tanpa logout Admin.
        // Triknya: Buat instance FirebaseApp sementara.
        val appName = "SecondaryApp_${System.currentTimeMillis()}"
        var secondaryApp: FirebaseApp? = null

        try {
            val currentOptions = FirebaseApp.getInstance().options
            secondaryApp = FirebaseApp.initializeApp(FirebaseApp.getInstance().applicationContext, currentOptions, appName)
            val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)

            // 1. Buat Auth Mahasiswa
            val mhsAuthResult = secondaryAuth.createUserWithEmailAndPassword(formState.mhsEmail, formState.mhsPassword).await()
            val mhsUid = mhsAuthResult.user?.uid ?: throw Exception("Gagal buat UID Mahasiswa")

            // 2. Simpan Data Mahasiswa ke Firestore
            val mhsData = hashMapOf(
                "nama" to formState.mhsNama,
                "email" to formState.mhsEmail,
                "nim" to formState.mhsNim,
                "role" to "mahasiswa",
                "id_universitas" to universityId,
                "id_prodi" to prodiId,
                "jenjang" to formState.autoJenjang,
                "semester_berjalan" to (formState.mhsSemester.toIntOrNull() ?: 1),
                "foto_profil" to formState.mhsFoto,
                "saldo_saat_ini" to formState.autoClusterNominal,
                "total_penyalahgunaan" to 0,
                "nama_wali" to formState.waliNama,
                "last_update_period" to "" // Belum ada update semester otomatis
            )
            db.collection("users").document(mhsUid).set(mhsData).await()

            // Logout dari secondary auth agar bisa buat user lagi (wali)
            secondaryAuth.signOut()

            // 3. Buat Auth Wali
            val waliAuthResult = secondaryAuth.createUserWithEmailAndPassword(formState.waliEmail, formState.waliPassword).await()
            val waliUid = waliAuthResult.user?.uid ?: throw Exception("Gagal buat UID Wali")

            // 4. Simpan Data Wali ke Firestore
            val waliData = hashMapOf(
                "nama" to formState.waliNama,
                "email" to formState.waliEmail,
                "id_wali" to formState.waliId,
                "role" to "wali",
                "id_mahasiswa" to mhsUid // LINKING KE MAHASISWA
            )
            db.collection("users").document(waliUid).set(waliData).await()

            // SUKSES
            formState = AddStudentUiState() // Reset form & tutup dialog
            loadStudents() // Refresh list

        } catch (e: Exception) {
            formState = formState.copy(isLoading = false, error = "Gagal: ${e.message}")
        } finally {
            // Bersihkan instance app sementara
            secondaryApp?.delete()
        }
    }
}

// --- UI STATE LAMA (Tetap dipertahankan) ---
data class ListMahasiswaUiState(
    val isLoading: Boolean = true,
    val prodiName: String = "Daftar Mahasiswa",
    val mahasiswaList: List<MahasiswaItem> = emptyList()
)

class ListMahasiswaViewModelFactory(private val uId: String, private val pId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ListMahasiswaViewModel(uId, pId) as T
    }
}