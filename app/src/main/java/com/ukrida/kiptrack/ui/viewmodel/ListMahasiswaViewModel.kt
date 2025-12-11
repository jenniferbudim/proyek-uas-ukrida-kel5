package com.ukrida.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MahasiswaItem(val uid: String, val nama: String, val nim: String)

data class AddStudentUiState(
    val isLoading: Boolean = false,
    val step: Int = 0,
    val error: String? = null,
    val autoJenjang: String = "",
    val autoClusterNominal: Long = 0L,
    val mhsNama: String = "",
    val mhsEmail: String = "",
    val mhsPassword: String = "",
    val mhsNim: String = "",
    val mhsSemester: String = "1",
    val mhsFoto: String = "",
    val waliNama: String = "",
    val waliEmail: String = "",
    val waliPassword: String = "",
    val waliId: String = ""
)

// State untuk UI List
data class ListMahasiswaUiState(
    val isLoading: Boolean = true,
    val prodiName: String = "Daftar Mahasiswa",
    val mahasiswaList: List<MahasiswaItem> = emptyList(),
    val deleteError: String? = null,
    val deleteSuccess: String? = null
)

class ListMahasiswaViewModel(private val universityId: String, private val prodiId: String) : ViewModel() {

    var uiState by mutableStateOf(ListMahasiswaUiState())
        private set

    var formState by mutableStateOf(AddStudentUiState())
        private set

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        fetchData()
    }

    private fun fetchData() {
        db.collection("universitas").document(universityId)
            .collection("prodi").document(prodiId).get()
            .addOnSuccessListener {
                val name = it.getString("nama") ?: "Prodi"
                val jenjang = it.getString("jenjang") ?: "S1"
                uiState = uiState.copy(prodiName = name)
                formState = formState.copy(autoJenjang = jenjang)
                fetchClusterInfo(jenjang)
            }
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

    private fun fetchClusterInfo(jenjang: String) = viewModelScope.launch {
        try {
            val docUniv = db.collection("universitas").document(universityId).get().await()
            val clusterVal = docUniv.get("wilayah_klaster")?.toString() ?: "1"
            val isKedokteran = prodiId.contains("kedokteran", ignoreCase = true) && !prodiId.contains("non", ignoreCase = true)
            val configId = if (isKedokteran) "aturan_biaya_hidup_kedokteran" else "aturan_biaya_hidup_non-kedokteran"
            val docConfig = db.collection("konfigurasi").document(configId).get().await()
            val nominal = docConfig.getLong("klaster_$clusterVal") ?: 0L
            formState = formState.copy(autoClusterNominal = nominal)
        } catch (e: Exception) { }
    }

    // --- FORM HANDLERS ---
    fun openAddDialog() { formState = formState.copy(step = 1, error = null) }
    fun closeAddDialog() { formState = AddStudentUiState() }
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

    // --- CREATE USERS ---
    fun submitAllData() = viewModelScope.launch {
        formState = formState.copy(isLoading = true, error = null)
        val appName = "SecondaryApp_${System.currentTimeMillis()}"
        var secondaryApp: FirebaseApp? = null
        try {
            val currentOptions = FirebaseApp.getInstance().options
            secondaryApp = FirebaseApp.initializeApp(FirebaseApp.getInstance().applicationContext, currentOptions, appName)
            val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)

            // Mhs
            val mhsAuthResult = secondaryAuth.createUserWithEmailAndPassword(formState.mhsEmail, formState.mhsPassword).await()
            val mhsUid = mhsAuthResult.user?.uid ?: throw Exception("Gagal buat UID Mahasiswa")
            val mhsData = hashMapOf(
                "nama" to formState.mhsNama, "email" to formState.mhsEmail, "nim" to formState.mhsNim,
                "role" to "mahasiswa", "id_universitas" to universityId, "id_prodi" to prodiId,
                "jenjang" to formState.autoJenjang, "semester_berjalan" to (formState.mhsSemester.toIntOrNull() ?: 1),
                "foto_profil" to formState.mhsFoto, "saldo_saat_ini" to formState.autoClusterNominal,
                "total_penyalahgunaan" to 0, "nama_wali" to formState.waliNama, "last_update_period" to ""
            )
            db.collection("users").document(mhsUid).set(mhsData).await()
            secondaryAuth.signOut()

            // Wali
            val waliAuthResult = secondaryAuth.createUserWithEmailAndPassword(formState.waliEmail, formState.waliPassword).await()
            val waliUid = waliAuthResult.user?.uid ?: throw Exception("Gagal buat UID Wali")
            val waliData = hashMapOf(
                "nama" to formState.waliNama, "email" to formState.waliEmail, "id_wali" to formState.waliId,
                "role" to "wali", "id_mahasiswa" to mhsUid
            )
            db.collection("users").document(waliUid).set(waliData).await()

            formState = AddStudentUiState()
            loadStudents()
        } catch (e: Exception) {
            formState = formState.copy(isLoading = false, error = "Gagal: ${e.message}")
        } finally {
            secondaryApp?.delete()
        }
    }

    // --- HAPUS MAHASISWA & WALI (DENGAN PASSWORD ADMIN) ---
    fun deleteStudentWithAuth(studentUid: String, adminPassword: String) = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)
        val adminUser = auth.currentUser

        if (adminUser == null || adminUser.email == null) {
            uiState = uiState.copy(isLoading = false, deleteError = "Admin tidak terautentikasi")
            return@launch
        }

        try {
            // 1. Re-Authenticate Admin (Cek Password)
            val credential = EmailAuthProvider.getCredential(adminUser.email!!, adminPassword)
            adminUser.reauthenticate(credential).await()

            // 2. Cari & Hapus Wali
            val waliSnapshot = db.collection("users")
                .whereEqualTo("role", "wali")
                .whereEqualTo("id_mahasiswa", studentUid)
                .get().await()

            for (doc in waliSnapshot.documents) {
                db.collection("users").document(doc.id).delete().await()
                // Catatan: Auth Wali tidak bisa dihapus dari Client SDK tanpa login sebagai Wali
            }

            // 3. Hapus Mahasiswa
            db.collection("users").document(studentUid).delete().await()
            // Catatan: Auth Mahasiswa tidak bisa dihapus dari Client SDK tanpa login sebagai Mahasiswa

            // 4. Refresh & Success
            uiState = uiState.copy(deleteSuccess = "Data Mahasiswa & Wali berhasil dihapus", isLoading = false)
            loadStudents()

        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false, deleteError = "Gagal Hapus: Password salah atau error jaringan")
        }
    }

    fun resetDeleteState() {
        uiState = uiState.copy(deleteError = null, deleteSuccess = null)
    }
}

class ListMahasiswaViewModelFactory(private val uId: String, private val pId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ListMahasiswaViewModel(uId, pId) as T
    }
}