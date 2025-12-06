package com.example.kiptrack.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class DropdownItem(val id: String, val name: String)

data class ProfileAdminUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,

    // Field Input
    val nim: String = "",
    val nama: String = "",
    val jenjang: String = "",
    val semesterBerjalan: String = "",
    val namaWali: String = "",

    // Field Read-Only / Helper
    val selectedUnivId: String = "",
    val selectedUnivName: String = "",
    val selectedProdiId: String = "",
    val selectedProdiName: String = "",
    val photoProfile: String = "",

    val maxSemester: Int = 8,
    val currentDbSemester: Int = 0, // Semester asli di DB

    val universityOptions: List<DropdownItem> = emptyList(),
    val prodiOptions: List<DropdownItem> = emptyList()
)

class ProfileAdminViewModel(private val studentUid: String) : ViewModel() {

    var uiState by mutableStateOf(ProfileAdminUiState())
        private set

    private val db = FirebaseFirestore.getInstance()

    // Simpan Saldo & Pelanggaran Lama untuk perhitungan kenaikan kelas
    private var oldBalance: Long = 0L
    private var oldViolations: Long = 0L

    init {
        initialLoad()
    }

    fun onNameChange(v: String) { uiState = uiState.copy(nama = v) }
    fun onNimChange(v: String) { uiState = uiState.copy(nim = v) }
    fun onJenjangChange(v: String) { uiState = uiState.copy(jenjang = v) }
    fun onSemesterChange(v: String) { uiState = uiState.copy(semesterBerjalan = v) }
    fun onWaliChange(v: String) { uiState = uiState.copy(namaWali = v) }

    fun onUniversitySelected(univId: String, univName: String) {
        uiState = uiState.copy(selectedUnivId = univId, selectedUnivName = univName, selectedProdiId = "", selectedProdiName = "", prodiOptions = emptyList())
        fetchProdisForUniversity(univId)
    }
    fun onProdiSelected(prodiId: String, prodiName: String) {
        uiState = uiState.copy(selectedProdiId = prodiId, selectedProdiName = prodiName)
    }

    private fun initialLoad() = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)
        try {
            val univSnapshot = db.collection("universitas").get().await()
            val univList = univSnapshot.documents.map { DropdownItem(it.id, it.getString("nama_kampus") ?: "") }

            val doc = db.collection("users").document(studentUid).get().await()
            if (doc.exists()) {
                val data = doc.data ?: emptyMap()

                // Cache data keuangan lama
                oldBalance = (data["saldo_saat_ini"] as? Number)?.toLong() ?: 0L
                oldViolations = (data["total_penyalahgunaan"] as? Number)?.toLong() ?: 0L

                val currentUnivId = data["id_universitas"] as? String ?: ""
                val currentProdiId = data["id_prodi"] as? String ?: ""
                val currentSemester = (data["semester_berjalan"] as? Number)?.toInt() ?: 1
                val jenjang = data["jenjang"] as? String ?: "S1"
                val maxSem = if (jenjang.contains("D3", true)) 6 else 8

                uiState = uiState.copy(
                    nama = data["nama"] as? String ?: "",
                    nim = data["nim"] as? String ?: "",
                    jenjang = jenjang,
                    semesterBerjalan = currentSemester.toString(),
                    currentDbSemester = currentSemester,
                    maxSemester = maxSem,
                    namaWali = data["nama_wali"] as? String ?: "",
                    photoProfile = data["foto_profil"] as? String ?: "",
                    selectedUnivId = currentUnivId,
                    selectedUnivName = univList.find { it.id == currentUnivId }?.name ?: "",
                    selectedProdiId = currentProdiId,
                    universityOptions = univList
                )

                if (currentUnivId.isNotBlank()) {
                    fetchProdisForUniversity(currentUnivId, currentProdiId)
                } else {
                    uiState = uiState.copy(isLoading = false)
                }
            } else {
                uiState = uiState.copy(isLoading = false)
            }
        } catch (e: Exception) {
            uiState = uiState.copy(isLoading = false, errorMessage = e.message)
        }
    }

    private fun fetchProdisForUniversity(univId: String, preSelectedProdiId: String? = null) = viewModelScope.launch {
        try {
            val prodiSnapshot = db.collection("universitas").document(univId).collection("prodi").get().await()
            val prodiList = prodiSnapshot.documents.map { DropdownItem(it.id, it.getString("nama") ?: "") }

            var currentProdiName = uiState.selectedProdiName
            if (preSelectedProdiId != null) {
                currentProdiName = prodiList.find { it.id == preSelectedProdiId }?.name ?: ""
            }
            uiState = uiState.copy(prodiOptions = prodiList, selectedProdiName = currentProdiName, isLoading = false)
        } catch (e: Exception) { uiState = uiState.copy(isLoading = false) }
    }

    // --- LOGIKA SAVE + AUTO TOP UP ---
    fun saveChanges() = viewModelScope.launch {
        uiState = uiState.copy(isSaving = true)
        try {
            val newSemesterInt = uiState.semesterBerjalan.toIntOrNull() ?: 1

            // 1. Data Update Dasar
            val updates = mutableMapOf<String, Any>(
                "nama" to uiState.nama,
                "nim" to uiState.nim,
                "jenjang" to uiState.jenjang,
                "semester_berjalan" to newSemesterInt,
                "nama_wali" to uiState.namaWali,
                "id_universitas" to uiState.selectedUnivId,
                "id_prodi" to uiState.selectedProdiId
            )

            // 2. DETEKSI KENAIKAN SEMESTER
            if (newSemesterInt > uiState.currentDbSemester) {
                // Ambil data Cluster Universitas TERBARU (berjaga-jaga jika Univ diganti juga)
                val docUniv = db.collection("universitas").document(uiState.selectedUnivId).get().await()
                val clusterVal = docUniv.get("wilayah_klaster")?.toString() ?: "1"

                // Ambil Config Nominal
                val prodiId = uiState.selectedProdiId
                val isKedokteran = prodiId.contains("kedokteran", ignoreCase = true) && !prodiId.contains("non", ignoreCase = true)
                val configDocId = if (isKedokteran) "aturan_biaya_hidup_kedokteran" else "aturan_biaya_hidup_non-kedokteran"

                val docConfig = db.collection("konfigurasi").document(configDocId).get().await()
                val nominalBantuan = docConfig.getLong("klaster_$clusterVal") ?: 0L

                // HITUNG LOGIKA KEUANGAN
                // Uang Masuk = Jatah Cluster - Pelanggaran Semester Lalu
                val topUpAmount = (nominalBantuan - oldViolations).coerceAtLeast(0L)

                // Saldo Baru = Saldo Lama + Uang Masuk
                val newBalance = oldBalance + topUpAmount

                // Masukkan ke map update
                updates["saldo_saat_ini"] = newBalance
                updates["total_penyalahgunaan"] = 0 // Reset pelanggaran

                println("DEBUG: Naik Semester. Saldo Lama: $oldBalance + TopUp: $topUpAmount = $newBalance. Reset Pelanggaran.")
            }

            // 3. Kirim ke Firestore
            db.collection("users").document(studentUid)
                .update(updates)
                .await()

            uiState = uiState.copy(isSaving = false, isSuccess = true)

            // Refresh data agar UI lokal sinkron
            initialLoad()

        } catch (e: Exception) {
            uiState = uiState.copy(isSaving = false, errorMessage = "Gagal menyimpan: ${e.message}")
            e.printStackTrace()
        }
    }

    fun resetSuccess() { uiState = uiState.copy(isSuccess = false) }
}

class ProfileAdminViewModelFactory(private val studentUid: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileAdminViewModel(studentUid) as T
    }
}