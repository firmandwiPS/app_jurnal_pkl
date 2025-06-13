package com.ioreum.app_jurnal_pkl

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class PresensiFragment : Fragment() {

    private lateinit var tablePresensi: TableLayout

    private val urlTampil = "http://192.168.36.139/jurnal_pkl/tampil_presensi.php"
    private val urlTambahPresensi = "http://192.168.36.139/jurnal_pkl/tambah_presensi.php"
    private val urlUbahPresensi = "http://192.168.36.139/jurnal_pkl/ubah_presensi.php"
    private val urlGetNis = "http://192.168.36.139/jurnal_pkl/daftar_siswa.php"
    private val urlHapusPresensi = "http://192.168.36.139/jurnal_pkl/hapus_presensi.php"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_presensi, container, false)

        tablePresensi = view.findViewById(R.id.tablePresensi)
        val btnTambah = view.findViewById<Button>(R.id.btnTambahPresensi)

        btnTambah.setOnClickListener {
            showBottomSheetPresensi()
        }

        tampilkanHeader()
        loadDataPresensi()

        return view
    }

    private fun tampilkanHeader() {
        val row = TableRow(requireContext())
        val headers = listOf("No", "NIS", "Nama Siswa", "Tanggal", "Keterangan", "Aksi")
        headers.forEach {
            val tv = TextView(requireContext()).apply {
                text = it
                setPadding(20, 18, 20, 18)
                setTextColor(0xFF000000.toInt())
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            row.addView(tv)
        }
        tablePresensi.addView(row)
    }

    private fun loadDataPresensi() {
        val request = StringRequest(urlTampil, { response ->
            try {
                val array = JSONArray(response)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val row = TableRow(requireContext())

                    val idPresensi = obj.getString("id_presensi")
                    val data = listOf(
                        (i + 1).toString(),
                        obj.getString("nis"),
                        obj.getString("nama_siswa"),
                        obj.getString("tanggal"),
                        obj.getString("keterangan")
                    )

                    data.forEach {
                        val tv = TextView(requireContext()).apply {
                            text = it
                            setPadding(16, 12, 16, 12)
                            gravity = Gravity.CENTER
                            textAlignment = View.TEXT_ALIGNMENT_CENTER
                            setTextColor(0xFF000000.toInt())
                            layoutParams = TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                            ).apply {
                                gravity = Gravity.CENTER
                            }
                        }
                        row.addView(tv)
                    }

                    val layoutAksi = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(8, 0, 8, 0)
                    }

                    val btnEdit = Button(requireContext()).apply {
                        text = "Ubah"
                        textSize = 12f
                        setOnClickListener {
                            showBottomSheetUbahPresensi(
                                idPresensi,
                                obj.getString("nis"),
                                obj.getString("tanggal"),
                                obj.getString("keterangan")
                            )
                        }
                    }

                    val btnHapus = Button(requireContext()).apply {
                        text = "Hapus"
                        textSize = 12f
                        setOnClickListener {
                            val alertDialog = android.app.AlertDialog.Builder(requireContext())
                                .setTitle("Konfirmasi Hapus")
                                .setMessage("Yakin ingin menghapus presensi siswa ${obj.getString("nama_siswa")} dengan NIS: ${obj.getString("nis")}?")
                                .setPositiveButton("Ya") { _, _ ->
                                    hapusPresensi(obj.getString("id_presensi"))
                                }
                                .setNegativeButton("Tidak") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create()

                            alertDialog.setOnShowListener {
                                alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                                    resources.getColor(android.R.color.holo_green_dark, null)
                                )
                                alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                                    resources.getColor(android.R.color.holo_red_dark, null)
                                )
                            }

                            alertDialog.show()
                        }
                    }

                    layoutAksi.addView(btnEdit)
                    layoutAksi.addView(btnHapus)
                    row.addView(layoutAksi)

                    tablePresensi.addView(row)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Data tidak valid", Toast.LENGTH_SHORT).show()
            }
        }, {
            Toast.makeText(requireContext(), "Tambahkan data presensi", Toast.LENGTH_SHORT).show()
        })

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun hapusPresensi(idPresensi: String) {
        val request = object : StringRequest(Method.POST, urlHapusPresensi,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("status")) {
                        Toast.makeText(requireContext(), "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                        tablePresensi.removeAllViews()
                        tampilkanHeader()
                        loadDataPresensi()
                    } else {
                        Toast.makeText(requireContext(), json.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Respon tidak valid", Toast.LENGTH_SHORT).show()
                }
            }, {
                Toast.makeText(requireContext(), "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("id_presensi" to idPresensi)
            }
        }
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun showBottomSheetPresensi() {
        showBottomSheet(null, null, null, null)
    }

    private fun showBottomSheetUbahPresensi(idPresensi: String, nis: String, tanggal: String, keterangan: String) {
        showBottomSheet(idPresensi, nis, tanggal, keterangan)
    }

    private fun showBottomSheet(idPresensi: String?, nisLama: String?, tanggalLama: String?, keteranganLama: String?) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.fragment_tambah_presensi, null)
        dialog.setContentView(view)

        val spinnerNis = view.findViewById<Spinner>(R.id.spinnerNis)
        val etTanggal = view.findViewById<EditText>(R.id.etTanggal)
        val spinnerKeterangan = view.findViewById<Spinner>(R.id.spinnerKeterangan)
        val btnSimpan = view.findViewById<Button>(R.id.btnSimpan)

        val nisList = mutableListOf<String>()
        val nisMap = mutableMapOf<String, String>()
        val reverseMap = mutableMapOf<String, String>()

        val requestNis = JsonArrayRequest(urlGetNis, { response ->
            for (i in 0 until response.length()) {
                val obj = response.getJSONObject(i)
                val nis = obj.getString("nis")
                val nama = obj.getString("nama_siswa")
                val display = "$nis - $nama"
                nisList.add(display)
                nisMap[display] = nis
                reverseMap[nis] = display
            }
            spinnerNis.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, nisList)
            if (nisLama != null) spinnerNis.setSelection(nisList.indexOf(reverseMap[nisLama]))
        }, {
            Toast.makeText(requireContext(), "Gagal memuat NIS", Toast.LENGTH_SHORT).show()
        })
        Volley.newRequestQueue(requireContext()).add(requestNis)

        val keteranganOptions = listOf("Masuk", "Izin", "Alpa")
        spinnerKeterangan.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, keteranganOptions)

        if (tanggalLama != null) etTanggal.setText(tanggalLama)
        if (keteranganLama != null) spinnerKeterangan.setSelection(keteranganOptions.indexOf(keteranganLama))

        etTanggal.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                etTanggal.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnSimpan.setOnClickListener {
            val nisSelected = spinnerNis.selectedItem?.toString()
            val nis = nisMap[nisSelected]
            val tanggal = etTanggal.text.toString().trim()
            val keterangan = spinnerKeterangan.selectedItem?.toString()

            if (nis.isNullOrEmpty() || tanggal.isEmpty() || keterangan.isNullOrEmpty()) {
                Toast.makeText(context, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (idPresensi == null) {
                simpanPresensi(nis, tanggal, keterangan) {
                    dialog.dismiss()
                    tablePresensi.removeAllViews()
                    tampilkanHeader()
                    loadDataPresensi()
                }
            } else {
                ubahPresensi(idPresensi, nis, tanggal, keterangan) {
                    dialog.dismiss()
                    tablePresensi.removeAllViews()
                    tampilkanHeader()
                    loadDataPresensi()
                }
            }
        }

        dialog.show()
    }

    private fun simpanPresensi(nis: String, tanggal: String, keterangan: String, onSuccess: () -> Unit) {
        val request = object : StringRequest(Method.POST, urlTambahPresensi,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("status")) {
                        onSuccess()
                    } else {
                        Toast.makeText(context, json.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal parsing respon", Toast.LENGTH_SHORT).show()
                }
            }, {
                Toast.makeText(context, "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "nis" to nis,
                    "tanggal" to tanggal,
                    "keterangan" to keterangan
                )
            }
        }
        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun ubahPresensi(idPresensi: String, nis: String, tanggal: String, keterangan: String, onSuccess: () -> Unit) {
        val request = object : StringRequest(Method.POST, urlUbahPresensi,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("status")) {
                        Toast.makeText(context, "Data berhasil diubah", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    } else {
                        Toast.makeText(context, json.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal parsing respon", Toast.LENGTH_SHORT).show()
                }
            }, {
                Toast.makeText(context, "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "id_presensi" to idPresensi,
                    "nis" to nis,
                    "tanggal" to tanggal,
                    "keterangan" to keterangan
                )
            }
        }
        Volley.newRequestQueue(requireContext()).add(request)
    }
}
