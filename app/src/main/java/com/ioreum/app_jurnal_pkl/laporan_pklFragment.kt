package com.ioreum.app_jurnal_pkl

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import org.json.JSONArray
import org.json.JSONObject

class laporan_pklFragment : Fragment() {

    private lateinit var tableLaporan: TableLayout
    private lateinit var btnTambah: Button
    private lateinit var etCari: EditText

    private val urlTampil = "http://172.16.100.6/jurnal_pkl/tampil_laporan.php"
    private val urlTambah = "http://172.16.100.6/jurnal_pkl/tambah_laporan.php"
    private val urlHapus = "http://172.16.100.6/jurnal_pkl/hapus_laporan.php"
    private val urlUbah = "http://172.16.100.6/jurnal_pkl/ubah_laporan.php"
    private val urlSiswa = "http://172.16.100.6/jurnal_pkl/tampil_siswa.php"
    private val baseFileUrl = "http://172.16.100.6/jurnal_pkl/uploads/"

    private val siswaList = mutableListOf<Siswa>()
    private lateinit var selectedNis: String

    private var fileLaporanUri: Uri? = null
    private var fileProjectUri: Uri? = null

    private lateinit var tvNamaFileLaporan: TextView
    private lateinit var tvNamaFileProject: TextView

    private val fileLaporanPicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        fileLaporanUri = it
        tvNamaFileLaporan.text = it?.lastPathSegment ?: "File laporan dipilih"
    }

    private val fileProjectPicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        fileProjectUri = it
        tvNamaFileProject.text = it?.lastPathSegment ?: "File project dipilih"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_laporan_pkl, container, false)
        tableLaporan = view.findViewById(R.id.tableLaporan)
        btnTambah = view.findViewById(R.id.btnTambahLaporan)
        etCari = view.findViewById(R.id.etCariLaporan)

        btnTambah.setOnClickListener { showTambahBottomSheet() }
        tampilkanLaporan()

        etCari.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                tampilkanLaporan(s.toString())
            }
        })
        return view
    }

    private fun tampilkanLaporan(filterNIS: String = "") {
        val req = StringRequest(urlTampil,
            { resp ->
                val arr = JSONArray(resp)
                tableLaporan.removeAllViews()

                val header = TableRow(requireContext())
                arrayOf("No", "NIS", "Nama", "Asal Sekolah", "File Laporan", "File Project", "Nilai Akhir PKL", "Aksi")
                    .forEach { title ->
                        TextView(requireContext()).apply {
                            text = title
                            setPadding(12, 40, 12, 40)
                            gravity = Gravity.CENTER
                            textAlignment = View.TEXT_ALIGNMENT_CENTER
                            setTextColor(0xFF000000.toInt())
                        }.let(header::addView)
                    }
                tableLaporan.addView(header)

                var no = 1
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val nis = obj.getString("nis")
                    val nama = obj.getString("nama_siswa")
                    val sekolah = obj.getString("asal_sekolah")
                    val nilai = obj.getString("nilai_akhir_pkl")
                    val gabungan = "$nis $nama $sekolah $nilai".lowercase()


                    if (filterNIS.lowercase() in gabungan) {
                        val row = TableRow(requireContext())
                        val fileLaporan = obj.getString("file_laporan")
                        val fileProject = obj.getString("file_project")
                        val fileLaporanUrl = baseFileUrl + fileLaporan
                        val fileProjectUrl = baseFileUrl + fileProject

                        val cells = listOf(
                            (no++).toString(),
                            nis,
                            nama,
                            sekolah,
                            fileLaporan,
                            fileProject,
                            obj.getString("nilai_akhir_pkl")
                        )

                        for ((j, text) in cells.withIndex()) {
                            val tv = TextView(requireContext()).apply {
                                this.text = text
                                setPadding(12, 40, 12, 40)
                                gravity = Gravity.CENTER
                                textAlignment = View.TEXT_ALIGNMENT_CENTER
                                setTextColor(0xFF000000.toInt())
                            }

                            if (j == 4) {
                                tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark))
                                tv.setOnClickListener {
                                    previewAndDownloadFile(fileLaporanUrl, fileLaporan)
                                }
                            } else if (j == 5) {
                                tv.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark))
                                tv.setOnClickListener {
                                    previewAndDownloadFile(fileProjectUrl, fileProject)
                                }
                            }

                            row.addView(tv)
                        }

                        val btnUbah = Button(requireContext()).apply {
                            text = "Ubah"
                            textSize = 12f
                            setOnClickListener {
                                showUbahBottomSheet(obj)
                            }
                        }

                        val btnHapus = Button(requireContext()).apply {
                            text = "Hapus"
                            textSize = 12f
                            setOnClickListener {
                                val dialog = android.app.AlertDialog.Builder(requireContext())
                                    .setTitle("Konfirmasi Hapus")
                                    .setMessage("Yakin ingin menghapus laporan siswa $nama?")
                                    .setPositiveButton("Ya, Hapus") { _, _ ->
                                        hapusLaporan(obj.getString("id_laporan_pkl"))
                                    }
                                    .setNegativeButton("Tidak", null)
                                    .create()

                                dialog.setOnShowListener {
                                    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                                        ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                                    )
                                    dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                                        ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                                    )
                                }

                                dialog.show()
                            }
                        }

                        row.addView(btnUbah)
                        row.addView(btnHapus)
                        tableLaporan.addView(row)
                    }
                }
            },
            {
                Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
            })
        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun hapusLaporan(id: String) {
        val req = object : StringRequest(Method.POST, urlHapus,
            { resp ->
                val js = JSONObject(resp)
                if (js.getBoolean("status")) {
                    Toast.makeText(requireContext(), "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                    tampilkanLaporan()
                } else {
                    Toast.makeText(requireContext(), "Gagal hapus: ${js.getString("message")}", Toast.LENGTH_SHORT).show()
                }
            }, {
                Toast.makeText(requireContext(), "Koneksi gagal", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> = hashMapOf("id" to id)
        }
        Volley.newRequestQueue(requireContext()).add(req)
    }


    private fun showTambahBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.fragment_tambah_laporan, null)

        val spinner = view.findViewById<Spinner>(R.id.spinnerSiswa)
        val etNilai = view.findViewById<EditText>(R.id.etNilai)
        val btnSimpan = view.findViewById<Button>(R.id.btnSimpan)
        val btnLap = view.findViewById<MaterialButton>(R.id.btnPilihLaporan)
        val btnProj = view.findViewById<MaterialButton>(R.id.btnPilihProject)

        tvNamaFileLaporan = view.findViewById(R.id.tvNamaFileLaporan)
        tvNamaFileProject = view.findViewById(R.id.tvNamaFileProject)

        btnLap.setOnClickListener { fileLaporanPicker.launch("application/pdf") }
        btnProj.setOnClickListener { fileProjectPicker.launch("*/*") }

        ambilDataSiswa { ok ->
            if (ok) {
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    siswaList.map { "${it.nis} – ${it.nama} – ${it.sekolah}" }
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                }
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                        selectedNis = siswaList[pos].nis
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
        }

        btnSimpan.setOnClickListener {
            val nilai = etNilai.text.trim().toString()
            if (selectedNis.isEmpty() || nilai.isEmpty() || fileLaporanUri == null || fileProjectUri == null) {
                Toast.makeText(requireContext(), "Lengkapi semua field & pilih file", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uploadLaporan(selectedNis, nilai)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }


    // Tambahkan variabel global ini
    private var fileLaporanAwal: String? = null
    private var fileProjectAwal: String? = null


    private fun showUbahBottomSheet(data: JSONObject) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.fragment_tambah_laporan, null)

        val spinner = view.findViewById<Spinner>(R.id.spinnerSiswa)
        val etNilai = view.findViewById<EditText>(R.id.etNilai)
        val btnSimpan = view.findViewById<Button>(R.id.btnSimpan)
        val btnLap = view.findViewById<MaterialButton>(R.id.btnPilihLaporan)
        val btnProj = view.findViewById<MaterialButton>(R.id.btnPilihProject)

        tvNamaFileLaporan = view.findViewById(R.id.tvNamaFileLaporan)
        tvNamaFileProject = view.findViewById(R.id.tvNamaFileProject)

        view.findViewById<TextView>(R.id.tvTitle).text = "Ubah Laporan PKL"

        val idLaporan = data.getString("id_laporan_pkl")
        val nisAwal = data.getString("nis")
        val nilaiAwal = data.getString("nilai_akhir_pkl")

        // Simpan nama file awal
        fileLaporanAwal = data.getString("file_laporan")
        fileProjectAwal = data.getString("file_project")

        tvNamaFileLaporan.text = fileLaporanAwal ?: "Belum ada file"
        tvNamaFileProject.text = fileProjectAwal ?: "Belum ada file"

        ambilDataSiswa { ok ->
            if (ok) {
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    siswaList.map { "${it.nis} – ${it.nama} – ${it.sekolah}" }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
                val index = siswaList.indexOfFirst { it.nis == nisAwal }
                if (index >= 0) spinner.setSelection(index)

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                        selectedNis = siswaList[pos].nis
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
        }

        etNilai.setText(nilaiAwal)

        btnLap.setOnClickListener {
            fileLaporanPicker.launch("application/pdf")
        }
        btnProj.setOnClickListener {
            fileProjectPicker.launch("*/*")
        }

        btnSimpan.setOnClickListener {
            val nilaiBaru = etNilai.text.toString().trim()
            if (selectedNis.isEmpty() || nilaiBaru.isEmpty()) {
                Toast.makeText(requireContext(), "Lengkapi semua field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gunakan file lama jika user tidak memilih ulang
            ubahLaporan(
                id = idLaporan,
                nis = selectedNis,
                nilai = nilaiBaru,
                fileLaporanLama = fileLaporanAwal,
                fileProjectLama = fileProjectAwal
            )

            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }


    private fun ubahLaporan(id: String, nis: String, nilai: String, fileLaporanLama: String?, fileProjectLama: String?) {
        val siswa = siswaList.find { it.nis == nis } ?: return

        val req = object : VolleyMultipartRequest(
            Method.POST, urlUbah,
            Response.Listener { res ->
                val js = JSONObject(String(res.data))
                if (js.getBoolean("status")) {
                    Toast.makeText(requireContext(), "Berhasil diubah", Toast.LENGTH_SHORT).show()
                    tampilkanLaporan()
                } else {
                    Toast.makeText(requireContext(), js.getString("message"), Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(requireContext(), "Gagal koneksi", Toast.LENGTH_SHORT).show()
            }
        ) {}

        req.setParams(
            mapOf(
                "id" to id,
                "nis" to nis,
                "nama_siswa" to siswa.nama,
                "asal_sekolah" to siswa.sekolah,
                "nilai" to nilai,
                "file_laporan_lama" to (fileLaporanLama ?: ""),
                "file_project_lama" to (fileProjectLama ?: "")
            )
        )

        val files = mutableMapOf<String, VolleyMultipartRequest.DataPart>()
        fileLaporanUri?.let {
            val data = requireContext().contentResolver.openInputStream(it)?.readBytes()
            data?.let { bytes ->
                files["file_laporan"] = VolleyMultipartRequest.DataPart("laporan_${System.currentTimeMillis()}.pdf", bytes)
            }
        }
        fileProjectUri?.let {
            val data = requireContext().contentResolver.openInputStream(it)?.readBytes()
            data?.let { bytes ->
                files["file_project"] = VolleyMultipartRequest.DataPart("project_${System.currentTimeMillis()}.zip", bytes)
            }
        }

        req.setByteData(files)
        Volley.newRequestQueue(requireContext()).add(req)
    }



    private fun ambilDataSiswa(callback: (Boolean) -> Unit) {
        val req = JsonObjectRequest(urlSiswa, { resp ->
            if (resp.getBoolean("status")) {
                siswaList.clear()
                resp.getJSONArray("data").let { arr ->
                    for (i in 0 until arr.length()) {
                        arr.getJSONObject(i).let { obj ->
                            siswaList.add(
                                Siswa(
                                    obj.getString("nis"),
                                    obj.getString("nama_siswa"),
                                    obj.getString("asal_sekolah")
                                )
                            )
                        }
                    }
                }
                callback(true)
            } else callback(false)
        }, { callback(false) })
        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun uploadLaporan(nis: String, nilai: String) {
        val siswa = siswaList.find { it.nis == nis } ?: return

        val req = object : VolleyMultipartRequest(
            Method.POST, urlTambah,
            Response.Listener { res ->
                JSONObject(String(res.data)).let {
                    if (it.getBoolean("status")) {
                        Toast.makeText(requireContext(), "Berhasil disimpan", Toast.LENGTH_SHORT).show()
                        tampilkanLaporan()
                    } else {
                        Toast.makeText(requireContext(), it.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                }
            },
            Response.ErrorListener {
                Toast.makeText(requireContext(), "Upload gagal", Toast.LENGTH_SHORT).show()
            }
        ) {}

        req.setParams(
            mapOf(
                "nis" to nis,
                "nama_siswa" to siswa.nama,
                "asal_sekolah" to siswa.sekolah,
                "nilai" to nilai
            )
        )

        val files = mutableMapOf<String, VolleyMultipartRequest.DataPart>()
        fileLaporanUri?.let {
            val data = requireContext().contentResolver.openInputStream(it)?.readBytes()
            data?.let { bytes ->
                files["file_laporan"] = VolleyMultipartRequest.DataPart("laporan_${System.currentTimeMillis()}.pdf", bytes)
            }
        }
        fileProjectUri?.let {
            val data = requireContext().contentResolver.openInputStream(it)?.readBytes()
            data?.let { bytes ->
                files["file_project"] = VolleyMultipartRequest.DataPart("project_${System.currentTimeMillis()}.zip", bytes)
            }
        }

        req.setByteData(files)
        Volley.newRequestQueue(requireContext()).add(req)
    }

    private fun downloadFile(url: String, fileName: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Mengunduh $fileName")
            .setDescription("File sedang diunduh...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val dm = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(requireContext(), "Mengunduh $fileName...", Toast.LENGTH_SHORT).show()
    }

    private fun previewAndDownloadFile(url: String, fileName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(url), "*/*")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(intent)

            Handler(Looper.getMainLooper()).postDelayed({
                downloadFile(url, fileName)
            }, 3000)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Tidak bisa menampilkan file", Toast.LENGTH_SHORT).show()
            downloadFile(url, fileName)
        }
    }

    data class Siswa(val nis: String, val nama: String, val sekolah: String)
}