package com.ioreum.app_jurnal_pkl

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import org.json.JSONArray
import org.json.JSONObject

class laporan_pklFragment : Fragment() {

    private lateinit var tableLaporan: TableLayout
    private lateinit var btnTambah: Button
    private lateinit var etCari: EditText

    private val urlTampil = "http://192.168.36.139/jurnal_pkl/tampil_laporan.php"
    private val urlTambah = "http://192.168.36.139/jurnal_pkl/tambah_laporan.php"
    private val urlSiswa = "http://192.168.36.139/jurnal_pkl/tampil_siswa.php"

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
        return view
    }

    private fun tampilkanLaporan(filterNIS: String = "") {
        val req = com.android.volley.toolbox.StringRequest(
            urlTampil,
            { resp ->
                val arr = JSONArray(resp)
                tableLaporan.removeAllViews()
                // Header
                val header = TableRow(requireContext())
                arrayOf("No", "NIS", "Nama", "Sekolah", "File Laporan", "File Project", "Nilai")
                    .forEach { title ->
                        TextView(requireContext()).apply {
                            text = title
                            setPadding(8, 8, 8, 8)
                            setTypeface(null, android.graphics.Typeface.BOLD)
                        }.let(header::addView)
                    }
                tableLaporan.addView(header)

                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val nis = obj.getString("nis")
                    if (filterNIS.isEmpty() || nis.contains(filterNIS, ignoreCase = true)) {
                        val row = TableRow(requireContext())
                        val cells = listOf(
                            (i + 1).toString(),
                            nis,
                            obj.getString("nama_siswa"),
                            obj.getString("asal_sekolah"),
                            obj.getString("file_laporan"),
                            obj.getString("file_project"),
                            obj.getString("nilai_akhir_pkl")
                        )
                        cells.forEach { text ->
                            TextView(requireContext()).apply {
                                this.text = text
                                setPadding(8, 8, 8, 8)
                            }.let(row::addView)
                        }
                        tableLaporan.addView(row)
                    }
                }
            },
            {
                Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        )
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

        btnLap.setOnClickListener { fileLaporanPicker.launch("*/*") }
        btnProj.setOnClickListener { fileProjectPicker.launch("*/*") }

        ambilDataSiswa { ok ->
            if (ok) {
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    siswaList.map { "${it.nis} – ${it.nama} – ${it.sekolah}" }
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
        val siswa = siswaList.find { it.nis == nis }
        if (siswa == null) {
            Toast.makeText(requireContext(), "Data siswa tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

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

        // Kirim semua data siswa + nilai
        req.setParams(
            mapOf(
                "nis" to nis,
                "nama_siswa" to siswa.nama,
                "asal_sekolah" to siswa.sekolah,
                "nilai" to nilai
            )
        )

        // Kirim file
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


    data class Siswa(val nis: String, val nama: String, val sekolah: String)
}
